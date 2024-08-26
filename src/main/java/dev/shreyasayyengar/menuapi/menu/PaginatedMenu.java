package dev.shreyasayyengar.menuapi.menu;

import com.google.common.base.Preconditions;
import dev.shreyasayyengar.menuapi.action.MenuItemClickAction;
import dev.shreyasayyengar.menuapi.action.PaginatedMenuCloseAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a paginated menu that dynamically manages multiple pages of items.
 * If the number of items exceeds the available slots, the menu automatically creates additional pages.
 * The menu also provides navigation items for moving between pages and uses the builder pattern for configuration.
 *
 * <p>Key features:
 * <ul>
 *     <li>Automatic pagination</li>
 *     <li>Customizable navigation items for next and previous pages</li>
 *     <li>Fixed items that remain constant across all pages</li>
 *     <li>Configurable slot boundaries for paginated items</li>
 *     <li>Support for closing actions when the menu is closed</li>
 * </ul>
 *
 * @see Menu
 * @see PaginatedMenuCloseAction
 * @see dev.shreyasayyengar.menuapi.action.MenuItemClickAction
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class PaginatedMenu extends Menu<PaginatedMenu> {
    private final Map<Integer, MenuItem> finalDisplayItems = new HashMap<>();
    private final Map<Integer, MenuItem> fixedItems = new HashMap<>();
    private final List<MenuItem> paginatedItems = new ArrayList<>();
    private final String pageIndicator = "Page " + ChatColor.YELLOW + "{current_page} &7of " + ChatColor.YELLOW + "{total_pages}";
    private Inventory baseBukkitInventory;
    private MenuItem nextPageItem, previousPageItem;
    private PaginatedMenuCloseAction closeAction;
    private int previousPageSlot = -1;
    private int nextPageSlot = -1;
    private int[] allowedSlots;
    private boolean usePageIndicator;
    private int currentPageNumber = 0;
    private String noAdditionalPages = ChatColor.RED + "There are no additional pages to view";
    private String noPreviousPages = ChatColor.RED + "There are no previous pages to view";

    /**
     * Constructs a new menu with the specified title and size.
     *
     * @param title The title of the menu.
     * @param size  The number of slots in the menu.
     */
    public PaginatedMenu(String title, int size) {
        super(title, size);
    }

    /**
     * Adds a fixed item to the menu. A fixed item remains in the same slot across all pages.
     * Useful for items close buttons, or general static buttons.
     * <p>
     * <b>Important: Fixed items always have priority</b> over paginated items. If a fixed item is placed in a slot that is also
     * occupied by a paginated item, that slot will be removed from the allowed paginated slots.
     *
     * @param slot The slot to place the item.
     * @param item The MenuItem to place in the slot.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setFixedItem(int slot, MenuItem item) {
        this.fixedItems.put(slot, item);
        return this;
    }

    /**
     * Adds the specified MenuItems to this menu as paginated items.
     *
     * @param menuItems The MenuItems to add.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setPaginatedItems(MenuItem... menuItems) {
        this.paginatedItems.addAll(Arrays.asList(menuItems));
        return this;
    }

    /**
     * Adds the specified MenuItems to this menu as paginated items.
     *
     * @param menuItems The MenuItems to add as a list.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setPaginatedItems(List<MenuItem> menuItems) {
        this.paginatedItems.addAll(menuItems);
        return this;
    }

    /**
     * Sets the slots that can be filled with items, defining a rectangular area within the menu.
     * Useful for creating borders or specific layouts.
     *
     * @param topLeft     The top-left slot of the area.
     * @param bottomRight The bottom-right slot of the area.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setAllowedSlots(int topLeft, int bottomRight) {
        // calculate rows and columns for slots
        int startRow = topLeft / 9;
        int endRow = bottomRight / 9;
        int startColumn = topLeft % 9;
        int endColumn = bottomRight % 9;

        // calculate the number of slots
        int numRows = endRow - startRow + 1;
        int numColumns = endColumn - startColumn + 1;

        int[] slots = new int[numRows * numColumns];

        int index = 0;
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startColumn; j <= endColumn; j++) {
                slots[index] = i * 9 + j;
                index++;
            }
        }

        this.allowedSlots = slots;
        return this;
    }

    /**
     * Sets the range of slots that can be filled with items, allowing for a more specific layout.
     * This does not take any rectangular shape, and instead fills all slots from startSlot to endSlot.
     *
     * @param startSlot The starting slot.
     * @param endSlot   The ending slot.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setAllowedSlotsRange(int startSlot, int endSlot) {
        Preconditions.checkArgument(startSlot >= 0 && endSlot < this.size, "Start and endSlot must be within the bounds of the inventory size");
        Preconditions.checkArgument(startSlot < endSlot, "Start must be less than endSlot");
        this.allowedSlots = new int[endSlot - startSlot + 1];
        for (int i = startSlot; i <= endSlot; i++) {
            this.allowedSlots[i - startSlot] = i;
        }
        return this;
    }

    /**
     * Sets the explicit slots that can be filled with items, allowing for custom layouts. This may appeal to developers who want to have
     * a more specific layout for their menu. e.g. a diagonal layout or a cross layout.
     *
     * @param slots the slots that are allowed to be filled with items.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setExplicitAllowedSlots(int... slots) {
        this.allowedSlots = slots;
        return this;
    }

    /**
     * Sets the item used to navigate to the next page.
     *
     * @param nextPageItem The item to set as the next page item.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setNextPageItem(int slot, MenuItem nextPageItem) {
        this.nextPageSlot = slot;
        this.nextPageItem = nextPageItem;
        return this;
    }

    /**
     * Sets the item used to navigate to the previous page.
     *
     * @param previousPageItem The item to set as the previous page item.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setPreviousPageItem(int slot, MenuItem previousPageItem) {
        this.previousPageSlot = slot;
        this.previousPageItem = previousPageItem;
        return this;
    }

    /**
     * Sets the slot for the next page item. By default, this is set to -1. If kept at -1, an exception will be thrown.
     *
     * @param nextPageSlot the slot to place the next page item in
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setNextPageSlot(int nextPageSlot) {
        Preconditions.checkState(nextPageSlot >= 0 && nextPageSlot < this.size, "Next page slot must be a valid slot in the inventory");
        this.nextPageSlot = nextPageSlot;
        return this;
    }

    /**
     * Sets the slot for the previous page item. By default, this is set to -1. If kept at -1, an exception will be thrown.
     *
     * @param previousPageSlot the slot to place the previous page item in
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setPreviousPageSlot(int previousPageSlot) {
        Preconditions.checkState(previousPageSlot >= 0 && previousPageSlot < this.size, "Previous page slot must be a valid slot in the inventory");
        this.previousPageSlot = previousPageSlot;
        return this;
    }

    /**
     * Sets the message displayed when there are no additional pages to navigate to.
     *
     * @param noAdditionalPages The message to display.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setNoAdditionalPagesMessage(String noAdditionalPages) {
        this.noAdditionalPages = noAdditionalPages;
        return this;
    }

    /**
     * Sets the message displayed when there are no previous pages to navigate to.
     *
     * @param noPreviousPages The message to display.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu setNoPreviousPagesMessage(String noPreviousPages) {
        this.noPreviousPages = noPreviousPages;
        return this;
    }

    /**
     * Adds a page indicator to the title of the menu. <b>The provided string must contain
     * {current_page} and {total_pages} for replacement.</b>
     *
     * @param pageIndicator The string to add to the title.
     * @return The PaginatedMenu instance.
     * @apiNote This method is internal due to Bukkit's InventoryView#setTitle being added in version 1.20+.
     */
    @ApiStatus.Internal
    public PaginatedMenu setPageIndicator(String pageIndicator) {
//        if (!pageIndicator.contains("{current_page}") || !pageIndicator.contains("{total_pages}")) {
//            throw new IllegalArgumentException("Page indicator must contain {current_page} and {max_page} for guidance. Please include these in your String. Example: PaginatedMenu#setPageIndicator(\"&7Page &e{current_page} &7of &e{total_pages}\")");
//        }
//        this.pageIndicator = pageIndicator;
//        this.usePageIndicator = true;
        return this;
    }

    /**
     * Sets the action to perform when the menu is closed.
     *
     * @param closeAction The action to perform.
     * @return The PaginatedMenu instance.
     */
    public PaginatedMenu onClose(PaginatedMenuCloseAction closeAction) {
        this.closeAction = closeAction;
        return this;
    }


    public void open(Player player, int pageNumber) {
        if (previousPageItem == null) {
            setPreviousPageItem(this.previousPageSlot, new MenuItem(Material.PLAYER_HEAD)
                    .setName(ChatColor.YELLOW + "Previous Page")
                    .setSkullTexture("81c96a5c3d13c3199183e1bc7f086f54ca2a6527126303ac8e25d63e16b64ccf")
                    .onClick((whoClicked, itemStack, clickType, event) -> whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F))
            );
        }
        if (nextPageItem == null) {
            setNextPageItem(this.nextPageSlot, new MenuItem(Material.PLAYER_HEAD)
                    .setName(ChatColor.YELLOW + "Next Page")
                    .setSkullTexture("333ae8de7ed079e38d2c82dd42b74cfcbd94b3480348dbb5ecd93da8b81015e3")
                    .onClick((whoClicked, itemStack, clickType, event) -> whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F))
            );
        }

        Preconditions.checkState(this.previousPageSlot >= 0, "Previous page slot must be a valid slot in the inventory");
        Preconditions.checkState(this.nextPageSlot >= 0, "Next page slot must be a valid slot in the inventory");
        Preconditions.checkState(this.allowedSlots != null, "Allowed slots cannot be null. Please set them using setAllowedSlots, setAllowedSlotsRange, or setExplicitAllowedSlots");
        Preconditions.checkState(this.allowedSlots.length != 0, "Allowed slots cannot be empty. Please set them using setAllowedSlots, setAllowedSlotsRange, or setExplicitAllowedSlots");

        // Previous Page Item
        MenuItemClickAction previousPageOriginalClickAction = this.previousPageItem.getClickAction();
        this.previousPageItem.onClick((whoClicked, itemStack, clickType, event) -> {
            // Call user-defined action first
            if (previousPageOriginalClickAction != null) {
                previousPageOriginalClickAction.onClick(whoClicked, itemStack, clickType, event);
            }

            prepareSpecificPage(whoClicked, this.currentPageNumber - 1);
        });
        this.previousPageItem.closeWhenClicked(false); // override, menu cannot close here

        // Next Page Item
        MenuItemClickAction nextPageOriginalClickAction = this.nextPageItem.getClickAction();
        this.nextPageItem.onClick((whoClicked, itemStack, clickType, event) -> {

            // Call user-defined action first
            if (nextPageOriginalClickAction != null) {
                nextPageOriginalClickAction.onClick(whoClicked, itemStack, clickType, event);
            }

            prepareSpecificPage(whoClicked, this.currentPageNumber + 1);
        });
        this.nextPageItem.closeWhenClicked(false); // override, menu cannot close here

        // PaginatedMenu is validated;

        baseBukkitInventory = Bukkit.createInventory(null, this.size, getFormattedTitle());

        prepareSpecificPage(player, pageNumber);

        player.openInventory(baseBukkitInventory);
        MenuManager.getManagerInstance().getOpenMenus().put(player.getUniqueId(), this);
    }

    // --------- Internal ---------

    private void prepareSpecificPage(Player player, int pageNumber) {
        if (pageNumber < 0 || pageNumber > getMaxPages()) {
            player.sendMessage(pageNumber < 0 ? noPreviousPages : noAdditionalPages);
            return;
        }

        currentPageNumber = pageNumber;

        this.finalDisplayItems.clear();
        baseBukkitInventory.clear();

        updateFixedItems();
        this.fixedItems.keySet().forEach(fixedSlot -> {
            this.allowedSlots = Arrays.stream(this.allowedSlots)
                    .filter(s -> s != fixedSlot)
                    .toArray();
        });
        updatePaginatedItems();

        this.finalDisplayItems.forEach((integer, menuItem) -> baseBukkitInventory.setItem(integer, (menuItem == null) ? null : menuItem.getItemStack()));
    }

    private void updateFixedItems() {
        this.fixedItems.put(this.previousPageSlot, currentPageNumber == 0 ? null : this.previousPageItem);
        this.fixedItems.put(this.nextPageSlot, currentPageNumber == getMaxPages() ? null : this.nextPageItem);
        this.finalDisplayItems.putAll(this.fixedItems);
    }

    private void updatePaginatedItems() {
        List<MenuItem> itemsOnPage = getItemsForPage(currentPageNumber);

        for (int i = 0; i < itemsOnPage.size(); i++) {
            MenuItem menuItem = itemsOnPage.get(i);
            int slot = this.allowedSlots[i];

            // Set the item in the inventory
            this.finalDisplayItems.put(slot, menuItem);
        }
    }

    private int getMaxPages() { // zero-indexed
        return ((int) Math.ceil((double) paginatedItems.size() / allowedSlots.length)) - 1;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    private List<MenuItem> getItemsForPage(int page) {
        int start = page * allowedSlots.length;
        int end = Math.min(start + allowedSlots.length, paginatedItems.size());
        return paginatedItems.subList(start, end);
    }

    private String getFormattedTitle() {
        return this.usePageIndicator ? this.title + this.pageIndicator.replace("{current_page}", String.valueOf(currentPageNumber + 1)).replace("{total_pages}", String.valueOf(getMaxPages())) : this.title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleClose(InventoryCloseEvent event) {
        if (this.closeAction == null) return;
        this.closeAction.performCloseAction((Player) event.getPlayer(), event.getInventory(), this.currentPageNumber, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MenuItem> getItem(int slot) {
        return Optional.ofNullable(this.finalDisplayItems.get(slot));
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return this.finalDisplayItems.values().stream().map(MenuItem::getItemStack).iterator();
    }
}
// TODO page indicator very flimsy/find different impl for page indicator.