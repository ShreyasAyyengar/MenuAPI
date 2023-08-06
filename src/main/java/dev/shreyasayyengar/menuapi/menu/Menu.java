package dev.shreyasayyengar.menuapi.menu;

import dev.shreyasayyengar.menuapi.action.PaginatedMenuCloseAction;
import dev.shreyasayyengar.menuapi.action.StandardMenuCloseAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Abstract representation of a menu that can be opened by a player. This class provides the base functionality
 * for all menus, and should be extended by any menu that is to be opened by a player. This class follows
 * the builder pattern, and as such, all methods return the instance of the menu that they are called on.
 *
 * @param <MenuType> - The type of menu that is being represented. This is used to allow for method chaining.
 * @see PaginatedMenu
 * @see StandardMenu
 */
@SuppressWarnings("unchecked") // i know what im doing (or at least i think i do)
public abstract class Menu<MenuType extends Menu<MenuType>> implements Iterable<ItemStack> {

    protected String title;
    protected int size;

    /**
     * Sets the title of the menu.
     *
     * @param title The title of the menu
     * @return The menu instance
     */
    public MenuType withTitle(String title) {
        this.title = title;
        return (MenuType) this;
    }

    /**
     * Sets the size of the menu. This method is used to set the number of rows in the menu, and as such, the
     * number of rows must be between 1 and 6 as a Minecraft inventory can only have a maximum of 54 slots, with
     * 9 slots per row.
     *
     * @param rows The number of rows in the menu
     * @return The menu instance
     */
    public MenuType withRows(int rows) {
        if (rows < 1 || rows > 9) {
            throw new IllegalArgumentException("Inventory rows must be between 1 and 9");
        }

        this.size = rows * 9;
        return (MenuType) this;
    }

    /**
     * Sets the size of the menu. This method is used to set the number of slots in the menu, and as such, the
     * number of slots must be between 9 and 54, and must be a multiple of 9 as a Minecraft inventory can only
     * have a maximum of 54 slots, with 9 slots per row.
     *
     * @param size The number of slots in the menu
     * @return The menu instance
     * @throws IllegalArgumentException If the size is not between 9 and 54, or is not a multiple of 9, indicating an invalid inventory size
     */
    public MenuType withSize(int size) {
        if (size < 9 || size > 54) {
            throw new IllegalArgumentException("Inventory size must be between 9 and 54");
        }

        if (size % 9 != 0) {
            throw new IllegalArgumentException("Inventory size must be a multiple of 9");
        }

        this.size = size;
        return (MenuType) this;
    }

    // ---------- Functionality ---------- //

    /**
     * Invoked when a player closes this menu in-game. This method should be overridden by any menu that
     * requires functionality to be executed when the menu is closed. Most external developers using this API
     * will either choose or not choose to provide this functionality with {@link StandardMenu#onClose(StandardMenuCloseAction)} or
     * {@link PaginatedMenu#onClose(PaginatedMenuCloseAction)} respectively.
     *
     * @param event The instance of the {@link InventoryCloseEvent} that was fired when this menu was closed
     */
    public abstract void handleClose(InventoryCloseEvent event);

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
}