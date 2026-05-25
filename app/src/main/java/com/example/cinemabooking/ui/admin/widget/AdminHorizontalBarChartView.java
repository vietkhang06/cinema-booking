package com.example.cinemabooking.ui.admin.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminHorizontalBarChartView extends View {

    private Paint barPaint;
    private Paint bgBarPaint;
    private Paint textPaint;
    private Paint valuePaint;

    private List<String> labels = new ArrayList<>();
    private List<Float> values = new ArrayList<>();

    public AdminHorizontalBarChartView(Context context) {
        super(context);
        init();
    }

    public AdminHorizontalBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(Color.parseColor("#E91E63")); // Pinkish red for movies
        barPaint.setStyle(Paint.Style.FILL);

        bgBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBarPaint.setColor(Color.parseColor("#F6F4F8")); // Light background
        bgBarPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1E1A23"));
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.LEFT);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(Color.parseColor("#7A757F"));
        valuePaint.setTextSize(28f);
        valuePaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setData(List<String> labels, List<Float> values) {
        this.labels.clear();
        this.labels.addAll(labels);
        this.values.clear();
        this.values.addAll(values);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) {
            canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, valuePaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 40f;
        float paddingRight = 40f;
        float paddingTop = 40f;
        float paddingBottom = 40f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        float maxData = Collections.max(values);
        if (maxData == 0) maxData = 10f; // Default

        int numBars = values.size();
        float barHeight = 40f;
        float spacing = (chartHeight - (numBars * barHeight)) / (numBars + 1);

        RectF rect = new RectF();
        
        for (int i = 0; i < numBars; i++) {
            float val = values.get(i);
            String label = i < labels.size() ? labels.get(i) : "";

            float y = paddingTop + spacing + (i * (barHeight + spacing));

            // Prepare texts
            String valueStr = String.valueOf((int)val) + " vé";
            float valueWidth = valuePaint.measureText(valueStr);
            
            // Truncate label if it is too long
            float maxLabelWidth = chartWidth * 0.65f; // Limit label to 65% of chart width to ensure it never overflows
            if (maxLabelWidth > 0) {
                android.text.TextPaint tp = new android.text.TextPaint(textPaint);
                label = android.text.TextUtils.ellipsize(label, tp, maxLabelWidth, android.text.TextUtils.TruncateAt.END).toString();
            }

            // Draw label above the bar
            canvas.drawText(label, paddingLeft, y - 10f, textPaint);
            
            // Draw value text aligned right
            canvas.drawText(valueStr, width - paddingRight, y - 10f, valuePaint);

            // Background bar (100% width)
            rect.set(paddingLeft, y, width - paddingRight, y + barHeight);
            canvas.drawRoundRect(rect, barHeight / 2f, barHeight / 2f, bgBarPaint);

            // Value bar
            float barWidth = (val / maxData) * chartWidth;
            if (barWidth < barHeight) barWidth = barHeight; // Ensure it looks rounded even if value is small
            rect.set(paddingLeft, y, paddingLeft + barWidth, y + barHeight);
            canvas.drawRoundRect(rect, barHeight / 2f, barHeight / 2f, barPaint);
        }
    }
}
