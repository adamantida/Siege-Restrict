package com.example.commandrestrict;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

@Mod(modid = "CommandRestrict", version = "1.0", acceptableRemoteVersions = "*")
public class CommandRestrictMod {
    private static RegionManager regionManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configDir = new File(event.getModConfigurationDirectory(), "CommandRestrict");
        configDir.mkdirs();
        regionManager = new RegionManager(new File(configDir, "regions.json"));

        PlayerEventHandler playerHandler = new PlayerEventHandler();
        FMLCommonHandler.instance().bus().register(playerHandler);
        MinecraftForge.EVENT_BUS.register(playerHandler);
        MinecraftForge.EVENT_BUS.register(new CommandEventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
        MinecraftForge.EVENT_BUS.register(new PvPEventHandler());
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRestrict());
    }

    public static RegionManager getRegionManager() {
        return regionManager;
    }
}