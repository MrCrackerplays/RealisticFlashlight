package nl.patrickdruart.realisticFlashlight.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.Flashlight;
import nl.patrickdruart.realisticFlashlight.flashlight.FlashlightsManager;
import nl.tabuu.tabuucore.command.Command;
import nl.tabuu.tabuucore.command.CommandResult;
import nl.tabuu.tabuucore.command.SenderType;
import nl.tabuu.tabuucore.command.argument.ArgumentType;
import nl.tabuu.tabuucore.command.argument.converter.OrderedArgumentConverter;
import nl.tabuu.tabuucore.nms.wrapper.INBTTagCompound;
import nl.tabuu.tabuucore.util.Dictionary;

/**
 * Flashlight command class, defines handling for the flashlight command and all
 * its subcommands
 */
public class CommandFlashlight extends Command {

	public CommandFlashlight() {
		super("flashlight");
		this.addSubCommand("help", new CommandFlashlightHelp(this));
		this.addSubCommand("set", new CommandFlashlightSet(this));
		this.addSubCommand("get", new CommandFlashlightGet(this));
		this.addSubCommand("remove", new CommandFlashlightRemove(this));
		this.addSubCommand("reload", new CommandFlashlightReload(this));
		this.addSubCommand("edit", new CommandFlashlightEdit(this));
		Bukkit.getPluginCommand("flashlight")
				.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_HELP_DESCRIPTION"));
	}

	@Override
	protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
		return ((CommandFlashlightHelp) this.getSubCommand("help")).onCommand(commandSender, arguments);
	}

	class CommandFlashlightHelp extends Command {
		private final Map<String, String> commands;

		protected CommandFlashlightHelp(Command parent) {
			super("flashlight help", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING);
			this.setArgumentConverter(converter);
			this.commands = new HashMap<String, String>();
			Dictionary dict = FlashlightPlugin.getDictionary();
			Bukkit.getPluginCommand("flashlight help").setDescription(dict.translate("COMMAND_HELP_DESCRIPTION"));
			commands.put("set", dict.translate("COMMAND_HELP_SET"));
			commands.put("get", dict.translate("COMMAND_HELP_GET"));
			commands.put("remove", dict.translate("COMMAND_HELP_REMOVE"));
			commands.put("reload", dict.translate("COMMAND_HELP_RELOAD"));
			commands.put("edit", dict.translate("COMMAND_HELP_EDIT"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			String[] message = new String[2];
			int i = 1;
			Dictionary dict = FlashlightPlugin.getDictionary();
			String fancy = dict.translate("COMMAND_HELP_HEADER");
			for (String command : commands.keySet()) {
				if (commandSender instanceof Player
						&& !((Player) commandSender).hasPermission("flashlight.command." + command.toLowerCase()))
					continue;
				message = Arrays.copyOf(message, message.length + 1);
				message[0] = fancy;
				message[i++] = commands.get(command);
				message[i] = fancy;
			}
			if (message[0] == null) {
				message = new String[3];
				message[0] = fancy;
				message[1] = dict.translate("COMMAND_HELP_NONE");
				message[2] = fancy;
			}
			commandSender.sendMessage(message);
			return CommandResult.SUCCESS;
		}
	}

	class CommandFlashlightSet extends Command {

		protected CommandFlashlightSet(Command parent) {
			super("flashlight set", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING);
			this.setRequiredSenderType(SenderType.PLAYER);
			this.setArgumentConverter(converter);
			Bukkit.getPluginCommand("flashlight set")
					.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_SET_DESCRIPTION"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			Player player = (Player) commandSender;
			ItemStack flashlight = player.getEquipment().getItemInMainHand();
			Dictionary dict = FlashlightPlugin.getDictionary();
			if (flashlight == null || flashlight.getType() == Material.AIR) {
				player.sendMessage(dict.translate("COMMAND_SET_NO_ITEM"));
				return CommandResult.SUCCESS;
			}
			if (!arguments.get(0).isPresent() || ((String) arguments.get(0).get()).trim().isEmpty())
				return CommandResult.WRONG_SYNTAX;
			String name = ((String) arguments.get(0).get()).trim();
			INBTTagCompound nbt = INBTTagCompound.get(flashlight);
			INBTTagCompound defaultNBT = INBTTagCompound
					.get(FlashlightPlugin.getFlashlightsManager().getDefaultFlashlight());
			if (!nbt.hasKey("powered"))
				nbt.set("powered", defaultNBT.getBoolean("powered"));
			if (!nbt.hasKey("batteryMaxCharge"))
				nbt.set("batteryMaxCharge", defaultNBT.getInt("batteryMaxCharge"));
			if (!nbt.hasKey("batteryCharge"))
				nbt.set("batteryCharge", defaultNBT.getInt("batteryCharge"));
			if (!nbt.hasKey("batteryDrainRate"))
				nbt.set("batteryDrainRate", defaultNBT.getInt("batteryDrainRate"));
			if (!nbt.hasKey("luminocity"))
				nbt.set("luminocity", defaultNBT.getInt("luminocity"));
			if (!nbt.hasKey("distance"))
				nbt.set("distance", defaultNBT.getDouble("distance"));
			flashlight = FlashlightPlugin.getFlashlightsManager().putFlashlight(name, flashlight);
			String message = dict.translate("COMMAND_SET_SUCCESS", "%name%", name);
			player.sendMessage(message);
			player.getEquipment().setItemInMainHand(flashlight);
			FlashlightsManager.getInstance().forceReloadFlashlightKeeper(player);
			return CommandResult.SUCCESS;
		}
	}

	class CommandFlashlightGet extends Command {

		protected CommandFlashlightGet(Command parent) {
			super("flashlight get", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING);
			this.setRequiredSenderType(SenderType.PLAYER);
			this.setArgumentConverter(converter);
			Bukkit.getPluginCommand("flashlight get")
					.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_GET_DESCRIPTION"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			String name;
			if (arguments.get(0).isPresent())
				name = (String) arguments.get(0).get();
			else
				name = "default";
			Player player = (Player) commandSender;
			ItemStack item = FlashlightPlugin.getFlashlightsManager().getFlashlightItem(name);
			Dictionary dict = FlashlightPlugin.getDictionary();
			if (item == null) {
				player.sendMessage(dict.translate("COMMAND_GET_NOT_FOUND", "%name%", name));
				return CommandResult.SUCCESS;
			}
			HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
			for (ItemStack is : leftover.values()) {
				if (is != null)
					player.getLocation().getWorld().dropItem(player.getLocation(), is);
			}
			String message = dict.translate("COMMAND_GET_SUCCESS", "%name%", name);
			player.sendMessage(message);
			return CommandResult.SUCCESS;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label,
				String[] arguments) {
			List<String> subArguments = super.onTabComplete(sender, bukkitCommand, label, arguments);
			if (arguments.length == 1) {
				Map<String, ItemStack> flashlights = FlashlightPlugin.getFlashlightsManager().getFlashlights();
				for (String fl : flashlights.keySet()) {
					if (fl.toUpperCase().startsWith(arguments[0].toUpperCase()))
						subArguments.add(fl);
				}
			}
			return subArguments;
		}
	}

	class CommandFlashlightRemove extends Command {

		protected CommandFlashlightRemove(Command parent) {
			super("flashlight remove", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING);
			this.setArgumentConverter(converter);
			Bukkit.getPluginCommand("flashlight remove")
					.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_REMOVE_DESCRIPTION"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			if (!arguments.get(0).isPresent())
				return CommandResult.WRONG_SYNTAX;
			String name = (String) arguments.get(0).get();
			Dictionary dict = FlashlightPlugin.getDictionary();
			if (name.trim().equalsIgnoreCase("default")) {
				commandSender.sendMessage(dict.translate("COMMAND_REMOVE_DEFAULT"));
				return CommandResult.SUCCESS;
			}
			if (!FlashlightPlugin.getFlashlightsManager().removeFlashlight(name)) {
				commandSender.sendMessage(dict.translate("COMMAND_REMOVE_NOT_FOUND", "%name%", name));
				return CommandResult.WRONG_SYNTAX;
			}
			String message = dict.translate("COMMAND_REMOVE_SUCCESS", "%name%", name);
			commandSender.sendMessage(message);
			return CommandResult.SUCCESS;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label,
				String[] arguments) {
			List<String> subArguments = super.onTabComplete(sender, bukkitCommand, label, arguments);
			if (arguments.length == 1) {
				Map<String, ItemStack> flashlights = FlashlightPlugin.getFlashlightsManager().getFlashlights();
				for (String fl : flashlights.keySet()) {
					if (fl.equalsIgnoreCase("default"))
						continue;
					if (fl.toUpperCase().startsWith(arguments[0].toUpperCase()))
						subArguments.add(fl);
				}
			}
			return subArguments;
		}
	}

	class CommandFlashlightReload extends Command {

		protected CommandFlashlightReload(Command parent) {
			super("flashlight reload", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING);
			this.setArgumentConverter(converter);
			Bukkit.getPluginCommand("flashlight reload")
					.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_RELOAD_DESCRIPTION"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			boolean a = false, b = false;
			if (!arguments.get(0).isPresent()) {
				if (commandSender instanceof Player) {
					if (((Player) commandSender).hasPermission("flashlight.command.reload.configs"))
						a = true;
					if (!((Player) commandSender).hasPermission("flashlight.command.reload.flashlights"))
						b = true;
					if (!a && !b)
						return CommandResult.NO_PERMISSION;
				} else {
					a = true;
					b = true;
				}
			} else {
				String arg = (String) arguments.get(0).get();
				if (arg.equalsIgnoreCase("configs")) {
					if (commandSender instanceof Player
							&& !((Player) commandSender).hasPermission("flashlight.command.reload.configs"))
						return CommandResult.NO_PERMISSION;
					a = true;
				} else if (arg.equalsIgnoreCase("flashlights")) {
					if (commandSender instanceof Player
							&& !((Player) commandSender).hasPermission("flashlight.command.reload.flashlights"))
						return CommandResult.NO_PERMISSION;
					b = true;
				} else {
					return CommandResult.WRONG_SYNTAX;
				}
			}
			if (a)
				reloadConfigs();
			if (b)
				reloadFlashlights();
			Dictionary dict = FlashlightPlugin.getDictionary();
			commandSender.sendMessage(dict.translate("COMMAND_RELOAD_SUCCESS"));
			return CommandResult.SUCCESS;
		}

		private void reloadConfigs() {
			FlashlightPlugin.getConfigurationManager().reloadAll();
		}

		private void reloadFlashlights() {
			FlashlightPlugin.getFlashlightsManager().reload();
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label,
				String[] arguments) {
			List<String> subArguments = super.onTabComplete(sender, bukkitCommand, label, arguments);
			if (arguments.length == 1) {
				String[] sub = { "configs", "flashlights" };
				for (String arg : sub) {
					if (arg.toUpperCase().startsWith(arguments[0].toUpperCase())) {
						if (sender instanceof Player
								&& !((Player) sender).hasPermission("flashlight.command.reload." + arg.toLowerCase()))
							continue;
						subArguments.add(arg);
					}
				}
			}
			return subArguments;
		}
	}

	class CommandFlashlightEdit extends Command {

		protected CommandFlashlightEdit(Command parent) {
			super("flashlight edit", parent);
			OrderedArgumentConverter converter = new OrderedArgumentConverter();
			converter.setSequence(ArgumentType.STRING, ArgumentType.STRING, ArgumentType.STRING);
			this.setArgumentConverter(converter);
			Bukkit.getPluginCommand("flashlight edit")
					.setDescription(FlashlightPlugin.getDictionary().translate("COMMAND_EDIT_DESCRIPTION"));
		}

		@Override
		protected CommandResult onCommand(CommandSender commandSender, List<Optional<?>> arguments) {
			if (arguments.stream().anyMatch(op -> !op.isPresent()))
				return CommandResult.WRONG_SYNTAX;
			FlashlightsManager manager = FlashlightsManager.getInstance();
			Flashlight flashlight = manager.getFlashlight(manager.getFlashlightItem((String) arguments.get(0).get()));
			Dictionary dict = FlashlightPlugin.getDictionary();
			try {
				ItemStack item = flashlight.getItem();
				switch ((String) arguments.get(1).get()) {
				case "material":
					item.setType(Material.valueOf((String) arguments.get(2).get()));
					break;
				case "displayname":
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) arguments.get(2).get()));
					item.setItemMeta(meta);
					break;
				case "luminocity":
					flashlight.setLuminocity(Integer.valueOf((String) arguments.get(2).get()));
					item = flashlight.getItem();
					break;
				case "distance":
					flashlight.setDistance(Float.valueOf((String) arguments.get(2).get()));
					item = flashlight.getItem();
					break;
				case "batterymaxcharge":
					flashlight.setBatteryMaxCharge(Integer.valueOf((String) arguments.get(2).get()));
					item = flashlight.getItem();
					break;
				case "batterycharge":
					flashlight.setBatteryCharge(Integer.valueOf((String) arguments.get(2).get()));
					item = flashlight.getItem();
					break;
				case "batterydrainrate":
					flashlight.setBatteryDrainRate(Integer.valueOf((String) arguments.get(2).get()));
					item = flashlight.getItem();
					break;
				default:
					commandSender.sendMessage(
							dict.translate("COMMAND_EDIT_STAT_NOT_FOUND", "%stat%", (String) arguments.get(1).get()));
					return CommandResult.WRONG_SYNTAX;
				}
				manager.putFlashlight((String) arguments.get(0).get(), item);
				commandSender.sendMessage(dict.translate("COMMAND_EDIT_SUCCESS", "%name%",
						(String) arguments.get(0).get(), "%stat%", (String) arguments.get(1).get()));
				return CommandResult.SUCCESS;
			} catch (NullPointerException e) {
				commandSender.sendMessage(
						dict.translate("COMMAND_EDIT_NOT_FOUND", "%name%", (String) arguments.get(0).get()));
				return CommandResult.WRONG_SYNTAX;
			} catch (Exception e) {
				return CommandResult.WRONG_SYNTAX;
			}
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label,
				String[] arguments) {
			List<String> subArguments = super.onTabComplete(sender, bukkitCommand, label, arguments);
			if (arguments.length == 1) {
				for (String arg : FlashlightsManager.getInstance().getFlashlights().keySet())
					if (arg.toUpperCase().startsWith(arguments[0].toUpperCase()))
						subArguments.add(arg);
			} else if (arguments.length == 2) {
				String[] editables = { "material", "displayname", "luminocity", "distance", "batterymaxcharge",
						"batterycharge", "batterydrainrate" };
				for (String arg : editables) {
					if (arg.toUpperCase().startsWith(arguments[1].toUpperCase()))
						subArguments.add(arg);
				}
			} else if (arguments.length == 3) {
				FlashlightsManager manager = FlashlightsManager.getInstance();
				if (!manager.getFlashlights().containsKey(arguments[0])) {
					subArguments.add("ERROR_NO_SUCH_FLASHLIGHT_FOUND");
				} else {
					String str = "";
					switch (arguments[1].toLowerCase()) {
					case "material":
						str = manager.getFlashlightItem(arguments[0]).getType().name();
						break;
					case "displayname":
						str = manager.getFlashlightItem(arguments[0]).getItemMeta().getDisplayName();
						break;
					case "luminocity":
						str = "" + manager.getFlashlight(manager.getFlashlightItem(arguments[0])).getLuminocity();
						break;
					case "distance":
						str = "" + manager.getFlashlight(manager.getFlashlightItem(arguments[0])).getDistance();
						break;
					case "batterymaxcharge":
						str = "" + manager.getFlashlight(manager.getFlashlightItem(arguments[0])).getBatteryMaxCharge();
						break;
					case "batterycharge":
						str = "" + manager.getFlashlight(manager.getFlashlightItem(arguments[0])).getBatteryCharge();
						break;
					case "batterydrainrate":
						str = "" + manager.getFlashlight(manager.getFlashlightItem(arguments[0])).getBatteryDrainRate();
						break;
					default:
						subArguments.add("INVALID_STAT");
						break;
					}
					if (str.toUpperCase().startsWith(arguments[2].toUpperCase()))
						subArguments.add(str);
				}
			}
			return subArguments;
		}
	}
}
