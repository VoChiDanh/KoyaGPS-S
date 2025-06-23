package ir.alireza009.koyaGPS.listener;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.task.PlayersTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!KoyaGPS.getInstance().getConfig().getBoolean("ArrowVisibleToOtherPlayers")) {
            PlayersTask.hideArrowChainsFromPlayer(player);
        }
        if (KoyaGPS.getPlayersFileManager().getConfig().get("Locations." + player.getName()) != null) {
            String id = KoyaGPS.getPlayersFileManager().getConfig().getString("Locations." + player.getName());
            String[] xyz = id.split("@");
            Location location = new Location(Bukkit.getWorld(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]), Double.valueOf(xyz[3]));
            player.teleport(location);
            player.setGameMode(GameMode.SURVIVAL);
            KoyaGPS.getPlayersFileManager().getConfig().set("Locations." + player.getName(), null);
            KoyaGPS.getPlayersFileManager().save();
        }
    }
}
