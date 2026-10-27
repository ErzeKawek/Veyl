package net.erzekawek.veyl.types;

import net.erzekawek.veyl.utils.registry.UnifiedRegistry;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Arrays;
import java.util.List;

public class VeylWood {

    public static class WoodSet {

        public final String baseName;
        public final WoodType woodType;
        public final Block log;
        public final Block strippedLog;
        public final Block wood;
        public final Block strippedWood;
        public final Block planks;
        public final Block slab;
        public final Block stairs;
        public final Block door;
        public final Block trapdoor;
        public final Block fence;
        public final Block gate;
        public final Block button;
        public final Block pressurePlate;
        public final Block ladder;
        public final Block sign;
        public final Block wallSign;
        public final Block hangingSign;
        public final Block wallHangingSign;
        public final Block shelf;
        // public final Block stool;
        public final List<Block> blocks;
        private final MapColor woodColor;
        private final SoundType logSounds;

        public WoodSet(String modid, String name, MapColor barkColor, MapColor woodColor) {
            this.baseName = name;
            this.woodColor = woodColor;

            this.woodType = createWoodType(modid, baseName);
            this.logSounds = createWoodSoundGroup(modid, baseName + "_log");

            log = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_log",
                    settings -> new RotatedPillarBlock(
                            applyLogSettings(
                                    settings.mapColor(
                                            state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y
                                                    ? woodColor
                                                    : barkColor
                                    )
                            )
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            strippedLog = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_stripped_log",
                    settings -> new RotatedPillarBlock(
                            applyLogSettings(settings.mapColor(woodColor))
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            wood = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_wood",
                    settings -> new RotatedPillarBlock(
                            applyLogSettings(settings.mapColor(barkColor))
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            strippedWood = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_stripped_wood",
                    settings -> new RotatedPillarBlock(
                            applyLogSettings(settings.mapColor(woodColor))
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );

            BlockTransformerHelper.registerStripping(log, strippedLog);
            BlockTransformerHelper.registerStripping(wood, strippedWood);

            planks = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_planks",
                    settings -> new Block(applyPlankSettings(settings)),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            slab = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_slab",
                    settings -> new SlabBlock(applyPlankSettings(settings)),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_SLABS)
                    )
            );
            stairs = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_stairs",
                    settings -> new StairBlock(
                            planks.defaultBlockState(), applyPlankSettings(settings)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );

            door = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_door",
                    settings -> new DoorBlock(
                            woodType.setType(),
                            settings.mapColor(planks.defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(3.0F)
                                    .noOcclusion()
                                    .ignitedByLava()
                                    .pushReaction(PushReaction.POPPED)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE)
                    )

            );
            trapdoor = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_trapdoor",
                    settings -> new TrapDoorBlock(
                            woodType.setType(),
                            settings.mapColor(planks.defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(3.0F)
                                    .noOcclusion()
                                    .isValidSpawn(Blocks::never)
                                    .ignitedByLava()
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            fence = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_fence",
                    settings -> new FenceBlock(
                            settings.mapColor(planks.defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .ignitedByLava()
                                    .sound(woodType.soundType())
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            gate = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_fence_gate",
                    settings -> new FenceGateBlock(
                            woodType,
                            settings
                                    .mapColor(planks.defaultMapColor())
                                    .forceSolidOn()
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .ignitedByLava()
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            button = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_button",
                    settings -> new ButtonBlock(
                            woodType.setType(),
                            30,
                            settings.noCollision().strength(0.5F).pushReaction(PushReaction.POPPED)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL)
                    )
            );
            pressurePlate = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_pressure_plate",
                    settings -> new PressurePlateBlock(
                            woodType.setType(),
                            settings.mapColor(planks.defaultMapColor())
                                    .forceSolidOn()
                                    .instrument(NoteBlockInstrument.BASS)
                                    .noCollision()
                                    .strength(0.5F)
                                    .ignitedByLava()
                                    .pushReaction(PushReaction.POPPED)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            ladder = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_ladder",
                    settings -> new LadderBlock(
                            settings
                                    .strength(0.4F)
                                    .sound(SoundType.LADDER)
                                    .noOcclusion()
                                    .pushReaction(PushReaction.POPPED)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );
            sign = UnifiedRegistry.registerBlock(
                    modid,
                    baseName + "_sign",
                    settings -> new Sign.VeylStandingSignBlock(
                            woodType,
                            settings.mapColor(planks.defaultMapColor())
                    ),
                    false
            );
            wallSign = UnifiedRegistry.registerBlock(
                    modid,
                    baseName + "_wall_sign",
                    settings -> new Sign.VeylWallSignBlock(
                            woodType,
                            settings
                                    .mapColor(planks.defaultMapColor())
                                    .overrideLootTable(sign.getLootTable())
                                    .overrideDescription(sign.getDescriptionId())
                    ),
                    false
            );
            UnifiedRegistry.registerItem(
                    modid,
                    baseName + "_sign",
                    settings -> new StandingAndWallBlockItem(
                            sign,
                            wallSign,
                            Direction.DOWN,
                            settings
                    ),
                    new Item.Properties()
                            .stacksTo(16).setId(
                                    ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modid, baseName + "_sign"))
                            ).useBlockDescriptionPrefix()
                            .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE)
                            .signText()
            );
            hangingSign = UnifiedRegistry.registerBlock(
                    modid,
                    baseName + "_hanging_sign",
                    settings -> new Sign.VeylCeilingHangingSignBlock(
                            woodType,
                            settings.mapColor(planks.defaultMapColor())
                    ),
                    false
            );
            wallHangingSign = UnifiedRegistry.registerBlock(
                    modid,
                    baseName + "_wall_hanging_sign",
                    settings -> new Sign.VeylWallHangingSignBlock(
                            woodType,
                            settings
                                    .overrideLootTable(hangingSign.getLootTable())
                                    .overrideDescription(hangingSign.getDescriptionId())
                                    .mapColor(planks.defaultMapColor())
                    ),
                    false
            );
            UnifiedRegistry.registerItem(
                    modid,
                    baseName + "_hanging_sign",
                    settings -> new HangingSignItem(
                            hangingSign,
                            wallHangingSign,
                            settings
                    ),
                    new Item.Properties()
                            .stacksTo(16).setId(
                                    ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modid, baseName + "_hanging_sign"))
                            ).useBlockDescriptionPrefix()
                            .cookingFuel(ContextIntProviders.COOKING_TIME_HANGING_SIGNS)
                            .signText()
            );
            shelf = UnifiedRegistry.registerSpecialBlockItem(
                    modid,
                    baseName + "_shelf",
                    settings -> new Shelves(
                            settings.mapColor(planks.defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .sound(SoundType.SHELF)
                                    .ignitedByLava()
                                    .strength(2.0F, 3.0F)
                    ),
                    (block, id) -> new BlockItem(
                            block,
                            new Item.Properties()
                                    .setId(id.item())
                                    .useBlockDescriptionPrefix()
                                    .requiredFeatures(block.requiredFeatures())
                                    .cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)
                    )
            );

            for (Block block : Arrays.asList(log, strippedLog, wood, strippedWood)) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
            }
            for (Block block : Arrays.asList(planks, slab, stairs, fence, gate)) {
                FlammableBlockRegistry.getDefaultInstance().add(block, 5, 20);
            }
            FlammableBlockRegistry.getDefaultInstance().add(shelf, 30, 20);

            blocks = Arrays.asList(
                    log,
                    strippedLog,
                    wood,
                    strippedWood,
                    planks,
                    slab,
                    stairs,
                    door,
                    trapdoor,
                    fence,
                    gate,
                    button,
                    pressurePlate,
                    ladder,
                    sign,
                    hangingSign,
                    shelf
            );
        }

        public BlockBehaviour.Properties applyLogSettings(BlockBehaviour.Properties settings) {
            return settings
                    .instrument(NoteBlockInstrument.BASS)
                    .sound(this.logSounds)
                    .strength(2.0F)
                    .ignitedByLava();
        }

        public BlockBehaviour.Properties applyPlankSettings(BlockBehaviour.Properties settings) {
            return settings
                    .mapColor(this.woodColor)
                    .instrument(NoteBlockInstrument.BASS)
                    .sound(this.woodType.soundType())
                    .strength(2.0F, 3.0F)
                    .ignitedByLava();
        }
    }

    public static WoodType createWoodType(String modid, String name) {
        SoundType soundGroup = createWoodSoundGroup(modid, name);

        return (new WoodTypeBuilder())
                .soundType(soundGroup)
                .hangingSignSoundType(createWoodSoundGroup(modid, name + "_hanging_sign"))
                .fenceGateCloseSound(UnifiedRegistry.registerSound(modid, "block." + name + "_fence_gate.close"))
                .fenceGateOpenSound(UnifiedRegistry.registerSound(modid, "block." + name + "_fence_gate.open"))
                .register(Identifier.fromNamespaceAndPath(modid, name), createWoodSetType(modid, name, soundGroup));
    }

    private static BlockSetType createWoodSetType(String modid, String name, SoundType soundGroup) {
        return (new BlockSetTypeBuilder())
                .openableByHand(true)
                .openableByWindCharge(true)
                .buttonActivatedByArrows(true)
                .pressurePlateActivationRule(BlockSetType.PressurePlateSensitivity.EVERYTHING)
                .soundType(soundGroup)
                .doorCloseSound(UnifiedRegistry.registerSound(modid, "block." + name + "_door.close"))
                .doorOpenSound(UnifiedRegistry.registerSound(modid, "block." + name + "_door.open"))
                .trapdoorCloseSound(UnifiedRegistry.registerSound(modid, "block." + name + "_trapdoor.close"))
                .trapdoorOpenSound(UnifiedRegistry.registerSound(modid, "block." + name + "_trapdoor.open"))
                .pressurePlateClickOffSound(
                        UnifiedRegistry.registerSound(modid, "block." + name + "_pressure_plate.click_off")
                ).pressurePlateClickOnSound(
                        UnifiedRegistry.registerSound(modid, "block." + name + "_pressure_plate.click_on")
                ).register(Identifier.fromNamespaceAndPath(modid, name));

    }

    private static SoundType createWoodSoundGroup(String modid, String name) {
        return new SoundType(
                1.0F,
                1.0F,
                UnifiedRegistry.registerSound(modid, "block." + name + ".break"),
                UnifiedRegistry.registerSound(modid, "block." + name + ".step"),
                UnifiedRegistry.registerSound(modid, "block." + name + ".place"),
                UnifiedRegistry.registerSound(modid, "block." + name + ".hit"),
                UnifiedRegistry.registerSound(modid, "block." + name + ".fall")
        );
    }

}
