package net.erzekawek.veyl.utils.misc;

import net.minecraft.world.level.block.Block;

public class Flags {
    public static final int FLAG_UPDATE_BLOCK = 1;
    public static final int FLAG_SEND_CLIENT_CHANGES = 2;
    public static final int FLAG_NO_RERENDER = 4;
    public static final int FORSE_RERENDER = 8;
    public static final int FLAG_IGNORE_OBSERVERS = 16;

    public static final int SILENT = FLAG_IGNORE_OBSERVERS | FLAG_SEND_CLIENT_CHANGES;
    public static final int SET_OBSERV = FLAG_UPDATE_BLOCK | FLAG_SEND_CLIENT_CHANGES;

    public static final int CL_KNOWN_SHAPE = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

}
