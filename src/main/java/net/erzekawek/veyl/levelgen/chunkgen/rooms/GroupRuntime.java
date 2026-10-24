package net.erzekawek.veyl.levelgen.chunkgen.rooms;

import net.erzekawek.veyl.Veyl;
import net.erzekawek.veyl.levelgen.chunkgen.grid.Side;
import net.minecraft.util.RandomSource;

import java.util.*;

/** loaded rooms */
public final class GroupRuntime {

    public record Candidate(LoadedRoom room, int rot, float weight) {}

    private final Map<Integer, List<Candidate>> singleByMask = new HashMap<>();
    public final List<LoadedRoom> multis = new ArrayList<>();
    public final List<LoadedRoom> stairways = new ArrayList<>();
    public final LoadedRoom start;

    GroupRuntime(RoomGroup g, int cellSize) {
        for (RoomGroup.RoomDef def : g.singles) {
            LoadedRoom lr = LoadedRoom.load(def, cellSize, g.singleGuide());
            if (lr == null || lr.openMask == 0) continue;
            Veyl.LOGGER.info("[Veyl] single {} -> {}", def.nbt(), shapeName(lr.openMask));

            Map<Integer, List<Integer>> rotsPerMask = new HashMap<>();
            for (int k = 0; k < 4; k++)
                rotsPerMask.computeIfAbsent(Side.rotateMask(lr.openMask, k), m -> new ArrayList<>()).add(k);
            rotsPerMask.forEach((mask, rots) -> {
                for (int k : rots)
                    singleByMask.computeIfAbsent(mask, m -> new ArrayList<>())
                            .add(new Candidate(lr, k, (float) def.weight() / rots.size()));
            });
        }
        for (RoomGroup.RoomDef def : g.multis) {
            LoadedRoom lr = LoadedRoom.load(def, cellSize, g.multiGuide());
            if (lr != null) multis.add(lr);
        }
        for (RoomGroup.RoomDef def : g.stairways) {
            LoadedRoom lr = LoadedRoom.loadStairway(def, cellSize, g.stairLowerGuide(), g.stairUpperGuide());
            if (lr != null) stairways.add(lr);
        }
        start = g.start == null ? null : LoadedRoom.load(g.start, cellSize, g.multiGuide());
    }

    /** Exact-match: a piece whose openings are exactly `mask`. */
    public Candidate pickSingle(int mask, RandomSource rng) {
        List<Candidate> list = singleByMask.get(mask);
        if (list == null || list.isEmpty()) return null;
        float total = 0; for (Candidate c : list) total += c.weight();
        float r = rng.nextFloat() * total;
        for (Candidate c : list) {
            r -= c.weight(); if (r <= 0) return c;
        }
        return list.get(list.size() - 1);
    }

    public LoadedRoom pickMulti(RandomSource rng) {
        return weighted(multis, rng);
    }
    public LoadedRoom pickStairway(RandomSource rng) {
        return weighted(stairways, rng);
    }

    private static LoadedRoom weighted(List<LoadedRoom> list, RandomSource rng) {
        if (list.isEmpty()) return null;
        float total = 0; for (LoadedRoom r : list) total += r.def.weight();
        float v = rng.nextFloat() * total;
        for (LoadedRoom r : list) { v -= r.def.weight(); if (v <= 0) return r; }
        return list.get(list.size() - 1);
    }

    private static String shapeName(int mask) {
        return switch (Integer.bitCount(mask)) {
            case 1 -> "nub";
            case 2 -> (mask == 5 || mask == 10) ? "straight" : "turns";
            case 3 -> "T-crossing";
            case 4 -> "cross";
            default -> "?";
        };
    }
}
