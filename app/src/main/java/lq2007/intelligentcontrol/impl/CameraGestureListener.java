package lq2007.intelligentcontrol.impl;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;

import lq2007.intelligentcontrol.T;
import lq2007.intelligentcontrol.utils.SpUtil;

/**
 * 用于处理滑屏操作
 * Created by lq200 on 2016/9/12.
 */
public class CameraGestureListener implements GestureDetector.OnGestureListener {

    Context context;
    CameraCallback callback;
    File savePath;

    /**
     * 构造函数
     * @param context   上下文
     * @param callback  SurfaceView的控制器
     * @param savePath  文件保存位置
     */
    public CameraGestureListener(Context context, CameraCallback callback, File savePath){
        this.callback = callback;
        this.savePath = savePath;
        this.context = context;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float startX = e1.getRawX();
        float startY = e1.getRawY();
        float endX = e2.getRawX();
        float endY = e2.getRawY();
        //防止误触(斜划)
        if(Math.sqrt(startX - endX) > 500){
            return false;
        }
        //判断上下滑
        if((startY - endY) < 500){
            try {
                callback.getPhoto(savePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(T.SHOW_FILL == SpUtil.getInt(context, T.SHOW_TYPE, T.default_SHOW_TYPE)){
                SpUtil.putInt(context, T.SHOW_TYPE, T.SHOW_WRAP);
            } else {
                SpUtil.putInt(context, T.SHOW_TYPE, T.SHOW_FILL);
            }
        }

        return true;
    }
}
