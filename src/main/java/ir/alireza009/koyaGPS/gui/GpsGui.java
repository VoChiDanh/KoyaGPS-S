package ir.alireza009.koyaGPS.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.task.FastTravelTask;
import ir.alireza009.koyaGPS.task.PlayersTask;
import ir.alireza009.koyaGPS.utils.LangUtils;
import ir.alireza009.koyaGPS.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GpsGui {

    public static void open(Player player) {
        int size = KoyaGPS.getInstance().getConfig().getInt("GUI.Size", 6);
        String title = KoyaGPS.getInstance().getConfig().getString("GUI.Title", "&8KoyaGPS");

        // GUI
        PaginatedGui paginatedGUI = Gui.paginated()
                .title(Component.text(Utils.colorizeWithoutPrefix(title)))
                .rows(size)
                .disableAllInteractions()
                .create();

        // Stop Button
        paginatedGUI.setItem(size, 5, ItemBuilder.from(Material.CONDUIT)
                .setName(ChatColor.translateAlternateColorCodes('&', LangUtils.getMessage("stop_button_name")))
                .asGuiItem(event1 -> {
                    paginatedGUI.disableAllInteractions();
                    Storage.getPlayers().remove(player.getUniqueId());
                    paginatedGUI.close(player);
                    player.playSound(player, Sound.BLOCK_ANVIL_DESTROY, 10, 29);
                }));

        // Border Glass
        GuiItem glass = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .setName(ChatColor.translateAlternateColorCodes('&', "&7"))
                .asGuiItem(event1 -> event1.setCancelled(true));
        paginatedGUI.getFiller().fillBottom(glass);

        // Previous Button
        paginatedGUI.setItem(size, 3, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .setName(ChatColor.translateAlternateColorCodes('&', LangUtils.getMessage("previous_button_name")))
                .asGuiItem(event1 -> {
                    paginatedGUI.previous();
                    paginatedGUI.disableAllInteractions();
                }));

        // Next Button
        paginatedGUI.setItem(size, 7, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                .setName(ChatColor.translateAlternateColorCodes('&', LangUtils.getMessage("next_button_name")))
                .asGuiItem(event1 -> {
                    paginatedGUI.next();
                    paginatedGUI.disableAllInteractions();
                }));

        if (KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations") == null) {
            paginatedGUI.open(player);
            return;
        }

        ConfigurationSection gps = KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations");
        for (String id : gps.getKeys(false)) {
            String locationId = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Location");
            String[] xyz = locationId.split("@");
            Location location = new Location(Bukkit.getWorld(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]), Double.valueOf(xyz[3]));
            if (!location.getWorld().toString().equalsIgnoreCase(player.getLocation().getWorld().toString())) continue;
            String name = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Name", "NULL");
            String icon = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Icon", "COMPASS");
            double distance = player.getLocation().distance(location);
            String cost = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".FastTravelCost", "1");
            double fastTravelCost = 0.0;
            if (cost.startsWith("%distance*")) {
                double perBlockCost = Double.parseDouble(cost.replace("%distance*", "").replace("%", ""));
                fastTravelCost = (perBlockCost * distance);
            } else {
                fastTravelCost = Double.parseDouble(cost);
            }

            Boolean fastTravel = KoyaGPS.getLocationFileManager().getConfig().getBoolean("Locations." + id + ".FastTravel", true);
            List<String> loresList = new ArrayList<String>();
            Boolean globalFastTravel = KoyaGPS.getInstance().getConfig().getBoolean("FastTravel.Enable", true);

            // Add lores from configuration
            loresList.add(Utils.colorizeWithoutPrefix("&r"));
            loresList.add(Utils.colorizeWithoutPrefix(LangUtils.getMessage("location_message")
                    .replace("{X}", String.valueOf(location.getBlockX()))
                    .replace("{Y}", String.valueOf(location.getBlockY()))
                    .replace("{Z}", String.valueOf(location.getBlockZ()))
            ));
            loresList.add(Utils.colorizeWithoutPrefix(LangUtils.getMessage("distance_message")
                    .replace("{Distance}", String.valueOf((int) player.getLocation().distance(location)))
            ));
            loresList.add(Utils.colorizeWithoutPrefix("&r"));
            loresList.add(Utils.colorizeWithoutPrefix(LangUtils.getMessage("left_click_to_track")));
            if (fastTravel && globalFastTravel) {
                loresList.add(Utils.colorizeWithoutPrefix(LangUtils.getMessage("right_click_to_fast_travel")));
                if (fastTravelCost > 0) {
                    loresList.add(Utils.colorizeWithoutPrefix(LangUtils.getMessage("fast_travel_cost")
                            .replace("{Cost}", String.valueOf(fastTravelCost))
                    ));
                }
            }

            double finalFastTravelCost = fastTravelCost;
            GuiItem item = ItemBuilder.from(Material.valueOf(icon))
                    .setName(Utils.colorizeWithoutPrefix("&6&l" + name))
                    .setLore(loresList)
                    .asGuiItem(event1 -> {
                        paginatedGUI.disableAllInteractions();

                        if (event1.isLeftClick()) {
                            player.setCompassTarget(location);
                            Storage.getPlayers().put(player.getUniqueId(), location);
                            PlayersTask.startDestination(player);
                            

                        //if (KoyaGPS.getGPS() != null) KoyaGPS.getGPS().startCompass(player, location);

                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        player.sendMessage(Utils.colorize(
                                LangUtils.getMessage("gps_started")
                                        .replace("{Name}", name)
                                        .replace("{X}", String.valueOf(location.getBlockX()))
                                        .replace("{Y}", String.valueOf(location.getBlockY()))
                                        .replace("{Z}", String.valueOf(location.getBlockZ()))
                        ));
                        player.sendMessage(Utils.colorizeWithoutPrefix(LangUtils.getMessage("follow_compass")));

                        paginatedGUI.close(player);
                        return;
                        }

                        if (event1.isRightClick()) {
                            if (!fastTravel || !globalFastTravel) return;
                            if (finalFastTravelCost > 0) {
                                // Check balance for fast travel
                                if (KoyaGPS.getEconomy().getBalance(player) < finalFastTravelCost) {
                                    player.sendMessage(Utils.colorize(LangUtils.getMessage("insufficient_funds")));
                                    player.closeInventory();
                                    return; // This ensures fast travel doesn't start
                                }

                                KoyaGPS.getEconomy().withdrawPlayer(player, finalFastTravelCost);
                            }

                            FastTravelTask.startFastTravel(player, location);

                            return;
                        }
                    });

            paginatedGUI.addItem(item);
        }
        paginatedGUI.open(player);
    }
}
