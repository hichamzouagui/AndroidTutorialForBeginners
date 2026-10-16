package com.hichamzouagui.energymanager.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.hichamzouagui.energymanager.core.Tariff;

/** Tarif et budget de l'utilisateur, stockés dans les SharedPreferences. */
public class UserSettings {

    private static final String FILE = "energy_settings";
    private static final String KEY_PRICE = "price_per_kwh";
    private static final String KEY_STANDING_CHARGE = "standing_charge";
    private static final String KEY_CO2 = "co2_grams_per_kwh";
    private static final String KEY_BUDGET = "monthly_budget";

    private final SharedPreferences preferences;

    public UserSettings(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public Tariff getTariff() {
        return new Tariff(getPricePerKwh(), getStandingCharge(), getCo2GramsPerKwh());
    }

    public double getPricePerKwh() {
        return preferences.getFloat(KEY_PRICE, (float) Tariff.DEFAULT_PRICE_PER_KWH);
    }

    public double getStandingCharge() {
        return preferences.getFloat(KEY_STANDING_CHARGE, (float) Tariff.DEFAULT_STANDING_CHARGE);
    }

    public double getCo2GramsPerKwh() {
        return preferences.getFloat(KEY_CO2, (float) Tariff.DEFAULT_CO2_GRAMS_PER_KWH);
    }

    /** Budget mensuel en euros ; 0 signifie « pas de budget défini ». */
    public double getMonthlyBudget() {
        return preferences.getFloat(KEY_BUDGET, 0f);
    }

    public void save(double pricePerKwh, double standingCharge, double co2GramsPerKwh,
                     double monthlyBudget) {
        // Validé en amont par Tariff : une saisie invalide ne doit jamais être persistée.
        new Tariff(pricePerKwh, standingCharge, co2GramsPerKwh);
        if (monthlyBudget < 0) {
            throw new IllegalArgumentException("Le budget ne peut pas etre negatif");
        }
        preferences.edit()
                .putFloat(KEY_PRICE, (float) pricePerKwh)
                .putFloat(KEY_STANDING_CHARGE, (float) standingCharge)
                .putFloat(KEY_CO2, (float) co2GramsPerKwh)
                .putFloat(KEY_BUDGET, (float) monthlyBudget)
                .apply();
    }
}
