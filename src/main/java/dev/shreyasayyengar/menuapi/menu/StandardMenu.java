package dev.shreyasayyengar.menuapi.menu;

import dev.shreyasayyengar.menuapi.action.StandardMenuCloseAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A standard menu is a menu that is not paginated and has a fixed size. It is the most basic type of menu for
 * bukkit development. They are constructed using a builder pattern.
 */
public class StandardMenu extends Menu<StandardMenu> {

    private final Map<Integer, MenuItem> items = new HashMap<>();
    private StandardMenuCloseAction closeAction;

    /**
     * Adds a MenuItem to the menu at the given slot.
     *
     * @param slot The slot to add the item to.
     * @param item The MenuItem to add.
     * @return the StandardMenu instance.
     */
    public StandardMenu withItem(int slot, MenuItem item) {
        this.items.put(slot, item);
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

    protected void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, this.size, this.title);
        this.items.forEach((slot, item) -> inventory.setItem(slot, item.getItemStack()));

        player.openInventory(inventory);
    }

    /**
     * Handles the closing of the menu.
     *
     * @param event The instance of the {@link InventoryCloseEvent} that was fired when this menu was closed
     * @see Menu#handleClose(InventoryCloseEvent)
     */
    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (this.closeAction == null) return;
        this.closeAction.performCloseAction((Player) event.getPlayer(), event.getInventory(), event);
    }

    /**
     * Returns the {@link MenuItem} at the specified slot of the menu.
     *
     * @param slot The slot to get the {@link MenuItem} from
     * @return An {@link Optional} containing the {@link MenuItem} at the specified slot, or {@link Optional#empty()} if there is no MenuItem at the specified slot
     */
    @Override
    public Optional<MenuItem> getItem(int slot) {
        return Optional.ofNullable(this.items.get(slot));
    }
}