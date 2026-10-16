package com.hichamzouagui.energymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.EnergyCalculator;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.data.Device;
import com.hichamzouagui.energymanager.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Liste des appareils avec leur consommation et leur coût mensuel. */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {

    /** Actions déclenchées depuis une ligne de la liste. */
    public interface Listener {
        void onEditDevice(Device device);

        void onDeleteDevice(Device device);

        void onToggleDevice(Device device);
    }

    private final List<Device> devices = new ArrayList<>();
    private final Listener listener;
    private Tariff tariff;

    public DeviceAdapter(Tariff tariff, Listener listener) {
        this.tariff = tariff;
        this.listener = listener;
    }

    public void submit(List<Device> newDevices, Tariff newTariff) {
        this.tariff = newTariff;
        devices.clear();
        if (newDevices != null) {
            devices.addAll(newDevices);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        holder.bind(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView detailView;
        private final TextView consumptionView;
        private final TextView costView;
        private final ImageButton deleteButton;
        private final ImageButton toggleButton;

        DeviceHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.device_name);
            detailView = itemView.findViewById(R.id.device_detail);
            consumptionView = itemView.findViewById(R.id.device_consumption);
            costView = itemView.findViewById(R.id.device_cost);
            deleteButton = itemView.findViewById(R.id.device_delete);
            toggleButton = itemView.findViewById(R.id.device_toggle);
        }

        void bind(final Device device) {
            nameView.setText(device.getName());

            String quantity = device.getQuantity() > 1
                    ? itemView.getContext().getString(R.string.device_quantity, device.getQuantity())
                    : "";
            detailView.setText(itemView.getContext().getString(R.string.device_detail,
                    Formatters.watts(device.getPowerWatts()),
                    Formatters.hours(device.getHoursPerDay()),
                    device.getDaysPerWeek(), quantity));

            double monthlyKwh = EnergyCalculator.monthlyKwh(device);
            consumptionView.setText(Formatters.kwh(monthlyKwh));
            costView.setText(itemView.getContext().getString(R.string.device_cost,
                    Formatters.euros(tariff.energyCost(monthlyKwh))));

            float alpha = device.isActive() ? 1f : 0.4f;
            nameView.setAlpha(alpha);
            detailView.setAlpha(alpha);
            consumptionView.setAlpha(alpha);
            costView.setAlpha(alpha);
            toggleButton.setImageResource(device.isActive()
                    ? R.drawable.ic_power_on : R.drawable.ic_power_off);
            toggleButton.setContentDescription(itemView.getContext().getString(
                    device.isActive() ? R.string.device_disable : R.string.device_enable));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onEditDevice(device);
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteDevice(device);
                }
            });
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToggleDevice(device);
                }
            });
        }
    }
}
