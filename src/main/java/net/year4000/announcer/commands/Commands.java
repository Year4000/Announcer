package net.year4000.announcer.commands;

import com.sk89q.bungee.util.BungeeCommandsManager;
import com.sk89q.bungee.util.CommandExecutor;
import com.sk89q.bungee.util.CommandRegistration;
import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.CommandSender;
import net.year4000.announcer.Announcer;

public class Commands implements CommandExecutor<CommandSender> {
    private Announcer plugin = Announcer.inst();
    private BungeeCommandsManager commands;

    public Commands() {
        commands = new BungeeCommandsManager();
        new CommandRegistration(
                plugin,
                plugin.getProxy().getPluginManager(),
                commands, this
        ).register(AnnouncerBaseCommand.class);
    }

    @Override
    public void onCommand(CommandSender sender, String commandName, String[] args) {
        try {
            commands.execute(commandName, args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(plugin.makeMessage("&cYou don't have permission."));
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(plugin.makeMessage("&c" + e.getUsage()));
        } catch (CommandUsageException e) {
            sender.sendMessage(plugin.makeMessage("&c" + e.getMessage()));
            sender.sendMessage(plugin.makeMessage("&c" + e.getUsage()));
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(plugin.makeMessage("&cNumber expected, string received instead."));
            } else {
                sender.sendMessage(plugin.makeMessage("&cAn error has occurred. See console."));
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(plugin.makeMessage("&c" + e.getMessage()));
        }
    }
}