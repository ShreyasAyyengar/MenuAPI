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
        Bukkit.getServer().getPluginManager().registerEvents(this, providingPlugin);
    }

    /**
     * Open an instance of {@link StandardMenu} for a player
     *
     * @param player The player to open the menu for
     * @param menu   The menu to open
     */
    public void openStandardMenu(Player player, StandardMenu menu) {
        if (menu.getSize() == 0) {
            throw new IllegalStateException("Menu size must be set or cannot be 0");
        }

        menu.openMenu(player);
        this.openMenus.put(player.getUniqueId(), menu);
    }

    public void openStandardMenu(StandardMenu menu, Player... players) {
        if (menu.getSize() == 0) {
            throw new IllegalStateException("Menu size must be set or cannot be 0");
        }

        menu.openMenu(players);

        for (Player player : players) {
            this.openMenus.put(player.getUniqueId(), menu);
        }
    }

    /**
     * Open an instance of {@link PaginatedMenu} for a player
     *
     * @param player       The player to open the menu for
     * @param menu         The menu to open
     * @param startingPage The page to start the menu on
     */
    public void openPaginatedMenu(Player player, PaginatedMenu menu, int startingPage) {
        if (menu.getSize() == 0) {
            throw new IllegalStateException("Menu size must be set or cannot be 0");
        }

        menu.openMenu(player, startingPage);
        this.openMenus.put(player.getUniqueId(), menu);
    }

    public Map<UUID, Menu<?>> getOpenMenus() {
        return openMenus;
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
                menu.getItem(event.getRawSlot()).ifPresent(item -> {
                    if (item.getClickAction() != null) {
                        item.getClickAction().onClick(player, event.getCurrentItem(), event.getClick(), event);
                    }
                });
            }

            return;
        }

        if (menu.cancelClickEventsByDefault) event.setCancelled(true);
        menu.getItem(event.getRawSlot()).ifPresent(item -> {
            if (item.getClickAction() != null) {
                item.getClickAction().onClick(player, event.getCurrentItem(), event.getClick(), event);
            }
        });
    }
}