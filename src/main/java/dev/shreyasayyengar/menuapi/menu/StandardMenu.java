package dev.shreyasayyengar.menuapi.menu;

import com.google.common.base.Preconditions;
import dev.shreyasayyengar.menuapi.action.StandardMenuCloseAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * A standard menu is a menu that is not paginated and has a fixed size. It is the most basic type of menu for
 * bukkit development. They are constructed using a builder pattern.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class StandardMenu extends Menu<StandardMenu> {

    private final Inventory bukkitInventory;
    private final Map<Integer, MenuItem> items = new HashMap<>();
    private StandardMenuCloseAction closeAction;

    /**
     * Constructs a new menu with the specified title and size.
     *
     * @param title The title of the menu.
     * @param size  The number of slots in the menu.
     */
    public StandardMenu(String title, int size) {
        super(title, size);
        this.bukkitInventory = Bukkit.createInventory(null, size, title);
    }

    private StandardMenu(StandardMenu menu) {
        super(menu.title, menu.size);
        this.bukkitInventory = Bukkit.createInventory(null, this.size, title);
        this.items.putAll(menu.items);
        this.items.forEach((itemSlot, itemMenuItem) -> bukkitInventory.setItem(itemSlot, itemMenuItem.getItemStack()));
        this.closeAction = menu.closeAction;
        this.cancelClickEventsByDefault = menu.cancelClickEventsByDefault;
        this.overriddenInventoryClickAction = menu.overriddenInventoryClickAction;
    }

    public static StandardMenu copy(StandardMenu copyMenu) {
        return new StandardMenu(copyMenu);
    }

    /**
     * Adds a MenuItem to the menu at the given slot.
     *
     * @param slot The slot to add the item to.
     * @param item The MenuItem to add.
     * @return the StandardMenu instance.
     */
    public StandardMenu setItem(int slot, MenuItem item) {
        if (slot == -1) return this; // likely passed through from getNextEmptySlot which returned -1;

        // if slot already contains item, replace it:
        if (this.items.containsKey(slot)) {
            removeItem(slot);
        }

        this.items.put(slot, item);
        this.items.forEach((itemSlot, itemMenuItem) -> bukkitInventory.setItem(slot, item.getItemStack()));
        return this;
    }

    /**
     * Adds a map of items to the menu.
     *
     * @param items The map of items to add to the menu.
     * @return the StandardMenu instance.
     */
    public StandardMenu setItems(Map<Integer, MenuItem> items) {
        this.items.putAll(items);
        this.items.forEach((itemSlot, itemMenuItem) -> bukkitInventory.setItem(itemSlot, itemMenuItem.getItemStack()));
        return this;
    }

    /**
     * Removes an item from the menu at the given slot.
     *
     * @param slot The slot to remove the item from.
     * @return the StandardMenu instance.
     */
    public StandardMenu removeItem(int slot) {
        this.items.remove(slot);
        bukkitInventory.setItem(slot, null);
        return this;
    }

    /**
     * Sets the action to perform when a player closes the menu.
     *
     * @param closeAction The action to perform.
     * @return the StandardMenu instance.
     */
    public StandardMenu onClose(StandardMenuCloseAction closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    /**
     * Opens the menu for the specified players. If syncMenus is true, the physical menu displayed to the client will be synchronized
     * with all players passed into the method. This means any ItemStacks added or removed from the menu will be reflected to every player passed in.
     *
     * @param syncMenus Whether to synchronize the menu with all players
     * @param players   The players to open the menu for
     */
    // TODO check functiona1lity with calling this method for different groups of players (the first menu will have to be a copyOf)
    public void open(boolean syncMenus, Player... players) {
        Preconditions.checkState(this.size != 0, "Menu size must be set or cannot be 0");

        if (syncMenus) {
            this.items.forEach((slot, item) -> bukkitInventory.setItem(slot, item.getItemStack()));
            for (Player player : players) {
                player.openInventory(bukkitInventory);
                MenuManager.getManagerInstance().getOpenMenus().put(player.getUniqueId(), this);
            }
        } else {
            for (Player player : players) {
                StandardMenu menuCopy = StandardMenu.copy(this);
                menuCopy.items.forEach((slot, item) -> menuCopy.bukkitInventory.setItem(slot, item.getItemStack()));
                player.openInventory(menuCopy.bukkitInventory);
                MenuManager.getManagerInstance().getOpenMenus().put(player.getUniqueId(), menuCopy);
            }
        }
    }

    /**
     * Opens the menu for the specified player.
     *
     * @param player The player to open the menu for
     */
    public void open(Player player) {
        open(false, player);
    }

    // --------- Internal ---------

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleClose(InventoryCloseEvent event) {
        if (this.closeAction == null) return;
        this.closeAction.performCloseAction((Player) event.getPlayer(), event.getInventory(), event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MenuItem> getItem(int slot) {

        if (this.items.get(slot) == null || bukkitInventory.getItem(slot) == null) {
            return Optional.empty();
        }

        return Optional.of(this.items.get(slot));
    }

    /**
     * @return The next empty slot in the menu or -1 if there are no empty slots.
     */
    // TODO move this to Menu base class as an abstract method?
    public int getNextEmptySlot() {
        for (int i = 0; i < this.size; i++) {
            if (!this.items.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return this.items.values().stream().map(MenuItem::getItemStack).iterator();
    }
}