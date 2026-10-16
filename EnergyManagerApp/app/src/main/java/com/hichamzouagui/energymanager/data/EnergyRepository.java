package com.hichamzouagui.energymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hichamzouagui.energymanager.data.EnergyContract.DeviceTable;
import com.hichamzouagui.energymanager.data.EnergyContract.ReadingTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Accès unique à la base : les écrans ne manipulent jamais le SQL directement.
 * Les requêtes sont courtes et locales, elles restent donc sur le thread principal.
 */
public class EnergyRepository {

    private static EnergyRepository instance;

    private final EnergyDatabaseHelper helper;

    private EnergyRepository(Context context) {
        this.helper = new EnergyDatabaseHelper(context.getApplicationContext());
    }

    public static synchronized EnergyRepository get(Context context) {
        if (instance == null) {
            instance = new EnergyRepository(context);
        }
        return instance;
    }

    // --- Appareils ---------------------------------------------------------

    public List<Device> findAllDevices() {
        List<Device> devices = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DeviceTable.NAME, null, null, null, null, null,
                DeviceTable.LABEL + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                devices.add(readDevice(cursor));
            }
        } finally {
            cursor.close();
        }
        return devices;
    }

    public Device findDevice(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DeviceTable.NAME, null, DeviceTable.ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return cursor.moveToFirst() ? readDevice(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /** Insère ou met à jour l'appareil et retourne son identifiant. */
    public long saveDevice(Device device) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DeviceTable.LABEL, device.getName());
        values.put(DeviceTable.CATEGORY, device.getCategory());
        values.put(DeviceTable.POWER_WATTS, device.getPowerWatts());
        values.put(DeviceTable.HOURS_PER_DAY, device.getHoursPerDay());
        values.put(DeviceTable.DAYS_PER_WEEK, device.getDaysPerWeek());
        values.put(DeviceTable.QUANTITY, device.getQuantity());
        values.put(DeviceTable.ACTIVE, device.isActive() ? 1 : 0);

        if (device.isNew()) {
            long id = db.insert(DeviceTable.NAME, null, values);
            device.setId(id);
            return id;
        }
        db.update(DeviceTable.NAME, values, DeviceTable.ID + " = ?",
                new String[]{String.valueOf(device.getId())});
        return device.getId();
    }

    public void deleteDevice(long id) {
        helper.getWritableDatabase().delete(DeviceTable.NAME, DeviceTable.ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Device readDevice(Cursor cursor) {
        Device device = new Device();
        device.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DeviceTable.ID)));
        device.setName(cursor.getString(cursor.getColumnIndexOrThrow(DeviceTable.LABEL)));
        device.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DeviceTable.CATEGORY)));
        device.setPowerWatts(cursor.getDouble(cursor.getColumnIndexOrThrow(DeviceTable.POWER_WATTS)));
        device.setHoursPerDay(cursor.getDouble(cursor.getColumnIndexOrThrow(DeviceTable.HOURS_PER_DAY)));
        device.setDaysPerWeek(cursor.getInt(cursor.getColumnIndexOrThrow(DeviceTable.DAYS_PER_WEEK)));
        device.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DeviceTable.QUANTITY)));
        device.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DeviceTable.ACTIVE)) == 1);
        return device;
    }

    // --- Relevés de compteur ----------------------------------------------

    /** Relevés du plus ancien au plus récent, prêts pour {@code EnergyCalculator.toPeriods}. */
    public List<MeterReading> findAllReadings() {
        List<MeterReading> readings = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(ReadingTable.NAME, null, null, null, null, null,
                ReadingTable.TIMESTAMP + " ASC");
        try {
            while (cursor.moveToNext()) {
                readings.add(readReading(cursor));
            }
        } finally {
            cursor.close();
        }
        return readings;
    }

    public MeterReading findLatestReading() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(ReadingTable.NAME, null, null, null, null, null,
                ReadingTable.TIMESTAMP + " DESC", "1");
        try {
            return cursor.moveToFirst() ? readReading(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public long saveReading(MeterReading reading) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ReadingTable.TIMESTAMP, reading.getTimestampMillis());
        values.put(ReadingTable.INDEX_KWH, reading.getIndexKwh());
        values.put(ReadingTable.NOTE, reading.getNote());

        if (reading.isNew()) {
            long id = db.insert(ReadingTable.NAME, null, values);
            reading.setId(id);
            return id;
        }
        db.update(ReadingTable.NAME, values, ReadingTable.ID + " = ?",
                new String[]{String.valueOf(reading.getId())});
        return reading.getId();
    }

    public void deleteReading(long id) {
        helper.getWritableDatabase().delete(ReadingTable.NAME, ReadingTable.ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private MeterReading readReading(Cursor cursor) {
        MeterReading reading = new MeterReading();
        reading.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ReadingTable.ID)));
        reading.setTimestampMillis(cursor.getLong(cursor.getColumnIndexOrThrow(ReadingTable.TIMESTAMP)));
        reading.setIndexKwh(cursor.getDouble(cursor.getColumnIndexOrThrow(ReadingTable.INDEX_KWH)));
        reading.setNote(cursor.getString(cursor.getColumnIndexOrThrow(ReadingTable.NOTE)));
        return reading;
    }
}
