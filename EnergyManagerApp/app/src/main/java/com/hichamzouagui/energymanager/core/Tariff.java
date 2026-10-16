package com.hichamzouagui.energymanager.core;

/** Paramètres de facturation : prix du kWh, abonnement mensuel et facteur d'émission. */
public final class Tariff {

    /** Valeurs par défaut indicatives (tarif réglementé France, base 2024). */
    public static final double DEFAULT_PRICE_PER_KWH = 0.2516;
    public static final double DEFAULT_STANDING_CHARGE = 13.09;
    public static final double DEFAULT_CO2_GRAMS_PER_KWH = 56.0;

    private final double pricePerKwh;
    private final double standingChargePerMonth;
    private final double co2GramsPerKwh;

    public Tariff(double pricePerKwh, double standingChargePerMonth, double co2GramsPerKwh) {
        if (pricePerKwh < 0) {
            throw new IllegalArgumentException("Le prix du kWh ne peut pas etre negatif");
        }
        if (standingChargePerMonth < 0) {
            throw new IllegalArgumentException("L'abonnement ne peut pas etre negatif");
        }
        if (co2GramsPerKwh < 0) {
            throw new IllegalArgumentException("Le facteur CO2 ne peut pas etre negatif");
        }
        this.pricePerKwh = pricePerKwh;
        this.standingChargePerMonth = standingChargePerMonth;
        this.co2GramsPerKwh = co2GramsPerKwh;
    }

    public static Tariff defaults() {
        return new Tariff(DEFAULT_PRICE_PER_KWH, DEFAULT_STANDING_CHARGE, DEFAULT_CO2_GRAMS_PER_KWH);
    }

    public double getPricePerKwh() {
        return pricePerKwh;
    }

    public double getStandingChargePerMonth() {
        return standingChargePerMonth;
    }

    public double getCo2GramsPerKwh() {
        return co2GramsPerKwh;
    }

    /** Coût de l'énergie consommée, hors abonnement. */
    public double energyCost(double kwh) {
        return kwh * pricePerKwh;
    }

    /** Émissions associées à une consommation, en kg de CO2 équivalent. */
    public double co2Kilograms(double kwh) {
        return kwh * co2GramsPerKwh / 1000d;
    }
}
