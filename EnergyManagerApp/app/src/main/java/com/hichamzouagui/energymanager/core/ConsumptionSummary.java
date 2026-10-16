package com.hichamzouagui.energymanager.core;

import java.util.Collections;
import java.util.List;

/** Résultat agrégé d'une estimation de consommation sur un mois. */
public final class ConsumptionSummary {

    private final double monthlyKwh;
    private final double energyCost;
    private final double standingCharge;
    private final double co2Kilograms;
    private final List<ApplianceShare> breakdown;

    ConsumptionSummary(double monthlyKwh, double energyCost, double standingCharge,
                       double co2Kilograms, List<ApplianceShare> breakdown) {
        this.monthlyKwh = monthlyKwh;
        this.energyCost = energyCost;
        this.standingCharge = standingCharge;
        this.co2Kilograms = co2Kilograms;
        this.breakdown = Collections.unmodifiableList(breakdown);
    }

    public double getMonthlyKwh() {
        return monthlyKwh;
    }

    public double getAnnualKwh() {
        return monthlyKwh * 12d;
    }

    /** Coût de l'énergie seule. */
    public double getEnergyCost() {
        return energyCost;
    }

    public double getStandingCharge() {
        return standingCharge;
    }

    /** Facture mensuelle estimée, abonnement inclus. */
    public double getMonthlyBill() {
        return energyCost + standingCharge;
    }

    public double getAnnualBill() {
        return getMonthlyBill() * 12d;
    }

    public double getCo2Kilograms() {
        return co2Kilograms;
    }

    /** Détail par appareil, trié de la plus forte à la plus faible consommation. */
    public List<ApplianceShare> getBreakdown() {
        return breakdown;
    }

    /** Appareil le plus consommateur, ou {@code null} si aucun appareil actif. */
    public ApplianceShare getTopConsumer() {
        return breakdown.isEmpty() ? null : breakdown.get(0);
    }
}
