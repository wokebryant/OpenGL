package com.example.opendemo.opengl.pointcloud.view;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SelectView extends View {

    private static final String TAG = "SelectView";

    private enum Control {
        NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, LEFT, BOTTOM, RIGHT
    }

    /**
     * 未被激活的边框颜色
     */
    private static final int COLOR_FRAME = Color.parseColor("#ea6040");
    /**
     * 被激活的边框颜色
     */
    private static final int COLOR_FRAME_OPERATION = Color.parseColor("#ea6040");

    /**
     * 边框宽度
     */
    private static final int LINE_THICKNESS = dp(1);
    /**
     * 触摸条的宽度
     */
    private static final int HANDLE_THICKNESS = dp(4);
    /**
     * 最小宽度
     */
    private static final int MIN_WIDTH = dp(10);

    private final RectF topLeftCorner = new RectF();
    private final RectF topRightCorner = new RectF();
    private final RectF bottomLeftCorner = new RectF();
    private final RectF bottomRightCorner = new RectF();
    private final RectF topEdge = new RectF();
    private final RectF leftEdge = new RectF();
    private final RectF bottomEdge = new RectF();
    private final RectF rightEdge = new RectF();

    private Control activeControl;
    private final RectF actualRect = new RectF();
    private final RectF tempRect = new RectF();
    private int previousX;
    private int previousY;

    private final Paint handlePaint;
    private final Paint framePaint;

    private final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

    private boolean isDragging;

    private Animator animator;

    private final RectF targetRect = new RectF();

    public SelectView(Context context, AttributeSet attr) {
        super(context, attr);

        handlePaint = new Paint();
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(Color.WHITE);

        framePaint = new Paint();
        framePaint.setStyle(Paint.Style.FILL);
        framePaint.setColor(COLOR_FRAME_OPERATION);
    }

    public void setActualRect(RectF rect) {
        actualRect.set(rect);
        updateTouchAreas();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int lineThickness = LINE_THICKNESS;
        int handleThickness = HANDLE_THICKNESS;

        // 绘制边框
        int inset = handleThickness - lineThickness;

        int originX = (int) actualRect.left - inset;
        int originY = (int) actualRect.top - inset;
        int width = (int) (actualRect.right - actualRect.left) + inset * 2;
        int height = (int) (actualRect.bottom - actualRect.top) + inset * 2;

        // 上边框
        if (activeControl == Control.TOP_LEFT || activeControl == Control.TOP || activeControl == Control.TOP_RIGHT) {
            framePaint.setColor(COLOR_FRAME);
        } else {
            framePaint.setColor(COLOR_FRAME_OPERATION);
        }
        canvas.drawRect(originX + inset, originY + inset, originX + width - inset, originY + inset + lineThickness, framePaint);

        // 左边框
        if (activeControl == Control.TOP_LEFT || activeControl == Control.LEFT || activeControl == Control.BOTTOM_LEFT) {
            framePaint.setColor(COLOR_FRAME);
        } else {
            framePaint.setColor(COLOR_FRAME_OPERATION);
        }
        canvas.drawRect(originX + inset, originY + inset, originX + inset + lineThickness, originY + height - inset, framePaint);

        // 下边框
        if (activeControl == Control.BOTTOM_LEFT || activeControl == Control.BOTTOM || activeControl == Control.BOTTOM_RIGHT) {
            framePaint.setColor(COLOR_FRAME);
        } else {
            framePaint.setColor(COLOR_FRAME_OPERATION);
        }
        canvas.drawRect(originX + inset, originY + height - inset - lineThickness, originX + width - inset, originY + height - inset, framePaint);

        // 右边框
        if (activeControl == Control.TOP_RIGHT || activeControl == Control.RIGHT || activeControl == Control.BOTTOM_RIGHT) {
            framePaint.setColor(COLOR_FRAME);
        } else {
            framePaint.setColor(COLOR_FRAME_OPERATION);
        }
        canvas.drawRect(originX + width - inset - lineThickness, originY + inset, originX + width - inset, originY + height - inset, framePaint);
    }

    public void updateTouchAreas() {
        float outsideTouchPadding = dp(30);
        float horizontalInSideTouchPadding = dp(30);
        if (actualRect.width() < dp(60)) {
            horizontalInSideTouchPadding = actualRect.width() / 2;
        }
        float verticalInSideTouchPadding = dp(30);
        if (actualRect.height() < dp(60)) {
            verticalInSideTouchPadding = actualRect.height() / 2;
        }

        topLeftCorner.set(actualRect.left - outsideTouchPadding, actualRect.top - outsideTouchPadding, actualRect.left + horizontalInSideTouchPadding, actualRect.top + verticalInSideTouchPadding);
        topRightCorner.set(actualRect.right - horizontalInSideTouchPadding, actualRect.top - outsideTouchPadding, actualRect.right + outsideTouchPadding, actualRect.top + verticalInSideTouchPadding);
        bottomLeftCorner.set(actualRect.left - outsideTouchPadding, actualRect.bottom - verticalInSideTouchPadding, actualRect.left + horizontalInSideTouchPadding, actualRect.bottom + outsideTouchPadding);
        bottomRightCorner.set(actualRect.right - horizontalInSideTouchPadding, actualRect.bottom - verticalInSideTouchPadding, actualRect.right + outsideTouchPadding, actualRect.bottom + outsideTouchPadding);

        topEdge.set(actualRect.left + horizontalInSideTouchPadding, actualRect.top - outsideTouchPadding, actualRect.right - horizontalInSideTouchPadding, actualRect.top + verticalInSideTouchPadding);
        leftEdge.set(actualRect.left - outsideTouchPadding, actualRect.top + verticalInSideTouchPadding, actualRect.left + horizontalInSideTouchPadding, actualRect.bottom - verticalInSideTouchPadding);
        rightEdge.set(actualRect.right - horizontalInSideTouchPadding, actualRect.top + verticalInSideTouchPadding, actualRect.right + outsideTouchPadding, actualRect.bottom - verticalInSideTouchPadding);
        bottomEdge.set(actualRect.left + horizontalInSideTouchPadding, actualRect.bottom - verticalInSideTouchPadding, actualRect.right - horizontalInSideTouchPadding, actualRect.bottom + outsideTouchPadding);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) (event.getX() - ((ViewGroup) getParent()).getX());
        int y = (int) (event.getY() - ((ViewGroup) getParent()).getY());

        Log.i(TAG, "touchX= " + x + " touchY= " + y);

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (this.topLeftCorner.contains(x, y)) {
                activeControl = Control.TOP_LEFT;
            } else if (this.topRightCorner.contains(x, y)) {
                activeControl = Control.TOP_RIGHT;
            } else if (this.bottomLeftCorner.contains(x, y)) {
                activeControl = Control.BOTTOM_LEFT;
            } else if (this.bottomRightCorner.contains(x, y)) {
                activeControl = Control.BOTTOM_RIGHT;
            } else if (this.leftEdge.contains(x, y)) {
                activeControl = Control.LEFT;
            } else if (this.topEdge.contains(x, y)) {
                activeControl = Control.TOP;
            } else if (this.rightEdge.contains(x, y)) {
                activeControl = Control.RIGHT;
            } else if (this.bottomEdge.contains(x, y)) {
                activeControl = Control.BOTTOM;
            } else {
                activeControl = Control.NONE;
                return false;
            }

            previousX = x;
            previousY = y;

            isDragging = true;

            return true;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isDragging = false;

            if (activeControl == Control.NONE) {
                return false;
            }

            activeControl = Control.NONE;

            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (activeControl == Control.NONE) {
                return false;
            }

            tempRect.set(actualRect);

            float translationX = x - previousX;
            float translationY = y - previousY;
            previousX = x;
            previousY = y;

            switch (activeControl) {
                case TOP_LEFT:
                    tempRect.left += translationX;
                    tempRect.top += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.top = tempRect.bottom - MIN_WIDTH;
                    }
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.left = tempRect.right - MIN_WIDTH;
                    }
                    break;

                case TOP_RIGHT:
                    tempRect.right += translationX;
                    tempRect.top += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.top = tempRect.bottom - MIN_WIDTH;
                    }
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.right = tempRect.left + MIN_WIDTH;
                    }
                    break;

                case BOTTOM_LEFT:
                    tempRect.left += translationX;
                    tempRect.bottom += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.bottom = tempRect.top + MIN_WIDTH;
                    }
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.left = tempRect.right - MIN_WIDTH;
                    }
                    break;

                case BOTTOM_RIGHT:
                    tempRect.right += translationX;
                    tempRect.bottom += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.bottom = tempRect.top + MIN_WIDTH;
                    }
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.right = tempRect.left + MIN_WIDTH;
                    }
                    break;

                case TOP:
                    tempRect.top += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.top = tempRect.bottom - MIN_WIDTH;
                    }
                    break;

                case LEFT:
                    tempRect.left += translationX;
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.left = tempRect.right - MIN_WIDTH;
                    }
                    break;

                case RIGHT:
                    tempRect.right += translationX;
                    if (tempRect.left > tempRect.right - MIN_WIDTH) {
                        tempRect.right = tempRect.left + MIN_WIDTH;
                    }
                    break;

                case BOTTOM:
                    tempRect.bottom += translationY;
                    if (tempRect.top > tempRect.bottom - MIN_WIDTH) {
                        tempRect.bottom = tempRect.top + MIN_WIDTH;
                    }
                    break;

                default:
                    break;
            }

            setActualRect(tempRect);

            return true;
        }

        return false;
    }


    private static int dp(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    public void reset() {
        setActualRect(new RectF(0f, 0f, 0f, 0f));
    }
}