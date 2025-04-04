package ir.alireza009.koyaGPS.listener;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.gui.GpsGui;
import ir.alireza009.koyaGPS.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK ||
                event.getAction() == Action.LEFT_CLICK_AIR ||
                event.getAction() == Action.PHYSICAL) {
            return;
        }

        String materialName = KoyaGPS.getInstance().getConfig().getString("Item.Item", "COMPASS");
        String itemName = KoyaGPS.getInstance().getConfig().getString("Item.Name", "COMPASS");

        Material material = Material.matchMaterial(materialName);
        if (material == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != material) return;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            if (!displayName.equals(ChatColor.translateAlternateColorCodes('&', itemName))) {
                return;
            }
        } else {
            return;
        }

        GpsGui.open(player);
    }

}
