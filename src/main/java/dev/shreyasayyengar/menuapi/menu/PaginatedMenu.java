package dev.shreyasayyengar.menuapi.menu;

import dev.shreyasayyengar.menuapi.action.PaginatedMenuCloseAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A paginated menu is a menu that has a set amount of slots that can be filled with items. If there are more items than
 * slots, then the menu will automatically create pages for the items to be displayed on. The menu will also automatically
 * create a next and previous page item for the player to navigate through the pages. They are constructed by using a
 * builder pattern.
 */
public class PaginatedMenu extends Menu<PaginatedMenu> {

    private final Map<Integer, MenuItem> allItems = new HashMap<>();
    private final Map<Integer, MenuItem> fixedItems = new HashMap<>();
    private final List<MenuItem> paginatedItems = new ArrayList<>();

    private ItemStack nextPageItem, previousPageItem;

    private final int currentPage = 0; // TODO: for removal?
    private int previousPageSlot = -1;
    private int nextPageSlot = -1;
    private int topLeft, bottomRight;
    private int[] allowedSlots;

    private boolean usePageIndicator;

    private String noAdditionalPages = "There are no additional pages to view";
    private String noPreviousPages = "There are no previous pages to view";
    private String pageIndicator = "Page &e{current_page} &7of &e{max_page}";

    private PaginatedMenuCloseAction closeAction;

    /**
     * Adds specified MenuItems to this menu
     *
     * @param menuItems the MenuItems to add to the menu
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu withItems(MenuItem... menuItems) {
        this.paginatedItems.addAll(Arrays.asList(menuItems));
        return this;
    }

    /**
     * Adds specified MenuItems to this menu
     *
     * @param menuItems the MenuItems as a List of MenuItems to add to the menu
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu withItems(List<MenuItem> menuItems) {
        this.paginatedItems.addAll(menuItems);
        return this;
    }

    /**
     * Adds a 'fixed' item to the menu. A fixed item is an item that will always be in the same slot, regardless of
     * what page the player is on/pagination actions. This is useful for items such as a back button, or a close button.
     *
     * @param slot the slot to place the item in
     * @param item the MenuItem to place in the slot
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu withFixedItem(int slot, MenuItem item) {
        this.fixedItems.put(slot, item);
        return this;
    }

    /**
     * Sets the slots that are allowed to be filled with items. This is useful for when you want to have a border around
     * the menu, or if you want to have a specific layout for the menu. The parameters help create a square perimeter,
     * for which to place items inside.
     *
     * @param topLeft     the top left slot of the perimeter
     * @param bottomRight the bottom right slot of the perimeter
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setAllowedSlots(int topLeft, int bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;

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
     * Sets the explicit slots that are allowed to be filled with items. This may appeal to developers who want to have
     * a more specific layout for their menu. e.g. a diagonal layout or a cross layout.
     *
     * @param slots the slots that are allowed to be filled with items
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setExplicitAllowedSlots(int... slots) {
        this.allowedSlots = slots;
        return this;
    }

    /**
     * Sets the next page ItemStack. This is the item that will be displayed in the next page slot, and will allow the player
     * to navigate to the next page. If this is not set, an IllegalStateException will be thrown if a player tries to open
     * the menu.
     *
     * @param nextPageItem the ItemStack to set as the next page item
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setNextPageItem(ItemStack nextPageItem) {
        this.nextPageItem = nextPageItem;
        return this;
    }

    /**
     * Sets the previous page ItemStack. This is the item that will be displayed in the previous page slot, and will allow the player
     * to navigate to the previous page. If this is not set, an IllegalStateException will be thrown if a player tries to open
     * the menu.
     *
     * @param previousPageItem the ItemStack to set as the previous page item
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setPreviousPageItem(ItemStack previousPageItem) {
        this.previousPageItem = previousPageItem;
        return this;
    }

    /**
     * Sets the slot for the previous page item. By default, this is set to -1. If kept at -1, the value will change to the
     * most bottom left slot of the menu.
     *
     * @param previousPageSlot the slot to place the previous page item in
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setPreviousPageSlot(int previousPageSlot) {
        this.previousPageSlot = previousPageSlot;
        return this;
    }

    /**
     * Sets the slot for the next page item. By default, this is set to -1. If kept at -1, the value will change to the
     * most bottom right slot of the menu.
     *
     * @param nextPageSlot the slot to place the next page item in
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setNextPageSlot(int nextPageSlot) {
        this.nextPageSlot = nextPageSlot;
        return this;
    }

    /**
     * Sets the message to display when a player tries to navigate forward a page, but there are no additional pages.
     *
     * @param noAdditionalPages the message to display
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setNoAdditionalPagesMessage(String noAdditionalPages) {
        this.noAdditionalPages = noAdditionalPages;
        return this;
    }

    /**
     * Sets the message to display when a player tries to navigate back a page, but there are no previous pages.
     *
     * @param noPreviousPages the message to display
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setNoPreviousPagesMessage(String noPreviousPages) {
        this.noPreviousPages = noPreviousPages;
        return this;
    }

    /**
     * Adds a string to the title of the menu to help the player know what page they are on. <b>The string provided
     * must contain {current_page} and {max_page} for replacement</b> Example: PaginatedMenu#setPageIndicator("&7Page &e{current_page} &7of &e{max_page}")
     *
     * @param pageIndicator the string to add to the title
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu setPageIndicator(String pageIndicator) {
        if (!pageIndicator.contains("{current_page") || !pageIndicator.contains("{max_page}")) {
            throw new IllegalArgumentException("Page indicator must contain {current_page} and {max_page} for guidance. Please include these in your String. Example: PaginatedMenu#setPageIndicator(\"&7Page &e{current_page} &7of &e{max_page}\")");
        }
        this.pageIndicator = pageIndicator;
        this.usePageIndicator = true;
        return this;
    }

    /**
     * Sets the action to perform when a player closes the menu.
     *
     * @param closeAction the action to perform
     * @return the PaginatedMenu instance
     */
    public PaginatedMenu onClose(PaginatedMenuCloseAction closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    protected void openMenu(Player player, int pageNumber) {
        AtomicInteger atomicPageNumber = new AtomicInteger(pageNumber);

        if (nextPageItem == null) {
            throw new IllegalStateException("Next page item cannot be null. Please set it using PaginatedMenu#setNextPageItem(ItemStack)");
        }
        if (previousPageItem == null) {
            throw new IllegalStateException("Previous page item cannot be null. Please set it using PaginatedMenu#setPreviousPageItem(ItemStack)");
        }
        if (this.allowedSlots == null || this.allowedSlots.length == 0) {
            throw new IllegalStateException("No allowed slots have been set. Please set them using PaginatedMenu#setAllowedSlots(int, int), or PaginatedMenu#setExplicitAllowedSlots(int...)");
        }

        if (atomicPageNumber.get() <= 0) atomicPageNumber.set(1);

        // Inventory is validated;

        if (this.nextPageSlot == -1) {
            this.nextPageSlot = this.size - 1;
        } // default to last slot of last row. (bottom right)
        if (this.previousPageSlot == -1) {
            this.previousPageSlot = this.size - 9;
        } // default to first slot of last row. (bottom left)

        int itemsPerPage = this.allowedSlots.length;
        int pagesNeeded = (int) Math.ceil((double) this.paginatedItems.size() / itemsPerPage);

        List<MenuItem> itemsForPage = this.paginatedItems.stream()
                .skip((long) this.allowedSlots.length * (atomicPageNumber.get() - 1))
                .limit(this.allowedSlots.length)
                .toList();

        Inventory inventory = Bukkit.createInventory(null, this.size, (this.usePageIndicator ? this.pageIndicator.replace("{current_page}", String.valueOf(atomicPageNumber)).replace("{max_page}", String.valueOf(pagesNeeded)) : this.title));

        // Iterate over the items for the current page
        for (int i = 0; i < itemsForPage.size(); i++) {
            MenuItem menuItem = itemsForPage.get(i);
            // Get the corresponding slot from the allowed slots array
            int slot = this.allowedSlots[i];

            // Set the item in the inventory
            inventory.setItem(slot, menuItem.getItemStack());
            this.allItems.put(slot, menuItem);
        }

        this.fixedItems.put(this.nextPageSlot, new MenuItem(this.nextPageItem).onClick((whoClicked, itemStack, clickType, event) -> {
            if (atomicPageNumber.get() == pagesNeeded) {
                whoClicked.sendMessage(this.noAdditionalPages);
                return;
            }
            MenuManager.getManagerInstance().openPaginatedMenu(player, this, atomicPageNumber.get() + 1);
        }));
        this.fixedItems.put(this.previousPageSlot, new MenuItem(this.previousPageItem).onClick((whoClicked, itemStack, clickType, event) -> {
            if (atomicPageNumber.get() == 1) {
                whoClicked.sendMessage(this.noPreviousPages);
                return;
            }
            MenuManager.getManagerInstance().openPaginatedMenu(player, this, atomicPageNumber.get() - 1);
        }));

        // apply fixed items
        this.fixedItems.forEach((integer, menuItem) -> {
            inventory.setItem(integer, menuItem.getItemStack());
            this.allItems.put(integer, menuItem);
        });

        player.openInventory(inventory);
    }

    /**
     * Handles the closing of the menu.
     *
     * @param event The instance of the {@link InventoryCloseEvent} that was fired when this menu was closed
     * @see Menu#handleClose(InventoryCloseEvent)
     */
    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (this.closeAction == null) return;
        this.closeAction.performCloseAction((Player) event.getPlayer(), event.getInventory(), this.getCurrentPage(), event);
    }

    /**
     * Returns the {@link MenuItem} at the specified slot, <b>dependent</b> on the current page of the menu.
     *
     * @param slot The slot to get the {@link MenuItem} from
     * @return An {@link Optional} containing the {@link MenuItem} at the specified slot, or {@link Optional#empty()} if there is no MenuItem at the specified slot
     */
    @Override
    public Optional<MenuItem> getItem(int slot) {
        return Optional.ofNullable(this.allItems.get(slot));
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return this.allItems.values().stream().map(MenuItem::getItemStack).iterator();
    }

    // --------- Getters ---------

    public String getNoAdditionalPages() {
        return noAdditionalPages;
    }

    public String getNoPreviousPages() {
        return noPreviousPages;
    }

    public String getPageIndicator() {
        return pageIndicator;
    }

    public int getCurrentPage() {
        return currentPage;
    }


}

/* TODO:
    - sort out issues with explicit allowed slots. The items are not being placed in the correct slots when the page is turned.
    - make sure paginated items cannot overlap with fixed items. (maybe add a check to see if the slot is already occupied?)
 */