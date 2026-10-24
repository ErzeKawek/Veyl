package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.erzekawek.veyl.Veyl;
import net.erzekawek.veyl.levelgen.chunkgen.grid.Port;
import net.erzekawek.veyl.levelgen.chunkgen.grid.Side;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.GroupRuntime;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.LoadedRoom;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.RoomGroup;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One region = regionSize x regionSize cells of ONE story, generated deterministically from
 * (seed, regionX, regionZ) with an iterative Depth-First search (recursive backtracker).
 * Regions are stitched by deterministic border "gates" so the maze continues forever.
 */
public final class MazeRegion {
    private static final Map<Key, MazeRegion> CACHE = new ConcurrentHashMap<>();
    private record Key(MazeSettings s, int rx, int rz) {}

    /** single story entry point. */
    public static MazeRegion get(MazeSettings s, int rx, int rz) {
        if (CACHE.size() > 512) CACHE.clear();
        return CACHE.computeIfAbsent(new Key(s, rx, rz), k -> {
            Occupancy occ = new Occupancy(s.regionSize(), rx, rz, s.cellSize());
            RoomGroup g = RoomGroup.get(s.group());
            if (g != null) placeStart(s, g.runtime(s.cellSize()), rx, rz, occ);
            return build(s, rx, rz, occ);
        });
    }
    public static void clearCache() { CACHE.clear(); }

    /** A piece placed in the region (single cell or multi-cell room). Coordinates are region-local cells. */
    public static final class Placement {
        public final LoadedRoom room; public final int rot, lx, lz, w, d, index;
        public final List<Port> ports; // region-local
        public final int blockX, blockY, blockZ;
        public final Object pasteId;
        Placement(LoadedRoom room, int rot, int lx, int lz, int index, List<Port> ports,
                  int blockX, int blockY, int blockZ, Object pasteId) {
            this.room = room; this.rot = rot; this.lx = lx; this.lz = lz; this.index = index; this.ports = ports;
            this.w = room.footW(rot); this.d = room.footD(rot);
            this.blockX = blockX; this.blockY = blockY; this.blockZ = blockZ;
            this.pasteId = pasteId == null ? this : pasteId;
        }
    }

    /** Places the start room (region 0,0 only), centred. */
    static void placeStart(MazeSettings s, GroupRuntime rt, int rx, int rz, Occupancy occ) {
        if (rx != 0 || rz != 0 || rt == null || rt.start == null) return;
        LoadedRoom st = rt.start;
        int lx = (occ.n - st.cellsW) / 2, lz = (occ.n - st.cellsD) / 2;
        List<Port> ports = st.portsRotated(0, 0);
        if (!occ.fits(st.cellsW, st.cellsD, lx, lz, ports)) {
            Veyl.LOGGER.error("[Veyl] start room {} doesn't fit ; please raise region size", s.group());
            return;
        }
        occ.place(st, 0, lx, lz, ports, s.baseY() + st.def.yOffset(), null);
    }

    static MazeRegion build(MazeSettings s, int rx, int rz, Occupancy occ) {
        return new MazeRegion(s, rx, rz, occ).generate();
    }

    private final MazeSettings s;
    private final int rx, rz, n, cs;
    private final Occupancy occ;
    private final Placement[] cells;
    private final List<Placement> rooms;

    private MazeRegion(MazeSettings s, int rx, int rz, Occupancy occ) {
        this.s = s; this.rx = rx; this.rz = rz; this.n = s.regionSize(); this.cs = s.cellSize();
        this.occ = occ; this.cells = occ.cells; this.rooms = occ.rooms;
    }

    /** Placement covering a region-local cell, or empty cell. */
    public Placement at(int lx, int lz) { return cells[lz * n + lx]; }

    private MazeRegion generate() {
        RoomGroup group = RoomGroup.get(s.group());
        if (group == null) {
            Veyl.LOGGER.error("[Veyl] unknown room group {}", s.group()); return this;
        }
        GroupRuntime rt = group.runtime(cs);
        RandomSource rng = RandomSource.create(mix(s.seed(), rx, rz, 7));

        // random multi cell room
        if (!rt.multis.isEmpty()) {
            for (int lz = 1; lz < n - 1; lz++)
                for (int lx = 1; lx < n - 1; lx++) {
                    if (rng.nextFloat() >= s.multiRoomChance()) continue;
                    LoadedRoom lr = rt.pickMulti(rng);
                    if (lr == null) continue;
                    int rot = rng.nextInt(4);
                    List<Port> ports = lr.portsRotated(rot, 0);
                    if (occ.fits(lr.footW(rot), lr.footD(rot), lx, lz, ports))
                        occ.place(lr, rot, lx, lz, ports, s.baseY() + lr.def.yOffset(), null);
                }
        }

        // dfs (depth first search)
        int[] mask = new int[n * n];
        boolean[] vis = new boolean[n * n + rooms.size()];
        List<Integer> ring = new ArrayList<>();
        for (int i = 0; i < n * n; i++) {
            int x = i % n, z = i / n;
            if (x == 0 || z == 0 || x == n - 1 || z == n - 1) ring.add(i);
        }
        int start = ring.get(rng.nextInt(ring.size()));
        Deque<Integer> stack = new ArrayDeque<>();
        vis[start] = true; stack.push(start);
        List<int[]> exits = new ArrayList<>();
        while (!stack.isEmpty()) {
            int node = stack.peek();
            exits(node, exits);
            exits.removeIf(e -> vis[nodeOf(e[2])]);
            if (exits.isEmpty()) {
                stack.pop(); continue;
            }
            int[] e = exits.get(rng.nextInt(exits.size()));
            Side side = Side.VALUES[e[1]];
            if (cells[e[0]] == null) mask[e[0]] |= side.bit;
            if (cells[e[2]] == null) mask[e[2]] |= side.opposite().bit;
            int next = nodeOf(e[2]);
            vis[next] = true; stack.push(next);
        }

        // adds loop
        for (Placement p : rooms) {
            if (!vis[n * n + p.index]) {
                for (int z = p.lz; z < p.lz + p.d; z++) for (int x = p.lx; x < p.lx + p.w; x++) cells[z * n + x] = null;
                continue;
            }
            for (Port po : p.ports) {
                int nx = po.cx() + po.side().dx, nz = po.cz() + po.side().dz;
                mask[nz * n + nx] |= po.side().opposite().bit;
            }
        }

        // neighboring region border
        for (int i : ring) {
            if (cells[i] != null || !vis[i]) continue;
            int x = i % n, z = i / n;
            for (Side sd : Side.VALUES) {
                int nx = x + sd.dx, nz = z + sd.dz;
                if (nx >= 0 && nz >= 0 && nx < n && nz < n) continue;
                if (gate(rx * n + x, rz * n + z, sd)) mask[i] |= sd.bit;
            }
        }

        // piece per single cell
        for (int i = 0; i < n * n; i++) {
            if (cells[i] != null || mask[i] == 0 || !vis[i]) continue;
            GroupRuntime.Candidate c = rt.pickSingle(mask[i], rng);
            if (c == null) {
                Veyl.LOGGER.warn("[Veyl] no 1x1 piece with openings mask {} in {}, leaving cell empty", mask[i], s.group()); continue;
            }
            int lx = i % n, lz = i / n;
            cells[i] = new Placement(c.room(), c.rot(), lx, lz, -1, List.of(),
                    (rx * n + lx) * cs, s.baseY() + c.room().def.yOffset(), (rz * n + lz) * cs, null);
        }
        return this;
    }

    private int nodeOf(int idx) {
        Placement p = cells[idx]; return p == null ? idx : n * n + p.index;
    }

    private void exits(int node, List<int[]> out) {
        out.clear();
        if (node < n * n) {
            for (Side sd : Side.VALUES) addExit(out, node, sd, node % n, node / n);
        } else {
            for (Port p : rooms.get(node - n * n).ports) addExit(out, p.cz() * n + p.cx(), p.side(), p.cx(), p.cz());
        }
    }

    private void addExit(List<int[]> out, int from, Side sd, int x, int z) {
        int nx = x + sd.dx, nz = z + sd.dz;
        if (nx < 0 || nz < 0 || nx >= n || nz >= n) return;
        int to = nz * n + nx;
        Placement tp = cells[to];
        if (tp != null) {
            boolean ok = false;
            for (Port p : tp.ports) if (p.cx() == nx && p.cz() == nz && p.side() == sd.opposite()) {
                ok = true; break;
            }
            if (!ok) return;
        }
        out.add(new int[]{from, sd.ordinal(), to});
    }

    private boolean gate(int gx, int gz, Side sd) {
        int ax = gx, az = gz, axis;
        switch (sd) {
            case EAST -> axis = 0;
            case WEST -> { ax = gx - 1; axis = 0; }
            case SOUTH -> axis = 1;
            default -> { az = gz - 1; axis = 1; }
        }
        long h = mix(s.seed(), ax, az, 100 + axis);
        return ((h >>> 11) * 0x1.0p-53) < s.gateChance();
    }

    static long mix(long a, long b, long c, long d) {
        long h = a * 0x9E3779B97F4A7C15L ^ b * 0xC2B2AE3D27D4EB4FL ^ c * 0x165667B19E3779F9L ^ d * 0x27D4EB2F165667C5L;
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL; h ^= h >>> 33; h *= 0xc4ceb9fe1a85ec53L; h ^= h >>> 33;
        return h;
    }
}