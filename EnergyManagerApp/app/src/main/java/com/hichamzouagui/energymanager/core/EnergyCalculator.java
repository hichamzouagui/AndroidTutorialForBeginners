package com.hichamzouagui.energymanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Cœur métier de l'application : toutes les conversions puissance / énergie / euros / CO2.
 * Classe utilitaire sans état, sans dépendance Android, couverte par des tests unitaires JVM.
 */
public final class EnergyCalculator {

    /** Moyenne annuelle (365,25 / 12) pour éviter les écarts entre mois de 28 et 31 jours. */
    public static final double DAYS_PER_MONTH = 30.4375;

    public static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    /** Seuil à partir duquel on prévient l'utilisateur qu'il approche de son budget. */
    public static final double BUDGET_WARNING_RATIO = 0.9;

    private EnergyCalculator() {
    }

    /** Énergie consommée en une journée d'utilisation, pour un appareil et sa quantité. */
    public static double dailyKwh(Appliance appliance) {
        checkAppliance(appliance);
        return appliance.getPowerWatts() / 1000d * appliance.getHoursPerDay() * appliance.getQuantity();
    }

    /** Énergie consommée sur une semaine, en tenant compte des jours d'utilisation. */
    public static double weeklyKwh(Appliance appliance) {
        return dailyKwh(appliance) * appliance.getDaysPerWeek();
    }

    /** Énergie consommée sur un mois moyen. */
    public static double monthlyKwh(Appliance appliance) {
        return dailyKwh(appliance) * (appliance.getDaysPerWeek() / 7d) * DAYS_PER_MONTH;
    }

    public static double annualKwh(Appliance appliance) {
        return monthlyKwh(appliance) * 12d;
    }

    /**
     * Agrège une liste d'appareils en une estimation mensuelle complète.
     * Les appareils inactifs sont ignorés ; la liste peut être vide.
     */
    public static ConsumptionSummary summarize(List<? extends Appliance> appliances, Tariff tariff) {
        if (tariff == null) {
            throw new IllegalArgumentException("Le tarif est obligatoire");
        }
        List<Appliance> active = new ArrayList<>();
        double totalKwh = 0d;
        if (appliances != null) {
            for (Appliance appliance : appliances) {
                if (appliance == null || !appliance.isActive()) {
                    continue;
                }
                active.add(appliance);
                totalKwh += monthlyKwh(appliance);
            }
        }

        List<ApplianceShare> breakdown = new ArrayList<>(active.size());
        for (Appliance appliance : active) {
            double kwh = monthlyKwh(appliance);
            double share = totalKwh > 0 ? kwh / totalKwh * 100d : 0d;
            breakdown.add(new ApplianceShare(appliance, kwh, tariff.energyCost(kwh), share));
        }
        Collections.sort(breakdown, new Comparator<ApplianceShare>() {
            @Override
            public int compare(ApplianceShare left, ApplianceShare right) {
                return Double.compare(right.getMonthlyKwh(), left.getMonthlyKwh());
            }
        });

        return new ConsumptionSummary(totalKwh, tariff.energyCost(totalKwh),
                tariff.getStandingChargePerMonth(), tariff.co2Kilograms(totalKwh), breakdown);
    }

    /** Découpe une suite de relevés (triés par date croissante) en périodes consécutives. */
    public static List<MeterPeriod> toPeriods(List<? extends MeterSnapshot> readings) {
        List<MeterPeriod> periods = new ArrayList<>();
        if (readings == null || readings.size() < 2) {
            return periods;
        }
        List<MeterSnapshot> sorted = new ArrayList<>(readings);
        Collections.sort(sorted, new Comparator<MeterSnapshot>() {
            @Override
            public int compare(MeterSnapshot left, MeterSnapshot right) {
                return Long.compare(left.getTimestampMillis(), right.getTimestampMillis());
            }
        });
        for (int i = 1; i < sorted.size(); i++) {
            MeterSnapshot previous = sorted.get(i - 1);
            MeterSnapshot current = sorted.get(i);
            if (current.getTimestampMillis() <= previous.getTimestampMillis()
                    || current.getIndexKwh() < previous.getIndexKwh()) {
                // Relevé incohérent (saisie erronée, changement de compteur) : on l'ignore.
                continue;
            }
            periods.add(new MeterPeriod(previous.getTimestampMillis(), current.getTimestampMillis(),
                    previous.getIndexKwh(), current.getIndexKwh()));
        }
        return periods;
    }

    /**
     * Consommation moyenne journalière mesurée sur les {@code maxPeriods} dernières périodes.
     * Retourne 0 si l'historique est insuffisant.
     */
    public static double measuredDailyAverage(List<MeterPeriod> periods, int maxPeriods) {
        if (periods == null || periods.isEmpty() || maxPeriods <= 0) {
            return 0d;
        }
        int from = Math.max(0, periods.size() - maxPeriods);
        double kwh = 0d;
        double days = 0d;
        for (int i = from; i < periods.size(); i++) {
            kwh += periods.get(i).getKwh();
            days += periods.get(i).getDays();
        }
        return days <= 0 ? 0d : kwh / days;
    }

    /** Facture projetée sur un mois à partir d'une moyenne journalière mesurée. */
    public static double projectedMonthlyBill(double dailyAverageKwh, Tariff tariff) {
        if (dailyAverageKwh < 0) {
            throw new IllegalArgumentException("La moyenne journaliere ne peut pas etre negative");
        }
        return tariff.energyCost(dailyAverageKwh * DAYS_PER_MONTH) + tariff.getStandingChargePerMonth();
    }

    /** Compare une facture projetée au budget mensuel (budget &lt;= 0 : non défini). */
    public static BudgetStatus budgetStatus(double projectedBill, double monthlyBudget) {
        if (monthlyBudget <= 0) {
            return BudgetStatus.NOT_SET;
        }
        if (projectedBill > monthlyBudget) {
            return BudgetStatus.OVER;
        }
        if (projectedBill >= monthlyBudget * BUDGET_WARNING_RATIO) {
            return BudgetStatus.NEAR;
        }
        return BudgetStatus.UNDER;
    }

    /** Part du budget déjà engagée, en pourcentage (0 si budget non défini). */
    public static double budgetUsagePercent(double projectedBill, double monthlyBudget) {
        return monthlyBudget <= 0 ? 0d : projectedBill / monthlyBudget * 100d;
    }

    /** Écart entre l'estimation issue des appareils et la mesure du compteur, en pourcentage. */
    public static double estimationGapPercent(double estimatedMonthlyKwh, double measuredMonthlyKwh) {
        if (measuredMonthlyKwh <= 0) {
            return 0d;
        }
        return (estimatedMonthlyKwh - measuredMonthlyKwh) / measuredMonthlyKwh * 100d;
    }

    /** Gain mensuel si l'appareil n'était utilisé que {@code targetHoursPerDay} heures par jour. */
    public static SavingsAdvice savingsIfReducedTo(Appliance appliance, double targetHoursPerDay,
                                                   Tariff tariff) {
        checkAppliance(appliance);
        if (targetHoursPerDay < 0) {
            throw new IllegalArgumentException("La duree cible ne peut pas etre negative");
        }
        double currentKwh = monthlyKwh(appliance);
        double reducedHours = Math.min(targetHoursPerDay, appliance.getHoursPerDay());
        double targetKwh = appliance.getPowerWatts() / 1000d * reducedHours * appliance.getQuantity()
                * (appliance.getDaysPerWeek() / 7d) * DAYS_PER_MONTH;
        double savedKwh = Math.max(0d, currentKwh - targetKwh);
        return new SavingsAdvice(appliance.getName(), savedKwh, tariff.energyCost(savedKwh),
                tariff.co2Kilograms(savedKwh * 12d));
    }

    /** Les {@code count} appareils les plus consommateurs, pour les conseils d'économie. */
    public static List<ApplianceShare> topConsumers(ConsumptionSummary summary, int count) {
        List<ApplianceShare> breakdown = summary.getBreakdown();
        if (count <= 0 || breakdown.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(breakdown.subList(0, Math.min(count, breakdown.size())));
    }

    private static void checkAppliance(Appliance appliance) {
        if (appliance == null) {
            throw new IllegalArgumentException("L'appareil est obligatoire");
        }
        if (appliance.getPowerWatts() < 0) {
            throw new IllegalArgumentException("La puissance ne peut pas etre negative");
        }
        if (appliance.getHoursPerDay() < 0 || appliance.getHoursPerDay() > 24) {
            throw new IllegalArgumentException("La duree doit etre comprise entre 0 et 24 heures");
        }
        if (appliance.getDaysPerWeek() < 0 || appliance.getDaysPerWeek() > 7) {
            throw new IllegalArgumentException("Le nombre de jours doit etre compris entre 0 et 7");
        }
        if (appliance.getQuantity() < 0) {
            throw new IllegalArgumentException("La quantite ne peut pas etre negative");
        }
    }
}
