package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.erzekawek.veyl.levelgen.chunkgen.grid.Port;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.GroupRuntime;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.LoadedRoom;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.RoomGroup;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One region of ALL stories. Start rooms and stairways are decided first (jointly, so a stairway claims the
 * same cells in both stories it connects), then every story is carved with its own DFS maze.
 */
public final class MultiStoryRegion {
    private static final Map<Key, MultiStoryRegion> CACHE = new ConcurrentHashMap<>();
    private record Key(MultiStorySettings s, int rx, int rz) {}

    public static MultiStoryRegion get(MultiStorySettings s, int rx, int rz) {
        if (CACHE.size() > 256) CACHE.clear();
        return CACHE.computeIfAbsent(new Key(s, rx, rz), k -> build(s, rx, rz));
    }
    public static void clearCache() {
        CACHE.clear();
    }

    public final MazeRegion[] stories;

    private MultiStoryRegion(MazeRegion[] stories) {
        this.stories = stories;
    }

    private static MultiStoryRegion build(MultiStorySettings s, int rx, int rz) {
        int k = s.stories().size(), n = s.regionSize(), cs = s.cellSize();
        MazeSettings[] ms = new MazeSettings[k];
        GroupRuntime[] rt = new GroupRuntime[k];
        Occupancy[] occ = new Occupancy[k];

        for (int i = 0; i < k; i++) {
            ms[i] = s.story(i);
            RoomGroup g = RoomGroup.get(ms[i].group());
            rt[i] = g == null ? null : g.runtime(cs);
            occ[i] = new Occupancy(n, rx, rz, cs);
            MazeRegion.placeStart(ms[i], rt[i], rx, rz, occ[i]);
        }

        // stairways between story f and story f+1
        RandomSource rng = RandomSource.create(MazeRegion.mix(s.seed(), rx, rz, 4242));
        for (int i = 0; i < k - 1; i++) {
            if (rt[i] == null || rt[i].stairways.isEmpty()) continue;
            int lower = s.down() ? i + 1 : i;
            int upper = s.down() ? i : i + 1;

            for (int lz = 1; lz < n - 1; lz++)
                for (int lx = 1; lx < n - 1; lx++) {
                    if (rng.nextFloat() >= s.stairwayChance()) continue;
                    LoadedRoom st = rt[i].pickStairway(rng);
                    if (st == null) continue;
                    int rot = rng.nextInt(4);
                    List<Port> lowerPorts = st.portsRotated(rot, 1), upperPorts = st.portsRotated(rot, 2);
                    if (lowerPorts.isEmpty() || upperPorts.isEmpty()) continue;
                    int w = st.footW(rot), d = st.footD(rot);
                    if (!occ[lower].fits(w, d, lx, lz, lowerPorts) || !occ[upper].fits(w, d, lx, lz, upperPorts)) continue;

                    Object id = new Object();
                    int y = ms[lower].baseY() + st.def.yOffset();
                    occ[lower].place(st, rot, lx, lz, lowerPorts, y, id);
                    occ[upper].place(st, rot, lx, lz, upperPorts, y, id);
                }
        }

        MazeRegion[] out = new MazeRegion[k];
        for (int i = 0; i < k; i++) out[i] = MazeRegion.build(ms[i], rx, rz, occ[i]);
        return new MultiStoryRegion(out);
    }
}
