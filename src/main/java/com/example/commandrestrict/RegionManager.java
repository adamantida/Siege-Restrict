package com.example.commandrestrict;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class RegionManager {
    private final File saveFile;
    private final Map<String, RestrictedRegion> regions = new HashMap<>();
    private static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Vec3.class, new Vec3Serializer());
        gsonBuilder.registerTypeAdapter(Vec3.class, new Vec3Deserializer()); // Добавьте это
        gsonBuilder.registerTypeAdapter(RestrictedRegion.class, new RegionSerializer());
        GSON = gsonBuilder.setPrettyPrinting().create();
    }

    public RegionManager(File saveFile) {
        this.saveFile = saveFile;
        System.out.println("Loading regions from: " + saveFile.getAbsolutePath());
        loadRegions();
        System.out.println("Loaded " + regions.size() + " regions");
    }

    public void addRegion(String name, Vec3 pos1, Vec3 pos2, int dimension) {
        regions.put(name, new RestrictedRegion(
                name,
                pos1,
                pos2,
                dimension,
                new HashSet<>(),   // blockedCommands
                false,             // isWhitelist
                false,             // blockBreakDisabled
                false,             // blockPlaceDisabled
                false,             // killOnExit
                new HashSet<>(),   // allowedBreakBlocks
                new HashSet<>(),    // allowedPlaceBlocks
                false,
                false,
                false,
                false
        ));
        saveRegions();
    }

    public boolean removeRegion(String name) {
        if (regions.remove(name) != null) {
            saveRegions();
            return true;
        }
        return false;
    }

    public List<RestrictedRegion> getRegionsAt(World world, double x, double y, double z) {
        List<RestrictedRegion> result = new ArrayList<>();
        for (RestrictedRegion region : regions.values()) {
            if (region.isInRegion(world, x, y, z)) {
                result.add(region);
            }
        }
        return result;
    }

    public Collection<String> getRegionNames() {
        return regions.keySet();
    }

    public RestrictedRegion getRegion(String name) {
        return regions.get(name);
    }

    public void reload() {
        regions.clear();
        loadRegions();
    }

    public void saveRegions() {
        try (Writer writer = new FileWriter(saveFile)) {
            GSON.toJson(regions.values(), writer);
        } catch (IOException e) {
            System.err.println("Failed to save regions: " + e.getMessage());
        }
    }

    private void loadRegions() {
        if (!saveFile.exists()) return;

        try (Reader reader = new FileReader(saveFile)) {
            // Заменяем массив на правильный тип
            Type type = new TypeToken<List<RestrictedRegion>>() {
            }.getType();
            List<RestrictedRegion> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                for (RestrictedRegion region : loaded) {
                    regions.put(region.getName(), region);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load regions: " + e.getMessage());
        }
    }

    private static class Vec3Serializer implements JsonSerializer<Vec3> {
        @Override
        public JsonElement serialize(Vec3 src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.xCoord);
            obj.addProperty("y", src.yCoord);
            obj.addProperty("z", src.zCoord);
            return obj;
        }
    }

    private static class Vec3Deserializer implements JsonDeserializer<Vec3> {
        @Override
        public Vec3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject obj = json.getAsJsonObject();
            return Vec3.createVectorHelper(
                    obj.get("x").getAsDouble(),
                    obj.get("y").getAsDouble(),
                    obj.get("z").getAsDouble()
            );
        }
    }

    private static class RegionSerializer implements JsonSerializer<RestrictedRegion>, JsonDeserializer<RestrictedRegion> {
        @Override
        public JsonElement serialize(RestrictedRegion region, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", region.getName());
            obj.add("pos1", context.serialize(region.getPos1()));
            obj.add("pos2", context.serialize(region.getPos2()));
            obj.addProperty("dimension", region.getDimension());
            obj.add("blockedCommands", context.serialize(region.getBlockedCommands()));
            obj.addProperty("isWhitelist", region.isWhitelist());
            obj.addProperty("blockBreakDisabled", region.isBlockBreakDisabled());
            obj.addProperty("blockPlaceDisabled", region.isBlockPlaceDisabled());
            obj.addProperty("killOnExit", region.isKillOnExit());
            obj.add("allowedBreakBlocks", context.serialize(region.getAllowedBreakBlocks()));
            obj.add("allowedPlaceBlocks", context.serialize(region.getAllowedPlaceBlocks()));
            obj.addProperty("disableLava", region.isDisableLava()); // Сериализация нового поля
            obj.addProperty("disableWater", region.isDisableWater());
            obj.addProperty("disableDoorOpening", region.isDisableDoorOpening());
            obj.addProperty("disablePvP", region.isDisablePvP());
            return obj;
        }

        @Override
        public RestrictedRegion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

            JsonObject obj = json.getAsJsonObject();
            String name = obj.get("name").getAsString();
            Vec3 pos1 = context.deserialize(obj.get("pos1"), Vec3.class);
            Vec3 pos2 = context.deserialize(obj.get("pos2"), Vec3.class);
            int dimension = obj.get("dimension").getAsInt();
            Set<String> blockedCommands = context.deserialize(obj.get("blockedCommands"),
                    new TypeToken<HashSet<String>>() {
                    }.getType());
            boolean isWhitelist = obj.get("isWhitelist").getAsBoolean();
            boolean blockBreakDisabled = obj.has("blockBreakDisabled") && obj.get("blockBreakDisabled").getAsBoolean();
            boolean blockPlaceDisabled = obj.has("blockPlaceDisabled") && obj.get("blockPlaceDisabled").getAsBoolean();
            boolean disableLava = obj.has("disableLava") && obj.get("disableLava").getAsBoolean();
            boolean disableWater = obj.has("disableWater") && obj.get("disableWater").getAsBoolean();
            boolean killOnExit = obj.has("killOnExit") && obj.get("killOnExit").getAsBoolean();
            boolean disableDoorOpening = obj.has("disableDoorOpening") && obj.get("disableDoorOpening").getAsBoolean();
            boolean disablePvP = obj.has("disablePvP") && obj.get("disablePvP").getAsBoolean();

            Set<String> allowedBreakBlocks = context.deserialize(obj.get("allowedBreakBlocks"),
                    new TypeToken<HashSet<String>>() {
                    }.getType());
            Set<String> allowedPlaceBlocks = context.deserialize(obj.get("allowedPlaceBlocks"),
                    new TypeToken<HashSet<String>>() {
                    }.getType());

            return new RestrictedRegion(name, pos1, pos2, dimension, blockedCommands,
                    isWhitelist, blockBreakDisabled, blockPlaceDisabled, killOnExit,
                    allowedBreakBlocks, allowedPlaceBlocks,
                    disableLava, disableWater, disableDoorOpening,disablePvP);
        }
    }
}