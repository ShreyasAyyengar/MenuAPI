package dev.shreyasayyengar.menuapi.action;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Functional interface to define behaviour when a standard menu is closed.
 *
 * @see PaginatedMenuCloseAction
 */
@FunctionalInterface
public interface StandardMenuCloseAction {

    /**
     * Performs an action when a standard menu is closed.
     *
     * @param whoClosed           The {@link org.bukkit.entity.HumanEntity} that closed the menu.
     * @param inventory           The {@link Inventory} that was closed.
     * @param inventoryCloseEvent The instance of the {@link InventoryCloseEvent} that was fired in association with the close.
     */
    void performCloseAction(Player whoClosed, Inventory inventory, InventoryCloseEvent inventoryCloseEvent);
}
