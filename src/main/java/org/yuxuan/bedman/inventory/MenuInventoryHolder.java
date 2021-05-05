package org.yuxuan.bedman.inventory;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yuxuan.bedman.PluginMain;

public class MenuInventoryHolder extends EventInventoryHolder {
	private Button[] buttons;
	private String menuTitle;
	private Inventory inventory;
	private int menuLineNum;

	public MenuInventoryHolder(int menuLineNum, String menuTitle) {
		this.menuLineNum = menuLineNum;
		this.menuTitle = menuTitle;
		this.buttons = new Button[menuLineNum * 9];
		this.inventory = PluginMain.getInstance().getServer().createInventory(this, menuLineNum * 9, menuTitle);
	}

	public void setButton(Button button, int index) {
		if (button != null) {
			if (button.getHolder() != null) {
				throw new IllegalArgumentException("Button already in menu");
			}
			button.setHolder(this);
		}
		if (buttons[index] != null) {
			buttons[index].setHolder(null);
		}
		buttons[index] = button;
		drawButtons();
	}

	public Button getButton(int index) {
		return buttons[index];
	}

	public void setButton(Button button, int x, int y) {
		if (x < 0 || x > 8) {
			throw new IndexOutOfBoundsException("x must between 0 and 8");
		}
		if (y < 0 || y > menuLineNum - 1) {
			throw new IndexOutOfBoundsException("y must between 0 and " + (menuLineNum - 1));
		}
		setButton(button, x + y * 9);
	}

	public Button getButton(int x, int y) {
		if (x < 0 || x > 8) {
			throw new IndexOutOfBoundsException("x must between 0 and 8");
		}
		if (y < 0 || y > menuLineNum - 1) {
			throw new IndexOutOfBoundsException("y must between 0 and " + (menuLineNum - 1));
		}
		return getButton(x + y * 9);
	}

	public void clearButtons() {
		for (int index = 0; index < buttons.length; index++) {
			Button button = buttons[index];
			if (button != null) {
				button.setHolder(null);
				buttons[index] = null;
			}
		}
	}

	public void setMenuTitle(String menuTitle) {
		if (!this.menuTitle.equals(menuTitle)) {
			this.menuTitle = menuTitle;
			drawInventory();
		}
	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public void setMenuLineNum(int menuLineNum) {
		if (this.menuLineNum != menuLineNum) {
			this.menuLineNum = menuLineNum;
			this.buttons = new Button[menuLineNum * 9];
			drawInventory();
		}
	}

	public int getMenuLineNum() {
		return this.menuLineNum;
	}

	@SuppressWarnings("deprecation")
	protected void drawButtons() {
		inventory.clear();
		for (int idx = 0; idx < inventory.getSize(); idx++) {
			if (idx < buttons.length) {
				Button button = buttons[idx];
				if (button != null && button.isVisible()) {
					ItemStack icon = button.getIcon();
					if (icon != null) {
						inventory.setItem(idx, icon);
					}
				}
			}
		}
		for (HumanEntity human : inventory.getViewers()) {
			if (human instanceof Player) {
				((Player) human).updateInventory();
			}
		}
	}

	protected void drawInventory() {
		Inventory oldInventory = this.inventory;
		this.inventory = PluginMain.getInstance().getServer().createInventory(this, menuLineNum * 9, menuTitle);
		for (HumanEntity human : new ArrayList<HumanEntity>(oldInventory.getViewers())) {
			human.openInventory(this.inventory);
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void onInventoryDrage(InventoryDragEvent event) {
		for (int slot : event.getRawSlots()) {
			if (slot < event.getView().getTopInventory().getSize()) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		int clickSlot = event.getRawSlot();
		boolean affectedButton = false;
		if (clickSlot >= 0 && clickSlot < event.getView().getTopInventory().getSize() && clickSlot < buttons.length) {
			if (event.getView().getPlayer() instanceof Player) {
				Player player = (Player) event.getView().getPlayer();
				ClickType clickType = event.getClick();
				int mouse;
				switch (clickType) {
				case LEFT:
				case SHIFT_LEFT:
					mouse = 0;
					break;
				case RIGHT:
				case SHIFT_RIGHT:
					mouse = 1;
					break;
				case CREATIVE:
				case MIDDLE:
					mouse = 2;
					break;
				default:
					mouse = -1;
				}
				if (mouse != -1) {
					try {
						Button button = buttons[clickSlot];
						if (button != null && button.isEnable()) {
							int x = clickSlot % 9;
							int y = clickSlot / 9;
							buttons[clickSlot].onMousePressed(player, x, y, mouse, clickType.isShiftClick());
							affectedButton = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		InventoryAction action = event.getAction();
		switch (action) {
		case NOTHING:
		case PICKUP_ALL:
		case PICKUP_HALF:
		case PICKUP_ONE:
		case PICKUP_SOME:
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
		case SWAP_WITH_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_SLOT:
		case HOTBAR_SWAP: {
			if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
				event.setCancelled(true);
			}
			break;
		}
		case MOVE_TO_OTHER_INVENTORY:
		case HOTBAR_MOVE_AND_READD: {
			event.setCancelled(true);
			break;
		}
		case COLLECT_TO_CURSOR: {
			event.setCancelled(true);
			if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
				break;
			}
			int allInventorySize = event.getView().getTopInventory().getSize()
				+ event.getView().getBottomInventory().getSize();
			int maxStackSize = event.getCursor().getMaxStackSize();
			int count = event.getCursor().getAmount();
			if (count < maxStackSize) {
				for (int slot = event.getView().getTopInventory().getSize(); slot < allInventorySize; slot++) {
					if (slot != event.getRawSlot()) {
						ItemStack itemStack = event.getView().getItem(slot);
						if (itemStack.isSimilar(event.getCursor()) && itemStack.getAmount() < maxStackSize) {
							if (itemStack.getAmount() + count > maxStackSize) {
								itemStack.setAmount((itemStack.getAmount() + count) - maxStackSize);
								count = maxStackSize;
								event.getView().setItem(slot, itemStack);
								break;
							} else {
								count += itemStack.getAmount();
								event.getView().setItem(slot, null);
							}
						}
					}
				}
				ItemStack cursorItemStack = event.getCursor();
				cursorItemStack.setAmount(count);
				event.getView().setCursor(cursorItemStack);
			}
		}
		case UNKNOWN:
		default: {
			if (affectedButton) {
				PluginMain.getInstance().getLogger()
					.severe("Event cancelled caused by unknown inventory action affected at least one button");
				event.setCancelled(true);
			}
			break;
		}
		}
	}

	public static abstract class Button {
		private MenuInventoryHolder holder;

		private ItemStack icon;
		private boolean isVisible = true;
		private boolean isEnable = true;

		public Button() {

		}

		public Button(ItemStack icon) {
			if (icon != null) {
				this.icon = icon.clone();
			}
		}

		public ItemStack getIcon() {
			if (icon != null) {
				return icon.clone();
			} else {
				return null;
			}
		}

		public void setIcon(ItemStack icon) {
			if (icon != null) {
				this.icon = icon.clone();
			} else {
				this.icon = null;
			}
			updateHolder();
		}

		public boolean isVisible() {
			return isVisible;
		}

		public void setVisible(boolean visible) {
			this.isVisible = visible;
			updateHolder();
		}

		public boolean isEnable() {
			return isEnable;
		}

		public void setEnable(boolean enable) {
			this.isEnable = enable;
		}

		protected void setHolder(MenuInventoryHolder holder) {
			this.holder = holder;
		}

		protected MenuInventoryHolder getHolder() {
			return holder;
		}

		private void updateHolder() {
			if (holder != null) {
				holder.drawButtons();
			}
		}

		public abstract void onMousePressed(Player player, int x, int y, int mouse, boolean isShift);
	}

	public static class CallbackButton extends Button {
		private List<ButtonListener> listenerList = new ArrayList<ButtonListener>();
		private String actionCommand;

		public CallbackButton() {

		}

		public CallbackButton(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		public CallbackButton(ItemStack icon) {
			super(icon);
		}

		public CallbackButton(ItemStack icon, String actionCommand) {
			super(icon);
			this.actionCommand = actionCommand;
		}

		public void registerListener(ButtonListener listener) {
			listenerList.add(listener);
		}

		public void removeListener(ButtonListener listener) {
			listenerList.remove(listener);
		}

		public String getActionCommand() {
			return actionCommand;
		}

		public void setActionCommand(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		@Override
		public void onMousePressed(Player player, int x, int y, int mouse, boolean isShift) {
			for (ButtonListener listener : listenerList) {
				listener.onActionPerformed(new ActionEvent(this, player, x, y, mouse, isShift, getActionCommand()));
			}
		}
	}

	public static class ActionEvent {
		private Button button;
		private Player player;
		private int x;
		private int y;
		private int mouse;
		private boolean isShift;
		private String actionCommand;

		public ActionEvent(Button button, Player player, int x, int y, int mouse, boolean isShift,
			String actionCommand) {
			super();
			this.button = button;
			this.player = player;
			this.x = x;
			this.y = y;
			this.mouse = mouse;
			this.isShift = isShift;
			this.actionCommand = actionCommand;
		}

		public Button getButton() {
			return button;
		}

		public Player getPlayer() {
			return player;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getMouse() {
			return mouse;
		}

		public boolean isShift() {
			return isShift;
		}

		public String getActionCommand() {
			return actionCommand;
		}
	}

	@FunctionalInterface
	public static interface ButtonListener {
		void onActionPerformed(ActionEvent event);
	}
}