package com.example.commandrestrict;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class PvPEventHandler {

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        // Проверяем что цель - игрок
        if (!(event.target instanceof EntityPlayer)) return;

        EntityPlayer attacker = event.entityPlayer;
        EntityPlayer target = (EntityPlayer) event.target;

        // Проверка прав оператора
        if (attacker.capabilities.isCreativeMode || attacker.canCommandSenderUseCommand(2, "")) {
            return;
        }

        RegionManager manager = CommandRestrictMod.getRegionManager();
        if (manager == null) return;

        // Проверяем регионы атакующего и цели
        boolean cancelAttack = false;

        // Проверяем регионы атакующего
        for (RestrictedRegion region : manager.getRegionsAt(
                attacker.worldObj, attacker.posX, attacker.posY, attacker.posZ)) {

            if (region.isDisablePvP()) {
                cancelAttack = true;
                break;
            }
        }

        // Проверяем регионы цели
        if (!cancelAttack) {
            for (RestrictedRegion region : manager.getRegionsAt(
                    target.worldObj, target.posX, target.posY, target.posZ)) {

                if (region.isDisablePvP()) {
                    cancelAttack = true;
                    break;
                }
            }
        }

        if (cancelAttack) {
            event.setCanceled(true);
            attacker.addChatMessage(new ChatComponentText("§cPVP запрещен в этой зоне!"));
        }
    }
}