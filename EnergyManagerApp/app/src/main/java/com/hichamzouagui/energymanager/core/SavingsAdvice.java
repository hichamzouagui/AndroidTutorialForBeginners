package com.hichamzouagui.energymanager.core;

/** Gain estimé si l'usage d'un appareil est réduit. */
public final class SavingsAdvice {

    private final String applianceName;
    private final double kwhSavedPerMonth;
    private final double moneySavedPerMonth;
    private final double co2SavedPerYear;

    SavingsAdvice(String applianceName, double kwhSavedPerMonth, double moneySavedPerMonth,
                  double co2SavedPerYear) {
        this.applianceName = applianceName;
        this.kwhSavedPerMonth = kwhSavedPerMonth;
        this.moneySavedPerMonth = moneySavedPerMonth;
        this.co2SavedPerYear = co2SavedPerYear;
    }

    public String getApplianceName() {
        return applianceName;
    }

    public double getKwhSavedPerMonth() {
        return kwhSavedPerMonth;
    }

    public double getMoneySavedPerMonth() {
        return moneySavedPerMonth;
    }

    public double getMoneySavedPerYear() {
        return moneySavedPerMonth * 12d;
    }

    public double getCo2SavedPerYear() {
        return co2SavedPerYear;
    }
}
