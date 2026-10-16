package com.hichamzouagui.energymanager.core;

/** Part d'un appareil dans la consommation totale du foyer. */
public final class ApplianceShare {

    private final Appliance appliance;
    private final double monthlyKwh;
    private final double monthlyCost;
    private final double sharePercent;

    ApplianceShare(Appliance appliance, double monthlyKwh, double monthlyCost, double sharePercent) {
        this.appliance = appliance;
        this.monthlyKwh = monthlyKwh;
        this.monthlyCost = monthlyCost;
        this.sharePercent = sharePercent;
    }

    public Appliance getAppliance() {
        return appliance;
    }

    public String getName() {
        return appliance.getName();
    }

    public double getMonthlyKwh() {
        return monthlyKwh;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    /** Part de la consommation totale, de 0 à 100. */
    public double getSharePercent() {
        return sharePercent;
    }
}
