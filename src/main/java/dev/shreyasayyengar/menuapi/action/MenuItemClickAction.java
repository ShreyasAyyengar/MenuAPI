package dev.shreyasayyengar.menuapi.action;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A functional method that is invoked when a player clicks on a {@link dev.shreyasayyengar.menuapi.menu.MenuItem}
 */
@FunctionalInterface
public interface MenuItemClickAction {

    /**
     * Invoked when a player clicks on a {@link dev.shreyasayyengar.menuapi.menu.MenuItem}
     *
     * @param whoClicked The {@link org.bukkit.entity.HumanEntity} that clicked on the MenuItem
     * @param itemStack  The Bukkit {@link ItemStack} of the item clicked.
     * @param clickType  The {@link ClickType} of the click.
     * @param event      The instance of the {@link InventoryClickEvent} that was fired in association with this click.
     */
    void onClick(Player whoClicked, ItemStack itemStack, ClickType clickType, InventoryClickEvent event);
}
