package dev.shreyasayyengar.menuapi.menu

import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.twilight
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class MenuAPI private constructor(providingPlugin: JavaPlugin) : Listener {
    val openMenus = mutableMapOf<UUID, Menu>()

    init {
        twilight(providingPlugin)

        event<InventoryClickEvent> {
            val player = whoClicked as Player
            val menu = openMenus[player.uniqueId] ?: return@event

            if (menu.overriddenClickAction != null) {
                menu.overriddenClickAction!!.performClickAction(this)

                if (menu.handleMenuItemAction) handleClick(this, player, menu)
            } else {
                if (menu.cancelClickEventsByDefault) isCancelled = true
                handleClick(this, player, menu)
            }
        }

        event<InventoryCloseEvent> {
            val player = player
            val menu = openMenus.remove(player.uniqueId) ?: return@event

            menu.handleClose(this)
        }
    }

    private fun handleClick(event: InventoryClickEvent, player: Player, menu: Menu) {
        menu.getItem(event.rawSlot).ifPresent { menuItem ->
            if (menuItem.getClickAction() != null) {
                menuItem.getClickAction()!!.onClick(player, menuItem.itemStack(), event.click, event)
            }

            if (menuItem.shouldCloseIfClicked()) delay(1) { player.closeInventory() }
        }
    }

    companion object {
        private var instance: MenuAPI? = null

        fun getInstance(): MenuAPI {
            if (instance == null) throw IllegalStateException("MenuAPI has not been initialized yet! Call MenuAPI.initialize(JavaPlugin) first!")
            else return instance!!
        }

        fun initialize(providingPlugin: JavaPlugin) {
            if (instance != null) throw IllegalStateException("MenuAPI has already been initialized!")
            instance = MenuAPI(providingPlugin)
        }
    }
}