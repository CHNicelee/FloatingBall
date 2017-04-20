package com.ice.floatingball;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by asd on 1/1/2017.
 */

public class ViewManager {
    FloatingView floatBall;
    WindowManager windowManager;
    public static ViewManager manager;
    Context context;
    private WindowManager.LayoutParams floatBallParams;

    private ViewManager(Context context) {
        this.context = context;
    }

    public static ViewManager getInstance(Context context) {
        if (manager == null) {
            manager = new ViewManager(context);
        }
        return manager;
    }
    public void showFloatBall() {
        floatBall = new FloatingView(context);
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (floatBallParams == null) {
            floatBallParams = new WindowManager.LayoutParams();
            floatBallParams.width = floatBall.width;
            floatBallParams.height = floatBall.height;
            floatBallParams.gravity = Gravity.TOP | Gravity.LEFT;
            floatBallParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
        }

        windowManager.addView(floatBall, floatBallParams);

        floatBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(MyAccessibilityService.BACK);
                Toast.makeText(context, "点击了悬浮球 执行后退操作", Toast.LENGTH_SHORT).show();
            }
        });

        floatBall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EventBus.getDefault().post(MyAccessibilityService.HOME);
                Toast.makeText(context, "长按了悬浮球  执行返回桌面", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        floatBall.setOnTouchListener(new View.OnTouchListener() {
                float startX;
                float startY;
                float tempX;
                float tempY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getRawX();
                            startY = event.getRawY();

                            tempX = event.getRawX();
                            tempY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float dx = event.getRawX() - startX;
                            float dy = event.getRawY() - startY;
                            //计算偏移量，刷新视图
                            floatBallParams.x += dx;
                            floatBallParams.y += dy;
                            windowManager.updateViewLayout(floatBall, floatBallParams);
                            startX = event.getRawX();
                            startY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            //判断松手时View的横坐标是靠近屏幕哪一侧，将View移动到依靠屏幕
                            float endX = event.getRawX();
                            float endY = event.getRawY();
                            if (endX < getScreenWidth() / 2) {
                                endX = 0;
                            } else {
                                endX = getScreenWidth() - floatBall.width;
                            }
                            floatBallParams.x = (int) endX;
                            windowManager.updateViewLayout(floatBall, floatBallParams);
                            //如果初始落点与松手落点的坐标差值超过6个像素，则拦截该点击事件
                            //否则继续传递，将事件交给OnClickListener函数处理
                            if (Math.abs(endX - tempX) > 6 && Math.abs(endY - tempY) > 6) {
                                return true;
                            }
                            break;
                    }
                    return false;
                }

        });
    }

    public int getScreenWidth() {
        return windowManager.getDefaultDisplay().getWidth();
    }

}
