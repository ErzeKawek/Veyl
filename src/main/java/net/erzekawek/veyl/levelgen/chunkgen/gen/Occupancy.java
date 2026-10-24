package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.erzekawek.veyl.levelgen.chunkgen.grid.Port;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.LoadedRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Which cells of one region are taken by multi-cell rooms, and which neighbour cells are
 * reserved so every opening of a room has a free cell to connect to.
 */
final class Occupancy {
    final int n, rx, rz, cs;
    final MazeRegion.Placement[] cells;
    final boolean[] reserved;
    final List<MazeRegion.Placement> rooms = new ArrayList<>();

    Occupancy(int n, int rx, int rz, int cs) {
        this.n = n; this.rx = rx; this.rz = rz; this.cs = cs;
        this.cells = new MazeRegion.Placement[n * n];
        this.reserved = new boolean[n * n];
    }

    /** Rooms stay in the interior so the border ring is free for region gates. `ports` are footprint-relative. */
    boolean fits(int w, int d, int lx, int lz, List<Port> ports) {
        if (lx < 1 || lz < 1 || lx + w > n - 1 || lz + d > n - 1) return false;
        for (int z = lz; z < lz + d; z++)
            for (int x = lx; x < lx + w; x++) { int i = z * n + x; if (cells[i] != null || reserved[i]) return false; }
        for (Port p : ports) {
            int nx = lx + p.cx() + p.side().dx, nz = lz + p.cz() + p.side().dz;
            if (nx < 0 || nz < 0 || nx >= n || nz >= n || cells[nz * n + nx] != null) return false;
        }
        return true;
    }

    /** Call only after {@link #fits}. pasteId links placements of the same physical piece across stories. */
    MazeRegion.Placement place(LoadedRoom lr, int rot, int lx, int lz, List<Port> ports, int blockY, Object pasteId) {
        List<Port> local = new ArrayList<>();
        for (Port p : ports) local.add(new Port(lx + p.cx(), lz + p.cz(), p.side()));
        MazeRegion.Placement pl = new MazeRegion.Placement(lr, rot, lx, lz, rooms.size(), List.copyOf(local),
                (rx * n + lx) * cs, blockY, (rz * n + lz) * cs, pasteId);
        rooms.add(pl);
        for (int z = lz; z < lz + pl.d; z++) for (int x = lx; x < lx + pl.w; x++) cells[z * n + x] = pl;
        for (Port p : local) reserved[(p.cz() + p.side().dz) * n + p.cx() + p.side().dx] = true;
        return pl;
    }
}
