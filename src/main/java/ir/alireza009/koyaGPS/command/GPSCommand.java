package ir.alireza009.koyaGPS.command;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.gui.GpsGui;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.task.FastTravelTask;
import ir.alireza009.koyaGPS.task.PlayersTask;
import ir.alireza009.koyaGPS.utils.LangUtils;
import ir.alireza009.koyaGPS.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GPSCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        if (args.length < 1) {
            Player player = (Player) sender;
            player.sendMessage(Utils.colorizeWithoutPrefix("&8[&9KoyaGPS&8] &9» " + LangUtils.getMessage("gps_help_title")));
            player.sendMessage(LangUtils.getMessage("gps_help_add"));
            player.sendMessage(LangUtils.getMessage("gps_help_delete"));
            player.sendMessage(LangUtils.getMessage("gps_help_style"));
            player.sendMessage(LangUtils.getMessage("gps_help_set_icon"));
            player.sendMessage(LangUtils.getMessage("gps_help_fast_travel"));
            player.sendMessage(LangUtils.getMessage("gps_help_fast_travel_cost"));
            player.sendMessage(LangUtils.getMessage("gps_help_forceOpen"));
            player.sendMessage(LangUtils.getMessage("gps_help_forceTrack"));
            player.sendMessage(LangUtils.getMessage("gps_help_forceEnd"));
            player.sendMessage(LangUtils.getMessage("gps_help_forceFastTravel"));
            player.sendMessage(LangUtils.getMessage("gps_help_give"));
            player.sendMessage(LangUtils.getMessage("gps_help_reload"));
            return false;
        }

        if (args[0].equalsIgnoreCase("style")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colorize("&cThis command can only be used by players!"));
                return true;
            }

            Player player = (Player) sender;
            String configPath = KoyaGPS.getInstance().getConfig().getBoolean("CurveModel")
                    ? "ArrowChainCurve.Styles"
                    : "ArrowChain.Styles";

            ConfigurationSection styles = KoyaGPS.getInstance().getConfig().getConfigurationSection(configPath);

            // If no argument provided, list available styles
            if (args.length < 2) {
                if (styles != null) {
                    player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_style_list_header")));
                    for (String style : styles.getKeys(false)) {
                        if (player.hasPermission("gps.style." + style)) {
                            player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_style_list_format").replace("{Style}", style)));
                        }
                    }
                }
                return true;
            }

            String style = args[1].toLowerCase();

            // Validate the style existence in the config
            if (styles == null || !styles.getKeys(false).contains(style)) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_style_invalid")));
                return true;
            }

            // Check if the player has permission
            if (!player.hasPermission("gps.style." + style)) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return true;
            }

            // Apply the selected style
            PlayersTask.setArrowStyle(player, style);
            player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_style_changed").replace("{Style}", style)));

            return true;
        }


        if (args[0].equalsIgnoreCase("reload")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            KoyaGPS.getLocationFileManager().reloadConfig();
            KoyaGPS.getPlayersFileManager().reloadConfig();
            KoyaGPS.getLangFileManager().reloadConfig();
            KoyaGPS.getInstance().reloadConfig();
            player.sendMessage(Utils.colorize(LangUtils.getMessage("plugin_reloaded")));
            return false;
        }

        if (args[0].equalsIgnoreCase("forceFastTravel")) {
            if (!sender.hasPermission("gps.admin")) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 3) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceFastTravel_usage")));
                return false;
            }

            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            String target = args[2];

            if (Bukkit.getPlayer(target) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceFastTravel_playerNotFound")));
                return false;
            }

            Player PlayerObj = Bukkit.getPlayer(target);
            String locationId = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Location");
            String[] xyz = locationId.split("@");
            Location location = new Location(Bukkit.getWorld(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]), Double.valueOf(xyz[3]));
            FastTravelTask.startFastTravel(PlayerObj, location);

            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceFastTravel_success")));
            return false;
        }

        if (args[0].equalsIgnoreCase("forceTrack")) {
            if (!sender.hasPermission("gps.admin")) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 3) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceTrack_usage")));
                return false;
            }

            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            String target = args[2];

            if (Bukkit.getPlayer(target) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceTrack_playerNotFound")));
                return false;
            }

            Player PlayerObj = Bukkit.getPlayer(target);
            String locationId = KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Location");
            String[] xyz = locationId.split("@");
            Location location = new Location(Bukkit.getWorld(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]), Double.valueOf(xyz[3]));
            String name = Utils.colorize(KoyaGPS.getLocationFileManager().getConfig().getString("Locations." + id + ".Name", "NULL"));
            PlayerObj.setCompassTarget(location);
            Storage.getPlayers().put(PlayerObj.getUniqueId(), location);
            PlayersTask.startDestination(PlayerObj);

            PlayerObj.playSound(PlayerObj, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            PlayerObj.sendMessage(Utils.colorize(
                    LangUtils.getMessage("gps_started")
                            .replace("{Name}", name)
                            .replace("{X}", String.valueOf(location.getBlockX()))
                            .replace("{Y}", String.valueOf(location.getBlockY()))
                            .replace("{Z}", String.valueOf(location.getBlockZ()))
            ));
            PlayerObj.sendMessage(Utils.colorizeWithoutPrefix(LangUtils.getMessage("follow_compass")));

            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceTrack_success")));
            return false;
        }

        if (args[0].equalsIgnoreCase("forceEnd")) {
            if (!sender.hasPermission("gps.admin")) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceEnd_usage")));
                return false;
            }

            String plyerToOpen = args[1];

            if (Bukkit.getPlayer(plyerToOpen) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceEnd_playerNotFound")));
                return false;
            }

            Player PlayerObj = Bukkit.getPlayer(plyerToOpen);
            Storage.getPlayers().remove(PlayerObj.getUniqueId());
            PlayerObj.playSound(PlayerObj, Sound.BLOCK_ANVIL_DESTROY, 10, 29);

            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceEnd_success")));
            return false;
        }

        if (args[0].equalsIgnoreCase("forceOpen")) {
            if (!sender.hasPermission("gps.admin")) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceOpen_usage")));
                return false;
            }

            String plyerToOpen = args[1];

            if (Bukkit.getPlayer(plyerToOpen) == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceOpen_playerNotFound")));
                return false;
            }

            Player PlayerObj = Bukkit.getPlayer(plyerToOpen);
            GpsGui.open(PlayerObj);

            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_forceOpen_success")));
            return false;
        }

        if (args[0].equalsIgnoreCase("Delete")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_delete_usage")));
                return false;
            }

            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id, null);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("location_deleted").replace("{Name}", id)));
            return false;
        }

        if (args[0].equalsIgnoreCase("SetIcon")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_set_icon_usage")));
                return false;
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_item_in_hand")));
                return false;
            }

            String icon = String.valueOf(player.getInventory().getItemInMainHand().getType());
            String id = args[1];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Icon", icon);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("icon_changed")));
            return false;
        }

        if (args[0].equalsIgnoreCase("Add")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_add_usage")));
                return false;
            }

            StringBuilder name = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                name.append(args[i]).append(" ");
            }
            if (name.length() > 0) {
                name.setLength(name.length() - 1);
            }

            String id = name.toString().replace(" ", "-");

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) != null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_exists")));
                return false;
            }

            String icon = "COMPASS";
            Location location = player.getLocation();
            String locationId = location.getWorld().getName() + "@" + location.getBlockX() + "@" + location.getBlockY() + "@" + location.getBlockZ();

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Name", name.toString());
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Icon", icon);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravel", true);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravelCost", 0);
            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Location", locationId);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("location_added")
                    .replace("{Name}", name.toString())
                    .replace("{X}", String.valueOf(location.getBlockX()))
                    .replace("{Y}", String.valueOf(location.getBlockY()))
                    .replace("{Z}", String.valueOf(location.getBlockZ()))
            ));
            return false;
        }

        if (args[0].equalsIgnoreCase("SetName")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_set_name")));
                return false;
            }

            String id = args[1].replace(" ", "-");

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) != null) {
                String displayName = String.join(" ", Arrays.asList(args).subList(2, args.length));
                KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".Name", displayName);
                KoyaGPS.getLocationFileManager().save();
                player.sendMessage(Utils.colorize(LangUtils.getMessage("set_gps_name")
                        .replace("{Name}", id)
                        .replace("{DisplayName}", displayName)));
                return false;
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("FastTravel")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 3) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_fast_travel_usage")));
                return false;
            }

            String action = args[1].toLowerCase();
            String id = args[2];

            if (KoyaGPS.getLocationFileManager().getConfig().get("Locations." + id) == null) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return false;
            }

            boolean enable = action.equals("enable");

            KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravel", enable);
            KoyaGPS.getLocationFileManager().save();

            player.sendMessage(Utils.colorize(LangUtils.getMessage("fast_travel_status")
                    .replace("{Name}", id)
                    .replace("{Status}", enable ? LangUtils.getMessage("enabled") : LangUtils.getMessage("disabled"))
            ));
            return false;
        }

        if (args[0].equalsIgnoreCase("FastTravelCost")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command is only for players.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return true;
            }

            if (KoyaGPS.getEconomy() == null) {
                player.sendMessage(Utils.colorize("Vault and Essentials missing"));
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("gps_fast_travel_cost_usage")));
                return true;
            }

            String id = args[1];
            String costInput = args[2];

            if (!KoyaGPS.getLocationFileManager().getConfig().contains("Locations." + id)) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("location_not_exist")));
                return true;
            }

            if (costInput.startsWith("%distance*")) {
                // Extract the multiplier correctly
                String multiplierStr = costInput.replace("%distance*", "").replace("%", "");
                double multiplier = Double.parseDouble(multiplierStr);

                // Save dynamic pricing
                KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravelCost", costInput); // -1 indicates per-block pricing
                KoyaGPS.getLocationFileManager().save();

                player.sendMessage(Utils.colorize(LangUtils.getMessage("fast_travel_cost_updated_dynamic")
                        .replace("{Name}", id)
                        .replace("{Multiplier}", String.valueOf(multiplier))
                ));
                return false;
            }

            try {
                int fixedCost = Integer.parseInt(costInput);
                KoyaGPS.getLocationFileManager().getConfig().set("Locations." + id + ".FastTravelCost", fixedCost);
                KoyaGPS.getLocationFileManager().save();

                player.sendMessage(Utils.colorize(LangUtils.getMessage("fast_travel_cost_updated")
                        .replace("{Name}", id)
                        .replace("{Cost}", String.valueOf(fixedCost))
                ));
            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("invalid_cost")));
                return true;
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("Give")) {
            Player player = (Player) sender;
            if (!player.hasPermission("gps.admin")) {
                player.sendMessage(Utils.colorize(LangUtils.getMessage("no_permission")));
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_give_usage")));
                return false;
            }

            Player target = player.getServer().getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("player_not_found")));
                return false;
            }

            // Get the GPS item settings from the config
            String itemName = KoyaGPS.getInstance().getConfig().getString("Item.Name", "&8(&b&lGPS&8)");
            String materialName = KoyaGPS.getInstance().getConfig().getString("Item.Item", "COMPASS");
            Material material = Material.matchMaterial(materialName);

            if (material == null) {
                sender.sendMessage(Utils.colorize(LangUtils.getMessage("invalid_material_in_config")));
                return false;
            }

            ItemStack gpsItem = new ItemStack(material);
            ItemMeta meta = gpsItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Utils.colorizeWithoutPrefix(itemName));
                gpsItem.setItemMeta(meta);
            }

            target.getInventory().addItem(gpsItem);
            target.sendMessage(Utils.colorize(LangUtils.getMessage("gps_received")));
            sender.sendMessage(Utils.colorize(LangUtils.getMessage("gps_given").replace("{Player}", target.getName())));
            return false;
        }

        return false;
    }
}