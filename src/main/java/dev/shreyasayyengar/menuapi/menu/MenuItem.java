package dev.shreyasayyengar.menuapi.menu;

import com.google.common.base.Preconditions;
import dev.shreyasayyengar.menuapi.action.MenuItemClickAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The MenuItem class wraps functionality around a Bukkit {@link ItemStack}, as an item in a menu with customizable properties and actions.
 * It provides methods to modify the stack’s metadata, such as name, lore, and custom textures,
 * and allows setting a click action that defines the behavior when the item is clicked.
 *
 * <p>This class supports setting and updating various attributes of the item, such as:
 * <ul>
 *     <li>Name</li>
 *     <li>Lore</li>
 *     <li>Skull texture for PLAYER_HEAD items</li>
 *     <li>Enchantment glint effect</li>
 * </ul>
 *
 * <p>Additionally, the class allows developers to define custom actions through the {@link MenuItemClickAction} interface.
 *
 * <p>Example usage:
 * <pre>
 *     MenuItem menuItem = new MenuItem(Material.DIAMOND_SWORD)
 *         .setName("Excalibur")
 *         .setLore("Legendary Sword")
 *         .setEnchantmentGlint(true)
 *         .onClick((whoClicked, itemStack, clickType, event) -> {
 *             // custom click action
 *         });
 * </pre>
 */
@SuppressWarnings("unused")
public class MenuItem {

    private ItemStack item;
    private ItemMeta meta;
    private MenuItemClickAction clickAction;
    private boolean closeWhenClicked;

    /**
     * Constructs a MenuItem with the given ItemStack.
     *
     * @param stack The ItemStack for this MenuItem.
     */
    public MenuItem(ItemStack stack) {
        Preconditions.checkState(stack.getType() != Material.AIR, "ItemStack type must not be Material.AIR");

        this.item = stack;
        this.meta = stack.getItemMeta();
    }

    /**
     * Constructs a MenuItem with the given Material. The ItemStack will default to an amount of 1.
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
    public MenuItem setItemStack(ItemStack item) {
        Preconditions.checkState(item.getType() != Material.AIR, "ItemStack type must not be Material.AIR");

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
    public MenuItem setAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Sets the name of this MenuItem.
     *
     * @param name The name for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem setName(String name) {
        this.meta.setDisplayName(name);
        updateItemMeta();
        return this;
    }

    /**
     * Sets the lore of this MenuItem.
     *
     * @param lore The lore for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem setLore(String... lore) {
        return this.setLore(Arrays.asList(lore));
    }

    /**
     * Sets the lore of this MenuItem.
     *
     * @param lore The lore as a List of strings for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem setLore(List<String> lore) {
        this.meta.setLore(lore);
        updateItemMeta();
        return this;
    }

    /**
     * Adds new lore to the existing lore of this MenuItem.
     *
     * @param lore The lore to add to this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem addLore(String... lore) {
        return addLore(Arrays.asList(lore));
    }

    /**
     * Adds new lore to the existing lore of this MenuItem.
     *
     * @param lore The lore as a List of strings to add to this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem addLore(List<String> lore) {
        List<String> currentLores = this.meta.getLore();
        currentLores.addAll(lore);
        this.meta.setLore(currentLores);
        updateItemMeta();
        return this;
    }

    /**
     * Deletes a line of lore from the existing lore of this MenuItem.
     *
     * @param index The index of the lore line to delete.
     * @return This MenuItem.
     */
    public MenuItem deleteLore(int index) {
        List<String> currentLores = this.meta.getLore();
        currentLores.remove(index);
        this.meta.setLore(currentLores);
        updateItemMeta();
        return this;
    }

    /**
     * Sets the texture of this MenuItem to a custom skull texture.
     *
     * @param textureURL The URL of the texture for this profile skin. Sites like <a href="https://minecraft-heads.com/">Minecraft Heads</a> can be used to find textures.
     * @return This MenuItem.
     */
    public MenuItem setSkullTexture(String textureURL) {
        Preconditions.checkState(this.item.getType() == Material.PLAYER_HEAD, "ItemStack Material must be a PLAYER_HEAD to set a skull texture.");

        PlayerProfile skullProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures profileTextures = skullProfile.getTextures();
        try {
            profileTextures.setSkin(new URL("http://textures.minecraft.net/texture/" + textureURL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        skullProfile.setTextures(profileTextures);

        SkullMeta modifiedMeta = (SkullMeta) meta;
        modifiedMeta.setOwnerProfile(skullProfile);
        item.setItemMeta(modifiedMeta);

        return this;
    }

    /**
     * Sets the skull owner of this MenuItem.
     *
     * @param player The OfflinePlayer to set as the skull owner for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem setSkullOwner(OfflinePlayer player) {
        Preconditions.checkState(this.item.getType() == Material.PLAYER_HEAD, "ItemStack Material must be a PLAYER_HEAD to set a skull owner.");

        SkullMeta modifiedMeta = (SkullMeta) meta;
        modifiedMeta.setOwningPlayer(player);
        item.setItemMeta(modifiedMeta);

        return this;
    }

    /**
     * Adds an enchantment glint texture to this MenuItem.
     *
     * @return This MenuItem.
     */
    public MenuItem setEnchantmentGlint(boolean glint) {
        if (glint) {
            this.meta.addEnchant(Enchantment.DURABILITY, 1, true);
            this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            this.meta.removeEnchant(Enchantment.DURABILITY);
            this.meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        updateItemMeta();
        return this;
    }

    /**
     * Sets whether the Menu containing this MenuItem should close when the item is clicked.
     * When set to true, the Menu will automatically close upon clicking this MenuItem,
     * while any additional actions defined in the {@link MenuItemClickAction} will still be executed.
     * <p>
     * Default value: false
     * <p>
     * Example usage:
     * <pre>
     *     MenuItem closeMenuItem = new MenuItem(Material.BARRIER)
     *             .setName("Close Menu!")
     *             .setEnchantmentGlint(true)
     *             .closeWhenClicked(true);
     * </pre>
     * This method eliminates the need to manually close the Menu from within the click action,
     * avoiding the potential issues associated with unsafe calls:
     * <pre>
     *     MenuItem closeMenuItem = new MenuItem(Material.BARRIER)
     *         .setName("Close Menu!")
     *         .setEnchantmentGlint(true)
     *         .onClick((whoClicked, itemStack, clickType, event) -> {
     *             whoClicked.sendMessage("You closed the menu!");
     *             whoClicked.closeInventory(); <b>// UNSAFE</b>
     *         });
     * </pre>
     * <b>Note:</b> Bukkit explicitly states that the following methods should never be invoked by
     * an EventHandler for InventoryClickEvent using the HumanEntity or InventoryView associated
     * with the event:
     * <ul>
     *     <li>HumanEntity.closeInventory()</li>
     *     <li>HumanEntity.openInventory(Inventory)</li>
     *     <li>HumanEntity.openWorkbench(Location, boolean)</li>
     *     <li>HumanEntity.openEnchanting(Location, boolean)</li>
     *     <li>InventoryView.close()</li>
     * </ul>
     * The MenuAPI handles the closing of the Menu in a safe manner using Bukkit's Scheduler.
     *
     * @param close Whether the Menu should close when this MenuItem is clicked.
     * @return This MenuItem.
     */
    public MenuItem closeWhenClicked(boolean close) {
        this.closeWhenClicked = close;
        return this;
    }

    /**
     * Sets the ItemMeta for this MenuItem.
     * External developers may choose to use this method to set custom ItemMeta directly.
     *
     * @param meta The ItemMeta for this MenuItem.
     * @return This MenuItem.
     */
    public MenuItem setItemMeta(ItemMeta meta) {
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

    protected MenuItemClickAction getClickAction() {
        return clickAction;
    }

    protected boolean shouldCloseIfClicked() {
        return closeWhenClicked;
    }
}
// TODO - add support for MenuItems within player inventories.