package com.example.afinal;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 专业级股票价格图（仅折线，动态左侧间距+周刻度）
 */
public class PriceChartView extends View {
    private List<Float> dataPoints = new ArrayList<>();

    private Paint linePaint, fillPaint, gridPaint, axisPaint, textPaint;
    private float maxValue, minValue;

    // 左侧 padding 动态计算，其余保持固定
    private float paddingL;
    private final float paddingR = 30f, paddingT = 40f, paddingB = 100f;

    public PriceChartView(Context c) {
        super(c);
        init();
    }

    public PriceChartView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    private void init() {
        // 折线画笔（带阴影）
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setColor(Color.parseColor("#3F51B5"));
        linePaint.setShadowLayer(8f, 0, 2f, Color.parseColor("#33000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, linePaint);

        // 填充渐变
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        // 网格（柔和灰色）
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(Color.parseColor("#22000000"));

        // 坐标轴
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);
        axisPaint.setColor(Color.parseColor("#888888"));

        // 文字
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28f);
        textPaint.setColor(Color.parseColor("#444444"));
    }

    /** 设置折线数据 */
    public void setData(List<Float> pts) {
        if (pts == null || pts.size() < 2) return;
        dataPoints.clear();
        dataPoints.addAll(pts);
        calcMinMax(pts);
        invalidate();
    }

    private void calcMinMax(List<Float> values) {
        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
        for (float v : values) {
            maxValue = Math.max(maxValue, v);
            minValue = Math.min(minValue, v);
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (dataPoints.size() < 2) return;

        // 1. 测量 Y 轴标签宽度，动态计算 paddingL
        float midValue = (maxValue + minValue) / 2;
        String[] yLabels = {
                String.format("%.2f", maxValue),
                String.format("%.2f", midValue),
                String.format("%.2f", minValue)
        };
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float maxLabelWidth = 0;
        for (String lbl : yLabels) {
            maxLabelWidth = Math.max(maxLabelWidth, textPaint.measureText(lbl));
        }
        paddingL = maxLabelWidth + 20f;

        float w = getWidth(), h = getHeight();
        float cw = w - paddingL - paddingR;
        float ch = h - paddingT - paddingB;
        float x0 = paddingL, y0 = paddingT + ch;

        // 2. 绘制网格（4×4）
        for (int i = 0; i <= 4; i++) {
            float yf = paddingT + ch * i / 4f;
            c.drawLine(x0, yf, x0 + cw, yf, gridPaint);
        }
        for (int i = 0; i <= 4; i++) {
            float xf = paddingL + cw * i / 4f;
            c.drawLine(xf, paddingT, xf, y0, gridPaint);
        }

        // 3. 画坐标轴
        c.drawLine(x0, paddingT, x0, y0, axisPaint);
        c.drawLine(x0, y0, x0 + cw, y0, axisPaint);

        // 4. 画折线 + 填充
        drawLineChart(c, x0, y0, cw, ch);

        // 5. 画 Y 轴标签（右对齐）
        for (int i = 0; i < 3; i++) {
            float yf = paddingT + ch * i / 2f;
            String txt = yLabels[i];
            float tx = paddingL - 10f;
            float ty = yf + textPaint.getTextSize() / 2f;
            c.drawText(txt, tx, ty, textPaint);
        }

        // 6. 画 X 轴“周”刻度（居中）
        textPaint.setTextAlign(Paint.Align.CENTER);
        int totalWeeks = dataPoints.size() - 1;
        for (int i = 0; i <= 4; i++) {
            float xf = paddingL + cw * i / 4f;
            int week = Math.round(totalWeeks * i / 4f);
            String txt = week + "周";
            float ty = y0 + textPaint.getTextSize() + 30f;
            c.drawText(txt, xf, ty, textPaint);
        }
    }

    /** 绘制平滑折线及渐变下填充 */
    private void drawLineChart(Canvas c, float x0, float y0, float cw, float ch) {
        // 渐变填充 Shader
        Shader shader = new LinearGradient(
                0, paddingT, 0, y0,
                new int[]{0x553F51B5, 0x003F51B5},
                null, Shader.TileMode.CLAMP
        );
        fillPaint.setShader(shader);

        Path linePath = new Path();
        Path fillPath = new Path();

        int n = dataPoints.size();
        List<PointF> pts = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            float x = x0 + cw * i / (n - 1f);
            float norm = (dataPoints.get(i) - minValue) / (maxValue - minValue);
            float y = y0 - norm * ch;
            pts.add(new PointF(x, y));
        }

        // 构造填充区和曲线路径
        fillPath.moveTo(pts.get(0).x, y0);
        fillPath.lineTo(pts.get(0).x, pts.get(0).y);
        linePath.moveTo(pts.get(0).x, pts.get(0).y);
        for (int i = 1; i < n; i++) {
            PointF prev = pts.get(i - 1), cur = pts.get(i);
            float cx = (prev.x + cur.x) / 2;
            float cy = (prev.y + cur.y) / 2;
            linePath.quadTo(prev.x, prev.y, cx, cy);
            linePath.quadTo(cur.x, cur.y, cur.x, cur.y);
            fillPath.quadTo(prev.x, prev.y, cx, cy);
            fillPath.quadTo(cur.x, cur.y, cur.x, cur.y);
        }
        fillPath.lineTo(pts.get(n - 1).x, y0);
        fillPath.close();

        c.drawPath(fillPath, fillPaint);
        c.drawPath(linePath, linePaint);
    }
}
