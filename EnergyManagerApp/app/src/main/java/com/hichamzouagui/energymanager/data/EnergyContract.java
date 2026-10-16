package com.hichamzouagui.energymanager.data;

/** Noms des tables et colonnes de la base locale. */
public final class EnergyContract {

    public static final String DATABASE_NAME = "energy_manager.db";
    public static final int DATABASE_VERSION = 1;

    private EnergyContract() {
    }

    public static final class DeviceTable {
        public static final String NAME = "devices";
        public static final String ID = "_id";
        public static final String LABEL = "name";
        public static final String CATEGORY = "category";
        public static final String POWER_WATTS = "power_watts";
        public static final String HOURS_PER_DAY = "hours_per_day";
        public static final String DAYS_PER_WEEK = "days_per_week";
        public static final String QUANTITY = "quantity";
        public static final String ACTIVE = "active";

        static final String CREATE = "CREATE TABLE " + NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LABEL + " TEXT NOT NULL, "
                + CATEGORY + " TEXT, "
                + POWER_WATTS + " REAL NOT NULL DEFAULT 0, "
                + HOURS_PER_DAY + " REAL NOT NULL DEFAULT 0, "
                + DAYS_PER_WEEK + " INTEGER NOT NULL DEFAULT 7, "
                + QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                + ACTIVE + " INTEGER NOT NULL DEFAULT 1)";

        private DeviceTable() {
        }
    }

    public static final class ReadingTable {
        public static final String NAME = "meter_readings";
        public static final String ID = "_id";
        public static final String TIMESTAMP = "taken_at";
        public static final String INDEX_KWH = "index_kwh";
        public static final String NOTE = "note";

        static final String CREATE = "CREATE TABLE " + NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TIMESTAMP + " INTEGER NOT NULL, "
                + INDEX_KWH + " REAL NOT NULL, "
                + NOTE + " TEXT)";

        private ReadingTable() {
        }
    }
}
