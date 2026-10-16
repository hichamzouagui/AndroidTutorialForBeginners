package com.hichamzouagui.energymanager.core;

/**
 * Consommation réelle mesurée entre deux relevés de compteur.
 * C'est la contrepartie "mesurée" de l'estimation faite à partir des appareils.
 */
public final class MeterPeriod {

    private final long startMillis;
    private final long endMillis;
    private final double kwh;
    private final double days;

    public MeterPeriod(long startMillis, long endMillis, double startIndexKwh, double endIndexKwh) {
        if (endMillis <= startMillis) {
            throw new IllegalArgumentException("Le releve de fin doit etre posterieur au releve de debut");
        }
        if (endIndexKwh < startIndexKwh) {
            throw new IllegalArgumentException("L'index du compteur ne peut pas diminuer");
        }
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.kwh = endIndexKwh - startIndexKwh;
        this.days = (endMillis - startMillis) / (double) EnergyCalculator.MILLIS_PER_DAY;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    /** Énergie consommée sur la période, en kWh. */
    public double getKwh() {
        return kwh;
    }

    /** Durée de la période en jours (fractionnaire). */
    public double getDays() {
        return days;
    }

    public double getDailyAverageKwh() {
        return days <= 0 ? 0d : kwh / days;
    }

    /** Consommation ramenée à un mois moyen, utile pour comparer des périodes de longueurs différentes. */
    public double getProjectedMonthlyKwh() {
        return getDailyAverageKwh() * EnergyCalculator.DAYS_PER_MONTH;
    }

    public double getCost(Tariff tariff) {
        return tariff.energyCost(kwh);
    }
}
