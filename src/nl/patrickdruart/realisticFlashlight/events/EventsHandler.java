package nl.patrickdruart.realisticFlashlight.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.Flashlight;
import nl.patrickdruart.realisticFlashlight.flashlight.FlashlightKeeperInfoHolder;
import nl.patrickdruart.realisticFlashlight.flashlight.FlashlightsManager;
import nl.patrickdruart.realisticFlashlight.flashlight.FlashlightUtils;

/**
 * 
 * General event handler class, handles all events
 *
 */

public class EventsHandler implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL || event.getAction() == Action.LEFT_CLICK_AIR
				|| event.getAction() == Action.LEFT_CLICK_BLOCK)
			return;
		ItemStack handItem = event.getItem();
		FlashlightsManager manager = FlashlightPlugin.getFlashlightsManager();
		if (handItem == null || !manager.isFlashlight(handItem))
			return;
		// only interested in when the player is right clicking with a flashlight
		event.setCancelled(true);
		Flashlight flashlight = new Flashlight(handItem);
		boolean cancelled = !flashlight.togglePower();
		if (cancelled)
			return;
		handItem = flashlight.getItem();
		PlayerInventory plIv = event.getPlayer().getInventory();
		if (event.getHand() == EquipmentSlot.OFF_HAND)
			plIv.setItemInOffHand(handItem);
		else
			plIv.setItemInMainHand(handItem);
		manager.forceReloadFlashlightKeeper(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		FlashlightPlugin.getFlashlightsManager().forceReloadFlashlightKeeper(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		FlashlightsManager manager = FlashlightPlugin.getFlashlightsManager();
		manager.removeFlashlightKeeperInfoholder(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeathEvent(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		FlashlightsManager manager = FlashlightPlugin.getFlashlightsManager();
		manager.removeFlashlightKeeperInfoholder(entity);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				FlashlightPlugin.getFlashlightsManager().forceReloadFlashlightKeeper(event.getPlayer());
			}
		}.runTaskLater(FlashlightPlugin.getPlugin(), 0l);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
		FlashlightsManager manager = FlashlightPlugin.getFlashlightsManager();
		if (!manager.isFlashlight(event.getItem().getItemStack()))
			return;
		new BukkitRunnable() {
			@Override
			public void run() {
				manager.forceReloadFlashlightKeeper(event.getEntity());
			}
		}.runTaskLater(FlashlightPlugin.getPlugin(), 0l);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				FlashlightsManager flashlightsManager = FlashlightPlugin.getFlashlightsManager();
				flashlightsManager.forceReloadFlashlightKeeper(event.getPlayer());
				flashlightsManager.forceReloadFlashlightKeeper(event.getRightClicked());
			}
		}.runTaskLater(FlashlightPlugin.getPlugin(), 0l);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
		FlashlightPlugin.getFlashlightsManager().forceReloadFlashlightKeeper(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		FlashlightsManager flashlightsManager = FlashlightPlugin.getFlashlightsManager();
		Flashlight toMain = flashlightsManager.getFlashlight(event.getMainHandItem());
		Flashlight toOff = flashlightsManager.getFlashlight(event.getOffHandItem());
		if (toMain == null && toOff == null)
			return;
		Player player = event.getPlayer();
		flashlightsManager.forceReloadFlashlightKeeper(player,
				new FlashlightKeeperInfoHolder(player, FlashlightUtils.getOrigin(player, false),
						FlashlightUtils.getDirection(player, false), toMain, FlashlightUtils.getOrigin(player, true),
						FlashlightUtils.getDirection(player, true), toOff));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		FlashlightsManager manager = FlashlightPlugin.getFlashlightsManager();
		if (!(event.getClickedInventory() instanceof PlayerInventory)
				|| !(event.getClickedInventory().getHolder() instanceof Player)
				|| (!(manager.isFlashlight(event.getCurrentItem())) && !(manager.isFlashlight(event.getCursor()))))
			return;
		new BukkitRunnable() {
			@Override
			public void run() {
				manager.forceReloadFlashlightKeeper(event.getWhoClicked());
			}
		}.runTaskLater(FlashlightPlugin.getPlugin(), 0l);
	}
}
