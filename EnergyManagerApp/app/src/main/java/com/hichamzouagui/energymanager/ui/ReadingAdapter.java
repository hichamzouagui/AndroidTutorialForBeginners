package com.hichamzouagui.energymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.MeterPeriod;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.data.MeterReading;
import com.hichamzouagui.energymanager.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * Historique des relevés, du plus récent au plus ancien.
 * Chaque ligne affiche la consommation écoulée depuis le relevé précédent.
 */
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingHolder> {

    /** Actions déclenchées depuis une ligne de l'historique. */
    public interface Listener {
        void onEditReading(MeterReading reading);

        void onDeleteReading(MeterReading reading);
    }

    private final List<MeterReading> readings = new ArrayList<>();
    private final Listener listener;
    private Tariff tariff;

    public ReadingAdapter(Tariff tariff, Listener listener) {
        this.tariff = tariff;
        this.listener = listener;
    }

    /** Attend des relevés triés du plus récent au plus ancien. */
    public void submit(List<MeterReading> newReadings, Tariff newTariff) {
        this.tariff = newTariff;
        readings.clear();
        if (newReadings != null) {
            readings.addAll(newReadings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReadingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading, parent, false);
        return new ReadingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingHolder holder, int position) {
        MeterReading reading = readings.get(position);
        MeterReading previous = position + 1 < readings.size() ? readings.get(position + 1) : null;
        holder.bind(reading, previous);
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    class ReadingHolder extends RecyclerView.ViewHolder {

        private final TextView dateView;
        private final TextView indexView;
        private final TextView consumptionView;
        private final TextView noteView;
        private final ImageButton deleteButton;

        ReadingHolder(View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.reading_date);
            indexView = itemView.findViewById(R.id.reading_index);
            consumptionView = itemView.findViewById(R.id.reading_consumption);
            noteView = itemView.findViewById(R.id.reading_note);
            deleteButton = itemView.findViewById(R.id.reading_delete);
        }

        void bind(final MeterReading reading, MeterReading previous) {
            dateView.setText(Formatters.date(reading.getTimestampMillis()));
            indexView.setText(itemView.getContext().getString(R.string.reading_index,
                    Formatters.number(reading.getIndexKwh())));

            consumptionView.setText(describeConsumption(reading, previous));

            String note = reading.getNote();
            noteView.setVisibility(note == null || note.trim().isEmpty() ? View.GONE : View.VISIBLE);
            noteView.setText(note);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEditReading(reading);
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteReading(reading);
                }
            });
        }

        private String describeConsumption(MeterReading reading, MeterReading previous) {
            if (previous == null) {
                return itemView.getContext().getString(R.string.reading_first);
            }
            if (reading.getTimestampMillis() <= previous.getTimestampMillis()
                    || reading.getIndexKwh() < previous.getIndexKwh()) {
                return itemView.getContext().getString(R.string.reading_inconsistent);
            }
            MeterPeriod period = new MeterPeriod(previous.getTimestampMillis(),
                    reading.getTimestampMillis(), previous.getIndexKwh(), reading.getIndexKwh());
            return itemView.getContext().getString(R.string.reading_consumption,
                    Formatters.kwh(period.getKwh()),
                    Formatters.euros(period.getCost(tariff)),
                    Formatters.kwhPerDay(period.getDailyAverageKwh()));
        }
    }
}
