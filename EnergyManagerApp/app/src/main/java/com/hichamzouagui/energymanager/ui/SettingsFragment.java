package com.hichamzouagui.energymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.prefs.UserSettings;
import com.hichamzouagui.energymanager.util.Formatters;
import com.hichamzouagui.energymanager.util.Numbers;

/** Réglages du tarif, du facteur d'émission et du budget mensuel. */
public class SettingsFragment extends Fragment {

    private UserSettings settings;
    private EditText priceField;
    private EditText standingChargeField;
    private EditText co2Field;
    private EditText budgetField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        settings = new UserSettings(requireContext());

        priceField = root.findViewById(R.id.setting_price);
        standingChargeField = root.findViewById(R.id.setting_standing_charge);
        co2Field = root.findViewById(R.id.setting_co2);
        budgetField = root.findViewById(R.id.setting_budget);

        ((TextView) root.findViewById(R.id.settings_defaults)).setText(
                getString(R.string.settings_defaults,
                        Formatters.number(Tariff.DEFAULT_PRICE_PER_KWH),
                        Formatters.number(Tariff.DEFAULT_STANDING_CHARGE),
                        Formatters.number(Tariff.DEFAULT_CO2_GRAMS_PER_KWH)));

        bindCurrentValues();

        Button save = root.findViewById(R.id.settings_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        Button reset = root.findViewById(R.id.settings_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                priceField.setText(Formatters.input(Tariff.DEFAULT_PRICE_PER_KWH));
                standingChargeField.setText(Formatters.input(Tariff.DEFAULT_STANDING_CHARGE));
                co2Field.setText(Formatters.input(Tariff.DEFAULT_CO2_GRAMS_PER_KWH));
            }
        });

        return root;
    }

    private void bindCurrentValues() {
        priceField.setText(Formatters.input(settings.getPricePerKwh()));
        standingChargeField.setText(Formatters.input(settings.getStandingCharge()));
        co2Field.setText(Formatters.input(settings.getCo2GramsPerKwh()));
        double budget = settings.getMonthlyBudget();
        budgetField.setText(budget > 0 ? Formatters.input(budget) : "");
    }

    private void saveSettings() {
        double price = Numbers.parse(priceField.getText().toString(), -1);
        if (price < 0) {
            priceField.setError(getString(R.string.error_price));
            return;
        }
        double standingCharge = Numbers.parse(standingChargeField.getText().toString(), -1);
        if (standingCharge < 0) {
            standingChargeField.setError(getString(R.string.error_standing_charge));
            return;
        }
        double co2 = Numbers.parse(co2Field.getText().toString(), -1);
        if (co2 < 0) {
            co2Field.setError(getString(R.string.error_co2));
            return;
        }
        // Un budget vide signifie « pas de budget » : on enregistre 0.
        double budget = Numbers.parse(budgetField.getText().toString(), 0);
        if (budget < 0) {
            budgetField.setError(getString(R.string.error_budget));
            return;
        }

        settings.save(price, standingCharge, co2, budget);

        View root = getView();
        if (root != null) {
            Snackbar.make(root, R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
        }
    }
}
