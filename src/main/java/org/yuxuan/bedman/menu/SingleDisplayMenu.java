package org.yuxuan.bedman.menu;

import org.bukkit.entity.Player;

public class SingleDisplayMenu extends AbstractMenu {
	private Player player;

	public SingleDisplayMenu(Player player) {
		this(player, "Menu", 9, 6);
	}

	public SingleDisplayMenu(Player player, String title) {
		this(player, title, 9, 6);
	}

	public SingleDisplayMenu(Player player, String title, int xSize, int ySize) {
		super(title, xSize, ySize);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void displayMenu() {
		player.openInventory(getHolder().getInventory());
	}

	public void closeMenu() {
		if (player.getOpenInventory().getTopInventory().getHolder().equals(getHolder())) {
			player.closeInventory();
		}
	}
}