package com.example.commandrestrict;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class RestrictedRegion {
    private final String name;
    private final Vec3 pos1;
    private final Vec3 pos2;
    private final int dimension;
    private final Set<String> blockedCommands;
    private boolean killOnExit;
    private boolean isWhitelist;
    private boolean blockBreakDisabled;
    private boolean blockPlaceDisabled;
    private final Set<String> allowedBreakBlocks; // Разрешенные для ломания блоки
    private final Set<String> allowedPlaceBlocks;
    private boolean disableLava; // Новое поле: запрет лавы
    private boolean disableWater; // Новое поле: запрет воды
    private boolean disableDoorOpening;
    private boolean disablePvP;

    public RestrictedRegion(String name, Vec3 pos1, Vec3 pos2, int dimension,
                            Set<String> blockedCommands, boolean isWhitelist,
                            boolean blockBreakDisabled, boolean blockPlaceDisabled,
                            boolean killOnExit,
                            Set<String> allowedBreakBlocks, Set<String> allowedPlaceBlocks,
                            boolean disableLava, boolean disableWater,
                            boolean disableDoorOpening, boolean disablePvP) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.dimension = dimension;
        // Инициализируем пустым набором, если null
        this.blockedCommands = blockedCommands != null ? blockedCommands : new HashSet<>();
        this.isWhitelist = isWhitelist;
        this.blockBreakDisabled = blockBreakDisabled;
        this.blockPlaceDisabled = blockPlaceDisabled;
        this.killOnExit = killOnExit;
        this.allowedBreakBlocks = allowedBreakBlocks != null ? allowedBreakBlocks : new HashSet<>();
        this.allowedPlaceBlocks = allowedPlaceBlocks != null ? allowedPlaceBlocks : new HashSet<>();
        this.disableLava = disableLava;
        this.disableWater = disableWater;
        this.disableDoorOpening = disableDoorOpening;
        this.disablePvP = disablePvP;

    }

    @Override
    public String toString() {
        double minX = Math.min(pos1.xCoord, pos2.xCoord),
                maxX = Math.max(pos1.xCoord, pos2.xCoord);
        double minY = Math.min(pos1.yCoord, pos2.yCoord),
                maxY = Math.max(pos1.yCoord, pos2.yCoord);
        double minZ = Math.min(pos1.zCoord, pos2.zCoord),
                maxZ = Math.max(pos1.zCoord, pos2.zCoord);
        return String.format("%s{dim=%d, x[%.1f–%.1f], y[%.1f–%.1f], z[%.1f–%.1f], killOnExit=%s}",
                name, dimension, minX, maxX, minY, maxY, minZ, maxZ, killOnExit);
    }

    public boolean isInRegion(World world, double x, double y, double z) {
        // Сначала проверим размерность
        if (world.provider.dimensionId != dimension) {
            return false;
        }

        // Вычислим границы
        double minX = Math.min(pos1.xCoord, pos2.xCoord) - 0.5;
        double maxX = Math.max(pos1.xCoord, pos2.xCoord) + 0.5;
        double minY = Math.min(pos1.yCoord, pos2.yCoord) - 0.5;
        double maxY = Math.max(pos1.yCoord, pos2.yCoord) + 0.5;
        double minZ = Math.min(pos1.zCoord, pos2.zCoord) - 0.5;
        double maxZ = Math.max(pos1.zCoord, pos2.zCoord) + 0.5;

        // Наконец, проверяем попадание
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
    public boolean isDisablePvP() {
        return disablePvP;
    }

    public void setDisablePvP(boolean disablePvP) {
        this.disablePvP = disablePvP;
    }
    public boolean isDisableDoorOpening() {
        return disableDoorOpening;
    }

    public void setDisableDoorOpening(boolean disableDoorOpening) {
        this.disableDoorOpening = disableDoorOpening;
    }

    public boolean isCommandBlocked(String command) {
        command = command.toLowerCase();
        if (isWhitelist) {
            return !blockedCommands.contains(command);
        } else {
            return blockedCommands.contains(command);
        }
    }

    public void addBlockedCommand(String command) {
        blockedCommands.add(command.toLowerCase());
    }

    public void removeBlockedCommand(String command) {
        blockedCommands.remove(command.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public Set<String> getBlockedCommands() {
        return blockedCommands;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public void setMode(boolean isWhitelist) {
        this.isWhitelist = isWhitelist;
    }

    public Vec3 getPos1() {
        return pos1;
    }

    public Vec3 getPos2() {
        return pos2;
    }

    public int getDimension() {
        return dimension;
    }
    public boolean isBlockBreakDisabled() {
        return blockBreakDisabled;
    }

    public void setBlockBreakDisabled(boolean blockBreakDisabled) {
        this.blockBreakDisabled = blockBreakDisabled;
    }

    public boolean isBlockPlaceDisabled() {
        return blockPlaceDisabled;
    }

    public void setBlockPlaceDisabled(boolean blockPlaceDisabled) {
        this.blockPlaceDisabled = blockPlaceDisabled;
    }

    public boolean isKillOnExit() {
        return killOnExit;
    }

    public void setKillOnExit(boolean killOnExit) {
        this.killOnExit = killOnExit;
    }


    // Удаление блока из разрешенных для ломания
    public void removeAllowedBreakBlock(String blockId) {
        allowedBreakBlocks.remove(blockId.toLowerCase());
    }

    // Добавление блока в разрешенные для установки
    public void addAllowedPlaceBlock(String blockId) {
        allowedPlaceBlocks.add(blockId.toLowerCase());
    }

    // Удаление блока из разрешенных для установки
    public void removeAllowedPlaceBlock(String blockId) {
        allowedPlaceBlocks.remove(blockId.toLowerCase());
    }

    // Геттеры для списков блоков
    public Set<String> getAllowedBreakBlocks() {
        return allowedBreakBlocks;
    }

    public Set<String> getAllowedPlaceBlocks() {
        return allowedPlaceBlocks;
    }
    private String toNumericId(String stringId) {
        Block block = (Block)Block.blockRegistry.getObject(stringId);
        if (block == null) return stringId;
        int id = Block.getIdFromBlock(block);
        int meta = 0; // Метаданные не учитываем
        return id + (meta != 0 ? ":" + meta : "");
    }

    // Проверка разрешен ли блок для ломания (с поддержкой числовых ID)
    public boolean canBreakBlock(Block block) {
        String stringId = Block.blockRegistry.getNameForObject(block);
        String numericId = toNumericId(stringId);

        return allowedBreakBlocks.contains(stringId) ||
                allowedBreakBlocks.contains(numericId);
    }

    // Проверка разрешен ли блок для установки (с поддержкой числовых ID)
    public boolean canPlaceBlock(Block block) {
        String stringId = Block.blockRegistry.getNameForObject(block);
        String numericId = toNumericId(stringId);

        return allowedPlaceBlocks.contains(stringId) ||
                allowedPlaceBlocks.contains(numericId);
    }

    // Добавление блока в разрешенные (поддерживает оба формата)
    public void addAllowedBreakBlock(String blockId) {
        allowedBreakBlocks.add(blockId);
    }
    public boolean isDisableLava() {
        return disableLava;
    }

    public void setDisableLava(boolean disableLava) {
        this.disableLava = disableLava;
    }

    public boolean isDisableWater() {
        return disableWater;
    }

    public void setDisableWater(boolean disableWater) {
        this.disableWater = disableWater;
    }
}