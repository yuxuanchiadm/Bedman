package org.yuxuan.bedman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.yuxuan.bedman.inventory.EventInventoryHolder;
import org.yuxuan.bedman.menu.TeleportationMenu;

public final class PluginMain extends JavaPlugin implements Listener {
	public static String PLUGIN_MSG_PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "查床系统" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
	private static PluginMain INSTANCE;

	public PluginMain() {
		INSTANCE = this;
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof EventInventoryHolder) {
				player.closeInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getInventory().getHolder() instanceof EventInventoryHolder) {
			((EventInventoryHolder) event.getInventory().getHolder()).onInventoryOpen(event);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof EventInventoryHolder) {
			((EventInventoryHolder) event.getInventory().getHolder()).onInventoryClose(event);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof EventInventoryHolder) {
			((EventInventoryHolder) event.getInventory().getHolder()).onInventoryClick(event);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryDrage(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof EventInventoryHolder) {
			((EventInventoryHolder) event.getInventory().getHolder()).onInventoryDrage(event);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("bedman")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					openMenu(player);
					return true;
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "只有玩家才能使用此指令");
					return true;
				}
			} else if (args.length == 1) {
				Player player = getServer().getPlayerExact(args[0]);
				if (player != null) {
					openMenu(player);
					return true;
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "未找到玩家");
					return true;
				}
			}
		} else if (command.getName().equalsIgnoreCase("bedinfo")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					printBedInfo(sender, player);
					return true;
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "只有玩家才能使用此指令");
					return true;
				}
			} else if (args.length == 1) {
				if (hasPlayer(args[0])) {
					OfflinePlayer player = getServer().getOfflinePlayer(args[0]);
					printBedInfo(sender, player);
					return true;
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "未找到玩家");
					return true;
				}
			}
		} else if (command.getName().equalsIgnoreCase("tpbed")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					teleportToBed(player, player);
					return true;
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "只有玩家才能使用此指令");
					return true;
				}
			} else if (args.length == 1) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (hasPlayer(args[0])) {
						OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
						teleportToBed(player, target);
						return true;
					} else {
						sender.sendMessage(PLUGIN_MSG_PREFIX + "未找到玩家");
						return true;
					}
				} else {
					sender.sendMessage(PLUGIN_MSG_PREFIX + "只有玩家才能使用此指令");
					return true;
				}
			}
		}
        return false;
    }

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("bedman")) {
			if (args.length == 0) {
				return null;
			} else if (args.length == 1) {
				List<String> matched = new ArrayList<>();
				for (Player player : getServer().getOnlinePlayers()) {
					if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						matched.add(player.getName());
					}
				}
				return matched;
			}
		} else if (command.getName().equalsIgnoreCase("bedinfo")) {
			if (args.length == 0) {
				return null;
			} else if (args.length == 1) {
				List<String> matched = new ArrayList<>();
				for (OfflinePlayer player : getServer().getOfflinePlayers()) {
					if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						matched.add(player.getName());
					}
				}
				return matched;
			}
		} else if (command.getName().equalsIgnoreCase("tpbed")) {
			if (args.length == 0) {
				return null;
			} else if (args.length == 1) {
				List<String> matched = new ArrayList<>();
				for (OfflinePlayer player : getServer().getOfflinePlayers()) {
					if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						matched.add(player.getName());
					}
				}
				return matched;
			}
		}
		return null;
	}

	private boolean hasPlayer(String name) {
		return getServer().getPlayerExact(name) != null ||
			Arrays.stream(getServer().getOfflinePlayers())
				.map(OfflinePlayer::getName)
				.anyMatch(name::equalsIgnoreCase);
	}

	public void openMenu(Player player) {
		List<OfflinePlayer> targets = Arrays.asList(getServer().getOfflinePlayers());
		TeleportationMenu menu = new TeleportationMenu(player, targets);
		menu.displayMenu();
	}

	public void teleportToBed(Player player, OfflinePlayer target) {
		Location location = target.getBedSpawnLocation();
		if (location == null) {
			player.sendMessage(PLUGIN_MSG_PREFIX + "玩家 " + target.getName() + " 没有床");
		} else {
			if (player.teleport(location)) {
				player.sendMessage(PLUGIN_MSG_PREFIX + "已传送到玩家 " + target.getName() + " 的床");
			} else {
				player.sendMessage(PLUGIN_MSG_PREFIX + "无法传送到玩家 " + target.getName() + " 的床");
			}
		}
	}

	public void printBedInfo(CommandSender sender, OfflinePlayer player) {
		Location location = player.getBedSpawnLocation();
		if (location == null) {
			sender.sendMessage(PLUGIN_MSG_PREFIX + "玩家 " + player.getName() + " 没有床");
		} else {
			sender.sendMessage(PLUGIN_MSG_PREFIX + "玩家 " + player.getName() + " 的床在世界 "
				+ location.getWorld().getName() + " 的 ("
				+ location.getX() + ", " + location.getY() + ", " + location.getZ() + ") 位置"
			);
		}
	}

	public static PluginMain getInstance() {
		return INSTANCE;
	}
}
