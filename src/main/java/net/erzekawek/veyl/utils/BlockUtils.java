package net.erzekawek.veyl.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtils {

    private static void setWithoutUpdate(LevelAccessor world, BlockPos pos, BlockState state) {
        synchronized (world) {
            world.setBlock(pos, state, Flags.SILENT);
        }
    }

}
