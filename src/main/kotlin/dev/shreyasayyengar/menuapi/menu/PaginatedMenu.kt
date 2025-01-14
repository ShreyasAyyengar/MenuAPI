package dev.shreyasayyengar.menuapi.menu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

/**
 * Represents a paginated menu that dynamically manages and displays items based on the current page. If
 * the number of items exceeds the number of slots in the Minecraft inventory, the menu creates additional
 * pages.
 *
 * Key features:
 * - Automatic pagination
 * - Customisable navigation items for next and previous pages.
 * - Fixed items that remain constant/static across all pages.
 * - Configurable slot boundaries for paginated items.
 *
 * @param title The title of the menu.
 * @param size The size of the menu.
 */
class PaginatedMenu(
    title: Component,
    size: Int,
) : Menu(title, size) {
    private var fixedItems: MutableMap<Int, MenuItem> = mutableMapOf()
    private var paginatedItems: MutableList<MenuItem> = mutableListOf()
    private var closeAction: CloseAction? = null
    private var previousPageItem: MenuItem = MenuItem(Material.PLAYER_HEAD)
        .name(Component.text("Previous Page", NamedTextColor.YELLOW))
        .skullTexture("81c96a5c3d13c3199183e1bc7f086f54ca2a6527126303ac8e25d63e16b64ccf")
        .onClick { whoClicked, itemStack, clickType, event -> whoClicked.playSound(whoClicked.location, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F) }
    private var nextPageItem: MenuItem = MenuItem(Material.PLAYER_HEAD)
        .name(Component.text("Next Page", NamedTextColor.YELLOW))
        .skullTexture("333ae8de7ed079e38d2c82dd42b74cfcbd94b3480348dbb5ecd93da8b81015e3")
        .onClick { whoClicked, itemStack, clickType, event -> whoClicked.playSound(whoClicked.location, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F) }
    private var previousPageSlot: Int = -1
    private var nextPageSlot: Int = -1
    private var allowedSlots: IntArray? = null
    private var currentPage: Int = 0
    private var noAdditionalPages: Component = Component.text("There are no additional pages to view", NamedTextColor.RED)
    private var noPreviousPages: Component = Component.text("There are no previous pages to view", NamedTextColor.RED)
    private var tempFinalItems: MutableMap<Int, MenuItem> = mutableMapOf()
    private val bukkitInventory: Inventory = Bukkit.createInventory(null, size, title)

    override fun handleClose(event: InventoryCloseEvent) {
        this.closeAction?.performCloseAction(event.player as Player, event.inventory, currentPage, event)
    }

    override fun getItem(slot: Int): Optional<MenuItem> = Optional.ofNullable(tempFinalItems[slot])

    override fun iterator(): Iterator<ItemStack> = this.tempFinalItems.values.map { it.itemStack() }.iterator()

    /**
     * Adds a fixed [MenuItem] to the menu at the specified slot. Fixed items remain constant across all pages.
     * If an item already exists at the specified slot, it will be replaced.
     *
     * @param slot The slot to add the item to.
     * @param item The [MenuItem] to add.
     * @return This [PaginatedMenu] instance.
     */
    fun addFixedItem(slot: Int, item: MenuItem) = apply {
        require(slot in 0 until size) { "Slot must be within the inventory size (given $slot, size $size)" }
        this.fixedItems[slot] = item
    }

    /**
     * Adds multiple fixed [MenuItem] instances to the menu at the specified slots. Fixed items remain constant across all pages.
     * If an item already exists at the specified slot, it will be replaced.
     *
     * @param items A map of slot to [MenuItem] instances to add.
     * @return This [PaginatedMenu] instance.
     */
    fun addFixedItems(items: Map<Int, MenuItem>) = apply {
        items.keys.forEach { require(it in 0 until size) { "Slot must be within the inventory size (given $it, size $size)" } }
        this.fixedItems.putAll(items)
    }

    /**
     * Removes a fixed [MenuItem] from the menu at the specified slot.
     *
     * @param slot The slot to remove the item from.
     * @return This [PaginatedMenu] instance.
     */
    fun removeFixedItem(slot: Int) = apply { this.fixedItems.remove(slot) }

    /**
     * Adds a paginated [MenuItem] to the menu.
     *
     * @param item The [MenuItem] to add.
     * @return This [PaginatedMenu] instance.
     */
    fun addPaginatedItem(item: MenuItem) = apply { this.paginatedItems.add(item) }

    /**
     * Adds multiple paginated [MenuItem] instances to the menu.
     *
     * @param items A collection of [MenuItem] instances to add.
     * @return This [PaginatedMenu] instance.
     */
    fun addPaginatedItems(vararg items: MenuItem) = apply { this.paginatedItems.addAll(items) }

    /**
     * Adds multiple paginated [MenuItem] instances to the menu.
     *
     * @param items A collection of [MenuItem] instances to add.
     * @return This [PaginatedMenu] instance.
     */
    fun addPaginatedItems(items: Collection<MenuItem>) = apply { this.paginatedItems.addAll(items) }

    /**
     * Sets the slots that can be filled with paginated items, defining a rectangular area within the menu.
     * Useful for creating borders or specific layouts.
     *
     * @param topLeft The top left slot of the rectangular area.
     * @param bottomRight The bottom right slot of the rectangular area.
     * @return This [PaginatedMenu] instance.
     */
    fun allowedSlots(topLeft: Int, bottomRight: Int) = apply {
        require(topLeft < bottomRight) { "Top left slot must be less than bottom right slot" }
        require(topLeft in 0 until size) { "Top left slot must be within the inventory size (given $topLeft, size $size)" }
        require(bottomRight in 0 until size) { "Bottom right slot must be within the inventory size (given $bottomRight, size $size)" }
        // Calculate rows and columns for slots
        val startRow = topLeft / 9
        val endRow = bottomRight / 9
        val startColumn = topLeft % 9
        val endColumn = bottomRight % 9

        // Calculate the number of slots
        val numRows = endRow - startRow + 1
        val numColumns = endColumn - startColumn + 1

        val slots = IntArray(numRows * numColumns)

        var index = 0
        for (i in startRow..endRow) {
            for (j in startColumn..endColumn) {
                slots[index] = i * 9 + j
                index++
            }
        }

        this.allowedSlots = slots
    }

    /**
     * Sets the slots that can be filled with paginated items, defining a range of slots.
     *
     * @param startSlot The start slot of the range.
     * @param endSlot The end slot of the range.
     * @return This [PaginatedMenu] instance.
     */
    fun allowedSlotsDiscreteRange(startSlot: Int, endSlot: Int) = apply {
        require(startSlot < endSlot) { "Start slot must be less than end slot" }
        require(startSlot in 0 until size) { "Start slot must be within the inventory size (given $startSlot, size $size)" }
        require(endSlot in 0 until size) { "End slot must be within the inventory size (given $endSlot, size $size)" }
        this.allowedSlots = (startSlot..endSlot).toList().toIntArray()
    }

    /**
     * Sets the explicit slots that can be filled with paginated items, allowing for custom layouts.
     * May appeal to developers who want to have a more specific layout for their menu. (e.g. a diagonal layout
     * or a cross layout).
     *
     * @param slots The slots that can be filled with paginated items.
     * @return This [PaginatedMenu] instance.
     */
    fun explicitAllowedSlots(vararg slots: Int) = apply {
        slots.forEach { require(it in 0 until size) { "Slot must be within the inventory size (given $it, size $size)" } }
        this.allowedSlots = slots
    }

    /**
     * Sets the item used to navigate to the previous page.
     *
     * @param slot The slot to place the item in.
     * @param item The [MenuItem] to use as the previous page item.
     * @return This [PaginatedMenu] instance.
     */
    fun previousPageItem(slot: Int, item: MenuItem) = apply {
        require(slot in 0 until size) { "Slot must be within the inventory size (given $slot, size $size)" }
        this.previousPageItem = item
        this.previousPageSlot = slot
    }

    /**
     * Sets the item used to navigate to the next page.
     *
     * @param slot The slot to place the item in.
     * @param item The [MenuItem] to use as the next page item.
     * @return This [PaginatedMenu] instance.
     */
    fun nextPageItem(slot: Int, item: MenuItem) = apply {
        require(slot in 0 until size) { "Slot must be within the inventory size (given $slot, size $size)" }
        this.nextPageItem = item
        this.nextPageSlot = slot
    }

    /**
     * Sets the slot for the previous page item.
     *
     * @param slot The slot to place the previous page item in.
     * @return This [PaginatedMenu] instance.
     */
    fun previousPageSlot(slot: Int) = apply {
        require(slot in 0 until size) { "Slot must be within the inventory size (given $slot, size $size)" }
        this.previousPageSlot = slot
    }

    /**
     * Sets the slot for the next page item.
     *
     * @param slot The slot to place the next page item in.
     * @return This [PaginatedMenu] instance.
     */
    fun nextPageSlot(slot: Int) = apply {
        require(slot in 0 until size) { "Slot must be within the inventory size (given $slot, size $size)" }
        this.nextPageSlot = slot
    }

    /**
     * Sets the message to display when there are no previous pages to view.
     *
     * @param message The message to display.
     * @return This [PaginatedMenu] instance.
     */
    fun noPreviousPages(message: Component) = apply { this.noPreviousPages = message }

    /**
     * Sets the message to display when there are no additional pages to view.
     *
     * @param message The message to display.
     * @return This [PaginatedMenu] instance.
     */
    fun noAdditionalPages(message: Component) = apply { this.noAdditionalPages = message }

    /**
     * Sets the action to perform when the menu is closed.
     *
     * @param action The action to perform when the menu is closed.
     * @return This [PaginatedMenu] instance.
     */
    fun onClose(action: CloseAction) = apply { this.closeAction = action }

    /**
     * Opens this paginated menu for the specified player.
     *
     * @param player The player to open the menu for.
     * @param pageNumber The page number to open the menu at. (zero-indexed)
     *
     */
    fun open(player: Player, pageNumber: Int = 0) {
        require(previousPageSlot > -1) { "Previous page slot must be set or be greater than -1" }
        require(nextPageSlot > -1) { "Next page slot must be set or be greater than -1" }
        require(allowedSlots != null) { "Allowed slots must be set" }
        require(previousPageSlot !in (allowedSlots ?: IntArray(0))) { "Previous page slot cannot be in the paginating slots" }
        require(nextPageSlot !in (allowedSlots ?: IntArray(0))) { "Next page slot cannot be in the paginating slots" }
        require(fixedItems.keys.intersect(allowedSlots?.toList() ?: emptyList()).isEmpty()) { "Fixed slots cannot overlap with paginating slots" }

        val previousPageOriginalAction = this.previousPageItem.getClickAction()
        this.previousPageItem.onClick { whoClicked, itemStack, clickType, event ->
            previousPageOriginalAction?.onClick(whoClicked, itemStack, clickType, event) // run cosmetic actions

            prepareSpecificPage(whoClicked, this.currentPage - 1)
        }
        this.previousPageItem.closeWhenClicked(false) // override, menu cannot close when paginating

        val nextPageOriginalAction = this.nextPageItem.getClickAction()
        this.nextPageItem.onClick { whoClicked, itemStack, clickType, event ->
            nextPageOriginalAction?.onClick(whoClicked, itemStack, clickType, event) // run cosmetic actions

            prepareSpecificPage(whoClicked, this.currentPage + 1)
        }
        this.nextPageItem.closeWhenClicked(false) // override, menu cannot close when paginating

        prepareSpecificPage(player, pageNumber)
        player.openInventory(bukkitInventory)
        MenuAPI.getInstance().openMenus[player.uniqueId] = this
    }

    private fun prepareSpecificPage(player: Player, pageNumber: Int) {
        println("requested page: $pageNumber")
        println("max pages: ${maxPages()}")
        println("current page: $currentPage")
        println("-------")
        if (pageNumber < 0 || pageNumber > maxPages()) {
            player.sendMessage(if (pageNumber < 0) noPreviousPages else noAdditionalPages)
            return
        }

        currentPage = pageNumber

        this.tempFinalItems.clear()
        bukkitInventory.clear()

        updateFixedItems()
        this.fixedItems.keys.forEach { fixedSlot ->
            this.allowedSlots = Arrays.stream(allowedSlots).filter { it != fixedSlot }.toArray()
        }
        updatePaginatedItems()

        this.tempFinalItems.forEach { (slot, menuItem) -> bukkitInventory.setItem(slot, menuItem.itemStack()) }
    }

    private fun updateFixedItems() {
        this.tempFinalItems.put(this.previousPageSlot, this.previousPageItem)
        this.tempFinalItems.put(this.nextPageSlot, this.nextPageItem)
        this.tempFinalItems.putAll(this.fixedItems)
    }

    private fun updatePaginatedItems() {
        val itemsOnPage = getItemsForPage(currentPage)

        for (i in itemsOnPage.indices) {
            val menuItem = itemsOnPage[i]
            val slot = allowedSlots?.get(i) ?: continue

            // Set the item in the inventory
            tempFinalItems[slot] = menuItem
        }
    }

    private fun getItemsForPage(page: Int): List<MenuItem> {
        val start = page * allowedSlots!!.size
        val end = min(start + allowedSlots!!.size, paginatedItems.size)
        return paginatedItems.subList(start, end.coerceAtMost(paginatedItems.size))
    }

    private fun maxPages() = (ceil(paginatedItems.size.toDouble() / allowedSlots!!.size).toInt()) - 1

    @FunctionalInterface
    fun interface CloseAction {
        fun performCloseAction(whoClosed: Player, inventory: Inventory, pageNumber: Int, event: InventoryCloseEvent)
    }
}