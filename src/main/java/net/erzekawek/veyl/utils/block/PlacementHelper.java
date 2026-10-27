package net.erzekawek.veyl.utils.block;

import net.erzekawek.veyl.utils.misc.Flags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class PlacementHelper {

    public static void setWithoutUpdate(LevelAccessor level, BlockPos pos, BlockState state) {
        synchronized (level) {
            level.setBlock(pos, state, Flags.SILENT);
        }
    }

}
