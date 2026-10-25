package net.erzekawek.veyl.blocks;

import net.erzekawek.veyl.blockentities.CustomStorageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VeylConfigurableStorageBlock extends BaseEntityBlock {
    private final int slots;
    private BlockEntityType<CustomStorageEntity> type;

    public VeylConfigurableStorageBlock(int slots, Properties properties) {
        super(properties);
        this.slots = Mth.clamp(slots, 1, 54);
    }

    public int slots() { return slots; }
    public BlockEntityType<CustomStorageEntity> blockEntityType() {
        return type;
    }
    public void setBlockEntityType(BlockEntityType<CustomStorageEntity> type) {
        this.type = type;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomStorageEntity(type, pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CustomStorageEntity be) player.openMenu(be);
        return InteractionResult.SUCCESS;
    }

    @Override protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
