package lq2007.intelligentcontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import lq2007.intelligentcontrol.impl.CameraCallback;
import lq2007.intelligentcontrol.impl.CameraGestureListener;
import lq2007.intelligentcontrol.utils.SpUtil;

public class CameraActivity extends AppCompatActivity {

    SurfaceView sv_camera;
    ProgressBar pb_loading;
    TextView tv_loading, tv_loading_t;
    RelativeLayout welcome;
    GestureDetector detector;
    //两个连接成功时间,用来判断是否需要显示连接进度提示
    long lastSucceedTime = 0;
    long llastSucceedTime = 0;
    //handler用于处理进程通信
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //开始连接,判断是否需要显示加载
                case T.CONNECTED_START:
                    //本次开始连接时记录的上一次连接成功时间未变化-->上一次连接失败-->播放加载
                    if(llastSucceedTime == lastSucceedTime){
                        playLoading();
                    }
                    //更新连接时间
                    llastSucceedTime = lastSucceedTime;
                    break;
                //连接成功-->更新连接时间
                case T.CONNECTED_SUCCEED:
                    llastSucceedTime = lastSucceedTime;
                    lastSucceedTime = SystemClock.currentThreadTimeMillis();
                    stopLoading();
                    break;
                //文件保存结果-->Toast提示
                case T.SAVE_FAILED:
                    Toast.makeText(CameraActivity.this, "文件保存失败", Toast.LENGTH_SHORT).show();
                    break;
                case T.SAVE_SUCCEED:
                    File save = (File) msg.obj;
                    Toast.makeText(CameraActivity.this, "保存成功\n" + save.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    //判断是否需要显示进度条
    boolean isLoading = false;

    /**
     * 主要用来加载控件
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        tv_loading_t = (TextView) findViewById(R.id.tv_loading_t);
        welcome = (RelativeLayout) findViewById(R.id.camera_first);
        //处理SurfaceView,创建新线程显示摄像头画面
        sv_camera = (SurfaceView) findViewById(R.id.sv_camera);
        SurfaceHolder holder = sv_camera.getHolder();
        String url = SpUtil.getString(this, T.URL, T.default_URL);
        //新的画面显示将位于CallBack中处理,保证SurfaceView已经加载完成
        CameraCallback callback =new CameraCallback(this, url, getCacheDir(), handler);
        holder.addCallback(callback);
        playLoading();
        //文件保存位置
        File savePath = new File(SpUtil.getString(this, T.SAVE_PATH, T.default_SAVE_PATH));
        //添加滑动屏幕动作
        detector = new GestureDetector(this, new CameraGestureListener(this, callback, savePath));
        //确定是否显示第一次使用提示界面
        if(SpUtil.getBoolean(this, T.FIRST_USE_CAMERA, T.default_FIRST_USE_CAMERA)){
            welcome.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载屏幕滑动动作
     *      上划--设置缩放
     *      下滑--保存图片
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 用于隐藏加载中动画
     */
    private void stopLoading() {
        //已经隐藏--直接退出
        if(!isLoading){
            return;
        }
        //隐藏
        tv_loading.setVisibility(View.INVISIBLE);
        tv_loading.setAnimation(null);
        tv_loading_t.setVisibility(View.INVISIBLE);
        tv_loading_t.setAnimation(null);
        pb_loading.setVisibility(View.INVISIBLE);
        //改变状态值
        isLoading = false;
    }

    /**
     * 用于显示加载中动画
     */
    private void playLoading() {
        //已经加载--直接退出
        if(isLoading){
            return;
        }
        //显示控件
        pb_loading.setVisibility(View.VISIBLE);
        tv_loading.setVisibility(View.VISIBLE);
        tv_loading_t.setVisibility(View.VISIBLE);
        //添加动画
        Animation anim_tv = AnimationUtils.loadAnimation(this, R.anim.loading_alpha);
        anim_tv.setRepeatCount(Animation.INFINITE);
        tv_loading.startAnimation(anim_tv);
        tv_loading_t.startAnimation(anim_tv);
        //改变状态值
        isLoading = true;
    }

    /**
     * 用于隐藏第一次使用提示图片
     * @param v
     */
    public void closeWelcome(View v){
        welcome.setVisibility(View.INVISIBLE);
        SpUtil.putBoolean(this, T.FIRST_USE_CAMERA, false);
    }
}
