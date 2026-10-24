package net.erzekawek.veyl.levelgen.chunkgen.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** One story: its own NBT group, its own chances and the vertical distance to the next story. */
public record StorySettings(Identifier group, float multiRoomChance, float gateChance, int distanceToNext) {
    public static final Codec<StorySettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("group").forGetter(StorySettings::group),
            Codec.floatRange(0f, 1f).optionalFieldOf("multi_room_chance", 0.08f).forGetter(StorySettings::multiRoomChance),
            Codec.floatRange(0f, 1f).optionalFieldOf("gate_chance", 0.4f).forGetter(StorySettings::gateChance),
            Codec.intRange(1, 1024).optionalFieldOf("distance_to_next", 16).forGetter(StorySettings::distanceToNext)
    ).apply(i, StorySettings::new));
}
