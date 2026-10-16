package com.hichamzouagui.energymanager.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.data.MeterReading;
import com.hichamzouagui.energymanager.util.Formatters;
import com.hichamzouagui.energymanager.util.Numbers;

import java.util.Calendar;

/** Formulaire de saisie d'un relevé de compteur : date, index et commentaire. */
public final class ReadingEditorDialog {

    /** Appelé quand l'utilisateur valide un relevé correctement renseigné. */
    public interface OnReadingSaved {
        void onReadingSaved(MeterReading reading);
    }

    private ReadingEditorDialog() {
    }

    public static void show(final Context context, final MeterReading existing,
                            final OnReadingSaved callback) {
        final boolean creating = existing == null;
        final MeterReading reading = creating ? new MeterReading() : existing;

        final Calendar calendar = Calendar.getInstance();
        if (!creating) {
            calendar.setTimeInMillis(reading.getTimestampMillis());
        }

        View form = LayoutInflater.from(context).inflate(R.layout.dialog_reading, null, false);
        final Button dateButton = form.findViewById(R.id.field_date);
        final EditText indexField = form.findViewById(R.id.field_index);
        final EditText noteField = form.findViewById(R.id.field_note);

        dateButton.setText(Formatters.date(calendar.getTimeInMillis()));
        if (!creating) {
            indexField.setText(Formatters.input(reading.getIndexKwh()));
            noteField.setText(reading.getNote());
        }

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog picker = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, day);
                                dateButton.setText(Formatters.date(calendar.getTimeInMillis()));
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                // Un relevé est toujours passé : on interdit les dates futures.
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());
                picker.show();
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(creating ? R.string.reading_add : R.string.reading_edit)
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
                                double index = Numbers.parse(indexField.getText().toString(), -1);
                                if (index < 0) {
                                    indexField.setError(context.getString(R.string.error_index));
                                    return;
                                }
                                String note = noteField.getText().toString().trim();

                                reading.setTimestampMillis(calendar.getTimeInMillis());
                                reading.setIndexKwh(index);
                                reading.setNote(note.isEmpty() ? null : note);

                                callback.onReadingSaved(reading);
                                dialog.dismiss();
                            }
                        });
            }
        });
        dialog.show();
    }
}
