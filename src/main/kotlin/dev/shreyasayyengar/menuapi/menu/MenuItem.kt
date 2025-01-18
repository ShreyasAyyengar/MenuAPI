package dev.shreyasayyengar.menuapi.menu

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.util.*

class MenuItem(
    private var item: ItemStack,
    private var clickAction: MenuItemClickAction? = null,
    private var closeWhenClicked: Boolean = false
) {
    private var meta: ItemMeta = item.itemMeta

    init {
        require(item.type != Material.AIR) { "ItemStack type must not be Material.AIR" }
    }

    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

    private fun updateItemMeta() {
        item.itemMeta = meta
    }

    fun setItemStack(item: ItemStack): MenuItem {
        require(item.type != Material.AIR) { "ItemStack type must not be Material.AIR" }
        this.item = item
        this.meta = item.itemMeta ?: throw IllegalStateException("ItemMeta cannot be null")
        return this
    }

    fun amount(amount: Int): MenuItem {
        item.amount = amount
        return this
    }

    fun name(name: Component): MenuItem {
        meta.displayName(name)
        updateItemMeta()
        return this
    }

    fun lore(vararg lore: Component): MenuItem {
        return lore(lore.toList())
    }

    fun lore(lore: List<Component>): MenuItem {
        meta.lore(lore)
        updateItemMeta()
        return this
    }

    fun addLore(vararg lore: Component): MenuItem {
        return addLore(lore.toList())
    }

    fun addLore(lore: List<Component>): MenuItem {
        val currentLore = meta.lore()?.toMutableList() ?: mutableListOf()
        currentLore.addAll(lore)
        meta.lore(currentLore)
        updateItemMeta()
        return this
    }

    fun removeLore(index: Int): MenuItem {
        val currentLore = meta.lore()?.toMutableList() ?: return this
        currentLore.removeAt(index)
        meta.lore(currentLore)
        updateItemMeta()
        return this
    }

    fun skullTexture(textureURL: String): MenuItem {
        require(item.type == Material.PLAYER_HEAD) { "ItemStack Material must be PLAYER_HEAD to set a skull texture." }
        val skullProfile = Bukkit.createPlayerProfile(UUID.randomUUID())
        val profileTextures = skullProfile.textures
        profileTextures.skin = URI(textureURL).toURL()
        skullProfile.setTextures(profileTextures)

        val skullMeta = meta as SkullMeta
        skullMeta.ownerProfile = skullProfile
        item.itemMeta = skullMeta
        return this
    }

    fun skullOwner(player: OfflinePlayer): MenuItem {
        require(item.type == Material.PLAYER_HEAD) { "ItemStack Material must be PLAYER_HEAD to set a skull owner." }
        val skullMeta = meta as SkullMeta
        skullMeta.owningPlayer = player
        item.itemMeta = skullMeta
        return this
    }

    fun enchantmentGlint(glint: Boolean): MenuItem {
        if (glint) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        } else {
            meta.removeEnchant(Enchantment.UNBREAKING)
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        updateItemMeta()
        return this
    }

    fun closeWhenClicked(close: Boolean) = apply { this.closeWhenClicked = close }

    fun itemMeta(meta: ItemMeta): MenuItem {
        this.meta = meta
        updateItemMeta()
        return this
    }

    fun onClick(clickAction: MenuItemClickAction): MenuItem {
        this.clickAction = clickAction
        return this
    }

    fun itemStack(): ItemStack {
        return item
    }

    fun getMeta(): ItemMeta {
        return meta
    }

    fun getClickAction(): MenuItemClickAction? {
        return clickAction
    }

    fun shouldCloseIfClicked(): Boolean {
        return closeWhenClicked
    }

    @FunctionalInterface
    fun interface MenuItemClickAction {
        fun onClick(whoClicked: Player, itemStack: ItemStack, clickType: ClickType, event: InventoryClickEvent)
    }
}