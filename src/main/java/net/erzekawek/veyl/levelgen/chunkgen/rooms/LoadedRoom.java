package net.erzekawek.veyl.levelgen.chunkgen.rooms;

import net.erzekawek.veyl.Veyl;
import net.erzekawek.veyl.levelgen.chunkgen.grid.Port;
import net.erzekawek.veyl.levelgen.chunkgen.grid.Side;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.*;

/**
 * A room NBT parsed into flat arrays. Guide blocks are stripped and converted to {@link Port}s.
 * Port roles: 0 = normal\, 1 = to the LOWER story, 2 = to the UPPER story.
 */
public final class LoadedRoom {

    public final RoomGroup.RoomDef def;
    public final int sx, sy, sz, cellsW, cellsD;
    public final int openMask;

    private final List<List<Port>> basePorts;
    @SuppressWarnings("unchecked")
    private final List<Port>[][] portsRot = new List[3][4];
    private final Rotated[] rotated = new Rotated[4];
    private final int[] xs, ys, zs;
    private final BlockState[] states;
    private final CompoundTag[] nbts;

    public static final class Rotated {
        public final int[] xs, ys, zs;
        public final BlockState[] states;
        public final CompoundTag[] nbts;
        public final int sx, sz;
        Rotated(int[] xs, int[] ys, int[] zs, BlockState[] st, CompoundTag[] nbts, int sx, int sz) {
            this.xs = xs; this.ys = ys; this.zs = zs; this.states = st; this.nbts = nbts; this.sx = sx; this.sz = sz;
        }
    }

    private LoadedRoom(RoomGroup.RoomDef def, int sx, int sy, int sz, int[] xs, int[] ys, int[] zs, BlockState[] st, CompoundTag[] nbts, List<List<Port>> basePorts) {
        this.def = def; this.sx = sx; this.sy = sy; this.sz = sz;
        this.xs = xs; this.ys = ys; this.zs = zs; this.states = st; this.nbts = nbts;
        this.cellsW = def.cellsX(); this.cellsD = def.cellsZ();
        this.basePorts = basePorts;
        int m = 0; for (Port p : basePorts.get(0)) m |= p.side().bit;
        this.openMask = m;
    }

    public int footW(int rot) {
        return (rot & 1) == 0 ? cellsW : cellsD;
    }
    public int footD(int rot) {
        return (rot & 1) == 0 ? cellsD : cellsW;
    }

    public List<Port> portsRotated(int rot) {
        return portsRotated(rot, 0);
    }

    /** Ports of a role for a clockwise rotation, in footprint-local cell coords. */
    public synchronized List<Port> portsRotated(int rot, int role) {
        if (portsRot[role][rot] == null) {
            List<Port> cur = basePorts.get(role); int w = cellsW, d = cellsD;
            for (int r = 0; r < rot; r++) {
                List<Port> next = new ArrayList<>();
                for (Port p : cur) next.add(p.rotateCw(w, d));
                cur = next; int t = w; w = d; d = t;
            }
            portsRot[role][rot] = cur;
        }
        return portsRot[role][rot];
    }

    public synchronized Rotated rotated(int rot) {
        if (rotated[rot] == null) {
            int n = xs.length;
            int[] rx = new int[n], rz = new int[n]; BlockState[] rs = new BlockState[n];
            int outW = (rot & 1) == 0 ? sx : sz, outD = (rot & 1) == 0 ? sz : sx;
            for (int i = 0; i < n; i++) {
                int x = xs[i], z = zs[i], w = sx, d = sz; BlockState s = states[i];
                for (int r = 0; r < rot; r++) {
                    int nx = d - 1 - z, nz = x; x = nx; z = nz;
                    int t = w; w = d; d = t; s = s.rotate(Rotation.CLOCKWISE_90);
                }
                rx[i] = x; rz[i] = z; rs[i] = s;
            }
            rotated[rot] = new Rotated(rx, ys, rz, rs, nbts, outW, outD);
        }
        return rotated[rot];
    }

    /** Normal piece: 1 guide block type. */
    static LoadedRoom load(RoomGroup.RoomDef def, int cellSize, Block guide) {
        return parse(def, cellSize, guide, null, null);
    }

    /** Stairway: 2 guide block types (lower openings / upper openings). */
    static LoadedRoom loadStairway(RoomGroup.RoomDef def, int cellSize, Block lower, Block upper) {
        return parse(def, cellSize, null, lower, upper);
    }

    private static LoadedRoom parse(RoomGroup.RoomDef def, int cellSize, Block g0, Block lower, Block upper) {
        MinecraftServer server = Veyl.server();
        if (server == null) {
            Veyl.LOGGER.error("[Veyl] server not ready while loading {}", def.nbt()); return null;
        }

        Optional<StructureTemplate> opt = server.getStructureTemplateManager().get(def.nbt());
        if (opt.isEmpty()) {
            Veyl.LOGGER.error("[Veyl] missing structure NBT: {}", def.nbt()); return null;
        }

        CompoundTag tag = opt.get().save(new CompoundTag());
        ListTag size = tag.getListOrEmpty("size");
        int sx = size.getIntOr(0, 0), sy = size.getIntOr(1, 0), sz = size.getIntOr(2, 0);

        if (sx != def.cellsX() * cellSize || sz != def.cellsZ() * cellSize) {
            Veyl.LOGGER.warn("[Veyl] {} is {}x{} blocks but {}x{} cells of {} = {}x{}; check the NBT or cell_size",
                    def.nbt(), sx, sz, def.cellsX(), def.cellsZ(), cellSize, def.cellsX() * cellSize, def.cellsZ() * cellSize);
        }

        ListTag paletteTag = tag.getListOrEmpty("palette");
        BlockState[] palette = new BlockState[paletteTag.size()];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = BlockState.CODEC.parse(NbtOps.INSTANCE, paletteTag.getCompoundOrEmpty(i))
                    .result().orElse(Blocks.AIR.defaultBlockState());
        }

        ListTag blocks = tag.getListOrEmpty("blocks");
        int cap = blocks.size();
        int[] xs = new int[cap], ys = new int[cap], zs = new int[cap];
        BlockState[] st = new BlockState[cap];
        CompoundTag[] nb = new CompoundTag[cap];
        int count = 0;
        List<Set<Port>> sets = new ArrayList<>();
        for (int i = 0; i < 3; i++) sets.add(new LinkedHashSet<>());

        for (int i = 0; i < cap; i++) {
            CompoundTag b = blocks.getCompoundOrEmpty(i);
            ListTag pos = b.getListOrEmpty("pos");
            int x = pos.getIntOr(0, 0), y = pos.getIntOr(1, 0), z = pos.getIntOr(2, 0);
            BlockState s = palette[Math.max(0, Math.min(palette.length - 1, b.getIntOr("state", 0)))];

            int role = (g0 != null && s.is(g0)) ? 0 : (lower != null && s.is(lower)) ? 1 : (upper != null && s.is(upper)) ? 2 : -1;
            if (role >= 0) {
                sets.get(role).add(portAt(def, cellSize, x, z, sx, sz));
                continue;
            }
            if (s.isAir() || s.is(Blocks.STRUCTURE_VOID)) continue;
            xs[count] = x; ys[count] = y; zs[count] = z; st[count] = s;
            nb[count] = b.getCompound("nbt").orElse(null);
            count++;
        }

        List<List<Port>> base = new ArrayList<>();
        for (Set<Port> set : sets) base.add(List.copyOf(set));

        if (g0 != null && base.get(0).isEmpty()) Veyl.LOGGER.warn("[Veyl] {} yo bro wher da guides ({}), i not connecting", def.nbt(), g0);
        if (g0 == null && (base.get(1).isEmpty() || base.get(2).isEmpty()))
            Veyl.LOGGER.warn("[Veyl] yo ts stairway {} needs BOTH da lower and upper guide blocks (found {} lower, {} upper); it will not spawn",
                    def.nbt(), base.get(1).size(), base.get(2).size());

        return new LoadedRoom(def, sx, sy, sz,
                Arrays.copyOf(xs, count), Arrays.copyOf(ys, count), Arrays.copyOf(zs, count),
                Arrays.copyOf(st, count), Arrays.copyOf(nb, count), base);
    }

    private static Port portAt(RoomGroup.RoomDef def, int cellSize, int x, int z, int sx, int sz) {
        Side side = Side.nearestEdge(x, z, sx, sz);
        int cx, cz;
        switch (side) {
            case NORTH -> {
                cx = Math.min(x / cellSize, def.cellsX() - 1); cz = 0;
            }
            case SOUTH -> {
                cx = Math.min(x / cellSize, def.cellsX() - 1); cz = def.cellsZ() - 1;
            }
            case WEST  -> {
                cx = 0; cz = Math.min(z / cellSize, def.cellsZ() - 1);
            }
            default    -> {
                cx = def.cellsX() - 1; cz = Math.min(z / cellSize, def.cellsZ() - 1);
            }
        }
        return new Port(cx, cz, side);
    }
}
