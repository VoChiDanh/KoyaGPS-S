package ir.alireza009.koyaGPS.listener;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Storage.getLocation().containsKey(player.getUniqueId())) {
            if (KoyaGPS.getPlayersFileManager().getConfig().get("Locations." + player.getName()) == null) {
                Location location = Storage.getLocation().get(player.getUniqueId());
                String id = location.getWorld().getName() + "@" + location.getBlockX() + "@" + location.getBlockY() + "@" + location.getBlockZ();
                KoyaGPS.getPlayersFileManager().getConfig().set("Locations." + player.getName(), id);
                KoyaGPS.getPlayersFileManager().save();
                Storage.getLocation().remove(player.getUniqueId());
            }
            Storage.getPlayers().remove(player.getUniqueId());
        }
    }
}
