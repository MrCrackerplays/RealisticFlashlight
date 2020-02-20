package nl.patrickdruart.realisticFlashlight.flashlight;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.item.ItemBuilder;
import nl.tabuu.tabuucore.nms.wrapper.INBTTagCompound;

/**
 * FlashlightsManager manages all flashlights and all flashlight holders
 */
public class FlashlightsManager {
	private Map<UUID, FlashlightKeeperInfoHolder> holders;
	private Map<String, ItemStack> flashlights;

	private static FlashlightsManager instance;

	private FlashlightsManager() {
		holders = new HashMap<UUID, FlashlightKeeperInfoHolder>();
		flashlights = new HashMap<String, ItemStack>();
	}

	private void load() {
		flashlights = new HashMap<String, ItemStack>();
		IConfiguration config = FlashlightPlugin.getConfigurationManager().getConfiguration("flashlights");
		config.reload();
		ConfigurationSection configSection = config.getConfigurationSection("");
		Set<String> flashlightTypes = configSection.getKeys(false);
		for (String type : flashlightTypes) {
			putFlashlight(type, configSection.getItemStack(type));
		}
	}

	public void unload() {
		Iterator<FlashlightKeeperInfoHolder> iter = holders.values().iterator();
		while (iter.hasNext()) {
			FlashlightKeeperInfoHolder holder = iter.next();
			if (holder.getLeftHandFlashlightRunnable() != null) {
				holder.setLeftHand(null, null, null);
			}
			if (holder.getRightHandFlashlightRunnable() != null) {
				holder.setRightHand(null, null, null);
			}
		}
	}

	public static FlashlightsManager getInstance() {
		if (instance == null) {
			synchronized (FlashlightsManager.class) {
				if (instance == null) {
					instance = new FlashlightsManager();
				}
			}
		}
		return instance;
	}

	public void reload() {
		load();
		for (Player player : Bukkit.getOnlinePlayers())
			forceReloadFlashlightKeeper(player);
	}

	public void forceReloadFlashlightKeeper(LivingEntity entity) {
		FlashlightKeeperInfoHolder holder = new FlashlightKeeperInfoHolder(entity);
		forceReloadFlashlightKeeper(entity, holder);
	}

	public void forceReloadFlashlightKeeper(LivingEntity entity,
			FlashlightKeeperInfoHolder flashlightKeeperInfoHolder) {
		forceReloadFlashlightKeeper(entity.getUniqueId(), flashlightKeeperInfoHolder);
	}

	public void forceReloadFlashlightKeeper(UUID uuid, FlashlightKeeperInfoHolder flashlightKeeperInfoHolder) {
		removeFlashlightKeeperInfoholder(uuid);
		putFlashlightKeeperInfoholder(uuid, flashlightKeeperInfoHolder);
		FlashlightKeeperInfoHolder holder = getFlashlightKeeperInfoHolder(uuid);
		if (holder.getLeftHandFlashlightRunnable() != null) {
			holder.getLeftHandFlashlightRunnable().start();
		}
		if (holder.getRightHandFlashlightRunnable() != null) {
			holder.getRightHandFlashlightRunnable().start();
		}
	}

	public void removeFlashlightKeeperInfoholder(LivingEntity entity) {
		removeFlashlightKeeperInfoholder(entity.getUniqueId());
	}

	public void removeFlashlightKeeperInfoholder(UUID uniqueId) {
		FlashlightKeeperInfoHolder holder = holders.get(uniqueId);
		if (holder == null)
			return;
		holder.setLeftHand(null, null, null);
		holder.setRightHand(null, null, null);
		holders.remove(uniqueId);
	}

	public void putFlashlightKeeperInfoholder(LivingEntity entity, FlashlightKeeperInfoHolder keeper) {
		putFlashlightKeeperInfoholder(entity.getUniqueId(), keeper);
	}

	public void putFlashlightKeeperInfoholder(UUID uuid, FlashlightKeeperInfoHolder keeper) {
		holders.put(uuid, keeper);
	}

	public FlashlightKeeperInfoHolder getFlashlightKeeperInfoHolder(LivingEntity entity) {
		return getFlashlightKeeperInfoHolder(entity.getUniqueId());
	}

	public FlashlightKeeperInfoHolder getFlashlightKeeperInfoHolder(UUID uuid) {
		return holders.get(uuid);
	}

	public boolean isFlashlight(ItemStack flashlightCheck) {
		if (flashlightCheck == null || !flashlightCheck.hasItemMeta())
			return false;
		boolean result = false;
		INBTTagCompound tag1 = INBTTagCompound.get(flashlightCheck);
		for (ItemStack fl : flashlights.values()) {
			INBTTagCompound tag2 = INBTTagCompound.get(fl);
			if (flashlightCheck.getType() == fl.getType() && tag1.getString("id") != null
					&& tag1.getString("id").length() > 0
					&& (tag1.getString("id").equals(tag2.getString("id")) || flashlightCheck.equals(fl))) {
				result = true;
				break;
			}
		}
		return result;
	}

	public Flashlight getFlashlight(ItemStack flashlightItem) {
		if (!isFlashlight(flashlightItem))
			return null;
		return new Flashlight(flashlightItem);
	}

	public ItemStack getFlashlightItem(String name) {
		return flashlights.get(name.trim().replaceAll(" ", ""));
	}

	public ItemStack putFlashlight(ItemStack flashlight) {
		return putFlashlight(flashlight.getItemMeta().getDisplayName(), flashlight);
	}

	public ItemStack putFlashlight(String name, ItemStack flashlight) {
		name = name.trim().replaceAll(" ", "");
		INBTTagCompound nbt = INBTTagCompound.get(flashlight);
		IConfiguration config = FlashlightPlugin.getConfigurationManager().getConfiguration("config");
		if (!nbt.hasKey("powered"))
			nbt.setBoolean("powered", config.getBoolean("default.powered"));
		if (!nbt.hasKey("batteryMaxCharge"))
			nbt.setInt("batteryMaxCharge", config.getInt("default.batteryMaxCharge"));
		if (!nbt.hasKey("batteryCharge"))
			nbt.setInt("batteryCharge", config.getInt("default.batteryCharge"));
		if (!nbt.hasKey("batteryDrainRate"))
			nbt.set("batteryDrainRate", config.getInt("default.batteryDrainRate"));
		if (!nbt.hasKey("luminocity"))
			nbt.set("luminocity", config.getInt("default.luminocity"));
		if (!nbt.hasKey("distance"))
			nbt.set("distance", config.getDouble("default.distance"));
		nbt.set("id", name);
		flashlight = nbt.apply(flashlight);
		flashlights.put(name, flashlight);
		updateFlashlightsConfig();
		return flashlight;
	}

	private void updateFlashlightsConfig() {
		IConfiguration fls = FlashlightPlugin.getConfigurationManager().getConfiguration("flashlights");
		for (String id : flashlights.keySet()) {
			fls.set(id, flashlights.get(id));
		}
		fls.save();
	}

	public void removeFlashlight(ItemStack flashlight) {
		removeFlashlight(flashlight.getItemMeta().getDisplayName());
	}

	public boolean removeFlashlight(String name) {
		if (!flashlights.containsKey(name))
			return false;
		flashlights.remove(name.trim().replaceAll(" ", ""));
		updateFlashlightsConfig();
		return true;
	}

	public ItemStack getDefaultFlashlight() {
		IConfiguration config = FlashlightPlugin.getConfigurationManager().getConfiguration("config");
		ItemBuilder builder = new ItemBuilder(Material.valueOf(config.getString("default.material")));
		builder.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("default.name")));
		INBTTagCompound nbt = builder.getNBTTagCompound();
		nbt.set("powered", config.getBoolean("default.powered"));
		nbt.set("luminocity", config.getInt("default.luminocity"));
		nbt.set("distance", config.getDouble("default.distance"));
		nbt.set("batteryMaxCharge", config.getInt("default.batteryMaxCharge"));
		nbt.set("batteryCharge", config.getInt("default.batteryCharge"));
		nbt.set("batteryDrainRate", config.getInt("default.batteryDrainRate"));
		nbt.set("id", "default");
		return builder.build();
	}

	public Map<String, ItemStack> getFlashlights() {
		Map<String, ItemStack> fls = new HashMap<String, ItemStack>();
		fls.putAll(flashlights);
		return fls;
	}
}
