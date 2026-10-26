package net.erzekawek.veyl;

import net.erzekawek.veyl.levelgen.chunkgen.gen.MazeChunkGenerator;
import net.erzekawek.veyl.levelgen.chunkgen.gen.MazeRegion;
import net.erzekawek.veyl.levelgen.chunkgen.gen.MultiStoryChunkGenerator;
import net.erzekawek.veyl.levelgen.chunkgen.gen.MultiStoryRegion;
import net.erzekawek.veyl.levelgen.chunkgen.rooms.RoomGroup;
import net.erzekawek.veyl.utils.registry.CBlock;
import net.erzekawek.veyl.utils.registry.UnifiedRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Veyl implements ModInitializer {

    public static final String MOD_ID = "veyl";

    public static final String MOD_NAME = "Veyl";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    private static volatile MinecraftServer server;
    public static MinecraftServer server() {
        return server;
    }

    @Override
    public void onInitialize() {

        UnifiedRegistry.initialize();
        CBlock.initialize();

        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("maze"), MazeChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("multistory"), MultiStoryChunkGenerator.CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((s, rm, ok) -> clearCaches());
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            server = null; clearCaches();
        });


    }

    private static void clearCaches() {
        RoomGroup.clearRuntimes();
        MazeRegion.clearCache();
        MultiStoryRegion.clearCache();
    }
}
