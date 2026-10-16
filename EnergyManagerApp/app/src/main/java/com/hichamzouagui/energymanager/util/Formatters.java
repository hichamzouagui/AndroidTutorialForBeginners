package com.hichamzouagui.energymanager.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Mise en forme des nombres et des dates, en français. */
public final class Formatters {

    private static final Locale LOCALE = Locale.FRANCE;

    private Formatters() {
    }

    private static DecimalFormat format(String pattern) {
        return new DecimalFormat(pattern, new DecimalFormatSymbols(LOCALE));
    }

    public static String kwh(double value) {
        return format("#,##0.# kWh").format(value);
    }

    public static String kwhPerDay(double value) {
        return format("#,##0.## kWh/j").format(value);
    }

    public static String euros(double value) {
        return format("#,##0.00 ¤").format(value).replace("¤", "€");
    }

    public static String percent(double value) {
        return format("#,##0.# %").format(value / 100d);
    }

    public static String co2(double kilograms) {
        return format("#,##0.# kg").format(kilograms);
    }

    public static String watts(double value) {
        return format("#,##0 W").format(value);
    }

    public static String hours(double value) {
        return format("#,##0.# h").format(value);
    }

    /** Valeur lisible, avec séparateur de milliers (affichage seulement). */
    public static String number(double value) {
        return format("#,##0.####").format(value);
    }

    /**
     * Valeur destinée à préremplir un champ de saisie : virgule décimale mais
     * aucun séparateur de milliers, sinon la relecture du champ échouerait.
     */
    public static String input(double value) {
        return format("0.####").format(value);
    }

    public static String date(long millis) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, LOCALE).format(new Date(millis));
    }

    /** Libellé court « 12 mars » pour les axes de graphique. */
    public static String shortDate(long millis) {
        return new SimpleDateFormat("d MMM", LOCALE).format(new Date(millis));
    }
}
