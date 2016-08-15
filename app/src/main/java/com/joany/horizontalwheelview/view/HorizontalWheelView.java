package com.joany.horizontalwheelview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by joany on 2016/8/15.
 */
public class HorizontalWheelView extends View {

    /**
     * 水平横线
     */
    private Paint linePaint;
    /**
     * 当前选择刻度值
     */
    private Paint valuePaint;
    /**
     * 垂直刻度线
     */
    private Paint verticalPaint;
    /**
     * 垂直刻度值
     */
    private Paint textPaint;

    private Scroller scroller;
    /**
     * x方向滚动距离，左划增大，右划减小
     */
    private int lastScrollX;
    /**
     * 是否是从右向左划
     */
    private boolean isLeft = false;
    /**
     * 宽度
     */
    private int width;
    /**
     * y坐标，以此为基准画y轴方向
     */
    private int y;
    /**
     * 缺省高度
     */
    private int defaultHeightValue = 150;

    /**
     * 手势检测器
     */
    private GestureDetector gestureDetector;

    /**
     * 滚动是否正在执行
     */
    private boolean isScrollingPerformed;

    /**
     * 为使首位刻度显示完整，左右两侧起止像素点偏移量
     */
    private int padding = 10;

    private int offset;

    public HorizontalWheelView(Context context) {
        this(context, null);
    }

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(0xff999999);

        verticalPaint = new Paint();
        verticalPaint.setAntiAlias(true);
        verticalPaint.setStyle(Paint.Style.STROKE);
        verticalPaint.setStrokeWidth(2);
        verticalPaint.setColor(0xff999999);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(2);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(30);
        textPaint.setColor(0xff999999);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setStrokeWidth(3);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(60);
        valuePaint.setColor(0xff999999);

        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        width = getWidth();
        y = getHeight();
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                return widthSize;
            default:
                return Math.min(widthSize, Integer.MAX_VALUE);
        }
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                return heightSize;
            default:
                return Math.min(heightSize, defaultHeightValue);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(lastScrollX >= 0 && width - padding + lastScrollX <=  300 * 10) {
            //正常滑动
            canvas.drawLine(0, y, width, y, linePaint);
            canvas.drawText((width / 2 - padding + lastScrollX) / 10 + "",
                    width / 2, y - 80, valuePaint);
            for (int start = padding; start <= width - padding; start++) {
                int top = y - 10;
                if ((start - padding + lastScrollX) % (10 * 10) == 0) {
                    top = top - 20;
                    canvas.drawText((start - padding + lastScrollX) / 10 + "",
                            start, top - 8, textPaint);
                }
                if ((start - padding + lastScrollX) % 10 == 0) {
                    canvas.drawLine(start, y, start, top, verticalPaint);
                }
            }
        } else if(lastScrollX < 0){
            //0点右划至中点
            canvas.drawLine(offset,y,width,y,linePaint);
            canvas.drawText((width/ 2 - padding- offset) / 10 + "",
                    width / 2, y - 80, valuePaint);
            for (int start = offset + padding; start <= width - padding; start++) {
                int top = y - 10;
                if ((start - offset - padding) % (10 * 10) == 0) {
                    top = top - 20;
                    canvas.drawText((start - offset - padding) / 10 + "",
                            start, top - 8, textPaint);
                }
                if ((start - offset - padding) % 10 == 0) {
                    canvas.drawLine(start, y, start, top, verticalPaint);
                }
            }
        } else {
            //终点左划至中点
            canvas.drawLine(0,y,width - (width - padding + lastScrollX - 300 * 10),y,linePaint);
            canvas.drawText((width/ 2 - padding + offset) / 10 + "",
                    width / 2, y - 80, valuePaint);
            for (int start = padding;
                 start <= width - padding - (width - padding + lastScrollX - 300 * 10);
                 start++) {
                int top = y - 10;
                if ((start - padding + offset) % (10 * 10) == 0) {
                    top = top - 20;
                    canvas.drawText((start - padding + offset) / 10 + "",
                            start, top - 8, textPaint);
                }
                if ((start - padding +offset) % 10 == 0) {
                    canvas.drawLine(start, y, start, top, verticalPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
        }
        return true;
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (isScrollingPerformed) {
                scroller.forceFinished(true);
                clearMessages();
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceX > 0) {
                isLeft = true;
            } else {
                isLeft = false;
            }
            startScrolling();
            doScroll((int) distanceX);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling(lastScrollX, 0, (int) -velocityX / 2, 0, 0, 300, 0, 0);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    private void doScroll(int delta) {
        lastScrollX += delta;
        if(width - padding + lastScrollX > 300 * 10
                && width - padding + lastScrollX <= 300 * 10 + width/2) {
            offset = lastScrollX;
        } else if(width - padding + lastScrollX > 300 * 10 + width/2) {
            lastScrollX = 300 * 10 + width/2 - (width - padding);
            offset = lastScrollX;
        }else if (lastScrollX < 0 && lastScrollX >= -width/2 + padding) {
            offset = -lastScrollX;
        } else if(lastScrollX < -width/2 + padding) {
            lastScrollX = -width/2 + padding;
            offset = -lastScrollX;
        }
        invalidate();
    }

    private final int MESSAGE_SCROLL = 0;
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    private void setNextMessage(int message) {
        clearMessages();
        handler.sendEmptyMessage(message);
    }

    private void clearMessages() {
        handler.removeMessages(MESSAGE_SCROLL);
    }

    /**
     * 滑动的相对距离，向左划为正，向右划为负
     */
    private int delta;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            //相对scroller.fling中设置的起始坐标为(lastScrollX,0) X方向上的绝对值
            int currX = scroller.getCurrX();

            if (isLeft) {
                delta = currX;
            } else {
                delta = -currX;
            }
            if (delta != 0) {
                doScroll(delta);
            }

            if (Math.abs(currX - scroller.getFinalX()) < MIN_DELTA_FOR_SCROLLING) {
                scroller.forceFinished(true);
            }

            if (!scroller.isFinished()) {
                handler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                finishScrolling();
            }
            super.handleMessage(msg);
        }
    };

    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
        }
    }

    private void finishScrolling() {
        if (isScrollingPerformed) {
            isScrollingPerformed = false;
        }
        invalidate();
    }
}
