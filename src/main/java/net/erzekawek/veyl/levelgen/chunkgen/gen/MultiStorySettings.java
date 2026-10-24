package net.erzekawek.veyl.levelgen.chunkgen.gen;

import java.util.List;

/**
 * Settings of the multi-story generator. Story 0 sits at base_y. With direction "down" every next story is
 * below the previous one, with "up" above it, separated by the previous story's distance_to_next.
 */
public record MultiStorySettings(int cellSize, int regionSize, int baseY, long seed, boolean down, float stairwayChance, List<StorySettings> stories) {

    public int storyBaseY(int index) {
        int y = baseY;
        for (int k = 0; k < index; k++) y += (down ? -1 : 1) * stories.get(k).distanceToNext();
        return y;
    }

    /** Per-story view used by the normal single-story maze builder. Each story has its own seed. */
    public MazeSettings story(int index) {
        StorySettings st = stories.get(index);
        long storySeed = seed + 0x9E3779B97F4A7C15L * (index + 1);
        return new MazeSettings(cellSize, regionSize, storyBaseY(index), storySeed,
                st.multiRoomChance(), st.gateChance(), st.group());
    }
}
