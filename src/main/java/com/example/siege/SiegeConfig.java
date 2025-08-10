package com.example.siege;

import com.google.gson.*;
import java.io.*;
import java.util.*;

public class SiegeConfig {
    private final File configFile;
    private String siegeRegion;
    private final Map<String, Set<String>> factions = new HashMap<>();
    private final Map<String, String> playerFactionMap = new HashMap<>(); // Игрок -> Фракция

    public SiegeConfig(File configFile) {
        this.configFile = configFile;
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) return;

        try (Reader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            siegeRegion = json.has("siegeRegion") ? json.get("siegeRegion").getAsString() : null;

            if (json.has("factions")) {
                JsonObject factionsJson = json.getAsJsonObject("factions");
                for (Map.Entry<String, JsonElement> entry : factionsJson.entrySet()) {
                    Set<String> members = new HashSet<>();
                    for (JsonElement member : entry.getValue().getAsJsonArray()) {
                        String player = member.getAsString().toLowerCase();
                        members.add(player);
                        playerFactionMap.put(player, entry.getKey());
                    }
                    factions.put(entry.getKey(), members);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки конфига: " + e.getMessage());
        }
    }

    public void saveConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("siegeRegion", siegeRegion);

        JsonObject factionsJson = new JsonObject();
        for (Map.Entry<String, Set<String>> entry : factions.entrySet()) {
            JsonArray members = new JsonArray();
            for (String member : entry.getValue()) {
                members.add(new JsonPrimitive(member));
            }
            factionsJson.add(entry.getKey(), members);
        }
        json.add("factions", factionsJson);

        try (Writer writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения конфига: " + e.getMessage());
        }
    }

    public void addPlayerToFaction(String faction, String player) {
        player = player.toLowerCase();
        removePlayerFromAllFactions(player);

        factions.computeIfAbsent(faction, k -> new HashSet<>()).add(player);
        playerFactionMap.put(player, faction);
    }

    public void removePlayerFromFaction(String faction, String player) {
        player = player.toLowerCase();
        Set<String> members = factions.get(faction);
        if (members != null) {
            members.remove(player);
        }
        playerFactionMap.remove(player);
    }

    public void removePlayerFromAllFactions(String player) {
        player = player.toLowerCase();
        String faction = playerFactionMap.get(player);
        if (faction != null) {
            Set<String> members = factions.get(faction);
            if (members != null) {
                members.remove(player);
            }
            playerFactionMap.remove(player);
        }
    }

    public void addFaction(String name) {
        factions.put(name, new HashSet<>());
    }

    public void removeFaction(String name) {
        Set<String> members = factions.remove(name);
        if (members != null) {
            for (String player : members) {
                playerFactionMap.remove(player);
            }
        }
    }

    public boolean hasFaction(String name) {
        return factions.containsKey(name);
    }

    public String getFactionForPlayer(String playerName) {
        return playerFactionMap.get(playerName.toLowerCase());
    }

    public String getSiegeRegion() { return siegeRegion; }
    public void setSiegeRegion(String region) { siegeRegion = region; }
    public Map<String, Set<String>> getFactions() { return factions; }
}