package com.hichamzouagui.energymanager.data;

import com.hichamzouagui.energymanager.core.Appliance;

/** Appareil enregistré par l'utilisateur. Modèle simple, sans dépendance Android. */
public class Device implements Appliance {

    public static final long NO_ID = -1L;

    private long id = NO_ID;
    private String name;
    private String category;
    private double powerWatts;
    private double hoursPerDay;
    private int daysPerWeek = 7;
    private int quantity = 1;
    private boolean active = true;

    public Device() {
    }

    public Device(String name, String category, double powerWatts, double hoursPerDay,
                  int daysPerWeek, int quantity) {
        this.name = name;
        this.category = category;
        this.powerWatts = powerWatts;
        this.hoursPerDay = hoursPerDay;
        this.daysPerWeek = daysPerWeek;
        this.quantity = quantity;
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public double getPowerWatts() {
        return powerWatts;
    }

    public void setPowerWatts(double powerWatts) {
        this.powerWatts = powerWatts;
    }

    @Override
    public double getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(double hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    @Override
    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
