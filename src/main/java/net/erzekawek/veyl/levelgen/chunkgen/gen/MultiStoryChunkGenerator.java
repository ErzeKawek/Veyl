package net.erzekawek.veyl.levelgen.chunkgen.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/** multi-story roomgen */
public final class MultiStoryChunkGenerator extends ChunkGenerator {

    public static final MapCodec<MultiStoryChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(MultiStoryChunkGenerator::getBiomeSource),
            Codec.intRange(4, 64).optionalFieldOf("cell_size", 8).forGetter(g -> g.settings.cellSize()),
            Codec.intRange(4, 64).optionalFieldOf("region_size", 16).forGetter(g -> g.settings.regionSize()),
            Codec.INT.optionalFieldOf("base_y", 64).forGetter(g -> g.settings.baseY()),
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(g -> g.settings.seed()),
            Codec.STRING.optionalFieldOf("direction", "down").forGetter(g -> g.settings.down() ? "down" : "up"),
            Codec.floatRange(0f, 1f).optionalFieldOf("stairway_chance", 0.02f).forGetter(g -> g.settings.stairwayChance()),
            StorySettings.CODEC.listOf().fieldOf("stories").forGetter(g -> g.settings.stories())
    ).apply(i, MultiStoryChunkGenerator::new));

    private final MultiStorySettings settings;

    public MultiStoryChunkGenerator(BiomeSource biomes, int cellSize, int regionSize, int baseY, long seed, String direction, float stairwayChance, List<StorySettings> stories) {
        super(biomes);
        this.settings = new MultiStorySettings(cellSize, regionSize, baseY, seed,
                !"up".equalsIgnoreCase(direction), stairwayChance, List.copyOf(stories));
    }

    @Override protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
        generateStories(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    public void generateStories(ChunkAccess chunk) {
        ChunkPos cp = chunk.getPos();
        int minX = cp.getMinBlockX(), minZ = cp.getMinBlockZ(), maxX = minX + 15, maxZ = minZ + 15;
        int cs = settings.cellSize(), n = settings.regionSize();

        Set<Object> done = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int cx = Math.floorDiv(minX, cs); cx <= Math.floorDiv(maxX, cs); cx++) {
            for (int cz = Math.floorDiv(minZ, cs); cz <= Math.floorDiv(maxZ, cs); cz++) {
                MultiStoryRegion region = MultiStoryRegion.get(settings, Math.floorDiv(cx, n), Math.floorDiv(cz, n));
                for (MazeRegion story : region.stories) {
                    MazeRegion.Placement p = story.at(Math.floorMod(cx, n), Math.floorMod(cz, n));
                    if (p != null && done.add(p.pasteId)) ChunkPaster.paste(chunk, p, minX, minZ, maxX, maxZ);
                }
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
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext ctx) {
        int cs = settings.cellSize();
        result.add("Multistory cell: " + Math.floorDiv(feetPos.getX(), cs) + ", " + Math.floorDiv(feetPos.getZ(), cs)
                + " (" + settings.stories().size() + " stories)");
    }
}
