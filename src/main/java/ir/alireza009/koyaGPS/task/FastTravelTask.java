package ir.alireza009.koyaGPS.task;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.utils.LangUtils;
import ir.alireza009.koyaGPS.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class FastTravelTask {
    private static final List<Integer> ASCEND_TIMES = Arrays.asList(40, 70, 100, 130, 160, 190);
    private static final List<Integer> DESCEND_TIMES = Arrays.asList(280, 300, 320, 340, 360, 380, 400, 420);
    private static final int TELEPORT_DELAY = 220;
    private static final int END_TIME = 440;

    public static void startFastTravel(Player player, Location destination) {
        // Store original GameMode to restore it if something goes wrong
        final GameMode originalGameMode = player.getGameMode();

        // Start the teleport sequence
        player.setGameMode(GameMode.SPECTATOR);
        final Location[] loadingLocation = { player.getLocation().clone() };
        loadingLocation[0].setYaw(90.0F);
        loadingLocation[0].setPitch(90.0F);

        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                try {
                    if (!player.isOnline()) {
                        cancel();
                        return;
                    }

                    if (ASCEND_TIMES.contains(time)) {
                        loadingLocation[0].add(0, 7, 0);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                    if (time == TELEPORT_DELAY) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                        loadingLocation[0] = destination.clone().add(0, 45, 0);
                        loadingLocation[0].setYaw(90.0F);
                        loadingLocation[0].setPitch(90.0F);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                    if (DESCEND_TIMES.contains(time)) {
                        loadingLocation[0].add(0, -5, 0);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                    if (time == END_TIME) {
                        finalizeTeleport(player, loadingLocation[0]);
                        cancel();
                        return;
                    }

                    player.teleport(loadingLocation[0]);
                    time++;
                } catch (Exception e) {
                    // Restore the player's original state if an error occurs
                    player.setGameMode(originalGameMode);
                    player.sendMessage(Utils.colorize("&cAn error occurred during fast travel."));
                    e.printStackTrace();
                    cancel();
                }
            }
        }.runTaskTimer(KoyaGPS.getInstance(), 0, 1);
    }


    private static void finalizeTeleport(Player player, Location finalLocation) {
        player.setGameMode(GameMode.SURVIVAL);
        Storage.getLocation().remove(player.getUniqueId());
        KoyaGPS.getPlayersFileManager().getConfig().set("Locations." + player.getName(), null);
        KoyaGPS.getPlayersFileManager().save();

        finalLocation.add(0, -4.5, 0);
        finalLocation.setPitch(0.0F);
        player.teleport(finalLocation);
        player.playSound(finalLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}