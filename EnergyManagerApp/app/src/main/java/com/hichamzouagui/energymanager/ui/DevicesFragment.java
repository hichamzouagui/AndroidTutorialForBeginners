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
import com.hichamzouagui.energymanager.core.ConsumptionSummary;
import com.hichamzouagui.energymanager.core.EnergyCalculator;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.data.Device;
import com.hichamzouagui.energymanager.data.EnergyRepository;
import com.hichamzouagui.energymanager.prefs.UserSettings;
import com.hichamzouagui.energymanager.util.Formatters;

import java.util.List;

/** Inventaire des appareils : ajout, modification, activation et suppression. */
public class DevicesFragment extends Fragment implements DeviceAdapter.Listener {

    private EnergyRepository repository;
    private DeviceAdapter adapter;
    private TextView emptyView;
    private TextView totalView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_devices, container, false);
        repository = EnergyRepository.get(requireContext());

        emptyView = root.findViewById(R.id.devices_empty);
        totalView = root.findViewById(R.id.devices_total);

        adapter = new DeviceAdapter(currentTariff(), this);
        RecyclerView list = root.findViewById(R.id.devices_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);

        FloatingActionButton addButton = root.findViewById(R.id.devices_add);
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
        List<Device> devices = repository.findAllDevices();
        adapter.submit(devices, tariff);

        emptyView.setVisibility(devices.isEmpty() ? View.VISIBLE : View.GONE);

        ConsumptionSummary summary = EnergyCalculator.summarize(devices, tariff);
        totalView.setText(getString(R.string.devices_total,
                Formatters.kwh(summary.getMonthlyKwh()),
                Formatters.euros(summary.getEnergyCost())));
    }

    private void openEditor(Device device) {
        DeviceEditorDialog.show(requireContext(), device, new DeviceEditorDialog.OnDeviceSaved() {
            @Override
            public void onDeviceSaved(Device saved) {
                repository.saveDevice(saved);
                refresh();
            }
        });
    }

    @Override
    public void onEditDevice(Device device) {
        openEditor(device);
    }

    @Override
    public void onToggleDevice(Device device) {
        device.setActive(!device.isActive());
        repository.saveDevice(device);
        refresh();
    }

    @Override
    public void onDeleteDevice(final Device device) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.device_delete_title)
                .setMessage(getString(R.string.device_delete_message, device.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteDevice(device.getId());
                        refresh();
                        View root = getView();
                        if (root != null) {
                            Snackbar.make(root, getString(R.string.device_deleted, device.getName()),
                                    Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            device.setId(Device.NO_ID);
                                            repository.saveDevice(device);
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
