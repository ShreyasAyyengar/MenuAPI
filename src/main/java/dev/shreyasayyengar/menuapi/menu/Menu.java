package dev.shreyasayyengar.menuapi.menu;

import dev.shreyasayyengar.menuapi.action.OverriddenInventoryClickAction;
import dev.shreyasayyengar.menuapi.action.PaginatedMenuCloseAction;
import dev.shreyasayyengar.menuapi.action.StandardMenuCloseAction;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;

/**
 * An abstract representation of a menu that can be opened by a player in the game.
 * This class provides the foundational functionality for all types of menus and
 * should be extended by any specific menu implementations that players can interact with.
 *
 * <p>This class follows the builder pattern, which means all configuration methods return
 * the instance of the menu, allowing for method chaining to easily set up the menu.
 *
 * <p>To create a custom menu, extend this class and implement the required methods, particularly
 * {@link #handleClose(InventoryCloseEvent)} and {@link #getItem(int)}.
 *
 * @param <MenuType> The specific type of menu being represented, allowing for method chaining.
 * @see PaginatedMenu
 * @see StandardMenu
 */
@SuppressWarnings({"unchecked", "unused", "UnusedReturnValue"})
public abstract class Menu<MenuType extends Menu<MenuType>> implements Iterable<ItemStack>, Listener {

    protected String title;
    protected int size;
    protected boolean cancelClickEventsByDefault = true, handleMenuItems;
    protected OverriddenInventoryClickAction overriddenInventoryClickAction;

    /**
     * Constructs a new menu with the specified title and size.
     *
     * @param title The title of the menu.
     * @param size  The number of slots in the menu.
     */
    public Menu(String title, int size) {
        this.title = title;
        this.size = size;
    }

    /**
     * When MenuItems are clicked, the instance of the {@link InventoryClickEvent} that was fired is cancelled by default. This
     * produces the effect of the player not being able to interact with the inventory when a MenuItem is clicked. This method
     * allows for that behaviour to be toggled. If a Menu permits for items to be removed, shuffled or interacted with in any way
     * that does not require the event to be cancelled, this method should be used to disable the cancellation of the event.
     * <p><b>Note:</b> MenuItems will still run their onClick actions regardless of whether the event is cancelled or not. To modify that
     * behaviour, see {@link Menu#overrideInventoryClickAction(OverriddenInventoryClickAction, boolean)}.
     *
     * @param cancelByDefault Whether to cancel the InventoryClickEvent by default
     * @return The menu instance
     */
    public MenuType cancelClickEventsByDefault(boolean cancelByDefault) {
        this.cancelClickEventsByDefault = cancelByDefault;
        return (MenuType) this;
    }

    /**
     * When MenuItems are clicked, <b>(and ONLY when MenuItems are clicked)</b> the instance of the {@link InventoryClickEvent} that was fired is passed through to
     * the {@link dev.shreyasayyengar.menuapi.action.MenuItemClickAction}. A primary disadvantage of this is that a developer will not have reference to the
     * event if it was not clicked through a MenuItem. This method allows for the event to completely bypass checks when the initial event is fired, and to
     * provide the developer with the raw Bukkit event. If an InventoryClickEvent happens with this menu, it will be passed here. MenuAPI will not interfere
     * with the event in any way, and the developer will be responsible for handling it as they see fit.
     *
     * @param action          The action to perform when an InventoryClickEvent is fired inside this menu. Note: this could also be in the Player's bottom inventory.
     * @param handleMenuItems If set to true, MenuItems will still run their onClick actions. Disabling this will give the developer maximum control over the event.
     * @return The menu instance
     */
    public MenuType overrideInventoryClickAction(OverriddenInventoryClickAction action, boolean handleMenuItems) {
        this.overriddenInventoryClickAction = action;
        this.handleMenuItems = handleMenuItems;
        return (MenuType) this;
    }

    // ---------- Functionality ---------- //

    /**
     * Invoked when a player closes this menu in-game. This method should be overridden by any menu that
     * requires functionality to be executed when the menu is closed. Most external developers using this API
     * will either choose or not choose to provide this functionality with {@link StandardMenu#onClose(StandardMenuCloseAction)} or
     * {@link PaginatedMenu#onClose(PaginatedMenuCloseAction)} respectively.
     *
     * <p>Implementations like {@link StandardMenu} and {@link PaginatedMenu} have an {@link StandardMenuCloseAction} and {@link PaginatedMenuCloseAction}
     * respectively, which can be used to provide custom functionality when the menu is closed. When the menu is closed, the action will be executed.
     *
     * @param event The instance of the {@link InventoryCloseEvent} that was fired when this menu was closed
     * @see StandardMenu#onClose(StandardMenuCloseAction)
     * @see PaginatedMenu#onClose(PaginatedMenuCloseAction)
     * @see StandardMenuCloseAction
     * @see PaginatedMenuCloseAction
     */
    protected abstract void handleClose(InventoryCloseEvent event);

    /**
     * Gets the {@link MenuItem} at the specified slot in the menu. Implementations of this method depends
     * on the type of menu that is being represented. For example, {@link StandardMenu} will return the
     * {@link MenuItem} at the specified slot in the menu, whereas {@link PaginatedMenu} will return the
     * {@link MenuItem} at the specified slot in the current page of the menu.
     *
     * @param slot The slot to get the {@link MenuItem} from
     * @return An {@link Optional} containing the {@link MenuItem} at the specified slot, or an empty {@link Optional} if no {@link MenuItem} exists at the specified slot
     */
    public abstract Optional<MenuItem> getItem(int slot);

    // --------------- Getters --------------- //
    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    protected OverriddenInventoryClickAction getOverriddenInventoryClickAction() {
        return overriddenInventoryClickAction;
    }

    protected boolean handlesMenuItems() {
        return handleMenuItems;
    }

    @NotNull
    @Override
    public abstract Iterator<ItemStack> iterator();
}
// TODO support all types of BukkitInventories