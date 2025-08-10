package com.example.siege;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class SiegeEventHandler {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.entity instanceof EntityPlayerMP) {
            SiegeManager manager = SiegeMod.getManager();
            if (manager != null && manager.isSiegeActive()) {
                manager.onPlayerDeath((EntityPlayerMP) event.entity);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.entity;
            SiegeManager mgr = SiegeMod.getManager();
            if (mgr != null && mgr.isSiegeActive()) {
                mgr.onPlayerMove(player);
            }
        }
    }
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            SiegeManager mgr = SiegeMod.getManager();
            if (mgr != null && mgr.isSiegeActive()) {
                mgr.onPlayerLogout(player);
            }
        }
    }
}