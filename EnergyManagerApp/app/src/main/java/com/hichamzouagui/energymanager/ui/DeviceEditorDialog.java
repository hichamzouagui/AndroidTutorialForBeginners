package com.hichamzouagui.energymanager.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.EnergyCalculator;
import com.hichamzouagui.energymanager.data.Device;
import com.hichamzouagui.energymanager.util.Formatters;
import com.hichamzouagui.energymanager.util.Numbers;

/**
 * Formulaire de création / modification d'un appareil.
 * La boîte de dialogue n'est fermée que si la saisie est valide.
 */
public final class DeviceEditorDialog {

    /** Appelé quand l'utilisateur valide un appareil correctement renseigné. */
    public interface OnDeviceSaved {
        void onDeviceSaved(Device device);
    }

    private DeviceEditorDialog() {
    }

    public static void show(final Context context, final Device existing,
                            final OnDeviceSaved callback) {
        final boolean creating = existing == null;
        final Device device = creating ? new Device() : existing;

        View form = LayoutInflater.from(context).inflate(R.layout.dialog_device, null, false);
        final EditText nameField = form.findViewById(R.id.field_name);
        final Spinner categoryField = form.findViewById(R.id.field_category);
        final EditText powerField = form.findViewById(R.id.field_power);
        final EditText hoursField = form.findViewById(R.id.field_hours);
        final EditText daysField = form.findViewById(R.id.field_days);
        final EditText quantityField = form.findViewById(R.id.field_quantity);
        final CheckBox activeField = form.findViewById(R.id.field_active);

        final String[] categories = context.getResources().getStringArray(R.array.device_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryField.setAdapter(categoryAdapter);

        if (!creating) {
            nameField.setText(device.getName());
            categoryField.setSelection(indexOf(categories, device.getCategory()));
            powerField.setText(Formatters.input(device.getPowerWatts()));
            hoursField.setText(Formatters.input(device.getHoursPerDay()));
            daysField.setText(String.valueOf(device.getDaysPerWeek()));
            quantityField.setText(String.valueOf(device.getQuantity()));
            activeField.setChecked(device.isActive());
        }

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(creating ? R.string.device_add : R.string.device_edit)
                .setView(form)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface shown) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = nameField.getText().toString().trim();
                                if (name.isEmpty()) {
                                    nameField.setError(context.getString(R.string.error_required));
                                    return;
                                }
                                double power = Numbers.parse(powerField.getText().toString(), -1);
                                if (power < 0) {
                                    powerField.setError(context.getString(R.string.error_power));
                                    return;
                                }
                                double hours = Numbers.parse(hoursField.getText().toString(), -1);
                                if (hours < 0 || hours > 24) {
                                    hoursField.setError(context.getString(R.string.error_hours));
                                    return;
                                }
                                int days = (int) Numbers.parse(daysField.getText().toString(), -1);
                                if (days < 1 || days > 7) {
                                    daysField.setError(context.getString(R.string.error_days));
                                    return;
                                }
                                int quantity = (int) Numbers.parse(quantityField.getText().toString(), -1);
                                if (quantity < 1) {
                                    quantityField.setError(context.getString(R.string.error_quantity));
                                    return;
                                }

                                device.setName(name);
                                device.setCategory((String) categoryField.getSelectedItem());
                                device.setPowerWatts(power);
                                device.setHoursPerDay(hours);
                                device.setDaysPerWeek(days);
                                device.setQuantity(quantity);
                                device.setActive(activeField.isChecked());

                                // Dernier filet de sécurité avant d'enregistrer.
                                EnergyCalculator.monthlyKwh(device);

                                callback.onDeviceSaved(device);
                                dialog.dismiss();
                            }
                        });
            }
        });
        dialog.show();
    }

    private static int indexOf(String[] values, String candidate) {
        if (candidate != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(candidate)) {
                    return i;
                }
            }
        }
        return 0;
    }
}
