package me.tigerhix.lib.scoreboard;
 
 import me.tigerhix.lib.scoreboard.type.Scoreboard;
 import me.tigerhix.lib.scoreboard.type.SimpleScoreboard;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public final class ScoreboardLib extends JavaPlugin {
 
     private static Plugin instance;
     private static final List<SimpleScoreboard> activeScoreboards = new ArrayList<>();
 
     public static Plugin getPluginInstance() {
         return instance;
     }
 
     public static void setPluginInstance(Plugin instance) {
         if (ScoreboardLib.instance != null) return;
         ScoreboardLib.instance = instance;
     }
 
     public static Scoreboard createScoreboard(Player holder) {
         SimpleScoreboard sb = new SimpleScoreboard(holder);
         register(sb);
         return sb;
     }
 
     static void register(SimpleScoreboard scoreboard) {
         synchronized (activeScoreboards) {
             activeScoreboards.add(scoreboard);
         }
     }
 
     static void unregister(SimpleScoreboard scoreboard) {
         synchronized (activeScoreboards) {
             activeScoreboards.remove(scoreboard);
         }
     }
 
     @Override
     public void onEnable() {
         setPluginInstance(this);
     }
 
     @Override
     public void onDisable() {
         synchronized (activeScoreboards) {
             for (SimpleScoreboard sb : activeScoreboards) {
                 try { sb.deactivate(); } catch (Exception ignored) {}
             }
             activeScoreboards.clear();
         }
     }
 
 }
 