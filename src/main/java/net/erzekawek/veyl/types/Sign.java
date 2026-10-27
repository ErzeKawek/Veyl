package net.erzekawek.veyl.types;

import net.erzekawek.veyl.utils.registry.CBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;

public class Sign {

    public static class VeylStandingSignBlock extends StandingSignBlock {

        public VeylStandingSignBlock(WoodType woodType, Properties properties) {
            super(
                    woodType,
                    properties
                            .forceSolidOn()
                            .instrument(NoteBlockInstrument.BASS)
                            .noCollision()
                            .strength(1.0F)
                            .ignitedByLava()
            );
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new VeylSignBlockEntity(pos, state);
        }
    }

    public static class VeylWallSignBlock extends WallSignBlock {

        public VeylWallSignBlock(WoodType woodType, Properties properties) {
            super(
                    woodType,
                    properties
                            .forceSolidOn()
                            .instrument(NoteBlockInstrument.BASS)
                            .noCollision()
                            .strength(1.0F)
                            .ignitedByLava()
            );
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new VeylSignBlockEntity(pos, state);
        }
    }

    public static class VeylCeilingHangingSignBlock extends CeilingHangingSignBlock {

        public VeylCeilingHangingSignBlock(WoodType woodType, Properties properties) {
            super(
                    woodType,
                    properties
                            .forceSolidOn()
                            .instrument(NoteBlockInstrument.BASS)
                            .noCollision()
                            .strength(1.0F)
                            .ignitedByLava()
            );
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new VeylHangingSignBlockEntity(pos, state);
        }
    }

    public static class VeylWallHangingSignBlock extends WallHangingSignBlock {

        public VeylWallHangingSignBlock(WoodType woodType, Properties properties) {
            super(
                    woodType,
                    properties
                            .forceSolidOn()
                            .instrument(NoteBlockInstrument.BASS)
                            .noCollision()
                            .strength(1.0F)
                            .ignitedByLava()
            );
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new VeylHangingSignBlockEntity(pos, state);
        }
    }

    public static class VeylSignBlockEntity extends SignBlockEntity {

        public VeylSignBlockEntity(BlockPos pos, BlockState state) {
            super(CBlockEntity.of(state.getBlock()).sign(), pos, state);
        }
    }
    public static class VeylHangingSignBlockEntity extends HangingSignBlockEntity {

        public VeylHangingSignBlockEntity(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            BlockState s = getBlockState();
            return s == null ? super.getType() : CBlockEntity.of(s.getBlock()).hangingSign();
        }

        @Override
        public boolean isValidBlockState(BlockState blockState) {
            return CBlockEntity.of(blockState.getBlock()).hangingSign().isValid(blockState);
        }
    }
}
