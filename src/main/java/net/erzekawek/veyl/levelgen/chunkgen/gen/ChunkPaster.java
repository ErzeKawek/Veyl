package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** paste rotated room and block entities */
final class ChunkPaster {
    private ChunkPaster() {}

    static void paste(ChunkAccess chunk, MazeRegion.Placement p, int minX, int minZ, int maxX, int maxZ) {
        var r = p.room.rotated(p.rot);
        int minY = chunk.getMinY(), maxY = chunk.getMaxY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < r.states.length; i++) {
            int x = p.blockX + r.xs[i]; if (x < minX || x > maxX) continue;
            int z = p.blockZ + r.zs[i]; if (z < minZ || z > maxZ) continue;
            int y = p.blockY + r.ys[i]; if (y < minY || y > maxY) continue;

            BlockState state = r.states[i];
            BlockPos bp = pos.set(x, y, z);
            chunk.setBlockState(bp, state, 0);

            CompoundTag src = r.nbts[i];
            if (src != null) {
                CompoundTag t = src.copy();
                t.putInt("x", x); t.putInt("y", y); t.putInt("z", z);
                chunk.setBlockEntityNbt(t);
            } else if (state.getBlock() instanceof EntityBlock eb) {
                BlockEntity be = eb.newBlockEntity(bp.immutable(), state);
                if (be != null) chunk.setBlockEntity(be);
            }
        }
    }
}