package net.erzekawek.veyl.levelgen.chunkgen.gen;

import net.minecraft.resources.Identifier;

/** make everything configurable from dimension JSON. */
public record MazeSettings(int cellSize, int regionSize, int baseY, long seed, float multiRoomChance, float gateChance, Identifier group) {

}
