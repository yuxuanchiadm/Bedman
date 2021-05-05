package org.yuxuan.bedman.menu;

import java.util.ArrayList;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class MultiDisplayMenu extends AbstractMenu {
	public MultiDisplayMenu() {
		this("Menu", 9, 6);
	}

	public MultiDisplayMenu(String title) {
		this(title, 9, 6);
	}

	public MultiDisplayMenu(String title, int xSize, int ySize) {
		super(title, xSize, ySize);
	}

	public void displayMenu(Player player) {
		player.openInventory(getHolder().getInventory());
	}

	public void closeMenu(Player player) {
		if (player.getOpenInventory().getTopInventory().getHolder().equals(getHolder())) {
			player.closeInventory();
		}
	}

	public void closeAllMenu() {
		for (HumanEntity human : new ArrayList<HumanEntity>(getHolder().getInventory().getViewers())) {
			human.closeInventory();
		}
	}
}