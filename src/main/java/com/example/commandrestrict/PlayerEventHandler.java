package com.example.commandrestrict;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerEventHandler {
    private final Set<UUID> killOnExitPlayers = new HashSet<>();

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        RegionManager mgr = CommandRestrictMod.getRegionManager();
        if (mgr == null) return;

        // Проверяем, находится ли игрок в регионе с killOnExit
        for (RestrictedRegion region : mgr.getRegionsAt(
                player.worldObj,
                player.posX,
                player.posY,
                player.posZ)) {

            if (region.isKillOnExit()) {
                // Убиваем игрока НЕМЕДЛЕННО при выходе
                killPlayer(player);
                killOnExitPlayers.add(player.getUniqueID());
                System.out.println("Player killed and marked: " + player.getCommandSenderName());
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;

        if (killOnExitPlayers.remove(player.getUniqueID())) {
            // Если игрок был помечен, но не умер при выходе - убиваем при входе
            System.out.println("Killing player on login: " + player.getCommandSenderName());
            killPlayer(player);
        }
    }

    private void killPlayer(EntityPlayerMP player) {
        // 1. Запоминаем позицию для выпадения вещей
        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;
        World world = player.worldObj;

        // 2. Вызываем настоящую смерть с выпадением предметов
        player.setHealth(0.0F); // Устанавливаем здоровье в 0
        player.onDeath(DamageSource.outOfWorld); // Вызываем обработку смерти

        // 3. Принудительно выкидываем все предметы
        player.inventory.dropAllItems();

        // 4. Очищаем инвентарь
        player.inventory.clearInventory(null, -1);

        // 5. Уведомление игрока
        player.addChatMessage(new ChatComponentText("§4Вас убили за выход из запретной зоны!"));

        // 6. Телепортируем "труп" игрока обратно в регион (чтобы вещи выпали правильно)
        player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
    }
}