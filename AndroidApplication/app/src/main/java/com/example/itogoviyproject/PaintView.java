package com.example.itogoviyproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PaintView extends View {

    private static Paint mPaint;
    private int paintColor = 0xFF660000;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private float mX, mY;
    private Path mPath;
    private Paint canvasPaint;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private float brushSize, lastBrushSize;
    private boolean erase=false;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }
    private void setupDrawing(){
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(paintColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(brushSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        mPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void setColor(String newColor){
        invalidate();
        paintColor = Color.parseColor(newColor);
        mPaint.setColor(paintColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, canvasPaint);
        canvas.drawPath(mPath, mPaint);
    }

    public void setErase(boolean isErase){
        erase = isErase;
        if(erase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mPaint.setXfermode(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(mX, mY);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(mX, mY);
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }
}
//    private void touchStart(float x, float y) {
//        mPath = new Path();
//        FingerPath fp = new FingerPath(currentColor, strokeWidth, mPath);
//        paths.add(fp);
//
//        mPath.reset();
//        mPath.moveTo(x, y);
//        mX = x;
//        mY = y;
//    }
//    private void touchMove(float x, float y) {
//        float dx = Math.abs(x - mX);
//        float dy = Math.abs(y - mY);
//
//        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
//            mX = x;
//            mY = y;
//        }
//    }
//    private void touchUp() {
//        mPath.lineTo(mX, mY);
//    }

//    public void init(DisplayMetrics metrics) {
//        int height = metrics.heightPixels;
//        int width = metrics.widthPixels;
//
//        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);
//
//        currentColor = DEFAULT_COLOR;
//        strokeWidth = BRUSH_SIZE;
//    }