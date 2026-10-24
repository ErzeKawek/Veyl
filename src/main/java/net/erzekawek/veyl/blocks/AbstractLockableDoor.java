package net.erzekawek.veyl.blocks;

import net.erzekawek.veyl.items.AbstractConsumableItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AbstractLockableDoor extends DoorBlock {

    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public AbstractLockableDoor(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
        registerDefaultState(defaultBlockState().setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOCKED);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof AbstractConsumableItem key)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean nowLocked = !state.getValue(LOCKED);
        setLocked(level, pos, state, nowLocked);

        level.playSound(null, pos,
                nowLocked ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN,
                SoundSource.BLOCKS, 1.0f, 1.2f);
        player.sendOverlayMessage(Component.literal(nowLocked ? "Door locked" : "Door unlocked"));

        if (key.isConsume()) {
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(LOCKED)) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.sendOverlayMessage(Component.literal("This door is locked"));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    private void setLocked(Level level, BlockPos pos, BlockState state, boolean locked) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockPos upperPos = lowerPos.above();

        for (BlockPos p : new BlockPos[]{lowerPos, upperPos}) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() != this) continue;
            s = s.setValue(LOCKED, locked);
            if (locked) s = s.setValue(OPEN, false);
            level.setBlock(p, s, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    @Override
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (state.getValue(LOCKED) && open) return;
        super.setOpen(entity, level, state, pos, open);
    }
}
