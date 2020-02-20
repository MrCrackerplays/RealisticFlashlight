package nl.patrickdruart.realisticFlashlight;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import nl.patrickdruart.realisticFlashlight.commands.CommandFlashlight;
import nl.patrickdruart.realisticFlashlight.events.EventsHandler;
import nl.patrickdruart.realisticFlashlight.flashlight.FlashlightsManager;
import nl.tabuu.tabuucore.configuration.ConfigurationManager;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.util.Dictionary;

/**
 * The RealisticFlashlight plugin's main class
 */
public class FlashlightPlugin extends JavaPlugin {

	private static Plugin _plugin;
	private static ConfigurationManager _configurationManager;
	private static FlashlightsManager _flashlightsManager;
	private static IConfiguration _language;

	@Override
	public void onEnable() {
		_plugin = this;

		// loading configurations
		_configurationManager = new ConfigurationManager(_plugin);
		_configurationManager.addConfiguration("config");
		_configurationManager.addConfiguration("flashlights");
		_configurationManager
				.addConfiguration(_configurationManager.getConfiguration("config").getString("settings.language-file"));
		_language = _configurationManager
				.getConfiguration(_configurationManager.getConfiguration("config").getString("settings.language-file"));

		// setting up the flashlights manager
		_flashlightsManager = FlashlightsManager.getInstance();
		ConfigurationSection flashlights = _configurationManager.getConfiguration("flashlights")
				.getConfigurationSection("");
		HashMap<String, ItemStack> fls = new HashMap<String, ItemStack>();
		for (String key : flashlights.getKeys(false))
			fls.put(key, flashlights.getItemStack(key));
		for (String key : fls.keySet())
			_flashlightsManager.putFlashlight(key, fls.get(key));
		_flashlightsManager.putFlashlight("default", _flashlightsManager.getDefaultFlashlight());

		// settings up the command-, and event-handlers
		this.getCommand("flashlight").setExecutor(new CommandFlashlight());
		getServer().getPluginManager().registerEvents(new EventsHandler(), this);

		// for every player that's online (during a /reload) add them back into the
		// flashlights manager
		for (Player player : Bukkit.getOnlinePlayers())
			_flashlightsManager.forceReloadFlashlightKeeper(player);
	}

	@Override
	public void onDisable() {
		_flashlightsManager.unload();
	}

	public static Plugin getPlugin() {
		return _plugin;
	}

	public static ConfigurationManager getConfigurationManager() {
		return _configurationManager;
	}

	public static FlashlightsManager getFlashlightsManager() {
		return _flashlightsManager;
	}

	public static Dictionary getDictionary() {
		return _language.getDictionary("");
	}
}
