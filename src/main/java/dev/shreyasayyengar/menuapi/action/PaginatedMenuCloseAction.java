package dev.shreyasayyengar.menuapi.action;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Functional interface to define behaviour when a paginated menu is closed. This is different
 * to {@link StandardMenuCloseAction} as it provides the page number of the menu that was closed
 * in the event that a developer wants to do something with that information.
 *
 * @see StandardMenuCloseAction
 */
@FunctionalInterface
public interface PaginatedMenuCloseAction {

    /**
     * Performs an action when a paginated menu is closed.
     *
     * @param whoClosed           The {@link org.bukkit.entity.HumanEntity} that closed the menu.
     * @param inventory           The {@link Inventory} that was closed.
     * @param pageNumber          The page number of the menu that was closed.
     * @param inventoryCloseEvent The instance of the {@link InventoryCloseEvent} that was fired in association with the close.
     */
    void performCloseAction(Player whoClosed, Inventory inventory, int pageNumber, InventoryCloseEvent inventoryCloseEvent);
}
