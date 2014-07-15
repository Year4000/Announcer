package net.year4000.announcer.commands;

import com.ewized.utilities.bungee.util.MessageUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.sk89q.bungee.util.BungeeWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.pagination.SimplePaginatedResult;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.year4000.announcer.Announcer;
import net.year4000.announcer.Broadcaster;
import net.year4000.announcer.Settings;
import net.year4000.announcer.messages.InternalManager;
import net.year4000.announcer.messages.Message;
import net.year4000.announcer.messages.MessageManager;

import java.util.List;

public class AnnouncerCommands {
    @Command(
        aliases = {"reload", "refresh"},
        desc = "Reload the config.",
        min = 0,
        max = 1
    )
    @CommandPermissions({"announcer.admin", "announcer.reload"})
    public static void reload(CommandContext args, CommandSender sender) throws CommandException {
        ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
        Message locale = new Message(player) {{
            this.localeManager = InternalManager.get();
        }};

        Announcer.inst().reloadConfig();
        Announcer.inst().reloadSchedulers();
        MessageManager.get().reload();
        InternalManager.get().reload();
        sender.sendMessage(MessageUtil.makeMessage(locale.get("cmd.reload")));
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
        ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
        Message locale = new Message(player) {{
            this.localeManager = InternalManager.get();
        }};

        try {
            // Show raw messages or pretty messages.
            final int MAX_PER_PAGE = 8;
            new SimplePaginatedResult<String>(locale.get("cmd.messages", args.getString(0)), MAX_PER_PAGE) {
                @Override
                public String format(String server, int index) {
                    try {
                        String message = args.hasFlag('r') ? server : TextComponent.toLegacyText(Broadcaster.parseBroadcast(player, server));
                        return MessageUtil.replaceColors((index + 1) + " - " + message);
                    } catch (Exception e) {
                        return MessageUtil.replaceColors("&c" + e.getMessage());
                    }
                }
            }.display(
                new BungeeWrappedCommandSender(sender),
                Announcer.inst().getSettings().getServerMessages(args.getString(0)),
                args.argsLength() == 2 ? args.getInteger(1) : 1
            );
        } catch (NullPointerException e) {
            throw new CommandException(locale.get("cmd.server.not_found"));
        }
    }

    @Command(
        aliases = {"add", "create"},
        desc = "Add a message to a server.",
        usage = "[server|global] [messages]",
        min = 2
    )
    @CommandPermissions({"announcer.admin", "announcer.edit", "announcer.add"})
    public static void add(CommandContext args, CommandSender sender) throws CommandException {
        ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
        Message locale = new Message(player) {{
            this.localeManager = InternalManager.get();
        }};
        Settings settings = Announcer.inst().getSettings();

        try {
            settings.addServerMessages(args.getString(0), args.getJoinedStrings(1));
            sender.sendMessage(MessageUtil.makeMessage(locale.get("cmd.message.add", args.getString(0))));
        } catch (NullPointerException e) {
            throw new CommandException(locale.get("cmd.server.not_found"));
        } catch (InvalidConfigurationException e) {
            throw new CommandException(locale.get("cmd.message.add.error"));
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
        ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
        Message locale = new Message(player) {{
            this.localeManager = InternalManager.get();
        }};
        Settings settings = Announcer.inst().getSettings();

        try {
            if (args.getInteger(1) < 0 || args.getInteger(1) > settings.getServerMessages(args.getString(0)).size()) {
                throw new CommandException(locale.get("cmd.message.no_index"));
            } else {
                settings.removeServerMessages(args.getString(0), args.getInteger(1) - 1);
                sender.sendMessage(MessageUtil.makeMessage(locale.get("cmd.message.remove", args.getString(0))));
            }
        } catch (NullPointerException e) {
            throw new CommandException(locale.get("cmd.server.not_found"));
        } catch (InvalidConfigurationException e) {
            throw new CommandException(locale.get("cmd.message.remove.error"));
        }
    }

    @Command(
        aliases = {"setting", "option"},
        desc = "Change a setting or view it in the config.",
        usage = "[setting] [option]",
        min = 1
    )
    @CommandPermissions({"announcer.admin", "announcer.edit", "announcer.setting"})
    @SuppressWarnings("unchecked")
    public static void setting(CommandContext args, CommandSender sender) throws CommandException {
        ProxiedPlayer player = sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null;
        Message locale = new Message(player) {{
            this.localeManager = InternalManager.get();
        }};
        Settings settings = Announcer.inst().getSettings();

        // View the settings.
        if (args.argsLength() == 1) {
            String message;
            switch (args.getString(0)) {
                case "debug":
                    message = locale.get("cmd.config.debug", settings.isDebug());
                    break;
                case "delay":
                    message = locale.get("cmd.config.delay", settings.getDelay());
                    break;
                case "prefix":
                    message = locale.get("cmd.config.prefix", settings.getPrefix());
                    break;
                case "random":
                    message = locale.get("cmd.config.random", settings.isRandom());
                    break;
                case "locales":
                    message = locale.get("cmd.config.locales", Joiner.on("&7, &e").join(settings.getLocales()));
                    break;
                default:
                    message = locale.get("cmd.config.not_found");
            }
            sender.sendMessage(MessageUtil.makeMessage(message));
        }
        // Change the setting of option
        else {
            try {
                String message;
                switch (args.getString(0)) {
                    case "debug":
                        message = locale.get("cmd.config.debug", settings.setSetting("debug", args.getString(1).equalsIgnoreCase("true")));
                        break;
                    case "delay":
                        message = locale.get("cmd.config.delay", settings.setSetting("delay", args.getInteger(1)));
                        Announcer.inst().reloadSchedulers();
                        break;
                    case "prefix":
                        message = locale.get("cmd.config.prefix", settings.setSetting("prefix", args.getString(1)));
                        break;
                    case "random":
                        message = locale.get("cmd.config.random", settings.setSetting("random", args.getString(1).equalsIgnoreCase("true")));
                        break;
                    case "locales":
                        List<String> locales = (List<String>) settings.setSetting("locales", Splitter.on(' ').splitToList(args.getJoinedStrings(1)));
                        message = locale.get("cmd.config.locales", Joiner.on("&7, &e").join(locales));
                        break;
                    default:
                        message = locale.get("cmd.config.not_found");
                }
                sender.sendMessage(MessageUtil.makeMessage(message));
            } catch (InvalidConfigurationException e) {
                throw new CommandException(locale.get("cmd.config.error"));
            }
        }
    }
}