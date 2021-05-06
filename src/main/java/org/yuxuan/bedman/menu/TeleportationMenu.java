package org.yuxuan.bedman.menu;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.yuxuan.bedman.PluginMain;

public final class TeleportationMenu extends SingleDisplayMenu {
	private static final int SINGLE_LINE_MAX_TARGET_COUNT = 7;
	private static final int SINGLE_PAGE_MAX_TARGET_COUNT = SINGLE_LINE_MAX_TARGET_COUNT * 6;

	private final List<OfflinePlayer> targets;
	private final Queue<String> pendingIcons;
	private volatile boolean loading;
	private volatile boolean closing;
	private volatile boolean updating;
	private int page;

	public TeleportationMenu(Player player, List<OfflinePlayer> targets) {
		super(player);
		this.targets = targets;
		this.pendingIcons = targets.stream().map(OfflinePlayer::getName).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
		this.loading = false;
		this.closing = false;
		this.updating = false;
		updatePage();
	}

	@Override
	protected boolean onMenuOpening(Player player) {
		loading = true;
		player.sendMessage(PluginMain.PLUGIN_MSG_PREFIX + "正在加载玩家头像...");
		runTaskAsync(() -> {
			Inventory iconLoader = new DummyInventoryHolder().getInventory();
			String icon;
			while (!closing && (icon = pendingIcons.peek()) != null) {
				ItemStack targetIcon = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta targetIconMeta = (SkullMeta) targetIcon.getItemMeta();
				targetIconMeta.setOwner(icon);
				targetIcon.setItemMeta(targetIconMeta);
				iconLoader.setItem(0, targetIcon);
				pendingIcons.remove();
				updatePageSync();
			}
			runTaskSync(() -> {
				if (closing) player.sendMessage(PluginMain.PLUGIN_MSG_PREFIX + "玩家头像加载被中断！");
				else player.sendMessage(PluginMain.PLUGIN_MSG_PREFIX + "玩家头像加载完毕！");
				loading = false;
				closing = false;
			});
		});
		return super.onMenuOpening(player);
	}

	@Override
	protected void onMenuClosing(Player player) {
		if (this.loading) {
			this.closing = true;
			player.sendMessage(PluginMain.PLUGIN_MSG_PREFIX + "正在尝试中断玩家头像加载...");
		}
		super.onMenuClosing(player);
	}

	private void runTaskAsync(Runnable runnable) {
		PluginMain.getInstance().getServer().getScheduler().runTaskAsynchronously(PluginMain.getInstance(), runnable);
	}

	private void runTaskSync(Runnable runnable) {
		PluginMain.getInstance().getServer().getScheduler().runTask(PluginMain.getInstance(), runnable);
	}

	private void updatePageSync() {
		if (PluginMain.getInstance().getServer().isPrimaryThread()) {
			updating = false;
			if (closing) return;
			updatePage();
		} else {
			if (closing || updating) return;
			updating = true;
			runTaskSync(this::updatePageSync);
		}
	}

	private void updatePage() {
		int maxPage = getMaxPage();
		if (page < 0) {
			page = 0;
		}
		if (page >= maxPage) {
			page = maxPage - 1;
		}
		int currentPageYSize = getCurrentPageYSize();
		setTitle(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "选择要查床的玩家" + ChatColor.RESET.toString() + " (" + (page + 1) + "\\" + getMaxPage() + ")");
		setYSize(currentPageYSize);
		clearButtons();
		if (page >= 0 && page < maxPage) {
			{
				int start = page * SINGLE_PAGE_MAX_TARGET_COUNT;
				int end = Math.min(targets.size(), start + SINGLE_PAGE_MAX_TARGET_COUNT);
				int x = 0;
				int y = 0;
				for (int index = start; index < end; index++) {
					OfflinePlayer target = targets.get(index);
					drawTargetIcon(target, x, y);
					x++;
					if (x >= SINGLE_LINE_MAX_TARGET_COUNT) {
						x = 0;
						y++;
					}
				}
			}
			{
				ItemStack dividingLineIcon = new ItemStack(Material.THIN_GLASS);
				ItemMeta dividingLineIconMeta = dividingLineIcon.getItemMeta();
				dividingLineIconMeta.setDisplayName(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD.toString() + "分割线");
				dividingLineIcon.setItemMeta(dividingLineIconMeta);
				for (int index = 0; index < currentPageYSize; index++) {
					addButton(7, index, dividingLineIcon, null);
				}
			}
			if (page > 0) {
				ItemStack previousIcon = new ItemStack(Material.WOOL, 1, (byte) 14);
				ItemMeta previousIconMeta = previousIcon.getItemMeta();
				previousIconMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "上一页");
				previousIcon.setItemMeta(previousIconMeta);
				addButton(8, 0, previousIcon, event -> previousPage());
			}
			if (page < maxPage - 1) {
				ItemStack nextIcon = new ItemStack(Material.WOOL, 1, (byte) 11);
				ItemMeta nextIconMeta = nextIcon.getItemMeta();
				nextIconMeta.setDisplayName(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "下一页");
				nextIcon.setItemMeta(nextIconMeta);
				addButton(8, currentPageYSize - 1, nextIcon, event -> nextPage());
			}
		}
	}

	public void drawTargetIcon(OfflinePlayer target, int x, int y) {
		ItemStack targetIcon = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta targetIconMeta = (SkullMeta) targetIcon.getItemMeta();
		if (!pendingIcons.contains(target.getName())) {
			targetIconMeta.setOwner(target.getName());
		}
		targetIconMeta.setDisplayName(target.getName());
		targetIcon.setItemMeta(targetIconMeta);
		setButton(x, y, targetIcon, event -> {
			Player player = event.getPlayer();
			if (event.getMouse() == 0) {
				PluginMain.getInstance().teleportToBed(player, target);
			} else if (event.getMouse() == 1) {
				PluginMain.getInstance().printBedInfo(player, target);
			}
		});
	}

	public int getCurrentPageYSize() {
		int itemCount = targets.size();
		if (itemCount == 0) {
			return 0;
		}
		int maxPage = getMaxPage();
		if (page < 0 || page >= maxPage) {
			return 0;
		} else {
			if (itemCount % SINGLE_PAGE_MAX_TARGET_COUNT != 0 && page == maxPage - 1) {
				return (int) Math.ceil((double) (itemCount % SINGLE_PAGE_MAX_TARGET_COUNT) / (double) SINGLE_LINE_MAX_TARGET_COUNT);
			} else {
				return 6;
			}
		}
	}

	public int getMaxPage() {
		int itemCount = targets.size();
		if (itemCount == 0) {
			return 1;
		}
		return Math.max((int) Math.ceil((double) itemCount / (double) SINGLE_PAGE_MAX_TARGET_COUNT), 1);
	}

	public void previousPage() {
		page--;
		if (page < 0) {
			page = 0;
		}
		updatePage();
	}

	public void nextPage() {
		page++;
		int maxPage = getMaxPage();
		if (page > maxPage - 1) {
			page = maxPage - 1;
		}
		updatePage();
	}

	@Override
	public void displayMenu() {
		updatePage();
		super.displayMenu();
	}
}

final class DummyInventoryHolder implements InventoryHolder {
	private final Inventory inventory;

	public DummyInventoryHolder() {
		this.inventory = PluginMain.getInstance().getServer().createInventory(this, 9);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
