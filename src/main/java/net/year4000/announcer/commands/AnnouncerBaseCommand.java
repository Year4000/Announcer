package net.year4000.announcer.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.md_5.bungee.api.CommandSender;

public class AnnouncerBaseCommand {
    @Command(
            aliases = {"announcer", "broadcaster", "announce"},
            desc = "Announcer base command."
    )
    @NestedCommand({AnnouncerCommands.class})
    public static void announcer(CommandContext args, CommandSender sender) throws CommandException {
    }
}
