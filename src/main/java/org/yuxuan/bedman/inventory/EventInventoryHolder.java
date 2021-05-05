package org.yuxuan.bedman.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class EventInventoryHolder implements InventoryHolder {
	public void onInventoryOpen(InventoryOpenEvent event) {

	}

	public void onInventoryClose(InventoryCloseEvent event) {

	}

	public void onInventoryDrage(InventoryDragEvent event) {

	}

	public void onInventoryClick(InventoryClickEvent event) {

	}
}