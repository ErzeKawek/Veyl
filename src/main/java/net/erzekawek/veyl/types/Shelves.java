package net.erzekawek.veyl.types;

import net.erzekawek.veyl.utils.registry.CBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Shelves extends ShelfBlock {

    public Shelves(Properties settings) {
        super(settings
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    public static class Entity extends ShelfBlockEntity {

        public Entity(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            BlockState s = getBlockState();
            return s == null ? super.getType() : CBlockEntity.of(s.getBlock()).shelf();
        }

        @Override
        public boolean isValidBlockState(BlockState blockState) {
            return CBlockEntity.of(blockState.getBlock()).shelf().isValid(blockState);
        }
    }
}
