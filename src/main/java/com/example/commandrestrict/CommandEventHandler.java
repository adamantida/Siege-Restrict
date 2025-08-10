package com.example.commandrestrict;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.CommandEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommandEventHandler {
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (event.sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.sender;

            if (player.isDead) return;
            // Разрешить команды для операторов (2+ уровень)
            if (player.capabilities.isCreativeMode || player.canCommandSenderUseCommand(2, "")) {
                return;
            }

            RegionManager manager = CommandRestrictMod.getRegionManager();
            if (manager == null) return;

            String commandName = event.command.getCommandName().toLowerCase();

            for (RestrictedRegion region : manager.getRegionsAt(
                    player.worldObj, player.posX, player.posY, player.posZ)) {

                if (region.isCommandBlocked(commandName)) {
                    event.setCanceled(true);
                    player.addChatMessage(new ChatComponentText(
                            "§cКоманда §e/" + commandName + " §cзапрещена в зоне §e" + region.getName() + "§c!"
                    ));
                    return;
                }
            }
        }
    }
}