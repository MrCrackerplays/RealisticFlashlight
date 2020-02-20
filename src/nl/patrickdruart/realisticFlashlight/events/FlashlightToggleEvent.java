package nl.patrickdruart.realisticFlashlight.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import nl.patrickdruart.realisticFlashlight.flashlight.Flashlight;

/**
 * 
 * Represents an event that is called when a flashlight's power gets toggled.
 *
 */

public class FlashlightToggleEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean isCancelled;

	private ItemStack flashlight;
	private boolean powered;

	public FlashlightToggleEvent(Flashlight flashlight) {
		this(flashlight.getItem(), flashlight.isPowered());
	}

	public FlashlightToggleEvent(ItemStack flashlight, boolean wasPowered) {
		isCancelled = false;
		this.flashlight = flashlight;
		this.powered = wasPowered;
	}

	public boolean wasPowered() {
		return powered;
	}

	public ItemStack getFlashlight() {
		return flashlight;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
