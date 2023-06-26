package dev.shreyasayyengar.menuapi.menu;

import dev.shreyasayyengar.menuapi.action.MenuItemClickAction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an item in a menu. This class is used to create items for any instances of {@link Menu}.
 * They are constructed using the builder pattern.
 */
public class MenuItem {

    private ItemStack item;
    private ItemMeta meta;
    private MenuItemClickAction clickAction;

    /**
     * Constructs a blank MenuItem with no ItemStack associated.
     */
    public MenuItem() {
    }

    /**
     * Constructs a MenuItem with the given ItemStack.
     *
     * @param stack The ItemStack for this MenuItem.
     */
    public MenuItem(ItemStack stack) {
        this.item = stack;
        this.meta = stack.getItemMeta();
    }

    /**
     * Constructs a MenuItem with the given Material. The ItemStack will default to a quantity of 1.
     *
     * @param material The Material for this MenuItem.
     */
    public MenuItem(Material material) {
        this(new ItemStack(material, 1));
    }

    /**
     * Constructs a MenuItem with the given Material and quantity.
     *
     * @param material The Material for this MenuItem.
     * @param amount   The quantity of this MenuItem.
     */
    public MenuItem(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    /**
     * Updates the ItemMeta for this MenuItem. Called whenever a change is made to the ItemMeta.
     */
    private void updateItemMeta() {
        this.item.setItemMeta(this.meta);
    }

    /**
     * Sets the ItemStack for this MenuItem.
     *
     * @param item The ItemStack for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withItemStack(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
        return this;
    }

    /**
     * Sets the amount of this MenuItem.
     *
     * @param amount The amount of this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Sets the name of this MenuItem. Calls {@link #updateItemMeta()}.
     *
     * @param name The name for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withName(String name) {
        this.meta.setDisplayName(name);
        updateItemMeta();
        return this;
    }

    /**
     * Sets the lore of this MenuItem. Calls {@link #updateItemMeta()}.
     *
     * @param lore The lore for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withLore(String... lore) {
        return this.withLore(Arrays.asList(lore));
    }

    /**
     * Sets the lore of this MenuItem. Calls {@link #updateItemMeta()}.
     *
     * @param lore The lore as a List of strings for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withLore(List<String> lore) {
        this.meta.setLore(lore);
        updateItemMeta();
        return this;
    }

    /**
     * Sets the ItemMeta for this MenuItem. Calls {@link #updateItemMeta()}.
     * External developers may choose to use this method to set custom ItemMeta directly.
     *
     * @param meta The ItemMeta for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem withItemMeta(ItemMeta meta) {
        this.meta = meta;
        updateItemMeta();
        return this;
    }

    /**
     * Sets the intended behaviour for this MenuItem when clicked through a {@link MenuItemClickAction}.
     *
     * @param clickAction The MenuItemClickAction for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem onClick(MenuItemClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    // -------- Getters -------- //

    public ItemStack getItemStack() {
        return item;
    }

    public ItemMeta getMeta() {
        return meta;
    }

    public MenuItemClickAction getClickAction() {
        return clickAction;
    }
}