# Scribe
 
 Animated, flicker-free scoreboard library for Paper 1.21+.
 
 Scribe handles the quirks of Minecraft's scoreboard API so you can display formatted text, animations, and arbitrary-length lines without fighting the client.
 
 ## Features
 
 - **Up to 48 characters per line** — the vanilla 16-character limit is handled internally via team prefixes and suffixes
 - **Flicker-free** — reuses a single scoreboard instance instead of destroying and recreating it on every update
 - **Animated strings** — scrollable marquees, character-by-character highlights, and frame-based animations
 - **Duplicate line support** — identical text on multiple lines works correctly
 - **Lightweight** — no external dependencies beyond the Paper API; can be shaded or run standalone
 
 ## Requirements
 
 - Java 21+
 - Paper 1.21+ (or equivalent Spigot fork)
 
 ## Installation
 
 ### Standalone plugin
 
 Download the latest JAR from [Releases](https://github.com/TigerHixTang/Scribe/releases) and place it in your server's `plugins/` directory.
 
 ### Library dependency
 
 **Maven:**
 
 ```xml
 <repositories>
     <repository>
         <id>papermc</id>
         <url>https://repo.papermc.io/repository/maven-public/</url>
     </repository>
 </repositories>
 
 <dependencies>
     <dependency>
         <groupId>io.papermc.paper</groupId>
         <artifactId>paper-api</artifactId>
         <version>1.21.4-R0.1-SNAPSHOT</version>
         <scope>provided</scope>
     </dependency>
     <dependency>
         <groupId>me.tigerhixtang.lib</groupId>
         <artifactId>scoreboard</artifactId>
         <version>2.0.0-SNAPSHOT</version>
     </dependency>
 </dependencies>
 ```
 
 **Gradle:**
 
 ```kotlin
 repositories {
     maven("https://repo.papermc.io/repository/maven-public/")
 }
 
 dependencies {
     compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
     implementation("me.tigerhixtang.lib:scoreboard:2.0.0-SNAPSHOT")
 }
 ```
 
 ## Quick start
 
 If you're shading the library, call this in your plugin's `onEnable()`:
 
 ```java
 Scribe.setPluginInstance(this);
 ```
 
 Create a scoreboard for a player:
 
 ```java
 Scoreboard board = Scribe.createScoreboard(player)
     .setHandler(new ScoreboardHandler() {
 
         private final ScrollableString scroll = new ScrollableString("&aScrolling text", 40, 0);
         private final HighlightedString highlight = new HighlightedString("Highlighted", "&7", "&e");
 
         @Override
         public String getTitle(Player player) {
             return "&b&lSERVER INFO";
         }
 
         @Override
         public List<Entry> getEntries(Player player) {
             return new EntryBuilder()
                 .next("&7" + scroll.next())
                 .next("&7" + highlight.next())
                 .blank()
                 .next("&aOnline: &f" + Bukkit.getOnlinePlayers().size())
                 .build();
         }
     })
     .setUpdateInterval(2L);
 
 board.activate();
 ```
 
 To remove the scoreboard:
 
 ```java
 board.deactivate();
 ```
 
 ## API overview
 
 | Interface / Class | Purpose |
 |---|---|
 | `Scribe` | Entry point — create scoreboards and hold the plugin instance |
 | `Scoreboard` / `SimpleScoreboard` | Represents a player-bound scoreboard with animated title and entries |
 | `ScoreboardHandler` | Defines what title and entries to display on each update tick |
 | `EntryBuilder` | Convenience builder for constructing entry lists with blank lines |
 | `ScrollableString` | Horizontal marquee-style scrolling text |
 | `HighlightedString` | Walks through each character, highlighting one at a time |
 | `FrameAnimatedString` | Cycles through a pre-defined list of frames |
 | `StaticString` | Returns the same text every call |
 
 ## Building
 
 ```
 # Maven
 mvn clean package
 
 # Gradle
 ./gradlew build
 ```
 
 ## License
 
 [GNU Lesser General Public License v3.0](LICENSE)
 
