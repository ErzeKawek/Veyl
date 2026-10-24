package net.erzekawek.veyl.levelgen.chunkgen.grid;

/** sides n = -z, e = +x, s = +z, w = -x */
public enum Side {
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

    public static final Side[] VALUES = values();
    public final int dx, dz, bit;

    Side(int dx, int dz) { this.dx = dx; this.dz = dz; this.bit = 1 << ordinal(); }

    public Side opposite() { return VALUES[(ordinal() + 2) & 3]; }
    public Side cw() { return VALUES[(ordinal() + 1) & 3]; }

    /** an opening mask rotating clockwise n quarter turns. */
    public static int rotateMask(int mask, int n) {
        int r = 0;
        for (Side s : VALUES) if ((mask & s.bit) != 0) r |= VALUES[(s.ordinal() + n) & 3].bit;
        return r;
    }

    /** outer edge of a sizex*sizez footprint a block column is closest to. */
    public static Side nearestEdge(int x, int z, int sizeX, int sizeZ) {
        int n = z, s = sizeZ - 1 - z, w = x, e = sizeX - 1 - x;
        int min = Math.min(Math.min(n, s), Math.min(w, e));
        if (min == n) return NORTH;
        if (min == e) return EAST;
        if (min == s) return SOUTH;
        return WEST;
    }
}
