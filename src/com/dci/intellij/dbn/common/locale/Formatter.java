package com.dci.intellij.dbn.common.locale;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.intellij.openapi.project.Project;


public class Formatter {
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private DateFormat dateTimeFormat;
    private NumberFormat numberFormat;
    private NumberFormat integerFormat;

    private String dateFormatPattern;
    private String timeFormatPattern;
    private String datetimeFormatPattern;
    private String numberFormatPattern;
    private String integerFormatPattern;

    public Formatter(Locale locale, DBDateFormat dateFormatOption, DBNumberFormat numberFormatOption) {
        int dFormat = dateFormatOption.getDateFormat();
        dateFormat = SimpleDateFormat.getDateInstance(dFormat, locale);
        timeFormat = SimpleDateFormat.getTimeInstance(dFormat, locale);
        dateTimeFormat = SimpleDateFormat.getDateTimeInstance(dFormat, dFormat, locale);


        boolean groupingUsed = numberFormatOption == DBNumberFormat.GROUPED;

        integerFormat = NumberFormat.getIntegerInstance(locale);
        integerFormat.setGroupingUsed(groupingUsed);

        numberFormat = DecimalFormat.getInstance(locale);
        numberFormat.setGroupingUsed(groupingUsed);
        numberFormat.setMaximumFractionDigits(10);

        dateFormatPattern = ((SimpleDateFormat) dateFormat).toPattern();
        timeFormatPattern = ((SimpleDateFormat) timeFormat).toPattern();
        datetimeFormatPattern = ((SimpleDateFormat) dateTimeFormat).toPattern();
        numberFormatPattern = ((DecimalFormat) numberFormat).toPattern();
        integerFormatPattern = ((DecimalFormat) integerFormat).toPattern();
    }

    public Formatter(Locale locale, String dateFormatPattern, String timeFormatPattern, String numberFormatPattern) {
        if (StringUtil.isEmptyOrSpaces(dateFormatPattern)) throw new IllegalArgumentException("Date format pattern empty.");
        if (StringUtil.isEmptyOrSpaces(timeFormatPattern)) throw new IllegalArgumentException("Time format pattern empty.");
        if (StringUtil.isEmptyOrSpaces(numberFormatPattern)) throw new IllegalArgumentException("Number format pattern empty.");
        this.dateFormatPattern = dateFormatPattern;
        this.timeFormatPattern = timeFormatPattern;
        this.datetimeFormatPattern = dateFormatPattern + ' ' + timeFormatPattern;
        this.numberFormatPattern = numberFormatPattern;

        int fractionIndex = numberFormatPattern.lastIndexOf('.');
        if (fractionIndex > -1) {
            this.integerFormatPattern = numberFormatPattern.substring(0, fractionIndex);
        } else {
            this.integerFormatPattern = numberFormatPattern;
        }

        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        dateFormat = new SimpleDateFormat(this.dateFormatPattern, dateFormatSymbols);
        timeFormat = new SimpleDateFormat(this.timeFormatPattern, dateFormatSymbols);
        dateTimeFormat = new SimpleDateFormat(this.datetimeFormatPattern, dateFormatSymbols);

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        numberFormat = new DecimalFormat(this.numberFormatPattern, decimalFormatSymbols);
        integerFormat = new DecimalFormat(this.integerFormatPattern, decimalFormatSymbols);
        integerFormat.setMaximumFractionDigits(0);
    }

    public static Formatter getInstance(Project project) {
        return RegionalSettings.getInstance(project).getFormatter();
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public String getTimeFormatPattern() {
        return timeFormatPattern;
    }

    public String getDatetimeFormatPattern() {
        return datetimeFormatPattern;
    }

    public String getNumberFormatPattern() {
        return numberFormatPattern;
    }

    public String getIntegerFormatPattern() {
        return integerFormatPattern;
    }

    public synchronized String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public Date parseDate(String string) throws ParseException {
        return dateFormat.parse(string);
    }

    public synchronized String formatTime(Date date) {
        return timeFormat.format(date);
    }

    public Date parseTime(String string) throws ParseException {
        return timeFormat.parse(string);
    }

    public synchronized String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public Date parseDateTime(String string) throws ParseException {
        return dateTimeFormat.parse(string);
    }


    public synchronized String formatNumber(Number number) {
        return numberFormat.format(number);
    }

    public Number parseNumber(String string) throws ParseException {
        return numberFormat.parse(string);
    }

    public synchronized String formatInteger(Number number) {
        return integerFormat.format(number);
    }

    public Number parseInteger(String string) throws ParseException {
        return integerFormat.parse(string);
    }

    public synchronized String formatObject(Object object) {
        if (object != null) {
            return
                object instanceof Number ? formatNumber((Number) object) :
                object instanceof Date ? formatDateTime((Date) object) :
                object instanceof String ? (String) object :
                object instanceof ValueAdapter ? ((ValueAdapter) object).getDisplayValue() :
                object.toString();
        } else {
            return null;
        }
    }

    public Object parseObject(Class clazz, String string) throws ParseException {
        if (Date.class.isAssignableFrom(clazz)) {
            return parseDateTime(string);
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return parseNumber(string);
        }
        return string;
    }

    
}
