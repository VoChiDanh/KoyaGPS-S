package ir.alireza009.koyaGPS.command;

import ir.alireza009.koyaGPS.KoyaGPS;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabComplete implements TabCompleter {
    private final List<String> arguments = Arrays.asList("Delete", "Add", "SetIcon", "FastTravel", "FastTravelCost", "Reload", "Give", "Style", "ForceOpen", "ForceTrack", "ForceEnd", "ForceFastTravel");
    private final List<String> argumentsTwo = Arrays.asList("Disable", "Enable");
    private final List<String> gpsNames = new ArrayList<String>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;
        gpsNames.clear();
        if (KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations") != null) {
            gpsNames.addAll(KoyaGPS.getLocationFileManager().getConfig().getConfigurationSection("Locations").getKeys(false));
        }

        if (args.length == 1) {
            return arguments;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("Style")) {
                if (KoyaGPS.getInstance().getConfig().getBoolean("CurveModel")) {
                    ConfigurationSection styles = KoyaGPS.getInstance().getConfig().getConfigurationSection("ArrowChainCurve.Styles");
                    if (styles != null) {
                        return styles.getKeys(false).stream()
                                .filter(style -> player.hasPermission("gps.style." + style))
                                .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                }else{
                    ConfigurationSection styles = KoyaGPS.getInstance().getConfig().getConfigurationSection("ArrowChain.Styles");
                    if (styles != null) {
                        return styles.getKeys(false).stream()
                                .filter(style -> player.hasPermission("gps.style." + style))
                                .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                }
            }
            if (args[0].equalsIgnoreCase("FastTravel")) return argumentsTwo;
            if (args[0].equalsIgnoreCase("Add")) return List.of("<Name>");
            if (args[0].equalsIgnoreCase("SetIcon")) return gpsNames;
            if (args[0].equalsIgnoreCase("Delete")) return gpsNames;
            if (args[0].equalsIgnoreCase("FastTravelCost")) return gpsNames;
            if (args[0].equalsIgnoreCase("ForceTrack")) return gpsNames;
            if (args[0].equalsIgnoreCase("ForceFastTravel")) return gpsNames;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("FastTravel")) return gpsNames;
            if (args[0].equalsIgnoreCase("FastTravelCost")) return List.of("<Cost>", "%distance*10%");;
        }

        return null;
    }
}
