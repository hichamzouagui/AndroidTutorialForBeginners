package com.hichamzouagui.energymanager.core;

/**
 * Un équipement électrique dont on veut estimer la consommation.
 * Volontairement sans dépendance Android pour rester testable en JVM pure.
 */
public interface Appliance {

    String getName();

    String getCategory();

    /** Puissance de l'appareil en watts. */
    double getPowerWatts();

    /** Durée d'utilisation moyenne, en heures par jour d'utilisation. */
    double getHoursPerDay();

    /** Nombre de jours d'utilisation par semaine (1 à 7). */
    int getDaysPerWeek();

    /** Nombre d'exemplaires identiques (ex: 6 ampoules). */
    int getQuantity();

    /** Un appareil inactif est conservé en base mais exclu des calculs. */
    boolean isActive();
}
