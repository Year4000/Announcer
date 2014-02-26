package net.year4000.announcer;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.year4000.announcer.commands.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Announcer extends Plugin {
    private static Announcer inst;
    private Config config;
    private HashMap<String, Integer> messagesIndex = new HashMap<>();
    private List<ScheduledTask> broadcasters = new ArrayList<ScheduledTask>();

    @Override
    public void onEnable() {
        inst = this;

        // Set up the config
        config = new Config();

        // Register the loop for each server.
        addSchedulers();

        // Register the commands
        new Commands();
    }

    /**
     * Get the instance of this plugin.
     *
     * @return The instance.
     */
    public static Announcer inst() {
        return inst;
    }

    /**
     * Show log messages.
     *
     * @param message The log message.
     */
    public static void log(String message) {
        inst().getLogger().log(Level.INFO, message);
    }

    /**
     * Show debug messages when debug is set to true.
     *
     * @param message The debug message.
     */
    public static void debug(String message) {
        if (inst().getConfig().isDebug()) log(message);
    }

    /**
     * Get the current index of the message.
     *
     * @param server The server to get the index list.
     * @return The index position.
     */
    public int getMessageIndex(String server) {
        return messagesIndex.get(server);
    }

    /**
     * Set the index of the server.
     *
     * @param server The server to change.
     * @param index  The index to set to.
     */
    public int setMessagesIndex(String server, int index) {
        messagesIndex.put(server, index);
        return getMessageIndex(server);
    }

    /**
     * Get the config.
     *
     * @return The config.
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Reload the config.
     *
     * @return Config instance.
     */
    public Config reloadConfig() {
        config = new Config();
        return config;
    }

    /**
     * Add the schedulers for the broadcast tasks.
     */
    public void addSchedulers() {
        for (ServerInfo server : getProxy().getServers().values()) {
            debug("Registering the server: " + server.getName());
            setMessagesIndex(server.getName(), 0);
            broadcasters.add(getProxy().getScheduler().schedule(
                    this,
                    new Broadcaster(server.getName()),
                    config.getDelay(),
                    config.getDelay(),
                    TimeUnit.SECONDS
            ));
        }
    }

    /**
     * Reload the schedulers.
     */
    public void reloadSchedulers() {
        for (ScheduledTask task : broadcasters) {
            debug("Stopping broadcast task: " + task.getId());
            task.cancel();
        }
        addSchedulers();
    }
}