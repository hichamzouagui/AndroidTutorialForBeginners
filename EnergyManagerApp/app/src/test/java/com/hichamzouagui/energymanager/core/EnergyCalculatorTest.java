package com.hichamzouagui.energymanager.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hichamzouagui.energymanager.data.Device;
import com.hichamzouagui.energymanager.data.MeterReading;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Tests JVM de la logique métier : aucune dépendance Android requise. */
public class EnergyCalculatorTest {

    private static final double DELTA = 1e-9;
    private static final long DAY = EnergyCalculator.MILLIS_PER_DAY;
    private static final long T0 = 1_700_000_000_000L;

    private final Tariff tariff = new Tariff(0.25, 12.0, 60.0);

    private static Device washer() {
        return new Device("Lave-linge", "Lavage", 2000, 1.5, 4, 1);
    }

    private static Device fridge() {
        return new Device("Réfrigérateur", "Froid", 150, 24, 7, 1);
    }

    private static Device bulbs() {
        return new Device("Ampoules salon", "Éclairage", 9, 5, 7, 6);
    }

    @Test
    public void dailyConsumptionUsesPowerAndDuration() {
        assertEquals(3.0, EnergyCalculator.dailyKwh(washer()), DELTA);
    }

    @Test
    public void dailyConsumptionMultipliesByQuantity() {
        assertEquals(0.27, EnergyCalculator.dailyKwh(bulbs()), DELTA);
    }

    @Test
    public void weeklyConsumptionUsesDaysPerWeek() {
        assertEquals(12.0, EnergyCalculator.weeklyKwh(washer()), DELTA);
    }

    @Test
    public void monthlyConsumptionUsesAverageMonthLength() {
        assertEquals(3.0 * 4 / 7d * EnergyCalculator.DAYS_PER_MONTH,
                EnergyCalculator.monthlyKwh(washer()), DELTA);
        assertEquals(3.6 * EnergyCalculator.DAYS_PER_MONTH,
                EnergyCalculator.monthlyKwh(fridge()), DELTA);
    }

    @Test
    public void annualConsumptionIsTwelveMonths() {
        Device device = washer();
        assertEquals(EnergyCalculator.monthlyKwh(device) * 12,
                EnergyCalculator.annualKwh(device), DELTA);
    }

    @Test
    public void summaryIgnoresInactiveDevices() {
        Device unplugged = new Device("Climatiseur", "Chauffage", 2500, 6, 7, 1);
        unplugged.setActive(false);
        List<Device> devices = Arrays.asList(washer(), fridge(), unplugged);

        ConsumptionSummary summary = EnergyCalculator.summarize(devices, tariff);

        assertEquals(2, summary.getBreakdown().size());
        assertEquals(EnergyCalculator.monthlyKwh(washer()) + EnergyCalculator.monthlyKwh(fridge()),
                summary.getMonthlyKwh(), DELTA);
    }

    @Test
    public void summaryComputesCostCo2AndStandingCharge() {
        ConsumptionSummary summary = EnergyCalculator.summarize(
                Collections.singletonList(fridge()), tariff);
        double kwh = EnergyCalculator.monthlyKwh(fridge());

        assertEquals(kwh * 0.25, summary.getEnergyCost(), DELTA);
        assertEquals(kwh * 0.25 + 12.0, summary.getMonthlyBill(), DELTA);
        assertEquals((kwh * 0.25 + 12.0) * 12, summary.getAnnualBill(), DELTA);
        assertEquals(kwh * 12, summary.getAnnualKwh(), DELTA);
        assertEquals(kwh * 60 / 1000d, summary.getCo2Kilograms(), DELTA);
    }

    @Test
    public void breakdownIsSortedAndSharesAddUpToHundred() {
        ConsumptionSummary summary = EnergyCalculator.summarize(
                Arrays.asList(bulbs(), washer(), fridge()), tariff);

        double total = 0;
        double previous = Double.MAX_VALUE;
        for (ApplianceShare share : summary.getBreakdown()) {
            total += share.getSharePercent();
            assertTrue("détail non trié", share.getMonthlyKwh() <= previous);
            previous = share.getMonthlyKwh();
        }
        assertEquals(100.0, total, DELTA);
        assertEquals("Réfrigérateur", summary.getTopConsumer().getName());
    }

    @Test
    public void emptyHouseholdStillPaysStandingCharge() {
        ConsumptionSummary summary = EnergyCalculator.summarize(new ArrayList<Device>(), tariff);

        assertEquals(0.0, summary.getMonthlyKwh(), DELTA);
        assertEquals(12.0, summary.getMonthlyBill(), DELTA);
        assertNull(summary.getTopConsumer());
    }

    @Test
    public void summaryToleratesNullList() {
        assertEquals(0.0, EnergyCalculator.summarize(null, tariff).getMonthlyKwh(), DELTA);
    }

    @Test
    public void topConsumersIsCappedToAvailableDevices() {
        ConsumptionSummary summary = EnergyCalculator.summarize(
                Arrays.asList(bulbs(), washer(), fridge()), tariff);

        assertEquals(2, EnergyCalculator.topConsumers(summary, 2).size());
        assertEquals(3, EnergyCalculator.topConsumers(summary, 10).size());
        assertEquals(0, EnergyCalculator.topConsumers(summary, 0).size());
    }

    @Test
    public void readingsAreSortedBeforeBuildingPeriods() {
        List<MeterReading> readings = Arrays.asList(
                new MeterReading(T0 + 60 * DAY, 1300.0, "juin"),
                new MeterReading(T0, 1000.0, "avril"),
                new MeterReading(T0 + 30 * DAY, 1150.0, "mai"));

        List<MeterPeriod> periods = EnergyCalculator.toPeriods(readings);

        assertEquals(2, periods.size());
        assertEquals(150.0, periods.get(0).getKwh(), DELTA);
        assertEquals(30.0, periods.get(0).getDays(), DELTA);
        assertEquals(5.0, periods.get(0).getDailyAverageKwh(), DELTA);
        assertEquals(5.0 * EnergyCalculator.DAYS_PER_MONTH,
                periods.get(0).getProjectedMonthlyKwh(), DELTA);
        assertEquals(37.5, periods.get(0).getCost(tariff), DELTA);
    }

    @Test
    public void singleReadingProducesNoPeriod() {
        assertEquals(0, EnergyCalculator.toPeriods(
                Collections.singletonList(new MeterReading(T0, 10, null))).size());
        assertEquals(0, EnergyCalculator.toPeriods(null).size());
    }

    @Test
    public void inconsistentReadingIsSkippedInsteadOfCrashing() {
        List<MeterReading> readings = Arrays.asList(
                new MeterReading(T0, 1000.0, null),
                new MeterReading(T0 + 10 * DAY, 900.0, "compteur remplacé"),
                new MeterReading(T0 + 20 * DAY, 1100.0, null));

        assertEquals(1, EnergyCalculator.toPeriods(readings).size());
    }

    @Test
    public void measuredAverageWeighsPeriodsByTheirLength() {
        List<MeterPeriod> periods = Arrays.asList(
                new MeterPeriod(T0, T0 + 30 * DAY, 1000, 1150),
                new MeterPeriod(T0 + 30 * DAY, T0 + 60 * DAY, 1150, 1300));

        assertEquals(5.0, EnergyCalculator.measuredDailyAverage(periods, 2), DELTA);
        assertEquals(5.0, EnergyCalculator.measuredDailyAverage(periods, 1), DELTA);
        assertEquals(0.0, EnergyCalculator.measuredDailyAverage(
                new ArrayList<MeterPeriod>(), 3), DELTA);
    }

    @Test
    public void projectedBillAddsStandingCharge() {
        assertEquals(5.0 * EnergyCalculator.DAYS_PER_MONTH * 0.25 + 12.0,
                EnergyCalculator.projectedMonthlyBill(5.0, tariff), DELTA);
    }

    @Test
    public void budgetStatusFollowsWarningThreshold() {
        assertEquals(BudgetStatus.NOT_SET, EnergyCalculator.budgetStatus(50, 0));
        assertEquals(BudgetStatus.UNDER, EnergyCalculator.budgetStatus(50, 100));
        assertEquals(BudgetStatus.NEAR, EnergyCalculator.budgetStatus(90, 100));
        assertEquals(BudgetStatus.NEAR, EnergyCalculator.budgetStatus(100, 100));
        assertEquals(BudgetStatus.OVER, EnergyCalculator.budgetStatus(101, 100));
    }

    @Test
    public void budgetUsageIsZeroWhenNoBudgetIsSet() {
        assertEquals(75.0, EnergyCalculator.budgetUsagePercent(75, 100), DELTA);
        assertEquals(0.0, EnergyCalculator.budgetUsagePercent(75, 0), DELTA);
    }

    @Test
    public void estimationGapComparesEstimateToMeasure() {
        assertEquals(25.0, EnergyCalculator.estimationGapPercent(250, 200), DELTA);
        assertEquals(0.0, EnergyCalculator.estimationGapPercent(250, 0), DELTA);
    }

    @Test
    public void savingsAreComputedOnTheReducedDuration() {
        SavingsAdvice advice = EnergyCalculator.savingsIfReducedTo(fridge(), 20, tariff);
        double savedKwh = 0.150 * 4 * EnergyCalculator.DAYS_PER_MONTH;

        assertEquals(savedKwh, advice.getKwhSavedPerMonth(), DELTA);
        assertEquals(savedKwh * 0.25, advice.getMoneySavedPerMonth(), DELTA);
        assertEquals(savedKwh * 0.25 * 12, advice.getMoneySavedPerYear(), DELTA);
        assertEquals(savedKwh * 12 * 60 / 1000d, advice.getCo2SavedPerYear(), DELTA);
        assertEquals("Réfrigérateur", advice.getApplianceName());
    }

    @Test
    public void noSavingsWhenTargetIsLongerThanCurrentUsage() {
        assertEquals(0.0, EnergyCalculator.savingsIfReducedTo(fridge(), 30, tariff)
                .getKwhSavedPerMonth(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePowerIsRejected() {
        EnergyCalculator.monthlyKwh(new Device("KO", "x", -5, 1, 7, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreThanTwentyFourHoursIsRejected() {
        EnergyCalculator.monthlyKwh(new Device("KO", "x", 100, 25, 7, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreThanSevenDaysIsRejected() {
        EnergyCalculator.monthlyKwh(new Device("KO", "x", 100, 2, 8, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePriceIsRejected() {
        new Tariff(-1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reversedPeriodIsRejected() {
        new MeterPeriod(2000, 1000, 10, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decreasingMeterIndexIsRejected() {
        new MeterPeriod(1000, 2000, 20, 10);
    }
}
