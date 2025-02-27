package ir.alireza009.koyaGPS.utils;

import ir.alireza009.koyaGPS.KoyaGPS;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;

public class Utils {
    public static String colorize(String text) {
        return colorizeWithoutPrefix(LangUtils.getMessage("prefix") + text);
    }

    public static String colorizeWithoutPrefix(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String formatNumber(int number) {
        return new DecimalFormat("#,###").format(number);
    }
}
