package ir.alireza009.koyaGPS.storage;

import org.bukkit.Location;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage {
    private static final HashMap<UUID, Location> getLocation = new HashMap<>();

    public static HashMap<UUID, Location> getLocation() {
        return getLocation;
    }

    private static final HashMap<UUID, Location> getPlayers = new HashMap<>();

    public static HashMap<UUID, Location> getPlayers() {
        return getPlayers;
    }

    private static final HashMap<UUID, BossBar> getBossBar = new HashMap<>();

    public static HashMap<UUID, BossBar> getBossBar() {
        return getBossBar;
    }

}