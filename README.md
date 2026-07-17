# ScoreboardLib
 
 A flicker-free scoreboard library for Paper/Spigot servers (Minecraft 1.21+).
 Supports text up to 48 characters per line, animated titles, and scrolling/highlighting effects.
 
 > Originally created for Bukkit/Spigot 1.7, modernized for Paper 1.21+ with Java 21.
 
 ## Features
 
 - **Up to 48 characters** per line (vanilla limit is 16)
 - **No flickering** — ScoreboardLib reuses the same scoreboard object instead of recreating it
 - **Animated text** — scrollable strings, highlight animations, frame-by-frame animations
 - **Deduplication** — display identical text on multiple lines without issues
 - **Clean shutdown** — all active scoreboards are automatically cleaned up on plugin disable
 
 ## Requirements
 
 - Java 21+
 - Paper 1.21+ (or compatible Spigot fork)
 
 ## Usage
 
 ### As a standalone plugin
 
 Download the built JAR from Releases and place it in your server's `plugins/` folder.
 
 ### As a library (shaded into your plugin)
 
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
         <scope>compile</scope>
     </dependency>
 </dependencies>
 ```
 
 **Gradle (Kotlin DSL):**
 
 ```kotlin
 repositories {
     maven("https://repo.papermc.io/repository/maven-public/")
 }
 
 dependencies {
     compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
     implementation("me.tigerhixtang.lib:scoreboard:2.0.0-SNAPSHOT")
 }
 ```
 
 ### Building from source
 
 **Maven:**
 ```
 mvn clean package
 ```
 
 **Gradle:**
 ```
 ./gradlew build
 ```
 
 ### Quick start
 
 If you shade ScoreboardLib into your plugin, add this in your `onEnable()`:
 
 ```java
 ScoreboardLib.setPluginInstance(this);
 ```
 
 Then create a scoreboard for a player:
 
 ```java
 Scoreboard scoreboard = ScoreboardLib.createScoreboard(player)
     .setHandler(new ScoreboardHandler() {
 
         private final ScrollableString scroll = new ScrollableString("&aThis string is scrollable!", 40, 0);
         private final HighlightedString highlighted = new HighlightedString("This string is highlighted!", "&6", "&e");
 
         @Override
         public String getTitle(Player player) {
             return "&b&lSERVER INFO";
         }
 
         @Override
         public List<Entry> getEntries(Player player) {
             return new EntryBuilder()
                 .next("&7" + scroll.next())
                 .next("&7" + highlighted.next())
                 .blank()
                 .next("&bOnline: &f" + Bukkit.getOnlinePlayers().size())
                 .next("&bTPS: &f18.5")
                 .blank()
                 .next("&7github.com/TigerHixTang")
                 .build();
         }
 
     })
     .setUpdateInterval(2L);
 scoreboard.activate();
 ```
 
 To remove the scoreboard:
 
 ```java
 scoreboard.deactivate();
 ```
 
 ### Animated strings
 
 | Class | Description |
 |-------|-------------|
 | `ScrollableString` | Scrolls text horizontally like a marquee |
 | `HighlightedString` | Walks through each character, highlighting one at a time |
 | `FrameAnimatedString` | Cycles through a list of pre-defined frames |
 | `StaticString` | Always returns the same text (useful with interfaces) |
 
 ## License
 
 This project is licensed under the `GNU Lesser General Public License v3.0` — see the [LICENSE](LICENSE) file for details.
 
