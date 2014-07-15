package net.year4000.announcer.messages;

import com.ewized.utilities.bungee.util.BungeeLocale;
import com.ewized.utilities.core.util.locale.LocaleUtil;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.year4000.announcer.Announcer;

import static com.ewized.utilities.core.util.MessageUtil.message;
import static com.google.common.base.Preconditions.checkNotNull;

public class Message extends BungeeLocale implements LocaleUtil {
    public Message(ProxiedPlayer player) {
        super(player);
        this.localeManager = MessageManager.get();
    }

    /** Translate to the specific locale with formatting */
    public String get(String key, Object... args) {
        checkNotNull(locale);
        checkNotNull(localeManager);

        if (!localeManager.isLocale(locale)) {
            Announcer.debug("(" + locale + ") " + key + " " + Joiner.on(", ").join(args));
            locale = DEFAULT_LOCALE;
        }

        return message(localeManager.getLocale(locale).getProperty(key, key), args);
    }
}
