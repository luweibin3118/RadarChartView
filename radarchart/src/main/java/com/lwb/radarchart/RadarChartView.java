package com.lwb.radarchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/26.
 */
public class RadarChartView extends View implements GestureDetector.OnGestureListener {

    private Paint mPaint;

    private Path mPath;

    private Point[][] points;

    private int radius;

    private double alpha;

    private List<TypeData> typeDataList;

    private List<TypeData> secondTypeDataList;

    private Point centerPoint;

    private Config config;

    private Point[] valuePoints;

    private Point[] secondValuePoints;

    private Point[] textPoints;

    private GestureDetector mGestureDetector;

    private double offSet = 0d, scrollOffSet = 0d, tempOffset = 0d;

    private Point startPoint, scrollPoint;

    private boolean isTouchDown = false;

    private double PIx2 = Math.PI * 2;

    public RadarChartView(Context context) {
        super(context);
        init();
    }

    public RadarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPath = new Path();
        if (typeDataList == null) {
            typeDataList = new ArrayList<>();
        }
        mGestureDetector = new GestureDetector(getContext(), this);
        mGestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!canRefresh()) {
            return;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        centerPoint = new Point(width / 2, height / 2);
        radius = (int) ((Math.min(width, height) / 2) * config.chartWidget);
        alpha = PIx2 / typeDataList.size();
        measurePoint();
    }

    private void measurePoint() {
        int pointsX1 = points.length;
        for (int i = 0; i < pointsX1; i++) {
            int pointsX2 = points[i].length;
            for (int j = 0; j < pointsX2; j++) {
                float r = radius / config.maxLevel * (i + 1);
                double p = -(alpha * j + Math.PI + offSet);
                int x = (int) (r * Math.sin(p) + centerPoint.x);
                int y = (int) (r * Math.cos(p) + centerPoint.y);
                points[i][j] = new Point(x, y);
            }
        }
        int valueLength = valuePoints.length;
        for (int i = 0; i < valueLength; i++) {
            TypeData typeData = typeDataList.get(i);
            float value = radius * ((float) typeData.typeValue / (float) typeData.typeMaxValue);
            double p = -(alpha * (i) + Math.PI + offSet);
            int x = (int) (value * Math.sin(p) + centerPoint.x);
            int y = (int) (value * Math.cos(p) + centerPoint.y);
            valuePoints[i] = new Point(x, y);
            x = (int) (radius * config.textPosition * Math.sin(p) + centerPoint.x);
            y = (int) (radius * config.textPosition * Math.cos(p) + centerPoint.y);
            textPoints[i] = new Point(x, y);

            if (secondTypeDataList != null) {
                TypeData secondTypeData = secondTypeDataList.get(i);
                value = radius * ((float) secondTypeData.typeValue / (float) secondTypeData.typeMaxValue);
                int x2 = (int) (value * Math.sin(p) + centerPoint.x);
                int y2 = (int) (value * Math.cos(p) + centerPoint.y);
                secondValuePoints[i] = new Point(x2, y2);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canRefresh()) {
            return;
        }
        drawBackground(canvas);
        drawValue(canvas);
        drawValueName(canvas);
        if (!TextUtils.isEmpty(config.firstCompareName) && !TextUtils.isEmpty(config.secondCompareName)) {
            drawCompareName(canvas);
        }
    }

    private void drawCompareName(Canvas canvas) {
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int x = Math.min(height, width) / 40, y;
        if (height > width) {
            y = (Math.max(height, width) - Math.min(height, width)) / 2 + x;
        } else {
            y = Math.min(height, width) / 40;
        }

        Rect firstRect = new Rect(x, y, x + Math.min(height, width) / 15, y + Math.min(height, width) / 30);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(config.fillColor);
        mPaint.setAlpha((int) (255 * config.valueBackgroundAlpha));
        canvas.drawRect(firstRect, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(255);
        canvas.drawRect(firstRect, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(firstRect.bottom - firstRect.top);
        canvas.drawText(config.firstCompareName, firstRect.right + x / 2, firstRect.bottom - x / 4, mPaint);

        Rect secondRect = new Rect(x, firstRect.top + x * 2, firstRect.right, firstRect.bottom + x * 2);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(config.secondFillColor);
        mPaint.setAlpha((int) (255 * config.valueBackgroundAlpha));
        canvas.drawRect(secondRect, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(255);
        canvas.drawRect(secondRect, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(secondRect.bottom - secondRect.top);
        canvas.drawText(config.secondCompareName, secondRect.right + x / 2, secondRect.bottom - x / 4, mPaint);
    }

    private void drawValueName(Canvas canvas) {
        int typeSize = typeDataList.size();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(config.textColor);
        mPaint.setTextSize(config.textSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < typeSize; i++) {
            canvas.drawText(typeDataList.get(i).typeName,
                    textPoints[i].x,
                    textPoints[i].y + config.textSize / 2,
                    mPaint);
        }
    }

    private void drawValue(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(config.fillColor);
        mPath.reset();
        int valueLength = valuePoints.length;
        for (int i = 0; i < valueLength; i++) {
            canvas.drawCircle(valuePoints[i].x, valuePoints[i].y, config.valuePointRadius, mPaint);
            if (i == 0) {
                mPath.moveTo(valuePoints[i].x, valuePoints[i].y);
            } else {
                mPath.lineTo(valuePoints[i].x, valuePoints[i].y);
            }
        }
        mPath.lineTo(valuePoints[0].x, valuePoints[0].y);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(config.valueLineSize);
        canvas.drawPath(mPath, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha((int) (255 * config.valueBackgroundAlpha));
        canvas.drawPath(mPath, mPaint);

        if (secondTypeDataList != null && secondTypeDataList.size() == typeDataList.size()) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(config.secondFillColor);
            mPath.reset();
            for (int i = 0; i < valueLength; i++) {
                canvas.drawCircle(secondValuePoints[i].x, secondValuePoints[i].y, config.valuePointRadius, mPaint);
                if (i == 0) {
                    mPath.moveTo(secondValuePoints[i].x, secondValuePoints[i].y);
                } else {
                    mPath.lineTo(secondValuePoints[i].x, secondValuePoints[i].y);
                }
            }
            mPath.lineTo(secondValuePoints[0].x, secondValuePoints[0].y);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(config.valueLineSize);
            canvas.drawPath(mPath, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAlpha((int) (255 * config.valueBackgroundAlpha));
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        mPaint.setColor(config.backgroundColor);
        canvas.drawCircle(centerPoint.x, centerPoint.y, config.centerPointRadius, mPaint);
        int pointLength = points.length;
        for (int i = 0; i < pointLength; i++) {
            mPath.reset();
            int pointXLength = points[i].length;
            for (int j = 0; j < pointXLength; j++) {
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(points[i][j].x, points[i][j].y, config.pointRadius, mPaint);
                if (j == 0) {
                    mPath.moveTo(points[i][j].x, points[i][j].y);
                } else {
                    mPath.lineTo(points[i][j].x, points[i][j].y);
                }
                if (j == points[i].length - 1) {
                    mPath.lineTo(points[i][0].x, points[i][0].y);
                }
            }
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
        }

        int p0Length = points[0].length;
        for (int i = 0; i < p0Length; i++) {
            mPath.reset();
            mPath.moveTo(centerPoint.x, centerPoint.y);
            for (int j = 0; j < points.length; j++) {
                mPath.lineTo(points[j][i].x, points[j][i].y);
            }
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        isTouchDown = true;
        startPoint = new Point((int) e.getX(), (int) e.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrollPoint = new Point((int) e2.getX(), (int) e2.getY());
        scrollOffSet = angle(centerPoint, startPoint, scrollPoint);
        offSet = scrollOffSet + tempOffset;
        measurePoint();
        invalidate();
        if (scrollOffSet >= Math.PI * 0.9f) {
            tempOffset = tempOffset + scrollOffSet;
            startPoint = new Point((int) e2.getX(), (int) e2.getY());
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!config.canFling) {
            return true;
        }
        double fling = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (angle(centerPoint, new Point((int) e1.getX(), (int) e1.getY()),
                new Point((int) e2.getX(), (int) e2.getY())) > 0) {
            fling = fling / 50;
        } else {
            fling = -(fling / 50);
        }
        postDelayed(new FlingRunnable(fling), 16);
        return true;
    }

    private class FlingRunnable implements Runnable {
        double angle;

        public FlingRunnable(double angle) {
            this.angle = angle;
        }

        @Override
        public void run() {
            tempOffset = (tempOffset + 0.005d * angle) % PIx2;
            offSet = tempOffset;
            measurePoint();
            invalidate();
            if (!isTouchDown && Math.abs(angle) > 1) {
                postDelayed(new FlingRunnable(angle * 0.95d), 16);
            }
        }
    }

    private float angle(Point cen, Point first, Point second) {
        if (cen == null || first == null || second == null) {
            return 0;
        }
        float dx1, dx2, dy1, dy2;
        float angle;
        dx1 = first.x - cen.x;
        dy1 = first.y - cen.y;
        dx2 = second.x - cen.x;
        dy2 = second.y - cen.y;
        float c = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1) * (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
        if (c == 0) {
            return -1;
        }
        angle = (float) Math.acos((dx1 * dx2 + dy1 * dy2) / c);
        Point point12 = new Point(first.x - cen.x, first.y - cen.y);
        Point point23 = new Point(second.x - first.x, second.y - first.y);
        int p12xp23 = point12.x * point23.y - point12.y * point23.x;
        if (p12xp23 < 0) {
            return -angle;
        } else {
            return angle;
        }
    }

    private class TypeData {
        String typeName;
        int typeValue;
        int typeMaxValue;

        public TypeData(String typeName, int typeValue, int typeMaxValue) {
            this.typeName = typeName;
            this.typeValue = typeValue;
            this.typeMaxValue = typeMaxValue;
        }
    }

    private boolean canRefresh() {
        return (config != null) && (typeDataList != null) && (typeDataList.size() > 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!config.canScroll) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                tempOffset = (tempOffset + scrollOffSet) % PIx2;
                isTouchDown = false;
                break;
            default:
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 插入一条属性
     * @param typeName      属性名称
     * @param typeValue     属性值
     * @param typeMaxValue  属性最大值
     */
    public void insertType(String typeName, int typeValue, int typeMaxValue) {
        typeDataList.add(new TypeData(typeName, typeValue, typeMaxValue));
    }

    /**
     * 插入一条对比的属性
     * @param typename          属性名称
     * @param firstTypeValue    第一个比较对象的值
     * @param secondTypeValue   第二个比较对象的值
     * @param maxValue          该属性的最大值
     */
    public void compareType(String typename, int firstTypeValue, int secondTypeValue, int maxValue) {
        typeDataList.add(new TypeData(typename, firstTypeValue, maxValue));
        if (secondTypeDataList == null) {
            secondTypeDataList = new ArrayList<>();
        }
        secondTypeDataList.add(new TypeData(typename, secondTypeValue, maxValue));
    }

    /**
     * 设置雷达图的配置属性 {@link Config}
     * @param config
     */
    public void setConfig(Config config) {
        this.config = config;
        points = new Point[config.maxLevel][typeDataList.size()];
        valuePoints = new Point[typeDataList.size()];
        if (secondTypeDataList != null && secondTypeDataList.size() > 0) {
            secondValuePoints = new Point[secondTypeDataList.size()];
        }
        textPoints = new Point[typeDataList.size()];
        requestLayout();
        invalidate();
    }

    /**
     * 图表的配置属性
     */
    public static class Config {

        /**
         * 雷达图的最大分级
         */
        private int maxLevel = 5;

        /**
         * 图表节点大小
         */
        private int pointRadius = 2;

        /**
         * 中间节点大小
         */
        private int centerPointRadius = 5;

        /**
         * 图表在整个View中的占比
         */
        private float chartWidget = 0.8f;

        /**
         * 图表填充颜色
         */
        private int fillColor = 0xff268415;

        /**
         * 第二个对比图的填充颜色
         */
        private int secondFillColor = 0xffcd2626;

        /**
         * 图表网格背景颜色
         */
        private int backgroundColor = 0x88985615;

        /**
         * 每一个值的节点大小
         */
        private int valuePointRadius = 5;

        /**
         * 每个值节点间的连线宽度
         */
        private int valueLineSize = 1;

        /**
         * 值的背景图透明度
         */
        private float valueBackgroundAlpha = 0.2f;

        /**
         * 属性值的字体大小
         */
        private int textSize = 40;

        /**
         * 属性值的字体颜色
         */
        private int textColor = 0x88985615;

        /**
         * 属性值位置比例，1.0f表示正在节点边缘位置
         */
        private float textPosition = 1.1f;

        /**
         * 雷达图是否能够滚动
         */
        private boolean canScroll = false;

        /**
         * 雷达图是否能够Fling
         */
        private boolean canFling = false;

        /**
         * 对比图的对象名称
         */
        private String firstCompareName, secondCompareName;

        public Config setMaxLevel(int level) {
            this.maxLevel = level;
            return this;
        }

        public Config setPointRadius(int pointRadius) {
            this.pointRadius = pointRadius;
            return this;
        }

        public Config setCenterPointRadius(int centerPointRadius) {
            this.centerPointRadius = centerPointRadius;
            return this;
        }

        public Config setChartWidget(float chartWidget) {
            this.chartWidget = chartWidget;
            return this;
        }

        public Config setFillColor(int fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public Config setValuePointRadius(int valuePointRadius) {
            this.valuePointRadius = valuePointRadius;
            return this;
        }

        public Config setValueLineSize(int valueLineSize) {
            this.valueLineSize = valueLineSize;
            return this;
        }

        public Config setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Config setValueBackgroundAlpha(float valueBackgroundAlpha) {
            this.valueBackgroundAlpha = valueBackgroundAlpha;
            return this;
        }

        public Config setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Config setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Config setTextPosition(float textPosition) {
            this.textPosition = textPosition;
            return this;
        }

        public Config setCanScroll(boolean canScroll) {
            this.canScroll = canScroll;
            return this;
        }

        public Config setCanFling(boolean canFling) {
            this.canFling = canFling;
            return this;
        }

        public Config setSecondFillColor(int secondFillColor) {
            this.secondFillColor = secondFillColor;
            return this;
        }

        public Config setCompareName(String firstCompareName, String secondCompareName) {
            this.firstCompareName = firstCompareName;
            this.secondCompareName = secondCompareName;
            return this;
        }
    }
}
