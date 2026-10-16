package com.hichamzouagui.energymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hichamzouagui.energymanager.R;
import com.hichamzouagui.energymanager.core.ApplianceShare;
import com.hichamzouagui.energymanager.core.BudgetStatus;
import com.hichamzouagui.energymanager.core.ConsumptionSummary;
import com.hichamzouagui.energymanager.core.EnergyCalculator;
import com.hichamzouagui.energymanager.core.MeterPeriod;
import com.hichamzouagui.energymanager.core.SavingsAdvice;
import com.hichamzouagui.energymanager.core.Tariff;
import com.hichamzouagui.energymanager.data.Device;
import com.hichamzouagui.energymanager.data.EnergyRepository;
import com.hichamzouagui.energymanager.data.MeterReading;
import com.hichamzouagui.energymanager.prefs.UserSettings;
import com.hichamzouagui.energymanager.ui.widget.BarChartView;
import com.hichamzouagui.energymanager.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Synthèse : estimation mensuelle, budget, consommation mesurée et pistes d'économie. */
public class DashboardFragment extends Fragment {

    /** Nombre de périodes de compteur affichées dans l'histogramme. */
    private static final int CHART_PERIODS = 6;

    /** Réduction simulée pour le conseil d'économie : 25 % de temps d'usage en moins. */
    private static final double SAVINGS_REDUCTION_RATIO = 0.75;

    private TextView monthlyKwhView;
    private TextView monthlyBillView;
    private TextView co2View;
    private TextView annualBillView;
    private TextView budgetLabelView;
    private ProgressBar budgetProgressView;
    private TextView measuredView;
    private TextView gapView;
    private BarChartView chartView;
    private LinearLayout breakdownContainer;
    private TextView breakdownEmptyView;
    private TextView adviceView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        monthlyKwhView = root.findViewById(R.id.monthly_kwh);
        monthlyBillView = root.findViewById(R.id.monthly_bill);
        co2View = root.findViewById(R.id.monthly_co2);
        annualBillView = root.findViewById(R.id.annual_bill);
        budgetLabelView = root.findViewById(R.id.budget_label);
        budgetProgressView = root.findViewById(R.id.budget_progress);
        measuredView = root.findViewById(R.id.measured_average);
        gapView = root.findViewById(R.id.estimation_gap);
        chartView = root.findViewById(R.id.consumption_chart);
        breakdownContainer = root.findViewById(R.id.breakdown_container);
        breakdownEmptyView = root.findViewById(R.id.breakdown_empty);
        adviceView = root.findViewById(R.id.advice);

        chartView.setEmptyMessage(getString(R.string.chart_empty));
        chartView.setValueSuffix(getString(R.string.unit_kwh));
        chartView.setBarColor(ContextCompat.getColor(requireContext(), R.color.primary));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        EnergyRepository repository = EnergyRepository.get(requireContext());
        UserSettings settings = new UserSettings(requireContext());
        Tariff tariff = settings.getTariff();
        double budget = settings.getMonthlyBudget();

        List<Device> devices = repository.findAllDevices();
        ConsumptionSummary summary = EnergyCalculator.summarize(devices, tariff);

        monthlyKwhView.setText(Formatters.kwh(summary.getMonthlyKwh()));
        monthlyBillView.setText(Formatters.euros(summary.getMonthlyBill()));
        co2View.setText(Formatters.co2(summary.getCo2Kilograms()));
        annualBillView.setText(getString(R.string.dashboard_annual_bill,
                Formatters.euros(summary.getAnnualBill()), Formatters.kwh(summary.getAnnualKwh())));

        bindBudget(summary, budget);
        bindMeasured(repository.findAllReadings(), summary, tariff, budget);
        bindBreakdown(summary);
        bindAdvice(summary, tariff);
    }

    private void bindBudget(ConsumptionSummary summary, double budget) {
        BudgetStatus status = EnergyCalculator.budgetStatus(summary.getMonthlyBill(), budget);
        if (status == BudgetStatus.NOT_SET) {
            budgetLabelView.setText(R.string.dashboard_budget_not_set);
            budgetLabelView.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_primary));
            budgetProgressView.setProgress(0);
            budgetProgressView.setVisibility(View.GONE);
            return;
        }

        double usage = EnergyCalculator.budgetUsagePercent(summary.getMonthlyBill(), budget);
        budgetProgressView.setVisibility(View.VISIBLE);
        budgetProgressView.setProgress((int) Math.min(100, Math.round(usage)));
        budgetLabelView.setText(getString(R.string.dashboard_budget_usage,
                Formatters.euros(summary.getMonthlyBill()), Formatters.euros(budget),
                Formatters.percent(usage)));

        int colorRes = status == BudgetStatus.OVER ? R.color.danger
                : status == BudgetStatus.NEAR ? R.color.warning : R.color.primary;
        budgetLabelView.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void bindMeasured(List<MeterReading> readings, ConsumptionSummary summary,
                              Tariff tariff, double budget) {
        List<MeterPeriod> periods = EnergyCalculator.toPeriods(readings);
        double dailyAverage = EnergyCalculator.measuredDailyAverage(periods, CHART_PERIODS);

        if (periods.isEmpty()) {
            measuredView.setText(R.string.dashboard_no_reading);
            gapView.setVisibility(View.GONE);
        } else {
            double projectedBill = EnergyCalculator.projectedMonthlyBill(dailyAverage, tariff);
            double projectedKwh = dailyAverage * EnergyCalculator.DAYS_PER_MONTH;
            measuredView.setText(getString(R.string.dashboard_measured,
                    Formatters.kwhPerDay(dailyAverage), Formatters.kwh(projectedKwh),
                    Formatters.euros(projectedBill)));

            double gap = EnergyCalculator.estimationGapPercent(summary.getMonthlyKwh(), projectedKwh);
            gapView.setVisibility(View.VISIBLE);
            gapView.setText(getString(gap >= 0 ? R.string.dashboard_gap_over : R.string.dashboard_gap_under,
                    Formatters.percent(Math.abs(gap))));
        }

        List<BarChartView.Bar> bars = new ArrayList<>();
        int from = Math.max(0, periods.size() - CHART_PERIODS);
        for (int i = from; i < periods.size(); i++) {
            MeterPeriod period = periods.get(i);
            bars.add(new BarChartView.Bar(Formatters.shortDate(period.getEndMillis()),
                    period.getProjectedMonthlyKwh()));
        }
        chartView.setBars(bars);
        // Repère : le budget converti en kWh s'il est défini, sinon l'estimation des appareils.
        double reference = budget > 0 && tariff.getPricePerKwh() > 0
                ? Math.max(0d, (budget - tariff.getStandingChargePerMonth()) / tariff.getPricePerKwh())
                : summary.getMonthlyKwh();
        chartView.setReferenceValue(reference);
    }

    private void bindBreakdown(ConsumptionSummary summary) {
        breakdownContainer.removeAllViews();
        List<ApplianceShare> breakdown = summary.getBreakdown();

        if (breakdown.isEmpty()) {
            breakdownEmptyView.setVisibility(View.VISIBLE);
            return;
        }
        breakdownEmptyView.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ApplianceShare share : breakdown) {
            View row = inflater.inflate(R.layout.item_breakdown, breakdownContainer, false);
            ((TextView) row.findViewById(R.id.breakdown_name)).setText(share.getName());
            ((TextView) row.findViewById(R.id.breakdown_values)).setText(
                    getString(R.string.dashboard_breakdown_values,
                            Formatters.kwh(share.getMonthlyKwh()),
                            Formatters.euros(share.getMonthlyCost())));
            ((TextView) row.findViewById(R.id.breakdown_share)).setText(
                    Formatters.percent(share.getSharePercent()));
            ProgressBar bar = row.findViewById(R.id.breakdown_bar);
            bar.setProgress((int) Math.round(share.getSharePercent()));
            breakdownContainer.addView(row);
        }
    }

    private void bindAdvice(ConsumptionSummary summary, Tariff tariff) {
        ApplianceShare top = summary.getTopConsumer();
        if (top == null) {
            adviceView.setText(R.string.dashboard_advice_empty);
            return;
        }
        double targetHours = top.getAppliance().getHoursPerDay() * SAVINGS_REDUCTION_RATIO;
        SavingsAdvice advice = EnergyCalculator.savingsIfReducedTo(top.getAppliance(),
                targetHours, tariff);
        adviceView.setText(getString(R.string.dashboard_advice, advice.getApplianceName(),
                Formatters.hours(top.getAppliance().getHoursPerDay() - targetHours),
                Formatters.euros(advice.getMoneySavedPerYear()),
                Formatters.co2(advice.getCo2SavedPerYear())));
    }
}
