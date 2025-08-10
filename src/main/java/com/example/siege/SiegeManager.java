package com.example.siege;

import com.example.commandrestrict.RegionManager;
import com.example.commandrestrict.RestrictedRegion;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import java.util.*;

public class SiegeManager {
    private final SiegeConfig config;
    private boolean siegeActive = false;
    private RestrictedRegion siegeRegion;
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final Set<UUID> activeParticipants = new HashSet<>();
    private final Set<UUID> deadPlayers = new HashSet<>();
    private final Set<UUID> refugees = new HashSet<>();

    public SiegeManager(SiegeConfig config) {
        this.config = config;
    }

    public void startSiege() {
        if (siegeActive) return;

        playerFactions.clear();
        activeParticipants.clear();
        deadPlayers.clear();
        refugees.clear();

        siegeRegion = getSiegeRegion();
        if (siegeRegion == null) {
            broadcastMessage("§cРегион осады не найден!");
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        for (EntityPlayerMP player : (List<EntityPlayerMP>) server.getConfigurationManager().playerEntityList) {
            String playerName = player.getCommandSenderName().toLowerCase();
            String faction = config.getFactionForPlayer(playerName);

            if (faction != null) {
                UUID playerId = player.getUniqueID();
                playerFactions.put(playerId, faction);

                if (siegeRegion.isInRegion(player.worldObj, player.posX, player.posY, player.posZ)) {
                    activeParticipants.add(playerId);
                } else {
                    refugees.add(playerId);
                    player.addChatMessage(new ChatComponentText("§cВы вне зоны осады!"));
                }
            }
        }

        siegeActive = true;
        broadcastMessage("§6Осада началась! Участвующие фракции: " +
                String.join(", ", config.getFactions().keySet()));

        checkFactionDefeated();
    }

    public void stopSiege() {
        if (!siegeActive) return;

        siegeActive = false;
        siegeRegion = null;

        Map<String, List<String>> survivors = new HashMap<>();
        for (UUID playerId : activeParticipants) {
            String faction = playerFactions.get(playerId);
            EntityPlayerMP player = getPlayerByUUID(playerId);
            if (player != null) {
                survivors.computeIfAbsent(faction, k -> new ArrayList<>())
                        .add(player.getCommandSenderName());
            }
        }

        if (survivors.isEmpty()) {
            broadcastMessage("§6Осада завершилась без победителя!");
        } else if (survivors.size() == 1) {
            Map.Entry<String, List<String>> entry = survivors.entrySet().iterator().next();
            broadcastMessage("§6Фракция §e" + entry.getKey() + "§6 победила!");
            broadcastMessage("§aВыжившие: §e" + String.join(", ", entry.getValue()));
        } else {
            broadcastMessage("§6Осада завершилась ничьей!");
            survivors.forEach((faction, players) ->
                    broadcastMessage("§b" + faction + ": §e" + String.join(", ", players)));
        }

        playerFactions.clear();
        activeParticipants.clear();
        deadPlayers.clear();
        refugees.clear();
    }

    public void onPlayerDeath(EntityPlayerMP player) {
        UUID playerId = player.getUniqueID();
        if (playerFactions.containsKey(playerId)) {
            activeParticipants.remove(playerId);
            refugees.remove(playerId);
            deadPlayers.add(playerId);
            checkFactionDefeated();
        }
    }

    public void markAsRefugee(EntityPlayer player) {
        if (!siegeActive) return;

        UUID playerId = player.getUniqueID();
        if (playerFactions.containsKey(playerId)) {
            activeParticipants.remove(playerId);
            if (!refugees.contains(playerId)) {
                refugees.add(playerId);
                checkFactionDefeated();
            }
        }
    }
    public void onPlayerMove(EntityPlayerMP player) {
        if (!siegeActive) return;
        UUID pid = player.getUniqueID();

        // Получаем кейс, если регион ещё не инициализирован
        if (siegeRegion == null) {
            siegeRegion = getSiegeRegion();
            if (siegeRegion == null) return;
        }

        // Достаём угловые точки региона
        Vec3 v1 = siegeRegion.getPos1();
        Vec3 v2 = siegeRegion.getPos2();
        // Вычисляем мин/макс по X и Z
        double minX = Math.min(v1.xCoord, v2.xCoord) - 0.5;
        double maxX = Math.max(v1.xCoord, v2.xCoord) + 0.5;
        double minZ = Math.min(v1.zCoord, v2.zCoord) - 0.5;
        double maxZ = Math.max(v1.zCoord, v2.zCoord) + 0.5;

        double x = player.posX, z = player.posZ, y = player.posY;

        boolean inside = x >= minX && x <= maxX && z >= minZ && z <= maxZ;

        // 1) Блокируем возвращение мёртвых
        if (deadPlayers.contains(pid)) {
            if (inside) {
                // вычисляем, к какой стороне ближе
                double dxMin = x - minX;
                double dxMax = maxX - x;
                double dzMin = z - minZ;
                double dzMax = maxZ - z;
                double distMin = Math.min(Math.min(Math.abs(dxMin), Math.abs(dxMax)),
                        Math.min(Math.abs(dzMin), Math.abs(dzMax)));
                double tx = x, tz = z;
                if (distMin == Math.abs(dxMin))      tx = minX - 1;
                else if (distMin == Math.abs(dxMax)) tx = maxX + 1;
                else if (distMin == Math.abs(dzMin)) tz = minZ - 1;
                else                                 tz = maxZ + 1;

                player.setPositionAndUpdate(tx, y, tz);
                player.addChatMessage(new ChatComponentText(
                        "§cВы погибли и не можете вернуться в зону осады!"
                ));
            }
            return;
        }

        // 2) Обычная логика для живых игроков
        if (!playerFactions.containsKey(pid)) return;

        if (siegeRegion == null) siegeRegion = getSiegeRegion();
        if (siegeRegion == null) return;

        boolean inRegion = siegeRegion.isInRegion(
                player.worldObj, player.posX, player.posY, player.posZ
        );

        if (inRegion) {
            // Живой вернулся в зону
            if (refugees.remove(pid)) {
                player.addChatMessage(new ChatComponentText("§aВы вернулись в зону осады!"));
                broadcastMessage("§6[Осада] §e" + player.getCommandSenderName()
                        + " §aвернулся(лась) в зону осады!");
            }
            activeParticipants.add(pid);
        } else {
            // Живой вышел из зоны
            if (activeParticipants.remove(pid)) {
                refugees.add(pid);
                player.addChatMessage(new ChatComponentText(
                        "§cВы покинули зону осады и стали беженцем!"));
                broadcastMessage("§6[Осада] §e" + player.getCommandSenderName()
                        + " §cпокинул(а) зону осады и стал(а) беженцем!");
                checkFactionDefeated();
            }
        }
    }


    public void sendSiegeStatus(ICommandSender sender) {
        Map<String, List<String>> active = new HashMap<>();
        Map<String, List<String>> dead = new HashMap<>();
        Map<String, List<String>> refugee = new HashMap<>();

        for (Map.Entry<UUID, String> entry : playerFactions.entrySet()) {
            UUID playerId = entry.getKey();
            String faction = entry.getValue();
            String playerName = getPlayerName(playerId);

            if (playerName == null) continue;

            if (deadPlayers.contains(playerId)) {
                dead.computeIfAbsent(faction, k -> new ArrayList<>()).add(playerName);
            } else if (refugees.contains(playerId)) {
                refugee.computeIfAbsent(faction, k -> new ArrayList<>()).add(playerName);
            } else {
                active.computeIfAbsent(faction, k -> new ArrayList<>()).add(playerName);
            }
        }

        sender.addChatMessage(new ChatComponentText("§6==== Статус осады ===="));
        sender.addChatMessage(new ChatComponentText("§aРегион: §e" + config.getSiegeRegion()));

        for (String faction : config.getFactions().keySet()) {
            sender.addChatMessage(new ChatComponentText("§bФракция §e" + faction + "§b:"));

            if (active.containsKey(faction)) {
                sender.addChatMessage(new ChatComponentText("§a  Живые: §e" + String.join(", ", active.get(faction))));
            }
            if (refugee.containsKey(faction)) {
                sender.addChatMessage(new ChatComponentText("§6  Беженцы: §e" + String.join(", ", refugee.get(faction))));
            }
            if (dead.containsKey(faction)) {
                sender.addChatMessage(new ChatComponentText("§c  Мертвые: §e" + String.join(", ", dead.get(faction))));
            }
        }
        sender.addChatMessage(new ChatComponentText("§6======================"));
    }

    private void checkFactionDefeated() {
        for (String faction : config.getFactions().keySet()) {
            boolean hasActive = false;

            for (Map.Entry<UUID, String> entry : playerFactions.entrySet()) {
                if (!faction.equals(entry.getValue())) continue;

                UUID playerId = entry.getKey();
                if (!deadPlayers.contains(playerId) && !refugees.contains(playerId)) {
                    hasActive = true;
                    break;
                }
            }

            if (!hasActive) {
                stopSiege();
                broadcastMessage("§6Фракция §e" + faction + "§6 уничтожена!");
                break;
            }
        }
    }

    private RestrictedRegion getSiegeRegion() {
        RegionManager regionManager = com.example.commandrestrict.CommandRestrictMod.getRegionManager();
        return regionManager != null ? regionManager.getRegion(config.getSiegeRegion()) : null;
    }

    private EntityPlayerMP getPlayerByUUID(UUID uuid) {
        for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP p = (EntityPlayerMP) player;
            if (p.getUniqueID().equals(uuid)) return p;
        }
        return null;
    }

    private String getPlayerName(UUID uuid) {
        EntityPlayerMP player = getPlayerByUUID(uuid);
        return player != null ? player.getCommandSenderName() : null;
    }
    public void onPlayerLogout(EntityPlayerMP player) {
        if (!siegeActive) return;
        UUID pid = player.getUniqueID();
        if (playerFactions.containsKey(pid) && !deadPlayers.contains(pid)) {
            activeParticipants.remove(pid);
            if (refugees.add(pid)) {
                // Личное сообщение
                player.addChatMessage(new ChatComponentText("§cВы стали беженцем (вышли из игры)"));
                // Сообщение всем в чат
                broadcastMessage("§6[Осада] §e" + player.getCommandSenderName()
                        + " §cпокинул(а) зону осады и стал беженцем!");
                checkFactionDefeated();
            }
        }
    }

    private void broadcastMessage(String message) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            server.getConfigurationManager().sendChatMsg(new ChatComponentText(message));
        }
    }

    public boolean isSiegeActive() {
        return siegeActive;
    }
}