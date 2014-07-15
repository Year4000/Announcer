package net.year4000.announcer;

import com.ewized.utilities.bungee.BungeePlugin;
import com.ewized.utilities.core.util.FileUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.year4000.announcer.commands.AnnouncerBaseCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Announcer extends BungeePlugin {
    private static Announcer inst;
    private Settings settings;
    private Map<String, Integer> messagesIndex = new HashMap<>();
    private List<ScheduledTask> broadcasters = new ArrayList<>();

    @Override
    public void onEnable() {
        inst = this;

        // Set up the config
        settings = new Settings();
        debug = settings.isDebug();

        // Register the loop for each server.
        addSchedulers();

        // Register the commands
        registerCommand(AnnouncerBaseCommand.class);
    }

    /** The instance of Announcer */
    public static Announcer inst() {
        return inst;
    }

    /**
     * Get the current index of the message.
     * @param server The server to get the index list.
     * @return The index position.
     */
    public int getMessageIndex(String server) {
        return messagesIndex.get(server);
    }

    /**
     * Set the index of the server.
     * @param server The server to change.
     * @param index  The index to set to.
     */
    public int setMessagesIndex(String server, int index) {
        messagesIndex.put(server, index);
        return getMessageIndex(server);
    }

    /**
     * Get the config.
     * @return The config.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Reload the config.
     * @return Config instance.
     */
    public Settings reloadConfig() {
        settings = new Settings();
        return settings;
    }

    /** Add the schedulers for the broadcast tasks. */
    public void addSchedulers() {
        for (ServerInfo server : getProxy().getServers().values()) {
            debug("Registering the server: %s", server.getName());
            setMessagesIndex(server.getName(), 0);
            broadcasters.add(getProxy().getScheduler().schedule(
                this,
                new Broadcaster(server.getName()),
                settings.getDelay(),
                settings.getDelay(),
                TimeUnit.SECONDS
            ));
        }
    }

    /** Reload the schedulers. */
    public void reloadSchedulers() {
        for (ScheduledTask task : broadcasters) {
            debug("Stopping broadcast task: %s", task.getId());
            task.cancel();
        }
        addSchedulers();
    }
}