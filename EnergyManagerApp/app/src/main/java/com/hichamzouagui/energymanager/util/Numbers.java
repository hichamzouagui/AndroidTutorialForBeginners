package com.hichamzouagui.energymanager.util;

/** Lecture des nombres saisis au clavier, sans dépendance Android. */
public final class Numbers {

    private Numbers() {
    }

    /**
     * Lit une valeur décimale saisie par l'utilisateur. Accepte la virgule
     * comme séparateur décimal et tolère les espaces, y compris insécables,
     * que certains claviers ou préremplissages insèrent.
     *
     * @return la valeur lue, ou {@code fallback} si la saisie est inexploitable.
     */
    public static double parse(String raw, double fallback) {
        if (raw == null) {
            return fallback;
        }
        String normalized = raw.replaceAll("[\\s\\u00A0\\u202F]", "").replace(',', '.');
        if (normalized.isEmpty()) {
            return fallback;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException invalid) {
            return fallback;
        }
    }
}
