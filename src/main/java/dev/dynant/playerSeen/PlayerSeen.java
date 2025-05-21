/* Licensed under GNU General Public License v3.0 */
package dev.dynant.playerSeen;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

public final class PlayerSeen extends JavaPlugin {
  public static PlayerSeen pluginInstance;

  @Override
  public void onEnable() {
    pluginInstance = this;
    saveDefaultConfig();

    try {
      PaperCommandManager<CommandSourceStack> commandManager =
          PaperCommandManager.builder()
              .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
              .buildOnEnable(this);

      AnnotationParser<CommandSourceStack> annotationParser =
          new AnnotationParser(commandManager, CommandSourceStack.class);

      annotationParser.parseContainers();

    } catch (Exception exc) {
      getLogger().warning("Failed to parse command containers. Commands will not work!");
      exc.printStackTrace();
    }

    getLogger().info("PlayerSeen Enabled!");
  }

  @Override
  public void onDisable() {
    getLogger().info("PlayerSeen Disabled!");
  }
}
