package ir.alireza009.koyaGPS.task;

import ir.alireza009.koyaGPS.KoyaGPS;
import ir.alireza009.koyaGPS.storage.Storage;
import ir.alireza009.koyaGPS.utils.LangUtils;
import ir.alireza009.koyaGPS.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayersTask implements Listener {
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final Map<UUID, List<ArmorStand>> arrowChains = new HashMap<>();
    private static final Map<UUID, Double> playerDistances = new HashMap<>();
    private static final Map<UUID, String> playerStyles = new HashMap<>();
    private static final double MIN_DISTANCE = 0.5;
    private static final double MAX_DISTANCE = 4.0;
    private static final double DISTANCE_STEP = 0.2;
    public PlayersTask() {
        // Register the listener
        Bukkit.getPluginManager().registerEvents(this, KoyaGPS.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        // Only hide arrows if the config option is false
        if (!KoyaGPS.getInstance().getConfig().getBoolean("ArrowVisibleToOtherPlayers")) {
            arrowChains.forEach((ownerUUID, arrowChain) -> {
                if (!joiningPlayer.getUniqueId().equals(ownerUUID)) {
                    arrowChain.forEach(armorStand ->
                            armorStand.setVisible(false));
                    arrowChain.forEach(armorStand ->
                            joiningPlayer.hideEntity(KoyaGPS.getInstance(), armorStand));
                }
            });
        }
    }

    @EventHandler
    public void onPlayerScrollWheel(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!arrowChains.containsKey(player.getUniqueId()) || !player.isSneaking()) {
            return;
        }

        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();

        // Calculate scroll direction
        boolean scrolledUp = (previousSlot == 8 && newSlot == 0) || (newSlot - previousSlot == 1);

        // Get current distance or set default
        double currentDistance = playerDistances.getOrDefault(player.getUniqueId(), 1.5);

        // Adjust distance based on scroll direction
        if (scrolledUp) {
            currentDistance = Math.min(MAX_DISTANCE, currentDistance + DISTANCE_STEP);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f); // Higher pitch for up
        } else {
            currentDistance = Math.max(MIN_DISTANCE, currentDistance - DISTANCE_STEP);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f); // Lower pitch for down
        }

        playerDistances.put(player.getUniqueId(), currentDistance);

        // Cancel the item switch event
        event.setCancelled(true);
    }

    public static void startDestination(Player player) {
        Location destination = Storage.getPlayers().get(player.getUniqueId());

        cleanup(player);

        Storage.getPlayers().put(player.getUniqueId(), destination);
        // Initialize default distance for new GPS sessions
        playerDistances.put(player.getUniqueId(), 1.5);

        BossBar bossBar = KoyaGPS.getInstance().getServer().createBossBar(
                Utils.colorizeWithoutPrefix("&b&lGPS"),
                BarColor.BLUE,
                BarStyle.SEGMENTED_20);

        bossBar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bossBar);

        List<ArmorStand> arrowChain = createArrowChain(player);
        arrowChains.put(player.getUniqueId(), arrowChain);

        if (!KoyaGPS.getInstance().getConfig().getBoolean("ArrowVisibleToOtherPlayers")) {
            hideArrowChainFromOtherPlayers(player, arrowChain);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || Storage.getPlayers().get(player.getUniqueId()) == null) {
                    cleanup(player);
                    cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                double distance = playerLoc.distance(destination);

                if (distance < 3) {
                    cleanup(player);
                    cancel();
                    return;
                }

                updateBossBar(player, bossBar, distance);
                updateActionBar(player, distance);
                updateArrowChain(player, arrowChain, destination);
            }
        }.runTaskTimer(KoyaGPS.getInstance(), 0, 1);
    }
    private static void updateArrowChain(Player player, List<ArmorStand> arrowChain, Location destination) {
        if (player == null || destination == null || arrowChain.isEmpty()) return;

        Location eyeLoc = player.getEyeLocation();
        if (eyeLoc == null || !isValidLocation(eyeLoc)) {
            eyeLoc = player.getLocation().clone().add(0, 1.62, 0);
        }

        if (!isValidLocation(destination)) {
            destination = player.getLocation().clone().add(0, 0, 5);
        }

        if (KoyaGPS.getInstance().getConfig().getBoolean("CurveModel")) {
            Vector playerDirection = eyeLoc.getDirection().normalize();
            Vector toDestination = destination.toVector().subtract(eyeLoc.toVector()).normalize();

            if (!isValidVector(playerDirection)) playerDirection = new Vector(0, 0, 1);
            if (!isValidVector(toDestination)) toDestination = new Vector(0, 0, 1);

            double playerDistance = playerDistances.getOrDefault(player.getUniqueId(), 1.5);
            Location start = eyeLoc.add(playerDirection.clone().multiply(playerDistance));

            Vector lastBlockDirection = playerDirection.clone();
            Location lastBlockLocation = start.clone();

            for (int i = 0; i < arrowChain.size(); i++) {
                ArmorStand stand = arrowChain.get(i);
                double progress = i / (double) (arrowChain.size() - 1);

                Vector interpolatedDirection = playerDirection.clone().multiply(1 - progress)
                        .add(toDestination.clone().multiply(progress)).normalize();

                if (!isValidVector(interpolatedDirection)) interpolatedDirection = lastBlockDirection.clone();

                Location newLoc = lastBlockLocation.clone().add(lastBlockDirection.clone().multiply(0.25));

                if (!isValidLocation(newLoc)) newLoc = player.getLocation().clone().add(0, 1, 0);

                stand.teleport(newLoc);

                double yaw = Math.atan2(-interpolatedDirection.getX(), interpolatedDirection.getZ());
                double pitch = Math.asin(interpolatedDirection.getY());
                stand.setHeadPose(new EulerAngle(pitch, 0, 0));
                stand.setRotation((float) Math.toDegrees(yaw), 0);

                lastBlockDirection = interpolatedDirection.clone();
                lastBlockLocation = newLoc.clone();
            }
        } else {
            Vector playerDirection = eyeLoc.getDirection().normalize();
            Vector toDestination = destination.toVector().subtract(eyeLoc.toVector()).normalize();

            if (!isValidVector(playerDirection)) playerDirection = new Vector(0, 0, 1);
            if (!isValidVector(toDestination)) toDestination = new Vector(0, 0, 1);

            double playerDistance = playerDistances.getOrDefault(player.getUniqueId(), 1.5);
            Location start = eyeLoc.add(playerDirection.clone().multiply(playerDistance));

            Vector direction = toDestination.clone();
            Location arrowBase = start.clone();
            Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

            if (!isValidVector(perpendicular)) perpendicular = new Vector(1, 0, 0);

            Location[] positions = new Location[25];

            double shaftSpacing = 0.15;
            for (int i = 0; i < 12; i++) {
                positions[i] = arrowBase.clone().add(direction.clone().multiply(i * shaftSpacing));
            }

            double headStart = (12 - 1) * shaftSpacing;
            Location headBase = arrowBase.clone().add(direction.clone().multiply(headStart));

            double baseWidth = 0.6;
            int blocksPerWing = 6;

            for (int i = 0; i < blocksPerWing; i++) {
                double progress = i / (double) (blocksPerWing - 1);
                double currentWidth = baseWidth * (1 - progress);
                Vector leftPos = direction.clone().multiply(progress * 0.8)
                        .add(perpendicular.clone().multiply(currentWidth));
                positions[12 + i] = headBase.clone().add(leftPos);
            }

            for (int i = 0; i < blocksPerWing; i++) {
                double progress = i / (double) (blocksPerWing - 1);
                double currentWidth = baseWidth * (1 - progress);
                Vector rightPos = direction.clone().multiply(progress * 0.8)
                        .add(perpendicular.clone().multiply(-currentWidth));
                positions[12 + blocksPerWing + i] = headBase.clone().add(rightPos);
            }

            for (int i = 0; i < arrowChain.size() && i < positions.length; i++) {
                ArmorStand stand = arrowChain.get(i);
                Location pos = positions[i];

                if (!isValidLocation(pos)) pos = player.getLocation().clone().add(0, 1, 0);

                stand.teleport(pos);
                stand.setRotation(0, 0);
                stand.setHeadPose(new EulerAngle(0, 0, 0));
            }
        }
    }

    private static boolean isValidLocation(Location loc) {
        return loc != null && Double.isFinite(loc.getX()) && Double.isFinite(loc.getY()) && Double.isFinite(loc.getZ());
    }

    private static boolean isValidVector(Vector vec) {
        return vec != null && Double.isFinite(vec.getX()) && Double.isFinite(vec.getY()) && Double.isFinite(vec.getZ());
    }



    private static Material getArrowMaterialCurve(String style, int index, int chainLength) {
        String pathKey = "ArrowChainCurve.Styles." + style;

        // Get the destination block material (for the last block in chain)
        if (index == chainLength - 1) {
            String destBlock = KoyaGPS.getInstance().getConfig().getString(pathKey + ".DestinationBlock");
            try {
                return Material.valueOf(destBlock);
            } catch (IllegalArgumentException e) {
                return Material.DIAMOND_BLOCK; // Fallback
            }
        }

        // Handle path blocks
        Object pathBlocks = KoyaGPS.getInstance().getConfig().get(pathKey + ".PathBlocks");

        if (pathBlocks instanceof List) {
            // For styles with multiple block types (like rainbow)
            @SuppressWarnings("unchecked")
            List<String> materials = (List<String>) pathBlocks;
            int materialIndex = index % materials.size();
            try {
                return Material.valueOf(materials.get(materialIndex));
            } catch (IllegalArgumentException e) {
                return Material.GOLD_BLOCK; // Fallback
            }
        } else if (pathBlocks instanceof String) {
            // For styles with single block type
            try {
                return Material.valueOf((String) pathBlocks);
            } catch (IllegalArgumentException e) {
                return Material.GOLD_BLOCK; // Fallback
            }
        }

        return Material.GOLD_BLOCK; // Default fallback
    }
    private static List<ArmorStand> createArrowChain(Player player) {
        List<ArmorStand> chain = new ArrayList<>();
        if (KoyaGPS.getInstance().getConfig().getBoolean("CurveModel")) {


            String style = playerStyles.getOrDefault(player.getUniqueId(),
                    KoyaGPS.getInstance().getConfig().getString("ArrowChainCurve.DefaultStyle", "default"));

            int chainLength = KoyaGPS.getInstance().getConfig().getInt("ArrowChainCurve.Length", 8);

            for (int i = 0; i < chainLength; i++) {
                ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setMarker(true);

                Material material = getArrowMaterialCurve(style, i, chainLength);
                stand.setHelmet(new ItemStack(material));
                chain.add(stand);
            }


        }else{
            String style = playerStyles.getOrDefault(player.getUniqueId(),
                    KoyaGPS.getInstance().getConfig().getString("ArrowChain.DefaultStyle", "default"));

            // Create 24 armor stands for the enhanced arrow
            for (int i = 0; i < 24; i++) {
                ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setMarker(true);
                stand.setCollidable(false);
                stand.setBasePlate(false);

                Material material;
                if (i < 8) {
                    // Shaft material (8 blocks)
                    material = getArrowMaterial(style, "PathBlocks");
                } else {
                    // Wing materials (16 blocks)
                    material = getArrowMaterial(style, "ArrowHeadBlock");
                }

                ItemStack helmet = new ItemStack(material);
                stand.setHelmet(helmet);
                chain.add(stand);
            }

        }
        return chain;
    }

    private static Material getArrowMaterial(String style, String part) {
        String materialName = KoyaGPS.getInstance().getConfig().getString("ArrowChain.Styles." + style + "." + part);
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return Material.QUARTZ_BLOCK; // Default fallback
        }
    }
    public static void setArrowStyle(Player player, String style) {
        // Determine the config path based on the "CurveModel" setting
        String configPath = KoyaGPS.getInstance().getConfig().getBoolean("CurveModel")
                ? "ArrowChainCurve.Styles"
                : "ArrowChain.Styles";

        // Check if the style exists in the respective configuration section
        if (!KoyaGPS.getInstance().getConfig().contains(configPath + "." + style)) {
            return; // Return if the style doesn't exist
        }

        // Store the current destination before cleanup
        Location currentDestination = Storage.getPlayers().get(player.getUniqueId());

        // Save the new style
        playerStyles.put(player.getUniqueId(), style);

        // Only restart if there's an active GPS session and currentDestination is not null
        if (arrowChains.containsKey(player.getUniqueId()) && currentDestination != null) {
            cleanup(player);  // Clean up the previous setup
            Storage.getPlayers().put(player.getUniqueId(), currentDestination);  // Restore the destination
            startDestination(player);  // Restart the GPS session with the new style
        }
    }


    private static void hideArrowChainFromOtherPlayers(Player owner, List<ArmorStand> arrowChain) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(owner)) {
                arrowChain.forEach(armorStand -> {
                    armorStand.setVisible(false);
                    onlinePlayer.hideEntity(KoyaGPS.getInstance(), armorStand);
                });
            }
        }
    }

    public static void hideArrowChainsFromPlayer(Player player) {
        arrowChains.forEach((uuid, armorStands) -> {
            armorStands.forEach(armorStand -> {
                armorStand.setVisible(false);
                player.hideEntity(KoyaGPS.getInstance(), armorStand);
            });
        });
    }


    private static void updateBossBar(Player player, BossBar bossBar, double distance) {
        if (KoyaGPS.getInstance().getConfig().getBoolean("Bossbar")) {
            String title = LangUtils.getMessage("bossbar_title").replace("{Distance}", String.format("%.1f", distance));
            bossBar.setTitle(Utils.colorizeWithoutPrefix(title));
            bossBar.setProgress(Math.max(0.0, Math.min(1.0, 1.0 - (distance / 100.0))));
        }
    }

    private static void updateActionBar(Player player, double distance) {
        if (KoyaGPS.getInstance().getConfig().getBoolean("Actionbar")) {
            String actionbar = LangUtils.getMessage("actionbar").replace("{Distance}", String.format("%.1f", distance));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.colorizeWithoutPrefix(actionbar)));
        }
    }


    private static void cleanup(Player player) {
        UUID playerId = player.getUniqueId();
        Storage.getPlayers().remove(playerId);
        playerDistances.remove(playerId);
        // Don't remove the style during cleanup
        // playerStyles.remove(playerId);  <- Remove this line

        BossBar bossBar = bossBars.remove(playerId);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }

        List<ArmorStand> arrowChain = arrowChains.remove(playerId);
        if (arrowChain != null) {
            arrowChain.forEach(ArmorStand::remove);
        }
    }

    // Also modify stopAllBossBars to preserve styles
    public static void stopPlayersGPS() {
        for (UUID playerId : bossBars.keySet()) {
            BossBar bossBar = bossBars.get(playerId);
            if (bossBar != null) {
                bossBar.removeAll();
            }
        }
        bossBars.clear();

        for (List<ArmorStand> arrowChain : arrowChains.values()) {
            arrowChain.forEach(ArmorStand::remove);
        }
        arrowChains.clear();

        Storage.getPlayers().clear();
        // Don't clear styles when stopping all bossbars
        // playerStyles.clear();  <- Remove this if it exists
    }
}

