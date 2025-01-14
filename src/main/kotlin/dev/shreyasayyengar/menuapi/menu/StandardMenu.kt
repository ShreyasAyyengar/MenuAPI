package dev.shreyasayyengar.menuapi.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Represents a standard menu with a fixed size.
 *
 * @param title The title of the menu.
 * @param size The size of the menu.
 */
class StandardMenu(
    title: Component,
    size: Int,
) : Menu(title, size) {
    private var closeAction: CloseAction? = null
    private val items: MutableMap<Int, MenuItem> = mutableMapOf()
    private val bukkitInventory: Inventory = Bukkit.createInventory(null, size, title)

    companion object {
        /**
         * Copies the given [StandardMenu] instance.
         *
         * @param standardMenu The [StandardMenu] instance to copy.
         * @return An identical [StandardMenu] instance with the same properties as the given [StandardMenu].
         */
        fun copy(standardMenu: StandardMenu): StandardMenu {
            return StandardMenu(standardMenu.title, standardMenu.size).apply {
                cancelClickEventsByDefault = standardMenu.cancelClickEventsByDefault
                overriddenClickAction = standardMenu.overriddenClickAction
                handleMenuItemAction = standardMenu.handleMenuItemAction
                closeAction = standardMenu.closeAction
                items.putAll(standardMenu.items)
            }
        }
    }

    override fun handleClose(event: InventoryCloseEvent) {
        if (this.closeAction == null) return
        this.closeAction!!.performCloseAction(event.player as Player, event.inventory, event)
    }

    override fun getItem(slot: Int): Optional<MenuItem> {
        return if (this.items[slot] == null || bukkitInventory.getItem(slot) == null) Optional.empty()
        else Optional.of(this.items[slot]!!)
    }

    override fun iterator(): Iterator<ItemStack> = this.items.values.map { it.itemStack() }.iterator()

    /**
     * Adds a [MenuItem] to the menu at the specified slot.
     * If an item already exists at the specified slot, it will be replaced.
     *
     * @param slot The slot to add the item to.
     * @param item The [MenuItem] to add.
     * @return This [StandardMenu] instance.
     */
    fun addItem(slot: Int, item: MenuItem) = apply {
        require(slot in 0 until size) { "Item slot must be between 0 and ${size - 1}" }
        this.items[slot] = item
        this.items.forEach { (slot, menuItem) -> bukkitInventory.setItem(slot, menuItem.itemStack()) }
    }

    /**
     * Removes the [MenuItem] at the specified slot.
     *
     * @param slot The slot to remove the item from.
     * @return This [StandardMenu] instance.
     */
    fun removeItem(slot: Int) = apply {
        items.remove(slot)
        bukkitInventory.setItem(slot, null)
    }

    /**
     * Sets the close action for this menu.
     *
     * @param action The action to perform when the menu is closed.
     * @return This [StandardMenu] instance.
     * @see CloseAction
     */
    fun onClose(action: CloseAction) = apply { this.closeAction = action }

    /**
     * Opens this menu for the specified player(s).
     *
     * @param player The player(s) to open the menu for.
     * @see open
     */
    fun open(vararg player: Player) = open(false, *player)

    /**
     * Opens this menu for the specified player(s).
     *
     * @param syncMenus Whether to synchronize the menu for all players.
     * @param player The player(s) to open the menu for.
     */
    fun open(syncMenus: Boolean, vararg player: Player) {
        if (syncMenus || player.size == 1) {
            this.items.forEach { (slot, menuItem) -> bukkitInventory.setItem(slot, menuItem.itemStack()) }
            player.forEach {
                it.openInventory(bukkitInventory)
                MenuAPI.getInstance().openMenus[it.uniqueId] = this
            }
        } else {
            player.forEach {
                val clonedMenu = copy(this)
                clonedMenu.items.forEach { (slot, menuItem) -> clonedMenu.bukkitInventory.setItem(slot, menuItem.itemStack()) }

                it.openInventory(clonedMenu.bukkitInventory)
                MenuAPI.getInstance().openMenus[it.uniqueId] = clonedMenu
            }
        }
    }

    @FunctionalInterface
    fun interface CloseAction {
        fun performCloseAction(whoClosed: Player, inventory: Inventory, event: InventoryCloseEvent)
    }
}
