package com.basisplaceholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BasisPlaceholdersExpansion extends PlaceholderExpansion {

    private final BasisPlaceholders plugin;

    public BasisPlaceholdersExpansion(BasisPlaceholders plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bpe"; // 自定义标识符
    }

    @Override
    public @NotNull String getAuthor() {
        return "Wheat"; // 替换为你的名字
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // 返回 true 以允许占位符被持久化
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return null; // 不要在非玩家上下文中返回占位符
        }

        switch (identifier) {
//          case "last_online":
//              return String.valueOf(plugin.getLastOnlineTime(player) / 1000); // 转换为秒
            case "last_online":
                return getLastOnlineTimeFormatted(player);
            case "offline_days":
                return String.valueOf(plugin.getOfflineDays(player));
            case "current_day":
                return getCurrentDay();
            case "current_day_en":
                return getCurrentDayInEnglish();
            case "seconds_to_next_day":
                return String.valueOf(getSecondsToNextDay());
            case "minutes_to_next_day":
                return String.valueOf(getMinutesToNextDay());
            case "hours_to_next_day":
                return String.valueOf(getHoursToNextDay());
            case "remaining_inventory_slots":
                return String.valueOf(getRemainingInventorySlots(player));
            case "seconds_to_next_monday":
                return String.valueOf(getSecondsToNextMonday());
            case "minutes_to_next_monday":
                return String.valueOf(getMinutesToNextMonday());
            case "hours_to_next_monday":
                return String.valueOf(getHoursToNextMonday());
            case "days_to_next_monday":
                return String.valueOf(getDaysToNextMonday());
            default:
                return null; // 返回 null 表示未识别的占位符
        }
    }

    private String getLastOnlineTimeFormatted(Player player) {
        long lastOnline = plugin.getLastOnlineTime(player);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(lastOnline));
    }

    private String getCurrentDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentDayInEnglish() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    private long getSecondsToNextDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Duration.between(now, nextMidnight).getSeconds();
    }

    private long getMinutesToNextDay() {
        return TimeUnit.SECONDS.toMinutes(getSecondsToNextDay());
    }

    private long getHoursToNextDay() {
        return TimeUnit.SECONDS.toHours(getSecondsToNextDay());
    }

    private int getRemainingInventorySlots(Player player) {
        return (int) Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::isNull).count();
    }

    private long getSecondsToNextMonday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Duration.between(now, nextMonday).getSeconds();
    }

    private long getMinutesToNextMonday() {
        return TimeUnit.SECONDS.toMinutes(getSecondsToNextMonday());
    }

    private long getHoursToNextMonday() {
        return TimeUnit.SECONDS.toHours(getSecondsToNextMonday());
    }

    private long getDaysToNextMonday() {
        return TimeUnit.SECONDS.toDays(getSecondsToNextMonday());
    }
}

