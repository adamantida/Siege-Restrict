package com.example.siege;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import java.util.*;

public class SiegeCommand extends CommandBase {
    private static final String[] SUBCOMMANDS = {
            "start", "stop", "af", "rf", "ap", "rp", "sr", "list", "status", "cf"
    };

    @Override
    public String getCommandName() {
        return "siege";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/siege <start|stop|af|rf|ap|rp|sr|list|status|cf>";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        String command = args[0].toLowerCase();
        SiegeManager manager = SiegeMod.getManager();
        SiegeConfig config = SiegeMod.getConfig();

        // Проверка прав для привилегированных команд
        boolean admin = Arrays.asList("start","stop","af","rf","ap","rp","sr","list").contains(command);
        if (admin && !sender.canCommandSenderUseCommand(2, getCommandName())) {
            sendMessage(sender, "§cУ вас нет прав на эту команду");
            return;
        }

        switch (command) {
            case "start":      handleSiegeStart(sender, manager);    break;
            case "stop":       handleSiegeStop(sender, manager);     break;
            case "af":         handleAddFaction(sender, args, config);    break;
            case "rf":         handleRemoveFaction(sender, args, config); break;
            case "ap":         handleAddPlayer(sender, args, config);     break;
            case "rp":         handleRemovePlayer(sender, args, config);  break;
            case "sr":         handleSetRegion(sender, args, config);     break;
            case "list":       handleList(sender, config);       break;
            case "status":     handleStatus(sender, manager);        break;
            case "cf":         handleChooseFaction(sender, args, config); break;
            default:           sendHelp(sender);
        }
    }

    // Все вспомогательные методы перенесены ниже с оптимизацией

    private void handleSiegeStart(ICommandSender sender, SiegeManager manager) {
        if (manager.isSiegeActive()) {
            sendMessage(sender, "§cОсада уже начата!");
            return;
        }
        manager.startSiege();
        sendMessage(sender, "§aОсада начата!");
    }

    private void handleSiegeStop(ICommandSender sender, SiegeManager manager) {
        if (!manager.isSiegeActive()) {
            sendMessage(sender, "§cОсада не активна!");
            return;
        }
        manager.stopSiege();
        sendMessage(sender, "§aОсада остановлена!");
    }

    private void handleChooseFaction(ICommandSender sender, String[] args, SiegeConfig config) {
        if (!(sender instanceof EntityPlayer)) {
            sendMessage(sender, "§cТолько для игроков!");
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "§cИспользование: /siege cf <фракция>");
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        String playerName = player.getCommandSenderName();
        String factionName = args[1];

        if (!config.hasFaction(factionName)) {
            sendMessage(sender, "§cФракция не найдена!");
            return;
        }

        if (config.getFactionForPlayer(playerName) != null) {
            sendMessage(sender, "§cВы уже состоите во фракции!");
            return;
        }

        config.addPlayerToFaction(factionName, playerName);
        config.saveConfig();
        sendMessage(sender, "§aВы присоединились к фракции §e" + factionName);
    }

    private void handleStatus(ICommandSender sender, SiegeManager manager) {
        if (manager == null || !manager.isSiegeActive()) {
            sendMessage(sender, "§cОсада не активна!");
            return;
        }
        manager.sendSiegeStatus(sender);
    }

    private void handleAddFaction(ICommandSender sender, String[] args, SiegeConfig config) {
        if (args.length < 2) {
            sendMessage(sender, "§cИспользование: /siege af <название>");
            return;
        }
        config.addFaction(args[1]);
        config.saveConfig();
        sendMessage(sender, "§aФракция §e" + args[1] + "§a добавлена!");
    }

    private void handleRemoveFaction(ICommandSender sender, String[] args, SiegeConfig config) {
        if (args.length < 2) {
            sendMessage(sender, "§cИспользование: /siege rf <название>");
            return;
        }
        config.removeFaction(args[1]);
        config.saveConfig();
        sendMessage(sender, "§aФракция §e" + args[1] + "§a удалена!");
    }

    private void handleAddPlayer(ICommandSender sender, String[] args, SiegeConfig config) {
        if (args.length < 3) {
            sendMessage(sender, "§cИспользование: /siege ap <фракция> <игрок>");
            return;
        }
        config.addPlayerToFaction(args[1], args[2]);
        config.saveConfig();
        sendMessage(sender, "§aИгрок §e" + args[2] + "§a добавлен во фракцию §e" + args[1]);
    }

    private void handleRemovePlayer(ICommandSender sender, String[] args, SiegeConfig config) {
        if (args.length < 3) {
            sendMessage(sender, "§cИспользование: /siege rp <фракция> <игрок>");
            return;
        }
        config.removePlayerFromFaction(args[1], args[2]);
        config.saveConfig();
        sendMessage(sender, "§aИгрок §e" + args[2] + "§a удален из фракции §e" + args[1]);
    }

    private void handleSetRegion(ICommandSender sender, String[] args, SiegeConfig config) {
        if (args.length < 2) {
            sendMessage(sender, "§cИспользование: /siege sr <название региона>");
            return;
        }
        config.setSiegeRegion(args[1]);
        config.saveConfig();
        sendMessage(sender, "§aРегион осады установлен: §e" + args[1]);
    }

    private void handleList(ICommandSender sender, SiegeConfig config) {
        sender.addChatMessage(new ChatComponentText("§6===== Конфигурация осады ====="));
        sender.addChatMessage(new ChatComponentText("§aРегион: §e" + config.getSiegeRegion()));

        for (Map.Entry<String, Set<String>> entry : config.getFactions().entrySet()) {
            sender.addChatMessage(new ChatComponentText("§bФракция §e" + entry.getKey() + "§b:"));
            for (String player : entry.getValue()) {
                sender.addChatMessage(new ChatComponentText("§7- " + player));
            }
        }
        sender.addChatMessage(new ChatComponentText("§6============================="));
    }

    private void sendHelp(ICommandSender sender) {
        List<String> helpMessages = new ArrayList<>();
        helpMessages.add("§6===== Команды осады =====");

        // Команды только для операторов (если у игрока есть права)
        if (sender.canCommandSenderUseCommand(2, "")) {
            helpMessages.add("§c/siege start §7- Начать осаду");
            helpMessages.add("§c/siege stop §7- Завершить осаду");
            helpMessages.add("§c/siege list §7- Показать конфигурацию");
            helpMessages.add("§c/siege af <название> §7- Создать фракцию");
            helpMessages.add("§c/siege rf <название> §7- Удалить фракцию");
            helpMessages.add("§c/siege ap <фракция> <игрок> §7- Добавить игрока");
            helpMessages.add("§c/siege rp <фракция> <игрок> §7- Удалить игрока");
            helpMessages.add("§c/siege sr <регион> §7- Установить регион осады");
        }
        // Общедоступные команды для всех игроков
        helpMessages.add("§a/siege cf <фракция> §7- Выбрать фракцию");
        helpMessages.add("§a/siege status §7- Показать статус осады");

        helpMessages.add("§6========================");

        for (String msg : helpMessages) {
            sender.addChatMessage(new ChatComponentText(msg));
        }
    }

    private void sendMessage(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText(message));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Разрешаем запуската команд (проверка лвл тут не срабатывает)
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Пример: по умолчанию – нужна любая версия
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        }
        String sub = args[0].toLowerCase();
        if ((sub.equals("ap") || sub.equals("rp")) && args.length == 3) {
            return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }

        if ((sub.equals("af") || sub.equals("rf") || sub.equals("cf") || sub.equals("ap") || sub.equals("rp")) && args.length == 2) {
            SiegeConfig config = SiegeMod.getConfig();
            Set<String> factions = config.getFactions().keySet();
            return CommandBase.getListOfStringsMatchingLastWord(args, factions.toArray(new String[0]));
        }

        return null;
    }
}