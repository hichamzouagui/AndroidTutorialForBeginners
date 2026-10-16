package com.hichamzouagui.energymanager.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Histogramme simple, dessiné à la main pour éviter une dépendance externe.
 * Affiche une barre par période de consommation, avec son libellé et sa valeur.
 */
public class BarChartView extends View {

    /** Une barre : un libellé d'axe et une valeur positive. */
    public static class Bar {
        public final String label;
        public final double value;

        public Bar(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<Bar> bars = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private String emptyMessage = "";
    private String valueSuffix = "";
    private double referenceValue = 0d;

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;

        barPaint.setColor(Color.parseColor("#2E7D32"));
        gridPaint.setColor(Color.parseColor("#33000000"));
        gridPaint.setStrokeWidth(density);

        labelPaint.setColor(Color.parseColor("#8A000000"));
        labelPaint.setTextSize(11 * density);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(Color.parseColor("#DE000000"));
        valuePaint.setTextSize(12 * density);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        emptyPaint.setColor(Color.parseColor("#8A000000"));
        emptyPaint.setTextSize(13 * density);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setBarColor(int color) {
        barPaint.setColor(color);
        invalidate();
    }

    public void setEmptyMessage(String message) {
        this.emptyMessage = message == null ? "" : message;
        invalidate();
    }

    /** Suffixe affiché au-dessus des barres, par exemple « kWh ». */
    public void setValueSuffix(String suffix) {
        this.valueSuffix = suffix == null ? "" : suffix;
        invalidate();
    }

    /** Ligne horizontale de repère, par exemple la moyenne ou un objectif. */
    public void setReferenceValue(double referenceValue) {
        this.referenceValue = Math.max(0d, referenceValue);
        invalidate();
    }

    public void setBars(List<Bar> newBars) {
        bars.clear();
        if (newBars != null) {
            bars.addAll(newBars);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float width = getWidth();
        float height = getHeight();

        if (bars.isEmpty()) {
            canvas.drawText(emptyMessage, width / 2f, height / 2f, emptyPaint);
            return;
        }

        float topPadding = 18 * density;
        float bottomPadding = 20 * density;
        float sidePadding = 8 * density;
        float plotHeight = height - topPadding - bottomPadding;
        float baseline = height - bottomPadding;

        double max = Math.max(referenceValue, 0d);
        for (Bar bar : bars) {
            max = Math.max(max, bar.value);
        }
        if (max <= 0) {
            canvas.drawText(emptyMessage, width / 2f, height / 2f, emptyPaint);
            return;
        }

        canvas.drawLine(sidePadding, baseline, width - sidePadding, baseline, gridPaint);

        if (referenceValue > 0) {
            float y = baseline - (float) (referenceValue / max) * plotHeight;
            canvas.drawLine(sidePadding, y, width - sidePadding, y, gridPaint);
        }

        float slot = (width - 2 * sidePadding) / bars.size();
        float barWidth = Math.min(slot * 0.6f, 48 * density);
        float corner = 4 * density;

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            float center = sidePadding + slot * i + slot / 2f;
            float barHeight = (float) (Math.max(0d, bar.value) / max) * plotHeight;

            barRect.set(center - barWidth / 2f, baseline - barHeight, center + barWidth / 2f, baseline);
            canvas.drawRoundRect(barRect, corner, corner, barPaint);

            canvas.drawText(formatValue(bar.value), center, barRect.top - 5 * density, valuePaint);
            canvas.drawText(bar.label, center, baseline + 14 * density, labelPaint);
        }
    }

    private String formatValue(double value) {
        String rounded = value >= 100 ? String.valueOf(Math.round(value))
                : String.format(java.util.Locale.FRANCE, "%.1f", value);
        return valueSuffix.isEmpty() ? rounded : rounded + " " + valueSuffix;
    }
}
