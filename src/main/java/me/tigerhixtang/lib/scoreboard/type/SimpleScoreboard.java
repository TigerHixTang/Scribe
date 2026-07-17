package me.tigerhixtang.lib.scoreboard.type;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 import me.tigerhixtang.lib.scoreboard.ScoreboardLib;
 import me.tigerhixtang.lib.scoreboard.common.Strings;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Team;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class SimpleScoreboard implements Scoreboard {
 
     private static final String TEAM_PREFIX = "Scoreboard_";
     private static int TEAM_COUNTER = 0;
 
     private final org.bukkit.scoreboard.Scoreboard scoreboard;
     private final Objective objective;
     private final String id;
 
     private static int ID_COUNTER = 0;
 
     protected Player holder;
     protected long updateInterval = 10L;
 
     private boolean activated;
     private ScoreboardHandler handler;
     private Map<FakePlayer, Integer> entryCache = new ConcurrentHashMap<>();
     private Table<String, Integer, FakePlayer> playerCache = HashBasedTable.create();
     private Table<Team, String, String> teamCache = HashBasedTable.create();
     private BukkitRunnable updateTask;
 
     public SimpleScoreboard(Player holder) {
         this.holder = holder;
         this.id = "sb_" + (ID_COUNTER++);
         scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
         @SuppressWarnings("deprecation")
         Objective obj = scoreboard.registerNewObjective("board_" + id, "dummy");
         obj.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective = obj;
     }
 
     @Override
     public void activate() {
         if (activated) return;
         if (handler == null) throw new IllegalStateException("Scoreboard handler not set");
         activated = true;
         holder.setScoreboard(scoreboard);
         updateTask = new BukkitRunnable() {
             @Override
             public void run() {
                 update();
             }
         };
         updateTask.runTaskTimer(ScoreboardLib.getPluginInstance(), 0, updateInterval);
     }
 
     @Override
     public void deactivate() {
         if (!activated) return;
         activated = false;
         if (holder.isOnline()) {
             holder.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
         }
         for (Team team : teamCache.rowKeySet()) {
             team.unregister();
         }
         teamCache.clear();
         playerCache.clear();
         entryCache.clear();
         if (updateTask != null) {
             updateTask.cancel();
             updateTask = null;
         }
         ScoreboardLib.unregister(this);
     }
 
     @Override
     public boolean isActivated() {
         return activated;
     }
 
     @Override
     public ScoreboardHandler getHandler() {
         return handler;
     }
 
     @Override
     public Scoreboard setHandler(ScoreboardHandler handler) {
         this.handler = handler;
         return this;
     }
 
     @Override
     public long getUpdateInterval() {
         return updateInterval;
     }
 
     @Override
     public SimpleScoreboard setUpdateInterval(long updateInterval) {
         if (activated) throw new IllegalStateException("Scoreboard is already activated");
         this.updateInterval = updateInterval;
         return this;
     }
 
     @Override
     public Player getHolder() {
         return holder;
     }
 
     private void update() {
         if (!holder.isOnline()) {
             deactivate();
             return;
         }
         String handlerTitle = handler.getTitle(holder);
         String finalTitle = Strings.format(handlerTitle != null ? handlerTitle : ChatColor.BOLD.toString());
         if (!objective.getDisplayName().equals(finalTitle)) {
             objective.setDisplayName(finalTitle);
         }
         List<Entry> passed = handler.getEntries(holder);
         if (passed == null) return;
         Map<String, Integer> appeared = new HashMap<>();
         Map<FakePlayer, Integer> current = new HashMap<>();
         for (Entry entry : passed) {
             String key = entry.getName();
             Integer score = entry.getPosition();
             if (key.length() > 48) key = key.substring(0, 48);
             String appearance;
             if (key.length() > 16) {
                 appearance = key.substring(16);
             } else {
                 appearance = key;
             }
             if (!appeared.containsKey(appearance)) appeared.put(appearance, -1);
             appeared.put(appearance, appeared.get(appearance) + 1);
             FakePlayer faker = getFakePlayer(key, appeared.get(appearance));
             objective.getScore(faker).setScore(score);
             entryCache.put(faker, score);
             current.put(faker, score);
         }
         appeared.clear();
         for (FakePlayer fakePlayer : entryCache.keySet()) {
             if (!current.containsKey(fakePlayer)) {
                 entryCache.remove(fakePlayer);
                 scoreboard.resetScores(fakePlayer.getName());
             }
         }
     }
 
     private FakePlayer getFakePlayer(String text, int offset) {
         Team team = null;
         String name;
         if (text.length() <= 16) {
             name = text + Strings.repeat(" ", offset);
         } else {
             String prefix;
             String suffix = "";
             offset++;
             prefix = text.substring(0, 16 - Math.min(offset, 16));
             name = text.substring(16 - Math.min(offset, 16));
             if (name.length() > 16) name = name.substring(0, 16);
             if (text.length() > 32) {
                 suffix = text.substring(32 - Math.min(offset, 16));
                 if (suffix.length() > 16) suffix = suffix.substring(0, 16);
             }
             for (Team other : teamCache.rowKeySet()) {
                 if (other.getPrefix().equals(prefix) && other.getSuffix().equals(suffix)) {
                     team = other;
                     break;
                 }
             }
             if (team == null) {
                 team = scoreboard.registerNewTeam(TEAM_PREFIX + (TEAM_COUNTER++));
                 team.setPrefix(prefix);
                 team.setSuffix(suffix);
                 teamCache.put(team, prefix, suffix);
             }
         }
         FakePlayer faker;
         if (!playerCache.contains(name, offset)) {
             faker = new FakePlayer(name, team);
             playerCache.put(name, offset, faker);
             if (team != null) {
                 team.addPlayer(faker);
             }
         } else {
             faker = playerCache.get(name, offset);
             Team oldTeam = faker.getTeam();
             if (oldTeam != null) {
                 oldTeam.removePlayer(faker);
             }
             faker.setTeam(team);
             if (team != null) {
                 team.addPlayer(faker);
             }
         }
         return faker;
     }
 
     public Objective getObjective() {
         return objective;
     }
 
     public org.bukkit.scoreboard.Scoreboard getScoreboard() {
         return scoreboard;
     }
 
     private static class FakePlayer implements OfflinePlayer {
 
         private static final Map<String, UUID> uuidCache = new HashMap<>();
 
         private final String name;
         private final UUID uuid;
         private Team team;
 
         FakePlayer(String name, Team team) {
             this.name = name;
             this.team = team;
             this.uuid = uuidCache.computeIfAbsent(name, k ->
                 UUID.nameUUIDFromBytes(("ScoreboardLib:" + k).getBytes())
             );
         }
 
         public Team getTeam() { return team; }
         public void setTeam(Team team) { this.team = team; }
 
         public String getFullName() {
             if (team == null) return name;
             String prefix = team.getPrefix() != null ? team.getPrefix() : "";
             String suffix = team.getSuffix() != null ? team.getSuffix() : "";
             return prefix + name + suffix;
         }
 
         @Override public boolean isOnline() { return true; }
         @Override public @NotNull String getName() { return name; }
         @Override public @NotNull UUID getUniqueId() { return uuid; }
         @Override public @NotNull Map<String, Object> serialize() { return Map.of(); }
 
         @Override @Deprecated @SuppressWarnings("deprecation")
         public boolean isBanned() { return false; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public void setBanned(boolean banned) {}
         @Override @Deprecated @SuppressWarnings("deprecation")
         public boolean isWhitelisted() { return false; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public void setWhitelisted(boolean whitelisted) {}
         @Override @Deprecated @SuppressWarnings("deprecation")
         public Player getPlayer() { return null; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public long getFirstPlayed() { return 0; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public long getLastPlayed() { return 0; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public boolean hasPlayedBefore() { return false; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public org.bukkit.Location getBedSpawnLocation() { return null; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public boolean isOp() { return false; }
         @Override @Deprecated @SuppressWarnings("deprecation")
         public void setOp(boolean op) {}
 
         @Override
         public String toString() {
             return "FakePlayer{name='" + name + "', uuid=" + uuid + '}';
         }
     }
 
 }
 