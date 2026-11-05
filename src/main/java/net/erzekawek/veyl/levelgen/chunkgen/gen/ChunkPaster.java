package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.erzekawek.veyl.levelgen.chunkgen.rooms.LoadedRoom;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.Optional;

/** paste rotated room and block entities */
final class ChunkPaster {
    private ChunkPaster() {}

    static void spawnEntities(WorldGenLevel level, MazeRegion.Placement p, int minX, int minZ) {
        LoadedRoom room = p.room;
        for (LoadedRoom.EntityInfo e : room.entities) {
            // rotate the exact position and the anchor block the same way the blocks were rotated
            double x = e.x(), z = e.z();
            int bx = e.bx(), bz = e.bz();
            int w = room.sx, d = room.sz;
            for (int r = 0; r < p.rot; r++) {
                double nx = d - z; z = x; x = nx;
                int nbx = d - 1 - bz; bz = bx; bx = nbx;
                int t = w; w = d; d = t;
            }

            final double wx = p.blockX + x, wy = p.blockY + e.y(), wz = p.blockZ + z;
            if (wx < minX || wx >= minX + 16 || wz < minZ || wz >= minZ + 16) continue;

            CompoundTag tag = e.nbt().copy();
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(wx)); pos.add(DoubleTag.valueOf(wy)); pos.add(DoubleTag.valueOf(wz));
            tag.put("Pos", pos);
            tag.remove("UUID");

            // the fix: replace the stale build-site anchor with the real one
            tag.putIntArray("block_pos", new int[]{p.blockX + bx, p.blockY + e.by(), p.blockZ + bz});
            tag.remove("TileX");
            tag.remove("TileY");
            tag.remove("TileZ");

            ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
            Optional<Entity> loaded = EntityType.create(input, level.getLevel(),
                    new EntitySpawnRequest(EntitySpawnReason.STRUCTURE, true));

            loaded.ifPresent(entity -> {
                float yaw = entity.rotate(Rotation.values()[p.rot]);
                entity.snapTo(wx, wy, wz, yaw, entity.getXRot());
                level.addFreshEntityWithPassengers(entity);
            });
        }
    }

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