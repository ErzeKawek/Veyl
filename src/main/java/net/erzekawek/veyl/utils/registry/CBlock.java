package net.erzekawek.veyl.utils.registry;

import net.erzekawek.veyl.blocks.VeylDoublePlantBlock;
import net.erzekawek.veyl.blocks.VeylRiftBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

public class CBlock {

    public static Block makeRift(String modid, String name, int light, ResourceKey<Level> dim, int destX, int destY, int destZ, float yaw) {
        return UnifiedRegistry.registerBlock(modid, name, settings -> new VeylRiftBlock(
                BlockBehaviour.Properties.of()
                        .noCollision()
                        .strength(-1f, 3600000)
                        .lightLevel(blockState -> light)
                        .noLootTable()
                        .noOcclusion()
                        .setId(settings.blockId()),
                dim,
                new Vec3(destX, destY, destZ), yaw));
    }

    public static Block makeDoublePlant(String modid, String name, TagKey<Block> placeableOn) {
        return UnifiedRegistry.registerBlock(modid, name, settings -> new VeylDoublePlantBlock(
                placeableOn,
                BlockBehaviour.Properties.of()
                        .noCollision()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.POPPED)
                        .setId(settings.blockId())));
    }

    public static Block makeLight(String modid, String name, int light) {
        return UnifiedRegistry.registerBlock(modid, name, settings -> new Block(BlockBehaviour.Properties.of()
                .strength(-1, 3600000)
                .sound(SoundType.STONE)
                .lightLevel(blockState -> light)
                .noLootTable()
                .noOcclusion()
                .setId(settings.blockId())));
    }

    public static Block makeUnbreakable(String modid, String name, int light) {
        return UnifiedRegistry.registerBlock(modid, name, settings -> new Block(BlockBehaviour.Properties.of()
                .strength(-1, 3600000)
                .sound(SoundType.STONE)
                .lightLevel(blockState -> light)
                .noLootTable()
                .noOcclusion()
                .setId(settings.blockId())));
    }

    public static void initialize() {

    }

}
