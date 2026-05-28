package com.example.cinemabooking.ui.admin.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminLineChartView extends View {

    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint pointPaint;
    private Paint gridPaint;
    private Path linePath;
    private Path fillPath;

    private List<Float> dataPoints = new ArrayList<>();
    private List<String> xLabels = new ArrayList<>();

    public AdminLineChartView(Context context) {
        super(context);
        init();
    }

    public AdminLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#7B39F0"));
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#7B39F0"));
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#7A757F"));
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#ECE8F0"));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        linePath = new Path();
        fillPath = new Path();
    }

    public void setData(List<Float> data, List<String> labels) {
        this.dataPoints.clear();
        this.dataPoints.addAll(data);
        this.xLabels.clear();
        this.xLabels.addAll(labels);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints.isEmpty()) {
            canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 60f;
        float paddingRight = 40f;
        float paddingTop = 40f;
        float paddingBottom = 60f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        float maxData = Collections.max(dataPoints);
        if (maxData == 0) maxData = 10f; // Default max if all zeroes
        else maxData = maxData * 1.2f; // Add some headroom

        // Draw background grid lines (horizontal)
        int numGrids = 4;
        for (int i = 0; i <= numGrids; i++) {
            float y = paddingTop + chartHeight - (i * chartHeight / numGrids);
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            
            // Draw Y axis labels
            float val = (i * maxData / numGrids);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((int)val), paddingLeft - 10f, y + 10f, textPaint);
        }

        int numPoints = dataPoints.size();
        float xStep = numPoints > 1 ? chartWidth / (numPoints - 1) : chartWidth;

        linePath.reset();
        fillPath.reset();

        float[] xCoords = new float[numPoints];
        float[] yCoords = new float[numPoints];

        for (int i = 0; i < numPoints; i++) {
            float val = dataPoints.get(i);
            xCoords[i] = paddingLeft + (i * xStep);
            yCoords[i] = paddingTop + chartHeight - (val / maxData * chartHeight);

            if (i == 0) {
                linePath.moveTo(xCoords[i], yCoords[i]);
                fillPath.moveTo(xCoords[i], paddingTop + chartHeight);
                fillPath.lineTo(xCoords[i], yCoords[i]);
            } else {
                linePath.lineTo(xCoords[i], yCoords[i]);
                fillPath.lineTo(xCoords[i], yCoords[i]);
            }

            // Draw X axis label
            textPaint.setTextAlign(Paint.Align.CENTER);
            if (i < xLabels.size()) {
                canvas.drawText(xLabels.get(i), xCoords[i], height - 10f, textPaint);
            }
        }

        if (numPoints > 0) {
            fillPath.lineTo(xCoords[numPoints - 1], paddingTop + chartHeight);
            fillPath.close();

            // Gradient for fill
            LinearGradient gradient = new LinearGradient(
                    0, paddingTop, 0, paddingTop + chartHeight,
                    Color.parseColor("#4D7B39F0"),
                    Color.parseColor("#007B39F0"),
                    Shader.TileMode.CLAMP
            );
            fillPaint.setShader(gradient);

            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(linePath, linePaint);

            // Draw points
            for (int i = 0; i < numPoints; i++) {
                canvas.drawCircle(xCoords[i], yCoords[i], 8f, pointPaint);
            }
        }
    }
}
