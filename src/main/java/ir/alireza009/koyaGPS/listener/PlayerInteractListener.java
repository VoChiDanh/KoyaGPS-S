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

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void OnPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) return;
        if (event.getAction() == Action.LEFT_CLICK_AIR) return;
        if (event.getAction() == Action.PHYSICAL) return;
        String materialName = KoyaGPS.getInstance().getConfig().getString("GPS.Item.Material", "COMPASS");
        Material material = Material.matchMaterial(materialName);
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == material) {
            GpsGui.open(player);
        }
    }
}
