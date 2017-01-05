package com.android.projects.mateusz.olliecontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Mateusz on 02.01.2017.
 */

public class JoyStick {

    private int stickParamAlpha = 200;
    private int layoutParamAlpha = 200;
    private int distanceFromEdge = 0;

    private Context context;
    private ViewGroup layout;
    private ViewGroup.LayoutParams params;
    private int stickWidth, stickHeight;

    private int stickPositionX = 0, stickPositionY = 0, minDistFromCenterJoyStick = 0;
    private float distanceFromCenterJoyStick = 0, stickAngle = 0;
    private float maxDistance = 0;

    private DrawCanvas draw;
    private Paint paint;
    private Bitmap stick;

    private boolean touchState = false;

    public JoyStick(Context context, ViewGroup layout, int stickResource) {
        this.context = context;

        stick = BitmapFactory.decodeResource(this.context.getResources(), stickResource);

        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();

        draw = new DrawCanvas(this.context);
        paint = new Paint();
        this.layout = layout;
        params = this.layout.getLayoutParams();
    }

    public void drawStick(MotionEvent arg1) {
        stickPositionX = (int) (arg1.getX() - (params.width / 2));
        stickPositionY = (int) (arg1.getY() - (params.height / 2));
        distanceFromCenterJoyStick = (float) Math.sqrt(Math.pow(stickPositionX, 2) + Math.pow(stickPositionY, 2));
        stickAngle = (float) calculateAngle(stickPositionX, stickPositionY);


        if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
            if (distanceFromCenterJoyStick <= (params.width / 2) - distanceFromEdge) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
                touchState = true;
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_MOVE && touchState) {
            if (distanceFromCenterJoyStick <= (params.width / 2) - distanceFromEdge) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
            } else {
                float x = (float) (Math.sin(Math.toRadians(calculateAngle(stickPositionX, -stickPositionY)))
                        * ((params.width / 2) - distanceFromEdge));
                float y = (float) (Math.cos(Math.toRadians(calculateAngle(stickPositionX, -stickPositionY)))
                        * ((params.height / 2) - distanceFromEdge));
                x += (params.width / 2);
                y += (params.height / 2);
                draw.position(x, y);
                draw();
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
            draw.position(params.width / 2, params.height / 2);
            draw();
            touchState = false;
        }
    }

    public void drawCalibrationStick(MotionEvent arg1){
        stickPositionX = (int) (arg1.getX() - (params.width / 2));
        stickPositionY = (int) (arg1.getY() - (params.height / 2));
        stickAngle = (float) calculateAngle(stickPositionX, stickPositionY);
        distanceFromCenterJoyStick = maxDistance;

        if ( arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction() == MotionEvent.ACTION_MOVE) {
            touchState = true;
            float x = (float) (Math.sin(Math.toRadians(calculateAngle(stickPositionX, -stickPositionY)))
                    * ((params.width / 2) - distanceFromEdge));
            float y = (float) (Math.cos(Math.toRadians(calculateAngle(stickPositionX, -stickPositionY)))
                    * ((params.height / 2) - distanceFromEdge));
            x += (params.width / 2);
            y += (params.height / 2);
            draw.position(x, y);
            draw();
        } else {
            touchState = false;
        }
    }


    public float getStickAngle() {
        if (distanceFromCenterJoyStick > minDistFromCenterJoyStick && touchState) {
            return stickAngle;
        }
        return 0;
    }

    public float getDistanceFromCenterJoyStick() {
        if (distanceFromCenterJoyStick > minDistFromCenterJoyStick && touchState) {
            return distanceFromCenterJoyStick;
        }
        return 0;
    }

    public float getJoyStickPower(){
        if (distanceFromCenterJoyStick > minDistFromCenterJoyStick && touchState
                && distanceFromCenterJoyStick <= maxDistance) {
            return distanceFromCenterJoyStick / maxDistance;
        } else if(touchState && distanceFromCenterJoyStick > maxDistance) {
            return 1.0f;
        }
        return 0;
    }

    public int getMinDistFromCenterJoyStick() {
        return minDistFromCenterJoyStick;
    }

    public void setMinDistFromCenterJoyStick(int minDistFromCenterJoyStick) {
        this.minDistFromCenterJoyStick = minDistFromCenterJoyStick;
    }

    public int getDistanceFromEdge() {
        return distanceFromEdge;
    }

    public void setDistanceFromEdge(int distanceFromEdge) {
        this.distanceFromEdge = distanceFromEdge;
    }

    public void setStickAlpha(int alpha) {
        stickParamAlpha = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return stickParamAlpha;
    }

    public void setLayoutAlpha(int alpha) {
        layoutParamAlpha = alpha;
        layout.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return layoutParamAlpha;
    }

    public void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();
    }

    public int getStickWidth() {
        return stickWidth;
    }

    public int getStickHeight() {
        return stickHeight;
    }

    public void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
    }

    public void startPosition(){
        draw.position(params.width / 2, params.height / 2);
        draw();
        maxDistance = params.width / 2 - distanceFromEdge;
    }

    private double calculateAngle(float x, float y) {
        if (x >= 0 && y >= 0)        // 1
            return Math.toDegrees(Math.atan(y / x)) + 90;
        else if (x < 0 && y < 0)     // 2
            return Math.toDegrees(Math.atan(y / x)) + 270;
        else if (x < 0 && y >= 0)    // 3
            return Math.toDegrees(Math.atan(y / x)) + 270;
        else if (x >= 0 && y < 0)    // 4
            return Math.toDegrees(Math.atan(y / x)) + 90;
        return 0;
    }


    private void draw() {
        try {
            layout.removeView(draw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        layout.addView(draw);
    }

    private class DrawCanvas extends View {
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(stick, x, y, paint);
        }

        private void position(float posX, float posY) {
            x = posX - (stickWidth / 2);
            y = posY - (stickHeight / 2);
        }
    }

}