package net.year4000.announcer.commands;

import com.sk89q.bungee.util.BungeeWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.pagination.SimplePaginatedResult;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.year4000.announcer.Announcer;
import net.year4000.announcer.Broadcaster;
import net.year4000.announcer.Config;

public class AnnouncerCommands {
    @Command(
            aliases = {"reload", "refresh"},
            desc = "Reload the config.",
            min = 0,
            max = 1
    )
    @CommandPermissions({"announcer.admin", "announcer.reload"})
    public static void reload(CommandContext args, CommandSender sender) throws CommandException {
        Announcer.inst().reloadConfig();
        Announcer.inst().reloadSchedulers();
        sender.sendMessage(Announcer.makeMessage("Announcer config reloaded."));
    }

    @Command(
            aliases = {"list", "view", "show"},
            desc = "List the messages for the specify server.",
            usage = "[server|global] (page)",
            flags = "r",
            min = 1,
            max = 2
    )
    @CommandPermissions({"announcer.admin", "announcer.list"})
    public static void list(final CommandContext args, CommandSender sender) throws CommandException {
        try {
            // Show raw messages or pretty messages.
            final int MAXPERPAGE = 8;
            new SimplePaginatedResult<String>("Messages for " + args.getString(0), MAXPERPAGE) {
                @Override
                public String format(String server, int index) {
                    try {
                        String message = args.hasFlag('r') ?
                                server : TextComponent.toLegacyText(Broadcaster.parseBroadcast(server));
                        return Announcer.replaceColor((index + 1) + " - " + message);
                    } catch (Exception e) {
                        return Announcer.replaceColor("&c" + e.getMessage());
                    }
                }
            }.display(
                    new BungeeWrappedCommandSender(sender),
                    Announcer.inst().getConfig().getServerMessages(args.getString(0)),
                    args.argsLength() == 2 ? args.getInteger(1) : 1
            );
        } catch (NullPointerException e) {
            throw new CommandException("Server not found.");
        }
    }

    @Command(
            aliases = {"add", "create"},
            desc = "Add a message to a server.",
            usage = "[server|global] [messages]",
            flags = "i",
            min = 2
    )
    @CommandPermissions({"announcer.admin", "announcer.edit", "announcer.add"})
    public static void add(CommandContext args, CommandSender sender) throws CommandException {
        Config config = Announcer.inst().getConfig();
        try {
            config.addServerMessages(args.getString(0), args.getJoinedStrings(1));
            sender.sendMessage(Announcer.makeMessage("&6Message added to " + args.getString(0) + "."));
        } catch (NullPointerException e) {
            throw new CommandException("Server does not exist.");
        } catch (InvalidConfigurationException e) {
            throw new CommandException("Could not remove message.");
        }
    }

    @Command(
            aliases = {"remove", "delete", "del"},
            desc = "Remove a message from a server.",
            usage = "[server|global] [position]",
            min = 2,
            max = 2
    )
    @CommandPermissions({"announcer.admin", "announcer.edit", "announcer.remove"})
    public static void remove(CommandContext args, CommandSender sender) throws CommandException {
        Config config = Announcer.inst().getConfig();
        try {
            if (args.getInteger(1) < 0 || args.getInteger(1) > config.getServerMessages(args.getString(0)).size()) {
                throw new CommandException("There is no message with that index.");
            } else {
                config.removeServerMessages(args.getString(0), args.getInteger(1) - 1);
                sender.sendMessage(Announcer.makeMessage(String.format(
                        "&6Message removed from %s.",
                        args.getString(0))
                ));
            }
        } catch (NullPointerException e) {
            throw new CommandException("Server does not exist.");
        } catch (InvalidConfigurationException e) {
            throw new CommandException("Could not remove message.");
        }
    }

    @Command(
            aliases = {"setting", "option"},
            desc = "Change a setting or view it in the config.",
            usage = "[setting] [option]",
            min = 1,
            max = 2
    )
    @CommandPermissions({"announcer.admin", "announcer.edit", "announcer.setting"})
    public static void setting(CommandContext args, CommandSender sender) throws CommandException {
        Config config = Announcer.inst().getConfig();
        // View the settings.
        if (args.argsLength() == 1) {
            String message;
            switch (args.getString(0)) {
                case "debug":
                    message = "&6Debug is set to: &e" + config.isDebug();
                    break;
                case "delay":
                    message = "&6The delay is set to: &e" + config.getDelay() + " sec(s)";
                    break;
                case "prefix":
                    message = "&6The prefix is set to: &e" + config.getPrefix();
                    break;
                case "random":
                    message = "&6Random is set to: &e" + config.isRandom();
                    break;
                default:
                    message = "&cIs the setting in lowercase and exists?";
            }
            sender.sendMessage(Announcer.makeMessage(message));
        }
        // Change the setting of option
        else {
            try {
                String message;
                switch (args.getString(0)) {
                    case "debug":
                        message = "&6Debug is set to: &e" + config.setSetting(
                                "debug",
                                args.getString(1).equalsIgnoreCase("true")
                        );
                        break;
                    case "delay":
                        message = "&6The delay is set to: &e" + config.setSetting(
                                "delay",
                                args.getInteger(1)
                        ) + " sec(s)";
                        Announcer.inst().reloadSchedulers();
                        break;
                    case "prefix":
                        message = "&6The prefix is set to: &e" + config.setSetting(
                                "prefix",
                                args.getString(1)
                        );
                        break;
                    case "random":
                        message = "&6Random is set to: &e" + config.setSetting(
                                "random",
                                args.getString(1).equalsIgnoreCase("true")
                        );
                        break;
                    default:
                        message = "&cIs the setting in lowercase and exists?";
                }
                sender.sendMessage(Announcer.makeMessage(message));
            } catch (InvalidConfigurationException e) {
                throw new CommandException("Could not change the config.");
            }
        }
    }
}