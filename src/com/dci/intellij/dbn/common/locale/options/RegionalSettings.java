package com.dci.intellij.dbn.common.locale.options;

import com.dci.intellij.dbn.common.locale.DBDateFormat;
import com.dci.intellij.dbn.common.locale.DBNumberFormat;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.ui.RegionalSettingsEditorForm;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.setting.StringSetting;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;

import java.util.Locale;

public class RegionalSettings extends Configuration<RegionalSettingsEditorForm> {
    private Locale locale = Locale.getDefault();
    private DBDateFormat dateFormatOption = DBDateFormat.MEDIUM;
    private DBNumberFormat numberFormatOption = DBNumberFormat.UNGROUPED;

    private BooleanSetting useCustomFormats = new BooleanSetting("use-custom-formats", false);
    private StringSetting customNumberFormat = new StringSetting("custom-number-format", null);
    private StringSetting customDateFormat = new StringSetting("custom-date-format", null);
    private StringSetting customTimeFormat = new StringSetting("custom-time-format", null);

    private Formatter formatter;

    public static RegionalSettings getInstance(Project project) {
        return GeneralProjectSettings.getInstance(project).getRegionalSettings();
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
        formatter = useCustomFormats.value() ?
                new Formatter(locale, customDateFormat.value(), customTimeFormat.value(), customNumberFormat.value()) :
                new Formatter(locale, dateFormatOption, numberFormatOption);

    }

    public Formatter getFormatter(){
        if (formatter == null) formatter = new Formatter(locale, dateFormatOption, numberFormatOption);
        return formatter;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public DBDateFormat getDateFormatOption() {
        return dateFormatOption;
    }

    public void setDateFormatOption(DBDateFormat dateFormatOption) {
        this.dateFormatOption = dateFormatOption;
    }

    public DBNumberFormat getNumberFormatOption() {
        return numberFormatOption;
    }

    public void setNumberFormatOption(DBNumberFormat numberFormatOption) {
        this.numberFormatOption = numberFormatOption;
    }

    public BooleanSetting getUseCustomFormats() {
        return useCustomFormats;
    }

    public StringSetting getCustomDateFormat() {
        return customDateFormat;
    }

    public StringSetting getCustomTimeFormat() {
        return customTimeFormat;
    }

    public StringSetting getCustomNumberFormat() {
        return customNumberFormat;
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    public RegionalSettingsEditorForm createConfigurationEditor() {
        return new RegionalSettingsEditorForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "regional-settings";
    }

    public void readConfiguration(Element element) {
        String localeString = SettingsUtil.getString(element, "locale", Locale.getDefault().toString());
        boolean useSystemLocale = localeString.equals("SYSTEM_DEFAULT");
        if (useSystemLocale) {
             this.locale = Locale.getDefault();
        } else {
            for (Locale locale : Locale.getAvailableLocales()) {
                if (locale.toString().equals(localeString)) {
                    this.locale = locale;
                    break;
                }
            }
        }

        dateFormatOption = SettingsUtil.getEnum(element, "date-format", DBDateFormat.MEDIUM);
        numberFormatOption = SettingsUtil.getEnum(element, "number-format", DBNumberFormat.UNGROUPED);
        useCustomFormats.readConfiguration(element);

        if (useCustomFormats.value()) {
            customNumberFormat.readConfiguration(element);
            customDateFormat.readConfiguration(element);
            customTimeFormat.readConfiguration(element);
        }
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setEnum(element, "date-format", dateFormatOption);
        SettingsUtil.setEnum(element, "number-format", numberFormatOption);

        String localeString = this.locale.equals(Locale.getDefault()) ? "SYSTEM_DEFAULT" : locale.toString();
        SettingsUtil.setString(element, "locale", localeString);

        useCustomFormats.writeConfiguration(element);
        if (useCustomFormats.value()) {
            customNumberFormat.writeConfiguration(element);
            customDateFormat.writeConfiguration(element);
            customTimeFormat.writeConfiguration(element);
        }

    }


}
