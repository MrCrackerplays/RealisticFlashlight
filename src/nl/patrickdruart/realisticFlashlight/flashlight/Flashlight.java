package nl.patrickdruart.realisticFlashlight.flashlight;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.events.FlashlightToggleEvent;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.item.ItemBuilder;
import nl.tabuu.tabuucore.nms.wrapper.INBTTagCompound;

/**
 * Represents an ItemStack that's a flashlight
 */
public class Flashlight {
	private ItemStack flashlight;
	private Boolean powered;
	private Integer batteryMaxCharge, batteryCharge, batteryDrainRate, luminocity;
	private Float distance;
	private String id;

	@SuppressWarnings("unused")
	private Flashlight() {
		this(null);
	}

	public Flashlight(ItemStack flashlightItem) {
		if (flashlightItem == null)
			throw new NullPointerException("Attempted to create a Flashlight object with a null ItemStack");
		if (!FlashlightPlugin.getFlashlightsManager().isFlashlight(flashlightItem))
			throw new IllegalArgumentException(
					"Attempted to create a Flashlight object with a non-Flashlight ItemStack");
		this.flashlight = flashlightItem;
		IConfiguration config = FlashlightPlugin.getConfigurationManager().getConfiguration("config");
		INBTTagCompound nbt = INBTTagCompound.get(flashlightItem);
		powered = nbt.hasKey("powered") ? nbt.getBoolean("powered") : config.getBoolean("default.powered");
		batteryMaxCharge = nbt.hasKey("batteryMaxCharge") ? nbt.getInt("batteryMaxCharge")
				: config.getInt("default.batteryMaxCharge");
		batteryCharge = nbt.hasKey("batteryCharge") ? nbt.getInt("batteryCharge") : batteryMaxCharge;
		batteryDrainRate = nbt.hasKey("batteryDrainRate") ? nbt.getInt("batteryDrainRate")
				: config.getInt("default.batteryDrainRate");
		luminocity = nbt.hasKey("luminocity") ? nbt.getInt("luminocity") : config.getInt("default.luminocity");
		distance = nbt.hasKey("distance") ? nbt.getFloat("distance") : (float) config.getDouble("distance");
		id = nbt.getString("id");
	}

	public ItemStack getItem() {
		ItemBuilder itemBuilder = new ItemBuilder(flashlight);
		INBTTagCompound nbt = itemBuilder.getNBTTagCompound();
		nbt.setBoolean("powered", powered);
		nbt.setDouble("batteryMaxCharge", batteryMaxCharge);
		nbt.setDouble("batteryCharge", batteryCharge);
		nbt.setInt("batteryDrainRate", batteryDrainRate);
		nbt.setInt("luminocity", luminocity);
		nbt.setDouble("distance", distance);
		return itemBuilder.build();
	}

	public boolean isPowered() {
		return powered;
	}

	public void setPowered(boolean powered) {
		if (this.powered != powered) {
			togglePower();
		}
	}

	/**
	 * @return whether the toggling was successful
	 */
	public boolean togglePower() {
		FlashlightToggleEvent toggleEvent = new FlashlightToggleEvent(this);
		Bukkit.getPluginManager().callEvent(toggleEvent);
		if (!toggleEvent.isCancelled())
			this.powered = !this.powered;
		return !toggleEvent.isCancelled();
	}

	public int getBatteryMaxCharge() {
		return batteryMaxCharge;
	}

	public void setBatteryMaxCharge(int batteryMaxCharge) {
		this.batteryMaxCharge = batteryMaxCharge;
	}

	public int getBatteryCharge() {
		return batteryCharge;
	}

	public void setBatteryCharge(int charge) {
		this.batteryCharge = charge;
	}

	public int getBatteryDrainRate() {
		return batteryDrainRate;
	}

	public void setBatteryDrainRate(int batteryDrainRate) {
		this.batteryDrainRate = batteryDrainRate;
	}

	public int getLuminocity() {
		return luminocity;
	}

	public void setLuminocity(int luminocity) {
		this.luminocity = luminocity;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSigFields());
	}

	@Override
	public String toString() {
		String result = "Flashlight{" + "flashlight=" + flashlight + ",powered=" + powered + ",batteryMaxCharge="
				+ batteryMaxCharge + ",batteryCharge=" + batteryCharge + ",batteryDrainRate=" + batteryDrainRate
				+ ",luminocity=" + luminocity + ",distance=" + distance + " }";
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Flashlight))
			return false;
		Flashlight that = (Flashlight) obj;
		for (int i = 0; i < this.getSigFields().length; i++) {
			if (!Objects.equals(this.getSigFields()[i], that.getSigFields()[i])) {
				return false;
			}
		}
		return true;
	}

	private Object[] getSigFields() {
		Object[] result = { flashlight.getType(), flashlight.getItemMeta().getDisplayName(), powered, batteryMaxCharge,
				batteryCharge, batteryDrainRate, luminocity, distance, id };
		return result;
	}

}
