package me.tigerhixtang.lib.scoreboard.common;
 
 import org.bukkit.ChatColor;
 
 public final class Strings {
 
     private Strings() {}
 
     public static String format(String string) {
         return ChatColor.translateAlternateColorCodes('&', string);
     }
 
     public static String repeat(String string, int count) {
         return string.repeat(Math.max(0, count));
     }
 
 }
 
