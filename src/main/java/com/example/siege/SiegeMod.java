package com.example.siege;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import java.io.File;

@Mod(
        modid = "SiegeMod",
        version = "1.1",
        dependencies = "required-after:CommandRestrict",
        acceptableRemoteVersions = "*"
)
public class SiegeMod {
    private static SiegeConfig config;
    private static SiegeManager manager;

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        File configDir = new File(MinecraftServer.getServer().getFile("config"), "SiegeMod");
        if (!configDir.exists() && !configDir.mkdirs()) {
            System.err.println("Не удалось создать директорию конфигов");
            return;
        }

        config = new SiegeConfig(new File(configDir, "siege.json"));
        manager = new SiegeManager(config);
        config.loadConfig();

        event.registerServerCommand(new SiegeCommand());
        MinecraftForge.EVENT_BUS.register(new SiegeEventHandler());

    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        config = null;
        manager = null;
    }

    public static SiegeConfig getConfig() {
        return config;
    }

    public static SiegeManager getManager() {
        return manager;
    }
}