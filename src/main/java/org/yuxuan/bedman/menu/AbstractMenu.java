package org.yuxuan.bedman.menu;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.yuxuan.bedman.PluginMain;
import org.yuxuan.bedman.inventory.MenuInventoryHolder;
import org.yuxuan.bedman.inventory.MenuInventoryHolder.ButtonListener;
import org.yuxuan.bedman.inventory.MenuInventoryHolder.CallbackButton;

public abstract class AbstractMenu {
	private boolean isRedrawing;
	private MenuInventoryHolder holder;

	public AbstractMenu() {
		this("Menu", 9, 6);
	}

	public AbstractMenu(String title) {
		this(title, 9, 6);
	}

	public AbstractMenu(String title, int xSize, int ySize) {
		if (xSize != 9) {
			throw new IndexOutOfBoundsException("xSize must equals to 9");
		}
		if (ySize < 0) {
			throw new IndexOutOfBoundsException("x must greater than or equal to 0");
		}
		holder = new AbstractMenuHolder(ySize, title);
	}

	public String getTitle() {
		return holder.getMenuTitle();
	}

	public void setTitle(String title) {
		isRedrawing = true;
		holder.setMenuTitle(title);
		isRedrawing = false;
	}

	public int getXSize() {
		return 9;
	}

	public void setXSize(int xSize) {
		if (xSize != 9) {
			throw new IndexOutOfBoundsException("xSize must equals to 9");
		}
	}

	public int getYSize() {
		return holder.getMenuLineNum();
	}

	protected MenuInventoryHolder getHolder() {
		return holder;
	}

	public void setYSize(int ySize) {
		if (ySize < 0) {
			throw new IndexOutOfBoundsException("x must greater than or equal to 0");
		}
		isRedrawing = true;
		holder.setMenuLineNum(ySize);
		isRedrawing = false;
	}

	public Graphics getGraphics() {
		return new Graphics();
	}

	public void setButton(int x, int y, ItemStack icon) {
		setButton(x, y, icon, null);
	}

	public void setButton(int x, int y, ItemStack icon, ButtonListener listener) {
		CallbackButton button = new CallbackButton(icon);
		if (listener != null)
			button.registerListener(listener);
		holder.setButton(button, x, y);
	}

	public boolean addButton(int x, int y, ItemStack icon) {
		return this.addButton(x, y, icon, null);
	}

	public boolean addButton(int x, int y, ItemStack icon, ButtonListener listener) {
		if (holder.getButton(x, y) != null) {
			return false;
		}
		CallbackButton button = new CallbackButton(icon);
		if (listener != null)
			button.registerListener(listener);
		holder.setButton(button, x, y);
		return true;
	}

	public boolean removeButton(int x, int y) {
		if (holder.getButton(x, y) == null) {
			return false;
		}
		holder.setButton(null, x, y);
		return true;
	}

	public void clearButtons() {
		holder.clearButtons();
	}

	public ItemStack getButtonIcon(int x, int y) {
		if (holder.getButton(x, y) == null) {
			return null;
		}
		return holder.getButton(x, y).getIcon();
	}

	public boolean setButtonIcon(int x, int y, ItemStack icon) {
		if (holder.getButton(x, y) == null) {
			return false;
		}
		holder.getButton(x, y).setIcon(icon);
		return true;
	}

	protected boolean onMenuOpening(Player player) {
		return true;
	}

	protected void onMenuClosing(Player player) {

	}

	protected void onMenuOpened(Player player) {

	}

	protected void onMenuClosed(Player player) {

	}

	private class AbstractMenuHolder extends MenuInventoryHolder {
		public AbstractMenuHolder(int menuLineNum, String menuTitle) {
			super(menuLineNum, menuTitle);
		}

		@Override
		public void onInventoryOpen(final InventoryOpenEvent event) {
			if (isRedrawing) {
				return;
			}
			if (!AbstractMenu.this.onMenuOpening((Player) event.getPlayer())) {
				event.setCancelled(true);
			} else {
				PluginMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(
					PluginMain.getInstance(), () -> onMenuOpened((Player) event.getPlayer()));
			}
		}

		@Override
		public void onInventoryClose(final InventoryCloseEvent event) {
			if (isRedrawing) {
				return;
			}
			AbstractMenu.this.onMenuClosing((Player) event.getPlayer());
			PluginMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(
				PluginMain.getInstance(), () -> onMenuClosed((Player) event.getPlayer()));
		}
	}

	public class Graphics {
		private final int offsetX;
		private final int offsetY;
		private final boolean hasClipBounds;
		private final int clipX;
		private final int clipY;
		private final int clipWidth;
		private final int clipHeight;
		private BiFunction<Integer, Integer, ItemStack> icon;

		private Graphics() {
			this.offsetX = 0;
			this.offsetY = 0;
			this.hasClipBounds = false;
			this.clipX = 0;
			this.clipY = 0;
			this.clipWidth = 0;
			this.clipHeight = 0;
			this.icon = null;
		}

		private Graphics(int offsetX, int offsetY, boolean hasClipBounds, int clipX, int clipY, int clipWidth,
			int clipHeight, BiFunction<Integer, Integer, ItemStack> icon) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.hasClipBounds = hasClipBounds;
			this.clipX = clipX;
			this.clipY = clipY;
			this.clipWidth = clipWidth;
			this.clipHeight = clipHeight;
			this.icon = icon;
		}

		public Graphics create() {
			return new Graphics(offsetX, offsetY, hasClipBounds, clipX, clipY, clipWidth, clipHeight, icon);
		}

		public Graphics translate(int x, int y) {
			return new Graphics(offsetX + x, offsetY + y, hasClipBounds, clipX, clipY, clipWidth, clipHeight, icon);
		}

		public Graphics setClip(int x, int y, int width, int height) {
			return new Graphics(offsetX, offsetY, true, x, y, width, height, icon);
		}

		public Graphics clearClip() {
			return new Graphics(offsetX, offsetY, false, 0, 0, 0, 0, icon);
		}

		public Graphics setIcon(ItemStack icon) {
			this.icon = (x, y) -> icon.clone();
			return this;
		}

		public Graphics setIcon(BiFunction<Integer, Integer, ItemStack> icon) {
			this.icon = icon;
			return this;
		}

		public Graphics drawDot(int x, int y) {
			if (icon == null)
				return this;
			int buttonX = offsetX + x;
			int buttonY = offsetY + y;
			if (isValid(buttonX, buttonY))
				setButton(buttonX, buttonY, icon.apply(buttonX, buttonY));
			return this;
		}

		public Graphics drawHorizontalLine(int x, int y, int length) {
			if (icon == null)
				return this;
			for (int i = 0; i < length; i++) {
				int buttonX = offsetX + x + i;
				int buttonY = offsetY + y;
				if (isValid(buttonX, buttonY))
					setButton(buttonX, buttonY, icon.apply(buttonX, buttonY));
			}
			return this;
		}

		public Graphics drawVerticalLine(int x, int y, int length) {
			if (icon == null)
				return this;
			for (int i = 0; i < length; i++) {
				int buttonX = offsetX + x;
				int buttonY = offsetY + y + i;
				if (isValid(buttonX, buttonY))
					setButton(buttonX, buttonY, icon.apply(buttonX, buttonY));
			}
			return this;
		}

		public Graphics drawRect(int x, int y, int width, int height) {
			if (icon == null)
				return this;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int buttonX = offsetX + x + i;
					int buttonY = offsetY + y + j;
					if (isValid(buttonX, buttonY))
						setButton(buttonX, buttonY, icon.apply(buttonX, buttonY));
				}
			}
			return this;
		}

		private boolean isValid(int globalX, int globalY) {
			return isInMenu(globalX, globalY) && isInClip(globalX, globalY);
		}

		private boolean isInClip(int globalX, int globalY) {
			int localX = globalX - offsetX;
			int localY = globalY - offsetY;
			return localX >= clipX && localX < clipX + clipWidth && localY >= clipY && localY < clipY + clipHeight;
		}

		private boolean isInMenu(int globalX, int globalY) {
			return globalX >= 0 && globalX < getXSize() && globalY >= 0 && globalY < getYSize();
		}
	}
}