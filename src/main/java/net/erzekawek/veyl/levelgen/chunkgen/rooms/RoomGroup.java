package net.erzekawek.veyl.levelgen.chunkgen.rooms;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An NBT Group: every room NBT + its size in cells + its Y offset + spawn weight.
 * 1 group = 2 story.
 * 2 groups = 2 stories.
 * and so on.
 */
public final class RoomGroup {
    /** cellsX/cellsZ = footprint in cells. yOffset is added to the story's base Y. */
    public record RoomDef(Identifier nbt, int cellsX, int cellsZ, int yOffset, int weight) {

    }

    private static final Map<Identifier, RoomGroup> REGISTRY = new ConcurrentHashMap<>();

    public final Identifier id;
    public final Block singleGuide, multiGuide;
    /** Stairway guides */
    public final Block stairLowerGuide, stairUpperGuide;
    public final List<RoomDef> singles, multis, stairways;
    public final RoomDef start;
    private final Map<Integer, GroupRuntime> runtimes = new ConcurrentHashMap<>();

    private RoomGroup(Builder b) {
        id = b.id;
        singleGuide = b.singleGuide; multiGuide = b.multiGuide;
        stairLowerGuide = b.stairLower; stairUpperGuide = b.stairUpper;
        singles = List.copyOf(b.singles); multis = List.copyOf(b.multis);
        stairways = List.copyOf(b.stairways); start = b.start;
    }

    public Block singleGuide() {
        return singleGuide;
    }
    public Block multiGuide() {
        return multiGuide;
    }
    public Block stairLowerGuide() {
        return stairLowerGuide;
    }
    public Block stairUpperGuide() {
        return stairUpperGuide;
    }

    public static RoomGroup get(Identifier id) {
        return REGISTRY.get(id);
    }
    public static void clearRuntimes() {
        REGISTRY.values().forEach(g -> g.runtimes.clear());
    }

    public GroupRuntime runtime(int cellSize) {
        return runtimes.computeIfAbsent(cellSize, cs -> new GroupRuntime(this, cs));
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    /** look the id for block */
    private static Block block(Identifier id) {
        return BuiltInRegistries.BLOCK.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("[Veyl] unknown guide block id: " + id));
    }

    private static Block mc(String path) {
        return block(Identifier.fromNamespaceAndPath("minecraft", path));
    }

    public static final class Builder {
        private final Identifier id;
        private Block singleGuide = Blocks.PRISMARINE;
        private Block multiGuide = Blocks.COBBLED_DEEPSLATE;
        private Block stairLower = Blocks.NETHERRACK;
        private Block stairUpper = Blocks.BLACKSTONE;
        private final List<RoomDef> singles = new ArrayList<>(), multis = new ArrayList<>(), stairways = new ArrayList<>();
        private RoomDef start;

        private Builder(Identifier id) {
            this.id = id;
        }

        /** this story guides */
        public Builder guides(Block single, Block multi) {
            singleGuide = single; multiGuide = multi; return this;
        }
        public Builder guides(Identifier single, Identifier multi) {
            singleGuide = block(single); multiGuide = block(multi); return this;
        }

        /** lower and upper guides */
        public Builder stairGuides(Block lower, Block upper) {
            stairLower = lower; stairUpper = upper; return this;
        }
        public Builder stairGuides(Identifier lower, Identifier upper) {
            stairLower = block(lower); stairUpper = block(upper); return this;
        }

        public Builder single(Identifier nbt, int yOffset, int weight) {
            singles.add(new RoomDef(nbt, 1, 1, yOffset, weight)); return this;
        }
        public Builder multi(Identifier nbt, int cellsX, int cellsZ, int yOffset, int weight) {
            multis.add(new RoomDef(nbt, cellsX, cellsZ, yOffset, weight)); return this;
        }
        public Builder start(Identifier nbt, int cellsX, int cellsZ, int yOffset) {
            start = new RoomDef(nbt, cellsX, cellsZ, yOffset, 1); return this;
        }

        /** stairway connecting this story to next story */
        public Builder stairway(Identifier nbt, int cellsX, int cellsZ, int yOffset, int weight) {
            stairways.add(new RoomDef(nbt, cellsX, cellsZ, yOffset, weight)); return this;
        }

        public RoomGroup register() {
            RoomGroup g = new RoomGroup(this); REGISTRY.put(id, g); return g;
        }
    }
}
