package dev.shreyasayyengar.menuapi.action;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * See {@link dev.shreyasayyengar.menuapi.menu.PaginatedMenu#overrideInventoryClickAction(OverriddenInventoryClickAction, boolean)}
 */
public interface OverriddenInventoryClickAction {
    void performInventoryClickAction(InventoryClickEvent inventoryClickEvent);
}
