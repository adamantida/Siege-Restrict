package com.example.commandrestrict;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class BlockEventHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        handleBlockEvent(event, event.getPlayer(), true);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        handleBlockEvent(event, event.player, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Обрабатываем только на сервере
        if (event.world.isRemote) {
            return;
        }

        EntityPlayer player = event.entityPlayer;
        if (player == null || player.isDead) return;

        // Проверка прав оператора
        if (player.capabilities.isCreativeMode || player.canCommandSenderUseCommand(2, "")) {
            return;
        }

        // Обработка использования ведер с жидкостями
        handleFluidBuckets(event, player);

        // Обработка открытия дверей
        handleDoorOpening(event, player);
    }

    private void handleFluidBuckets(PlayerInteractEvent event, EntityPlayer player) {
        // Обрабатываем только использование предметов (правый клик)
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
                event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }

        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) return;

        Item item = heldItem.getItem();
        boolean isLavaBucket = item == Items.lava_bucket;
        boolean isWaterBucket = item == Items.water_bucket;

        if (!isLavaBucket && !isWaterBucket) {
            return;
        }

        RegionManager manager = CommandRestrictMod.getRegionManager();
        if (manager == null) return;

        // Проверяем регионы в позиции игрока
        for (RestrictedRegion region : manager.getRegionsAt(
                player.worldObj, player.posX, player.posY, player.posZ)) {

            if ((isLavaBucket && region.isDisableLava()) ||
                    (isWaterBucket && region.isDisableWater())) {

                event.setCanceled(true);
                player.addChatMessage(new ChatComponentText(
                        "§cИспользование " +
                                (isLavaBucket ? "ведра с лавой" : "ведра с водой") +
                                " запрещено в зоне §e" + region.getName() + "§c!"
                ));
                return;
            }
        }
    }

    private void handleDoorOpening(PlayerInteractEvent event, EntityPlayer player) {
        // Обрабатываем только клик по блоку
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.world.getBlock(event.x, event.y, event.z);
        boolean isDoor = block instanceof BlockDoor;
        boolean isLOTRGate = false;

        if (lotrGateClass != null) {
            isLOTRGate = lotrGateClass.isInstance(block);
        }

        if (!isDoor && !isLOTRGate) {
            return; // Не дверь и не врата - пропускаем
        }

        RegionManager manager = CommandRestrictMod.getRegionManager();
        if (manager == null) return;

        // Проверяем регионы в позиции двери
        for (RestrictedRegion region : manager.getRegionsAt(
                player.worldObj, event.x, event.y, event.z)) {

            if (region.isDisableDoorOpening()) {
                event.setCanceled(true);

                // Форсируем обновление состояния двери для игрока
                forceDoorStateUpdate(event.world, event.x, event.y, event.z, (EntityPlayerMP) player);

                player.addChatMessage(new ChatComponentText(
                        "§cОткрывание дверей запрещено в зоне §e" + region.getName() + "§c!"
                ));
                return;
            }
        }
    }
    private static Class<?> lotrGateClass;

    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("lotr.common.block.LOTRBlockGate");
        } catch (ClassNotFoundException e) {
            // Мод LOTR не установлен
        }
        lotrGateClass = clazz;
    }

    private void forceDoorStateUpdate(World world, int x, int y, int z, EntityPlayerMP player) {
        // Получаем текущие метаданные блока
        int meta = world.getBlockMetadata(x, y, z);

        // Определяем верхнюю и нижнюю часть двери
        boolean isTop = (meta & 8) != 0;
        int baseY = isTop ? y - 1 : y;

        // Просто форсируем обновление состояния двери на клиенте
        world.markBlockForUpdate(x, baseY, z);
        world.markBlockForUpdate(x, baseY + 1, z);
    }

    private void handleBlockEvent(BlockEvent event, EntityPlayer player, boolean isBreak) {
        if (player.isDead) return;
        // Проверка прав оператора
        if (player.capabilities.isCreativeMode || player.canCommandSenderUseCommand(2, "")) {
            return;
        }

        RegionManager manager = CommandRestrictMod.getRegionManager();
        if (manager == null) return;

        // Получаем идентификатор блока
        Block block = event.block;
        String blockId = Block.blockRegistry.getNameForObject(block);
        if (blockId == null) return;
        blockId = blockId.toLowerCase();

        // Проверка регионов в позиции блока
        for (RestrictedRegion region : manager.getRegionsAt(
                player.worldObj, event.x, event.y, event.z)) {

            // Проверка жидкостей (только для установки)
            if (!isBreak) {
                // Проверка лавы
                if ((block == Blocks.lava || block == Blocks.flowing_lava) && region.isDisableLava()) {
                    event.setCanceled(true);
                    player.addChatMessage(new ChatComponentText(
                            "§cРазлитие лавы запрещено в зоне §e" + region.getName() + "§c!"
                    ));
                    return;
                }
                // Проверка воды
                if ((block == Blocks.water || block == Blocks.flowing_water) && region.isDisableWater()) {
                    event.setCanceled(true);
                    player.addChatMessage(new ChatComponentText(
                            "§cРазлитие воды запрещено в зоне §e" + region.getName() + "§c!"
                    ));
                    return;
                }
            }

            // Проверка общего запрета на ломание/установку блоков
            boolean shouldCancel = false;
            if (isBreak && region.isBlockBreakDisabled()) {
                shouldCancel = !region.canBreakBlock(block);
            } else if (!isBreak && region.isBlockPlaceDisabled()) {
                shouldCancel = !region.canPlaceBlock(block);
            }

            if (shouldCancel) {
                event.setCanceled(true);
                player.addChatMessage(new ChatComponentText(
                        "§c" + (isBreak ? "Ломание" : "Установка") +
                                " блоков запрещена в зоне §e" + region.getName() + "§c!"
                ));
                return;
            }
        }
    }
}