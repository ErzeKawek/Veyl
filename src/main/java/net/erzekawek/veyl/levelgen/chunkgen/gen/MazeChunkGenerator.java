package net.erzekawek.veyl.levelgen.chunkgen.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Generates mazes */
public final class MazeChunkGenerator extends ChunkGenerator {

    public static final MapCodec<MazeChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(MazeChunkGenerator::getBiomeSource),
            Identifier.CODEC.optionalFieldOf("group", Identifier.fromNamespaceAndPath("fondest_memories", "default")).forGetter(g -> g.settings.group()),
            Codec.intRange(4, 64).optionalFieldOf("cell_size", 8).forGetter(g -> g.settings.cellSize()),
            Codec.intRange(4, 64).optionalFieldOf("region_size", 16).forGetter(g -> g.settings.regionSize()),
            Codec.INT.optionalFieldOf("base_y", 64).forGetter(g -> g.settings.baseY()),
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(g -> g.settings.seed()),
            Codec.floatRange(0f, 1f).optionalFieldOf("multi_room_chance", 0.08f).forGetter(g -> g.settings.multiRoomChance()),
            Codec.floatRange(0f, 1f).optionalFieldOf("gate_chance", 0.4f).forGetter(g -> g.settings.gateChance())
    ).apply(i, MazeChunkGenerator::new));

    private final MazeSettings settings;

    public MazeChunkGenerator(BiomeSource biomes, Identifier group, int cellSize, int regionSize, int baseY,
                              long seed, float multiChance, float gateChance) {
        super(biomes);
        this.settings = new MazeSettings(cellSize, regionSize, baseY, seed, multiChance, gateChance, group);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
        generateMaze(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    /** The Maze Logic itself */
    public void generateMaze(ChunkAccess chunk) {
        ChunkPos cp = chunk.getPos();
        int minX = cp.getMinBlockX(), minZ = cp.getMinBlockZ(), maxX = minX + 15, maxZ = minZ + 15;
        int cs = settings.cellSize(), n = settings.regionSize();

        Set<MazeRegion.Placement> done = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int cx = Math.floorDiv(minX, cs); cx <= Math.floorDiv(maxX, cs); cx++) {
            for (int cz = Math.floorDiv(minZ, cs); cz <= Math.floorDiv(maxZ, cs); cz++) {
                MazeRegion region = MazeRegion.get(settings, Math.floorDiv(cx, n), Math.floorDiv(cz, n));
                MazeRegion.Placement p = region.at(Math.floorMod(cx, n), Math.floorMod(cz, n));
                if (p != null && done.add(p)) paste(chunk, p, minX, minZ, maxX, maxZ);
            }
        }
    }

    /** Paste Rooms */
    private static void paste(ChunkAccess chunk, MazeRegion.Placement p, int minX, int minZ, int maxX, int maxZ) {
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
                // block entity data saved in the NBT: re-attach it at the new position
                CompoundTag t = src.copy();
                t.putInt("x", x); t.putInt("y", y); t.putInt("z", z);
                chunk.setBlockEntityNbt(t);
            } else if (state.getBlock() instanceof EntityBlock eb) {
                // block has an entity but the NBT stored none create fresh one
                BlockEntity be = eb.newBlockEntity(bp.immutable(), state);
                if (be != null) chunk.setBlockEntity(be);
            }
        }
    }

    @Override public void spawnOriginalMobs(WorldGenRegion region) {

    }

    @Override public int getGenDepth() {
        return 384;
    }
    @Override public int getSeaLevel() {
        return -63;
    }
    @Override public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        return settings.baseY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] air = new BlockState[level.getHeight()];
        Arrays.fill(air, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(level.getMinY(), air);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
        int cs = settings.cellSize();
        result.add("Maze cell: " + Math.floorDiv(feetPos.getX(), cs) + ", " + Math.floorDiv(feetPos.getZ(), cs));
    }
}
