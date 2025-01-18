package dev.shreyasayyengar.menuapi.menu

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * An abstract representation of a menu that can be opened as a Bukkit [org.bukkit.inventory.Inventory].
 *
 * @param title The title of the menu.
 * @param size The size of the menu.
 */
abstract class Menu<T : Menu<T>>(
    var title: Component = Component.empty(),
    var size: Int = 54,
) : Iterable<ItemStack> {
    internal var cancelClickEventsByDefault: Boolean = true
    internal var handleMenuItemAction: Boolean = false
    internal var overriddenClickAction: OverriddenInventoryClickAction? = null

    /**
     * Invoked when a player closes this menu in-game. Provided implementations like
     * [StandardMenu] and [PaginatedMenu] have delegated close actions respectively.
     *
     * @param event The [InventoryCloseEvent] instance fired when this menu was closed.
     */
    abstract fun handleClose(event: InventoryCloseEvent)

    /**
     * Retrieves the [MenuItem] at the specified slot in the menu. Implementations of this
     * method largly depend on the type of menu. For example, [StandardMenu] will return the
     * MenuItem at the specified slot, while [PaginatedMenu] will return the MenuItem at the
     * specified slot in the current page of the menu.
     *
     * @param slot The slot in the Bukkit [org.bukkit.inventory.Inventory] representation to retrieve the MenuItem from.
     */
    abstract fun getItem(slot: Int): Optional<MenuItem>

    abstract override fun iterator(): Iterator<ItemStack>

    /**
     * Sets whether to cancel [InventoryClickEvent] instances by default when a player clicks on a MenuItem.
     * To prevent players from mutating the Bukkit Inventory directly, the instance of [InventoryClickEvent] fired
     * when a player clicks on a MenuItem is cancelled by default.
     */
    @Suppress("UNCHECKED_CAST")
    fun cancelClickEventsByDefault(cancelClickEventsByDefault: Boolean): T {
        this.cancelClickEventsByDefault = cancelClickEventsByDefault
        return this as T
    }

    /**
     * Overrides the default event action for this menu.
     *
     * If overridden, the developer will be responsible for handling the [InventoryClickEvent] instance MenuAPI
     * will not interfere with the event, but for the exception to handling MenuItem actions.
     *
     * @param handleMenuItems If set to true, MenuItems will still run their onClick actions. Disabling this
     * gives the developer maximum control over the event.
     * @param overriddenInventoryClickAction The action to perform when [InventoryClickEvent] is fired. (**Note**:
     * the inventory could be the player's bottom inventory, not the top menu inventory).
     */
    @Suppress("UNCHECKED_CAST")
    fun overrideClickAction(handleMenuItems: Boolean, overriddenInventoryClickAction: OverriddenInventoryClickAction): T {
        overriddenClickAction = overriddenInventoryClickAction
        handleMenuItemAction = handleMenuItems
        return this as T
    }

    @FunctionalInterface
    fun interface OverriddenInventoryClickAction {
        fun performClickAction(event: InventoryClickEvent)
    }
}