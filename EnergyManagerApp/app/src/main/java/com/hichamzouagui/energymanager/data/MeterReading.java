package com.hichamzouagui.energymanager.data;

import com.hichamzouagui.energymanager.core.MeterSnapshot;

/** Relevé de compteur saisi par l'utilisateur. */
public class MeterReading implements MeterSnapshot {

    public static final long NO_ID = -1L;

    private long id = NO_ID;
    private long timestampMillis;
    private double indexKwh;
    private String note;

    public MeterReading() {
    }

    public MeterReading(long timestampMillis, double indexKwh, String note) {
        this.timestampMillis = timestampMillis;
        this.indexKwh = indexKwh;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == NO_ID;
    }

    @Override
    public long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    @Override
    public double getIndexKwh() {
        return indexKwh;
    }

    public void setIndexKwh(double indexKwh) {
        this.indexKwh = indexKwh;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
