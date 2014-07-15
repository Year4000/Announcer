package net.year4000.announcer.messages;

import com.ewized.utilities.core.util.locale.LocaleManager;
import net.year4000.announcer.Announcer;

public class InternalManager  extends LocaleManager {
    private static InternalManager inst;
    protected static final String LOCALE_PATH = "/net/year4000/announcer/locales/";
    private static String[] localeCodes = {"en_US", "pt_BR"};

    private InternalManager() {
        super(Announcer.class);
    }

    public static InternalManager get() {
        if (inst == null) {
            inst = new InternalManager();
        }

        return inst;
    }

    @Override
    protected void loadLocales(String path) {
        for (String locale : localeCodes) {
            loadLocale(locale, clazz.getResourceAsStream(LOCALE_PATH + locale + ".properties"));
        }
    }

    /** Reload locales */
    public void reload() {
        inst = new InternalManager();
    }
}
