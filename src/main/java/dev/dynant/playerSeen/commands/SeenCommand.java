/* Licensed under GNU General Public License v3.0 */
package dev.dynant.playerSeen.commands;

import dev.dynant.playerSeen.PlayerSeen;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

@CommandContainer
@CommandDescription("PlayerSeen command")
public class SeenCommand {
  private static final MiniMessage mm = MiniMessage.miniMessage();

  private static final String NOT_FOUND_MESSAGE =
      "<gray>Player <red>could not be found</red>.</gray>";
  private static final String ONLINE_MESSAGE =
      "<gray>Player <white>{name}</white> <green>is currently online</green></gray>";
  private static final String NEVER_PLAYED_MESSAGE =
      "<gray>Player <white>{name}</white> <red>has never played before</red></gray>";
  private static final String LAST_SEEN_MESSAGE =
      "<gray>Player <white>{name}</white> <gray>was last seen {time}</gray>";

  private String getMessage(String key) {
    return PlayerSeen.pluginInstance.getConfig().getString("messages." + key);
  }

  private void sendMessage(Player player, String message) {
    Component parsedMessage = MiniMessage.miniMessage().deserialize(message);
    player.sendMessage(parsedMessage);
  }

  /**
   * Gets a formatted message for the player's last seen status
   *
   * @param player The player to check
   * @return A component with the formatted message
   */
  @Command("seen <player>")
  @Permission("seen.use")
  public void seenPlayer(
      CommandSourceStack sourceStack,
      @Argument(value = "player", description = "Player to check if they have been seen")
          OfflinePlayer player) {

    if (!(sourceStack.getSender() instanceof Player sender)) return;

    String playerName = player.getName();

    Component message;

    if (playerName == null) {
      message = mm.deserialize(NOT_FOUND_MESSAGE);
    } else if (player.isOnline()) {
      // Player is online
      message = mm.deserialize(ONLINE_MESSAGE.replace("{name}", playerName));
    } else if (player.getLastSeen() == 0) {
      // Player has never played before
      message = mm.deserialize(NEVER_PLAYED_MESSAGE.replace("{name}", playerName));
    } else {
      // Get the last login of the player
      long lastSeen = player.getLastSeen();

      // Get formatted last seen string
      String timeAgo = formatLastSeenTime(lastSeen);

      // Get date of last seen to use for hover
      LocalDateTime dateTime =
          LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeen), ZoneId.systemDefault());
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String formattedDate = dateTime.format(formatter);

      message =
          mm.deserialize(LAST_SEEN_MESSAGE.replace("{name}", playerName).replace("{time}", timeAgo))
              .hoverEvent(HoverEvent.showText(Component.text(formattedDate)));
    }

    sender.sendMessage(message);
  }

  private static String formatLastSeenTime(long lastSeen) {
    long diff = System.currentTimeMillis() - lastSeen;

    long seconds = (diff / 1000) % 60;
    long minutes = (diff / 60000) % 60;
    long hours = (diff / 3600000) % 24;
    long days = (diff / 86400000) % 7;
    long weeks = diff / 604800000;

    // Get color for last diff
    String color;
    if (diff < 3600000) {
      color = "<green>"; // < 1 hour
    } else if (diff < 86400000) {
      color = "<yellow>"; // < 1 day
    } else if (diff < 604800000) {
      color = "<gold>"; // < 1 week
    } else {
      color = "<red>"; // ≥ 1 week
    }

    List<String> parts = new ArrayList<>();
    if (weeks > 0) parts.add(weeks + (weeks == 1 ? " week" : " weeks"));
    if (days > 0) parts.add(days + (days == 1 ? " day" : " days"));
    if (hours > 0) parts.add(hours + (hours == 1 ? " hour" : " hours"));
    if (minutes > 0) parts.add(minutes + (minutes == 1 ? " minute" : " minutes"));

    // Only show seconds if:
    // - minutes are present and no larger units (weeks, days, hours), OR
    // - seconds are the only non-zero unit
    if (seconds > 0
        && ((minutes > 0 && weeks == 0 && days == 0 && hours == 0) || parts.isEmpty())) {
      parts.add(seconds + (seconds == 1 ? " second" : " seconds"));
    }

    StringBuilder timeBuilder = new StringBuilder();
    for (int i = 0; i < parts.size(); i++) {
      timeBuilder.append(parts.get(i));
      if (i < parts.size() - 2) {
        timeBuilder.append(", ");
      } else if (i == parts.size() - 2) {
        timeBuilder.append(" and ");
      }
    }

    if (timeBuilder.isEmpty()) {
      timeBuilder.append("just now");
    } else {
      timeBuilder.append(" ago");
    }

    return color + timeBuilder;
  }
}
