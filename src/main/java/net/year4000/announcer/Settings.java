package net.year4000.announcer;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Settings extends Config {
    public Settings() {
        try {
            CONFIG_HEADER = new String[]{"Announcer Configuration"};
            CONFIG_FILE = new File(Announcer.inst().getDataFolder(), "config.yml");

            init();
        } catch (InvalidConfigurationException e) {
            Announcer.log("Could not load config file.");
            Announcer.debug(e.getMessage());
        }
    }

    @Comment("The setting for Announcer, the setting explains itself.")
    private Map<String, Object> settings = new HashMap<String, Object>() {{
        put("debug", false);
        put("delay", 60);
        put("random", false);
        put("prefix", "&7[&6TIP&7]&r ");
        put("locales", new ArrayList<String>() {{
            add("en_US");
            add("en_PT");
            add("pt_BR");
            add("pt_PT");
        }});
    }};

    // Messages Setting
    @Comment("The messages to be displayed to the server.")
    private Map<String, List<String>> messages = new HashMap<String, List<String>>() {{
        // Global messages
        put("global", new ArrayList<String>() {{
            add("&eWelcome &6%player%&e!");
            add("[{text:'Welcome ', color:yellow}, {text:'%player%', color: gold}, {text:'!',color:yellow}]");
        }});

        // Per server messages
        for (ServerInfo server : Announcer.inst().getProxy().getServers().values()) {
            put(server.getName(), new ArrayList<>());
        }
    }};

    /**
     * Config option to allow debug messages.
     * @return true|false
     */
    public boolean isDebug() throws NullPointerException {
        Announcer.inst().getLog().setDebug((boolean) settings.get("debug"));
        return (boolean) settings.get("debug");
    }

    /**
     * Get the messages delay setting.
     * @return The delay time.
     */
    public long getDelay() throws NullPointerException {
        return (int) settings.get("delay");
    }

    /**
     * Are the messages random.
     * @return true|false
     */
    public boolean isRandom() throws NullPointerException {
        return (boolean) settings.get("random");
    }

    /**
     * Get the prefix for the messages.
     * @return The prefix.
     */
    public String getPrefix() throws NullPointerException {
        return (String) settings.get("prefix");
    }

    /**
     * Get the prefix for the messages.
     * @return The prefix.
     */
    @SuppressWarnings("unchecked")
    public List<String> getLocales() throws NullPointerException {
        return (List<String>) settings.get("locales");
    }

    /**
     * Set a setting option to a value.
     * @param option  The name of the setting.
     * @param setting The setting object.
     * @throws InvalidConfigurationException
     */
    public Object setSetting(String option, Object setting) throws InvalidConfigurationException {
        settings.put(option, setting);
        save();
        return settings.get(option);
    }

    /**
     * Get the messages for the server in the config.
     * @param server The server name.
     * @return The list of messages.
     */
    public List<String> getMessages(final String server) {
        return new ArrayList<String>() {{
            try {
                // Create global messages.
                addAll(messages.get("global").stream().collect(Collectors.toList()));
            } catch(NullPointerException e) {
                Announcer.debug(e, true);
            }

            try {
                List<String> serverNames = new ArrayList<>();
                messages.keySet().stream().forEach(serverNames::add);

                // Create the per server messages.
                serverNames.stream()
                    .filter(name -> Pattern.matches(name, server))
                    .forEach(serverName -> addAll(messages.get(serverName).stream().collect(Collectors.toList())));
            } catch(NullPointerException e) {
                Announcer.debug(e, true);
            }
        }};
    }

    /**
     * Get the messages for only one server.
     * @param server The server name.
     * @return The list of messages.
     */
    public List<String> getServerMessages(String server) throws NullPointerException {
        return messages.get(server);
    }

    /**
     * Add a message to the server.
     * @param server  The server name.
     * @param message The message.
     * @return The added message.
     * @throws InvalidConfigurationException
     */
    public String addServerMessages(String server, String message) throws InvalidConfigurationException, NullPointerException {
        messages.get(server).add(message);
        save();
        return message;
    }

    /**
     * Add a message to the server.
     * @param server  The server name.
     * @param message The message.
     * @param index   The index to be added.
     * @return The added message.
     * @throws InvalidConfigurationException
     */
    public String addServerMessages(String server, int index, String message) throws InvalidConfigurationException, NullPointerException {
        messages.get(server).add(index, message);
        save();
        return message;
    }

    /**
     * Remove a message from the settings.
     * @param server The server to remove the message.
     * @param index  The index to be removed.
     * @throws InvalidConfigurationException
     */
    public void removeServerMessages(String server, int index) throws InvalidConfigurationException, NullPointerException {
        messages.get(server).remove(index);
        save();
    }
}