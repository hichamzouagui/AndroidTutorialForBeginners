package com.hichamzouagui.energymanager.core;

/** Un relevé de compteur : une date et un index cumulé en kWh. */
public interface MeterSnapshot {

    long getTimestampMillis();

    /** Index affiché par le compteur, en kWh (valeur cumulée, jamais décroissante). */
    double getIndexKwh();
}
