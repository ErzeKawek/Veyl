package net.erzekawek.veyl.types;

import net.erzekawek.veyl.utils.registry.UnifiedRegistry;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.*;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Arrays;
import java.util.List;

public class SimpleMaterial {
    public final String materialName;
    public final Block baseBlock;
    public final Block baseStairs;
    public final Block baseSlab;
    public final Block baseWall;
    public final Block bricks;
    public final Block brickStairs;
    public final Block brickSlab;
    public final Block brickWall;
    public final Block polished;
    public final Block polishedStairs;
    public final Block polishedSlab;
    public final Block polishedWall;
    public final Block tiles;
    public final Block tileStairs;
    public final Block tileSlab;
    public final Block tileWall;
    public final Block pillar;
    public final Block button;
    public final Block pressurePlate;
    public final List<Block> blocks;
    private final MapColor mapColor;


    public SimpleMaterial(String modid, String name, MapColor mapColor) {
        this.materialName = name;
        this.mapColor = mapColor;

        baseBlock = UnifiedRegistry.registerBlock(modid, materialName, settings -> new Block(applySettings(settings)));
        baseStairs = UnifiedRegistry.registerBlock(modid, materialName + "_stairs", settings -> new StairBlock(baseBlock.defaultBlockState(), applySettings(settings)));
        baseSlab = UnifiedRegistry.registerBlock(modid, materialName + "_slab", settings -> new SlabBlock(applySettings(settings)));
        baseWall = UnifiedRegistry.registerBlock(modid, materialName + "_wall", settings -> new WallBlock(applySettings(settings)));

        bricks = UnifiedRegistry.registerBlock(modid, materialName + "_bricks", settings -> new Block(applySettings(settings)));
        brickStairs = UnifiedRegistry.registerBlock(modid, materialName + "_brick_stairs", settings -> new StairBlock(bricks.defaultBlockState(), applySettings(settings)));
        brickSlab = UnifiedRegistry.registerBlock(modid, materialName + "_brick_slab", settings -> new SlabBlock(applySettings(settings)));
        brickWall = UnifiedRegistry.registerBlock(modid, materialName + "_brick_wall", settings -> new WallBlock(applySettings(settings)));

        polished = UnifiedRegistry.registerBlock(modid, materialName + "_polished", settings -> new Block(applySettings(settings)));
        polishedStairs = UnifiedRegistry.registerBlock(modid, materialName + "_polished_stairs", settings -> new StairBlock(polished.defaultBlockState(), applySettings(settings)));
        polishedSlab = UnifiedRegistry.registerBlock(modid, materialName + "_polished_slab", settings -> new SlabBlock(applySettings(settings)));
        polishedWall = UnifiedRegistry.registerBlock(modid, materialName + "_polished_wall", settings -> new WallBlock(applySettings(settings)));

        tiles = UnifiedRegistry.registerBlock(modid, materialName + "_tiles", settings -> new Block(applySettings(settings)));
        tileStairs = UnifiedRegistry.registerBlock(modid, materialName + "_tile_stairs", settings -> new StairBlock(tiles.defaultBlockState(), applySettings(settings)));
        tileSlab = UnifiedRegistry.registerBlock(modid, materialName + "_tile_slab", settings -> new SlabBlock(applySettings(settings)));
        tileWall = UnifiedRegistry.registerBlock(modid, materialName + "_tile_wall", settings -> new WallBlock(applySettings(settings)));

        pillar = UnifiedRegistry.registerBlock(modid, materialName + "_pillar", settings -> new RotatedPillarBlock(applySettings(settings)));
        button = UnifiedRegistry.registerBlock(modid, materialName + "_button", settings -> new ButtonBlock(
                BlockSetType.POLISHED_BLACKSTONE,
                30,
                settings.noCollision().strength(0.5F).pushReaction(PushReaction.POPPED)));
        pressurePlate = UnifiedRegistry.registerBlock(modid, materialName + "_pressure_plate",
                settings -> new PressurePlateBlock(BlockSetType.POLISHED_BLACKSTONE,
                        settings.mapColor(mapColor)
                                .forceSolidOn()
                                .instrument(NoteBlockInstrument.BASEDRUM)
                                .noCollision()
                                .strength(0.5F)
                                .pushReaction(PushReaction.POPPED)));

        blocks = Arrays.asList(baseBlock, baseStairs, baseSlab, baseWall, bricks, brickStairs,
                brickSlab, brickWall, polished, polishedStairs, polishedSlab, polishedWall, tiles,
                tileStairs, tileSlab, tileWall, pillar, button, pressurePlate);
    }

    public Properties applySettings(Properties settings) {
        return settings.instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                .strength(3.0F, 9.0F)
                .mapColor(this.mapColor);
    }
}
