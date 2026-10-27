package net.erzekawek.veyl.utils.registry;

import net.erzekawek.veyl.types.Shelves;
import net.erzekawek.veyl.types.Sign;
import net.erzekawek.veyl.types.Sign.*;
import net.erzekawek.veyl.types.VeylWood;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CBlockEntity {

    private CBlockEntity() {

    }

    public record Types(
            BlockEntityType<Sign.VeylSignBlockEntity> sign,
            BlockEntityType<Sign.VeylHangingSignBlockEntity> hangingSign,
            BlockEntityType<Shelves.Entity> shelf
    ) {

    }

    private static final Map<Block, Types> BY_BLOCK = new ConcurrentHashMap<>();

    /** Call ONCE per mod in onInitialize(), after creating that mod's WoodSets. */
    public static Types registerForWoodset(String modid, Collection<VeylWood.WoodSet> sets) {
        List<Block>
                signs = new ArrayList<>(),
                hanging = new ArrayList<>(),
                shelves = new ArrayList<>();
        for (VeylWood.WoodSet s : sets) {
            signs.add(s.sign);
            signs.add(s.wallSign);
            hanging.add(s.hangingSign);
            hanging.add(s.wallHangingSign);
            shelves.add(s.shelf);
        }

        BlockEntityType<Sign.VeylSignBlockEntity> sign = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(modid, "sign"),
                FabricBlockEntityTypeBuilder.create(Sign.VeylSignBlockEntity::new)
                        .addBlocks(signs.toArray(new Block[0])).build(null));

        BlockEntityType<Sign.VeylHangingSignBlockEntity> hangingSign = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(modid, "hanging_sign"),
                FabricBlockEntityTypeBuilder.create(Sign.VeylHangingSignBlockEntity::new)
                        .addBlocks(hanging.toArray(new Block[0])).build(null));

        BlockEntityType<Shelves.Entity> shelf = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(modid, "shelf"),
                FabricBlockEntityTypeBuilder.create(Shelves.Entity::new)
                        .addBlocks(shelves.toArray(new Block[0])).build(null));

        Types types = new Types(sign, hangingSign, shelf);
        for (List<Block> list : List.of(signs, hanging, shelves)) for (Block b : list) BY_BLOCK.put(b, types);
        return types;
    }

    public static Types of(Block block) {
        Types t = BY_BLOCK.get(block);
        if (t == null) throw new IllegalStateException("No Veyl block entity types for " + block
                + ". Call VeylBlockEntities.register(modid, sets) in onInitialize().");
        return t;
    }
}
