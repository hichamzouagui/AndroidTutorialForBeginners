package com.hichamzouagui.energymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Création et migration de la base SQLite locale. */
public class EnergyDatabaseHelper extends SQLiteOpenHelper {

    public EnergyDatabaseHelper(Context context) {
        super(context, EnergyContract.DATABASE_NAME, null, EnergyContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EnergyContract.DeviceTable.CREATE);
        db.execSQL(EnergyContract.ReadingTable.CREATE);
        insertStarterDevices(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 : aucune migration à gérer pour l'instant.
    }

    /**
     * Quelques appareils typiques pour que le tableau de bord ne soit pas vide
     * à la première ouverture. L'utilisateur peut les modifier ou les supprimer.
     */
    private void insertStarterDevices(SQLiteDatabase db) {
        insert(db, new Device("Réfrigérateur", "Froid", 150, 24, 7, 1));
        insert(db, new Device("Lave-linge", "Lavage", 2000, 1.5, 4, 1));
        insert(db, new Device("Box internet", "Multimédia", 15, 24, 7, 1));
        insert(db, new Device("Éclairage LED", "Éclairage", 9, 5, 7, 8));
        insert(db, new Device("Chauffe-eau", "Eau chaude", 2200, 3, 7, 1));
    }

    private void insert(SQLiteDatabase db, Device device) {
        ContentValues values = new ContentValues();
        values.put(EnergyContract.DeviceTable.LABEL, device.getName());
        values.put(EnergyContract.DeviceTable.CATEGORY, device.getCategory());
        values.put(EnergyContract.DeviceTable.POWER_WATTS, device.getPowerWatts());
        values.put(EnergyContract.DeviceTable.HOURS_PER_DAY, device.getHoursPerDay());
        values.put(EnergyContract.DeviceTable.DAYS_PER_WEEK, device.getDaysPerWeek());
        values.put(EnergyContract.DeviceTable.QUANTITY, device.getQuantity());
        values.put(EnergyContract.DeviceTable.ACTIVE, 1);
        db.insert(EnergyContract.DeviceTable.NAME, null, values);
    }
}
