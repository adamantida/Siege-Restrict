package com.example.commandrestrict;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.*;

public class CommandRestrict extends CommandBase {
    private final Map<String, Vec3> firstPositions = new HashMap<>();

    @Override
    public String getCommandName() {
        return "restrict";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/restrict <pos1|pos2|remove|list|addcmd|removecmd|mode|reload|blockbreak|blockplace>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "pos1":
                handlePos1(sender);
                break;

            case "pos2":
                handlePos2(sender, args);
                break;

            case "remove":
                handleRemove(sender, args);
                break;

            case "list":
                handleList(sender, args);
                break;

            case "addcmd":
                handleAddCommand(sender, args);
                break;

            case "removecmd":
                handleRemoveCommand(sender, args);
                break;

            case "mode":
                handleSetMode(sender, args);
                break;

            case "reload":
                handleReload(sender);
                break;
            case "blockbreak":
                handleBlockBreak(sender, args);
                break;
            case "blockplace":
                handleBlockPlace(sender, args);
                break;
            case "killonexit":
                handleKillOnExit(sender, args);
                break;
            case "addbreakblock":
                handleAddBreakBlock(sender, args);
                break;
            case "removebreakblock":
                handleRemoveBreakBlock(sender, args);
                break;
            case "addplaceblock":
                handleAddPlaceBlock(sender, args);
                break;
            case "removeplaceblock":
                handleRemovePlaceBlock(sender, args);
                break;
            case "disablelava":
                handleDisableLava(sender, args);
                break;
            case "disablewater":
                handleDisableWater(sender, args);
                break;
            case "disabledoor":
                handleDisableDoor(sender, args);
                break;
            case "disablepvp":
                handleDisablePvP(sender, args);
                break;
            default:
                sendHelp(sender);
        }
    }

    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("§6===== Команды управления регионами ====="));
        sender.addChatMessage(new ChatComponentText("§a/restrict pos1 §7- Установить первую позицию"));
        sender.addChatMessage(new ChatComponentText("§a/restrict pos2 <name> §7- Установить вторую позицию и создать регион"));
        sender.addChatMessage(new ChatComponentText("§a/restrict remove <name> §7- Удалить регион"));
        sender.addChatMessage(new ChatComponentText("§a/restrict list [name] §7- Список регионов или команд в регионе"));
        sender.addChatMessage(new ChatComponentText("§a/restrict addcmd <регион> <команда> §7- Добавить команду"));
        sender.addChatMessage(new ChatComponentText("§a/restrict removecmd <регион> <команда> §7- Удалить команду"));
        sender.addChatMessage(new ChatComponentText("§a/restrict blockbreak <регион> <true|false> §7- Запрет ломания блоков"));
        sender.addChatMessage(new ChatComponentText("§a/restrict blockplace <регион> <true|false> §7- Запрет установки блоков"));
        sender.addChatMessage(new ChatComponentText("§a/restrict disablelava <регион> <true|false> §7- Запрет разлития лавы"));
        sender.addChatMessage(new ChatComponentText("§a/restrict disablewater <регион> <true|false> §7- Запрет разлития воды"));
        sender.addChatMessage(new ChatComponentText("§a/restrict disabledoor <регион> <true|false> §7- Запрет открывания дверей"));
        sender.addChatMessage(new ChatComponentText("§a/restrict addbreakblock <регион> <blockId> §7- Разрешить блок для ломания"));
        sender.addChatMessage(new ChatComponentText("§a/restrict removebreakblock <регион> <blockId> §7- Запретить блок для ломания"));
        sender.addChatMessage(new ChatComponentText("§a/restrict addplaceblock <регион> <blockId> §7- Разрешить блок для установки"));
        sender.addChatMessage(new ChatComponentText("§a/restrict removeplaceblock <регион> <blockId> §7- Запретить блок для установки"));
        sender.addChatMessage(new ChatComponentText("§a/restrict killonexit <регион> <true|false> §7- Установить флаг смерти при выходе"));
        sender.addChatMessage(new ChatComponentText("§a/restrict disablepvp <регион> <true|false> §7- Запрет PVP"));
        sender.addChatMessage(new ChatComponentText("§a/restrict mode <регион> <whitelist|blacklist> §7- Изменить режим"));
        sender.addChatMessage(new ChatComponentText("§a/restrict reload §7- Перезагрузить конфигурацию"));
        sender.addChatMessage(new ChatComponentText("§6====================================="));
    }

    private void handleDisablePvP(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict disablepvp <регион> <true|false>"));
            return;
        }

        String regionName = args[1];
        String valueStr = args[2].toLowerCase();

        if (!valueStr.equals("true") && !valueStr.equals("false")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимое значение. Используйте true или false"));
            return;
        }

        boolean value = Boolean.parseBoolean(valueStr);
        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);

        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        region.setDisablePvP(value);
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aЗапрет PVP для региона §e" + regionName + "§a установлен в §e" + value
        ));
    }

    private void handleDisableDoor(ICommandSender sender, String[] args) {
        handleDoorFlag(sender, args);
    }
    private void handleDoorFlag(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict disabledoor <регион> <true|false>"));
            return;
        }

        String regionName = args[1];
        String valueStr = args[2].toLowerCase();

        if (!valueStr.equals("true") && !valueStr.equals("false")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимое значение. Используйте true или false"));
            return;
        }

        boolean value = Boolean.parseBoolean(valueStr);
        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);

        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        region.setDisableDoorOpening(value);
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aЗапрет открывания дверей для региона §e" + regionName + "§a установлен в §e" + value
        ));
    }


    private void handlePos1(ICommandSender sender) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText("§cТолько для игроков!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        firstPositions.put(sender.getCommandSenderName(),
                Vec3.createVectorHelper(player.posX, player.posY, player.posZ));
        sender.addChatMessage(new ChatComponentText("§aПервая позиция установлена!"));
    }

    private void handlePos2(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText("§cТолько для игроков!"));
            return;
        }

        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("§cУкажите имя региона: /restrict pos2 <name>"));
            return;
        }

        String playerName = sender.getCommandSenderName();
        Vec3 pos1 = firstPositions.get(playerName);
        if (pos1 == null) {
            sender.addChatMessage(new ChatComponentText("§cСначала установите первую позицию!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        Vec3 pos2 = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        firstPositions.remove(playerName);

        RegionManager manager = CommandRestrictMod.getRegionManager();
        manager.addRegion(args[1], pos1, pos2, player.worldObj.provider.dimensionId);
        sender.addChatMessage(new ChatComponentText("§aРегион '" + args[1] + "' создан!"));
    }

    private void handleRemove(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText("§cУкажите имя региона: /restrict remove <name>"));
            return;
        }

        RegionManager manager = CommandRestrictMod.getRegionManager();
        if (manager.removeRegion(args[1])) {
            sender.addChatMessage(new ChatComponentText("§aРегион '" + args[1] + "' удалён!"));
        } else {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
        }
    }

    private void handleList(ICommandSender sender, String[] args) {
        RegionManager manager = CommandRestrictMod.getRegionManager();

        if (args.length >= 2) {
            // Показать информацию о конкретном регионе
            RestrictedRegion region = manager.getRegion(args[1]);
            if (region == null) {
                sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
                return;
            }

            sender.addChatMessage(new ChatComponentText("§6Регион: §e" + region.getName()));
            sender.addChatMessage(new ChatComponentText("§6Режим: §e" + (region.isWhitelist() ? "whitelist" : "blacklist")));
            sender.addChatMessage(new ChatComponentText("§6Ломание блоков: §e" + (region.isBlockBreakDisabled() ? "запрещено" : "разрешено")));
            sender.addChatMessage(new ChatComponentText("§6Установка блоков: §e" + (region.isBlockPlaceDisabled() ? "запрещено" : "разрешено")));
            sender.addChatMessage(new ChatComponentText("§6Запрет лавы: §e" + (region.isDisableLava() ? "да" : "нет")));
            sender.addChatMessage(new ChatComponentText("§6Запрет воды: §e" + (region.isDisableWater() ? "да" : "нет")));
            sender.addChatMessage(new ChatComponentText("§6Запрет открытия дверей: §e" + (region.isDisableDoorOpening() ? "да" : "нет")));
            sender.addChatMessage(new ChatComponentText("§6Убивать при выходе: §e" + (region.isKillOnExit() ? "да" : "нет")));
            sender.addChatMessage(new ChatComponentText("§6Запрет PVP: §e" + (region.isDisablePvP() ? "да" : "нет")));
            sender.addChatMessage(new ChatComponentText("§6Разрешенные для ломания блоки:"));
            for (String blockId : region.getAllowedBreakBlocks()) {
                sender.addChatMessage(new ChatComponentText("§7- " + blockId));
            }
            sender.addChatMessage(new ChatComponentText("§6Разрешенные для установки блоки:"));
            for (String blockId : region.getAllowedPlaceBlocks()) {
                sender.addChatMessage(new ChatComponentText("§7- " + blockId));
            }


            sender.addChatMessage(new ChatComponentText("§6Команды:"));

            for (String cmd : region.getBlockedCommands()) {
                sender.addChatMessage(new ChatComponentText("§7- /" + cmd));
            }
        } else {
            // Показать список регионов
            Collection<String> regions = manager.getRegionNames();

            if (regions.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("§eНет созданных регионов"));
                return;
            }

            sender.addChatMessage(new ChatComponentText("§6Список регионов:"));
            for (String name : regions) {
                sender.addChatMessage(new ChatComponentText("§7- " + name));
            }
        }
    }

    private void handleAddCommand(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict addcmd <регион> <команда>"));
            return;
        }

        String regionName = args[1];
        String command = args[2].toLowerCase().replaceFirst("^/", "");

        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        region.addBlockedCommand(command);
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aКоманда §e/" + command + "§a добавлена в список " +
                        (region.isWhitelist() ? "разрешенных" : "запрещенных") +
                        " для региона §e" + regionName
        ));
    }

    private void handleRemoveCommand(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict removecmd <регион> <команда>"));
            return;
        }

        String regionName = args[1];
        String command = args[2].toLowerCase().replaceFirst("^/", "");

        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        region.removeBlockedCommand(command);
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aКоманда §e/" + command + "§a удалена из списка региона §e" + regionName
        ));
    }

    private void handleSetMode(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict mode <регион> <whitelist|blacklist>"));
            return;
        }

        String regionName = args[1];
        String mode = args[2].toLowerCase();

        if (!mode.equals("whitelist") && !mode.equals("blacklist")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимый режим. Используйте whitelist или blacklist"));
            return;
        }

        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        region.setMode(mode.equals("whitelist"));
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aРежим региона §e" + regionName + "§a изменен на §e" + mode
        ));
    }

    private void handleReload(ICommandSender sender) {
        CommandRestrictMod.getRegionManager().reload();
        sender.addChatMessage(new ChatComponentText("§aКонфигурация регионов успешно перезагружена!"));
    }

    private void handleBlockBreak(ICommandSender sender, String[] args) {
        handleBlockFlag(sender, args, true);
    }

    private void handleBlockPlace(ICommandSender sender, String[] args) {
        handleBlockFlag(sender, args, false);
    }

    private void handleBlockFlag(ICommandSender sender, String[] args, boolean isBreak) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict " +
                    (isBreak ? "blockbreak" : "blockplace") + " <регион> <true|false>"));
            return;
        }

        String regionName = args[1];
        String flagValue = args[2].toLowerCase();

        if (!flagValue.equals("true") && !flagValue.equals("false")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимое значение. Используйте true или false"));
            return;
        }

        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        boolean value = Boolean.parseBoolean(flagValue);
        String actionName = isBreak ? "ломание блоков" : "установка блоков";

        if (isBreak) {
            region.setBlockBreakDisabled(value);
        } else {
            region.setBlockPlaceDisabled(value);
        }

        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§a" + (value ? "Запрещено" : "Разрешено") + " §e" + actionName +
                        "§a в регионе §e" + regionName
        ));
    }

    private void handleKillOnExit(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict killonexit <регион> <true|false>"));
            return;
        }

        String regionName = args[1];
        String flagValue = args[2].toLowerCase();

        if (!flagValue.equals("true") && !flagValue.equals("false")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимое значение. Используйте true или false"));
            return;
        }

        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        boolean value = Boolean.parseBoolean(flagValue);
        region.setKillOnExit(value);
        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText(
                "§aФлаг killOnExit для региона §e" + regionName + "§a установлен в §e" + value
        ));
    }

    private void handleAddBreakBlock(ICommandSender sender, String[] args) {
        handleBlockList(sender, args, true, true);
    }

    private void handleRemoveBreakBlock(ICommandSender sender, String[] args) {
        handleBlockList(sender, args, true, false);
    }

    private void handleAddPlaceBlock(ICommandSender sender, String[] args) {
        handleBlockList(sender, args, false, true);
    }

    private void handleRemovePlaceBlock(ICommandSender sender, String[] args) {
        handleBlockList(sender, args, false, false);
    }

    private void handleBlockList(ICommandSender sender, String[] args, boolean isBreak, boolean isAdd) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict " +
                    (isBreak ?
                            (isAdd ? "addbreakblock" : "removebreakblock") :
                            (isAdd ? "addplaceblock" : "removeplaceblock")) +
                    " <регион> <blockId>"));
            return;
        }

        String regionName = args[1];
        String blockId = args[2].toLowerCase();
        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);

        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        if (isBreak) {
            if (isAdd) {
                region.addAllowedBreakBlock(blockId);
                sender.addChatMessage(new ChatComponentText("§aБлок §e" + blockId + "§a добавлен в разрешенные для ломания в регионе §e" + regionName));
            } else {
                region.removeAllowedBreakBlock(blockId);
                sender.addChatMessage(new ChatComponentText("§aБлок §e" + blockId + "§a удален из разрешенных для ломания в регионе §e" + regionName));
            }
        } else {
            if (isAdd) {
                region.addAllowedPlaceBlock(blockId);
                sender.addChatMessage(new ChatComponentText("§aБлок §e" + blockId + "§a добавлен в разрешенные для установки в регионе §e" + regionName));
            } else {
                region.removeAllowedPlaceBlock(blockId);
                sender.addChatMessage(new ChatComponentText("§aБлок §e" + blockId + "§a удален из разрешенных для установки в регионе §e" + regionName));
            }
        }
        CommandRestrictMod.getRegionManager().saveRegions();
    }

    private void handleDisableLava(ICommandSender sender, String[] args) {
        handleFluidFlag(sender, args, true);
    }

    private void handleDisableWater(ICommandSender sender, String[] args) {
        handleFluidFlag(sender, args, false);
    }

    private void handleFluidFlag(ICommandSender sender, String[] args, boolean isLava) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText("§cИспользование: /restrict " +
                    (isLava ? "disablelava" : "disablewater") + " <регион> <true|false>"));
            return;
        }

        String regionName = args[1];
        String valueStr = args[2].toLowerCase();

        if (!valueStr.equals("true") && !valueStr.equals("false")) {
            sender.addChatMessage(new ChatComponentText("§cНедопустимое значение. Используйте true или false"));
            return;
        }

        boolean value = Boolean.parseBoolean(valueStr);
        RestrictedRegion region = CommandRestrictMod.getRegionManager().getRegion(regionName);

        if (region == null) {
            sender.addChatMessage(new ChatComponentText("§cРегион не найден!"));
            return;
        }

        if (isLava) {
            region.setDisableLava(value);
        } else {
            region.setDisableWater(value);
        }

        CommandRestrictMod.getRegionManager().saveRegions();
        sender.addChatMessage(new ChatComponentText("§aФлаг " + (isLava ? "запрета лавы" : "запрета воды") +
                " для региона §e" + regionName + "§a установлен в §e" + value));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Только для операторов
    }
}