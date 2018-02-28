package com.dci.intellij.dbn.common.locale.options.ui;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public class LocaleOption implements Presentable{
    public static List<LocaleOption> ALL = new ArrayList<LocaleOption>();
    static {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            ALL.add(new LocaleOption(locale));
        }
        Collections.sort(ALL, new Comparator<LocaleOption>() {
            @Override
            public int compare(LocaleOption localeOption1, LocaleOption localeOption2) {
                return localeOption1.getName().compareTo(localeOption2.getName());
            }
        });
    }


    private Locale locale;

    public LocaleOption(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @NotNull
    @Override
    public String getName() {
        return locale.equals(Locale.getDefault()) ?
                locale.getDisplayName() + " - System default" :
                locale.getDisplayName();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @Nullable
    public static LocaleOption get(Locale locale) {
        for (LocaleOption localeOption : ALL) {
            if (localeOption.locale.equals(locale)) {
                return localeOption;
            }
        }
        return null;
    }
}
