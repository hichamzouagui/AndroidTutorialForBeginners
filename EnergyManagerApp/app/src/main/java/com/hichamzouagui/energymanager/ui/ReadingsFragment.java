package com.hichamzouagui.energymanager.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.EnergyCalculator;
import com.hichamzouagui.energymanager.core.MeterPeriod;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.data.EnergyRepository;
import com.hichamzouagui.energymanager.data.MeterReading;
import com.hichamzouagui.energymanager.prefs.UserSettings;
import com.hichamzouagui.energymanager.util.Formatters;

import java.util.Collections;
import java.util.List;

/** Historique des relevés de compteur : la consommation réellement facturée. */
public class ReadingsFragment extends Fragment implements ReadingAdapter.Listener {

    private EnergyRepository repository;
    private ReadingAdapter adapter;
    private TextView emptyView;
    private TextView summaryView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_readings, container, false);
        repository = EnergyRepository.get(requireContext());

        emptyView = root.findViewById(R.id.readings_empty);
        summaryView = root.findViewById(R.id.readings_summary);

        adapter = new ReadingAdapter(currentTariff(), this);
        RecyclerView list = root.findViewById(R.id.readings_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        FloatingActionButton addButton = root.findViewById(R.id.readings_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(null);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private Tariff currentTariff() {
        return new UserSettings(requireContext()).getTariff();
    }

    private void refresh() {
        Tariff tariff = currentTariff();
        List<MeterReading> ascending = repository.findAllReadings();
        List<MeterPeriod> periods = EnergyCalculator.toPeriods(ascending);

        // La liste s'affiche du plus récent au plus ancien, le calcul des périodes l'inverse.
        List<MeterReading> descending = new java.util.ArrayList<>(ascending);
        Collections.reverse(descending);
        adapter.submit(descending, tariff);

        emptyView.setVisibility(ascending.isEmpty() ? View.VISIBLE : View.GONE);

        if (periods.isEmpty()) {
            summaryView.setText(R.string.readings_need_two);
            return;
        }
        double dailyAverage = EnergyCalculator.measuredDailyAverage(periods, periods.size());
        summaryView.setText(getString(R.string.readings_summary,
                periods.size(), Formatters.kwhPerDay(dailyAverage),
                Formatters.euros(EnergyCalculator.projectedMonthlyBill(dailyAverage, tariff))));
    }

    private void openEditor(MeterReading reading) {
        ReadingEditorDialog.show(requireContext(), reading, new ReadingEditorDialog.OnReadingSaved() {
            @Override
            public void onReadingSaved(MeterReading saved) {
                repository.saveReading(saved);
                refresh();
            }
        });
    }

    @Override
    public void onEditReading(MeterReading reading) {
        openEditor(reading);
    }

    @Override
    public void onDeleteReading(final MeterReading reading) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reading_delete_title)
                .setMessage(getString(R.string.reading_delete_message,
                        Formatters.date(reading.getTimestampMillis())))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteReading(reading.getId());
                        refresh();
                        View root = getView();
                        if (root != null) {
                            Snackbar.make(root, R.string.reading_deleted, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            reading.setId(MeterReading.NO_ID);
                                            repository.saveReading(reading);
                                            refresh();
                                        }
                                    })
                                    .show();
                        }
                    }
                })
                .show();
    }
}
