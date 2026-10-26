package net.erzekawek.veyl.utils.block;

import net.erzekawek.veyl.utils.misc.Flags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class PlacementHelper {

    private static void setWithoutUpdate(LevelAccessor world, BlockPos pos, BlockState state) {
        synchronized (world) {
            world.setBlock(pos, state, Flags.SILENT);
        }
    }

}
