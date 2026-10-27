package net.erzekawek.veyl.datagen;

import net.erzekawek.veyl.types.SimpleMaterial;
import net.erzekawek.veyl.types.VeylWood.*;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Block;

public class DatagenHelpers {

    public static void generateWoodModels(
            BlockModelGenerators gen,
            WoodSet wood
    ) {
        gen.woodProvider(wood.log).log(wood.log).wood(wood.wood);
        gen.woodProvider(wood.strippedLog).log(wood.strippedLog).wood(wood.strippedWood);
        gen.family(wood.planks).stairs(wood.stairs).slab(wood.slab).fence(wood.fence).fenceGate(wood.gate).button(wood.button).pressurePlate(wood.pressurePlate);
        gen.family(wood.strippedLog).sign(wood.sign).hangingSign(wood.hangingSign);
        gen.createDoor(wood.door);
        gen.createTrapdoor(wood.trapdoor);
        gen.createShelf(wood.shelf, wood.strippedLog);
    }

    public static void generateMaterialModels(
            BlockModelGenerators gen,
            SimpleMaterial material
    ) {
        gen.family(material.baseBlock).stairs(material.baseStairs).slab(material.baseSlab).wall(material.baseWall);

        gen.family(material.bricks).stairs(material.brickStairs).slab(material.brickSlab).wall(material.brickWall);

        gen.family(material.polished).stairs(material.polishedStairs).slab(material.polishedSlab).wall(material.polishedWall).button(material.button).pressurePlate(material.pressurePlate);

        gen.family(material.tiles).stairs(material.tileStairs).slab(material.tileSlab).wall(material.tileWall);

        gen.createAxisAlignedPillarBlock(material.pillar, TexturedModel.COLUMN);
    }

    public static void createPillar(
            BlockModelGenerators gen,
            Block block
    ) {
        gen.createAxisAlignedPillarBlock(block, TexturedModel.COLUMN);
    }

}
