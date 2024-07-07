package dev.shreyasayyengar.menuapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager implements Listener {

    private static MenuManager managerInstance;
    private final JavaPlugin providingPlugin;
    private final Map<UUID, Menu<?>> openMenus = new HashMap<>();

    /**
     * Get the instance of the MenuManager
     *
     * @return MenuManager instance
     */
    public static MenuManager getManagerInstance() {
        return managerInstance;
    }

    /**
     * Create a new MenuManager instance
     *
     * @param providingPlugin The plugin providing the MenuManager
     */
    public MenuManager(JavaPlugin providingPlugin) {
        MenuManager.managerInstance = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, this.providingPlugin = providingPlugin);
    }

    public Map<UUID, Menu<?>> getOpenMenus() {
        return openMenus;
    }

    public JavaPlugin getProvidingPlugin() {
        return providingPlugin;
    }

    // ----------------------------------------------------------------------------------------------------- //

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu<?> menu = this.openMenus.remove(player.getUniqueId());

        if (menu != null) menu.handleClose(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Menu<?> menu = this.openMenus.get(player.getUniqueId());

        if (menu == null) return; // no open menu for player

        if (menu.getOverriddenInventoryClickAction() != null) {
            menu.getOverriddenInventoryClickAction().performInventoryClickAction(event);

            if (menu.handlesMenuItems()) {
                handleClick(event, player, menu);
            }
            return;
        }

        if (menu.cancelClickEventsByDefault) event.setCancelled(true);
        handleClick(event, player, menu);
    }

    // handles a MenuItem click if one is present/needed/allowed
    private void handleClick(InventoryClickEvent event, Player player, Menu<?> menu) {
        menu.getItem(event.getRawSlot()).ifPresent(item -> {
            if (item.getClickAction() != null) {
                item.getClickAction().onClick(player, event.getCurrentItem(), event.getClick(), event);
            }

            if (item.shouldCloseIfClicked()) {
                Bukkit.getScheduler().runTaskLater(providingPlugin, player::closeInventory, 1L);
            }
        });
    }
}