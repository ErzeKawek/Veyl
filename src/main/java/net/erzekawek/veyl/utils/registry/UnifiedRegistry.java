package net.erzekawek.veyl.utils.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UnifiedRegistry {

    public static Map<Item, ResourceKey<Item>> ItemidLookup = new HashMap<>();

    public static Block registerBlock(String modid, String name, Function<BlockBehaviour.Properties, Block> factory) {
        return registerBlock(modid, name, factory, true);
    }

    public static Block registerBlock(String modid, String name, Function<BlockBehaviour.Properties, Block> factory, boolean hasItem) {
        return registerBlock(modid, name, factory, BlockBehaviour.Properties.of(), hasItem);
    }

    private static Block registerBlock(
            String modid,
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties settings,
            boolean hasItem
    ) {
        if (hasItem) {
            BlockItemId id = BlockItemId.create(Identifier.fromNamespaceAndPath(modid, name), Identifier.fromNamespaceAndPath(modid, name));
            Block block = factory.apply(settings.setId(id.block()));
            registerItem(
                    id.item(),
                    new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                    )
            );
            return Registry.register(BuiltInRegistries.BLOCK, id.block(), block);
        }
        ResourceKey<Block> id = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(modid, name));
        Block block = factory.apply(settings.setId(id));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static Block registerSpecialBlockItem(
            String modid,
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BiFunction<Block, BlockItemId, BlockItem> itemFactory
    ) {
        return registerSpecialBlockItem(modid, name, name, factory, itemFactory);
    }

    public static Block registerSpecialBlockItem(
            String modid,
            String blockName,
            String itemName,
            Function<BlockBehaviour.Properties, Block> factory,
            BiFunction<Block, BlockItemId, BlockItem> itemFactory
    ) {
        BlockItemId id = BlockItemId.create(Identifier.fromNamespaceAndPath(modid, blockName), Identifier.fromNamespaceAndPath(modid, itemName));
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(id.block()));
        registerItem(id.item(), itemFactory.apply(block, id));
        return Registry.register(BuiltInRegistries.BLOCK, id.block(), block);
    }


    public static Item registerItem(String modid, String itemName) {
        return registerItem(modid, itemName, new Item.Properties());
    }

    public static Item registerItem(String modid, String itemName, Item.Properties settings) {
        return registerItem(modid, itemName, Item::new, settings);
    }

    public static Item registerItem(
            String modid,
            String itemName,
            Function<Item.Properties, Item> factory,
            Item.Properties settings
    ) {
        ResourceKey<Item> id = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modid, itemName));
        Item item = factory.apply(settings.setId(id));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return registerItem(id, item);
    }

    public static Item registerItem(ResourceKey<Item> id, Item item) {
        ItemidLookup.put(item, id);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static SoundEvent registerSound(String modid, String soundId) {
        Identifier id = Identifier.fromNamespaceAndPath(modid, soundId);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    public static Holder.Reference<SoundEvent> registerReference(String modid, String soundId) {
        Identifier id = Identifier.fromNamespaceAndPath(modid, soundId);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static Item registerMusicDisc(String modid, String diskName) {
        registerReference(modid, "music_disc." + diskName);

        Item.Properties settings = new Item.Properties()
                .overrideDescription("item." + modid + ".music_disc_" + diskName)
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .jukeboxPlayable(
                        ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(modid, diskName))
                );

        return registerItem(modid, "music_disc_" + diskName, settings);
    }

    public static void initialize() {
    }
}
