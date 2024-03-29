package dev.shreyasayyengar.menuapi.action;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface OverriddenInventoryClickAction {
    void performInventoryClickAction(InventoryClickEvent inventoryClickEvent);
}
