package net.erzekawek.veyl.types;

import net.erzekawek.veyl.blockentities.CustomStorageEntity;
import net.erzekawek.veyl.blocks.VeylConfigurableStorageBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class VeylStorage {
    private VeylStorage() {

    }

    public record Entry(VeylConfigurableStorageBlock block, BlockItem item, BlockEntityType<CustomStorageEntity> blockEntityType) {}

    public static Entry register(Identifier id, int slots, UnaryOperator<BlockBehaviour.Properties> properties) {
        return register(id, properties, props -> new VeylConfigurableStorageBlock(slots, props));
    }

    public static Entry register(Identifier id, UnaryOperator<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, ? extends VeylConfigurableStorageBlock> factory) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties props = properties.apply(BlockBehaviour.Properties.of().setId(blockKey));
        VeylConfigurableStorageBlock block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(props));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        BlockItem item = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));

        BlockEntityType<CustomStorageEntity> type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(
                        (pos, state) -> new CustomStorageEntity(((VeylConfigurableStorageBlock) state.getBlock()).blockEntityType(), pos, state),
                        block).build());

        block.setBlockEntityType(type);
        return new Entry(block, item, type);
    }
}
