package lq2007.intelligentcontrol.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import lq2007.intelligentcontrol.T;
import lq2007.intelligentcontrol.utils.SpUtil;


/**
 * Created by lq200 on 2016/9/12.
 */
public class CameraCallback implements SurfaceHolder.Callback {

    Context context;
    Handler handler;
    String cameraUrl;
    File cache;
    Thread getCamera;
    boolean startCamera = false;
    private File cacheFile;
    private int s_width;
    private int s_height;

    /**
     * 构造函数
     * @param context       上下文,主要用于存取参数
     * @param url           服务器URL地址
     * @param cachePath     图片cache地址
     * @param handler       handler,与主线程通讯用
     */
    public CameraCallback(Context context, String url, File cachePath, Handler handler){
        cameraUrl = url;
        cache = cachePath;
        this.handler = handler;
        this.context = context;
    }

    /**
     * SurfaceView创建成功 绘制图片
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawBitmap(holder);
    }

    /**
     * 画面长宽改变--重新读取赋值
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        this.s_width = width;
        this.s_height = height;

    }

    /**
     * 画面销毁--关闭摄像头线程
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(getCamera != null){
            getCamera.interrupt();
        }
    }

    /**
     * 绘制监控画面
     * @param holder SurfaceView控制器
     */
    private void drawBitmap(final SurfaceHolder holder) {
        getCamera = new Thread(){
            HttpURLConnection conn = null;
            @Override
            public void run() {
                super.run();
                try {
                    while (startCamera) {
                        URL url = new URL(cameraUrl);
                        //连接
                        handler.sendEmptyMessage(T.CONNECTED_START);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setConnectTimeout(3000);
                        conn.connect();
                        handler.sendEmptyMessage(T.CONNECTED_SUCCEED);
                        //根据流读取位图
                        Bitmap bmp = getBitmap(conn.getInputStream());
                        Canvas canvas = holder.lockCanvas();
                        if (canvas != null || bmp != null) {
                            //将位图绘制到画板上
                            drawCanvas(canvas, bmp);
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (MalformedURLException e) {
                    //URL错误
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_URL);
                    e.printStackTrace();
                } catch (IOException e) {
                    //连接失败,IO错误等
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_CONNECT);
                    e.printStackTrace();
                } finally {
                    //错误--关闭连接
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            @Override
            public synchronized void start() {
                startCamera = true;
                super.start();
            }

            @Override
            public void interrupt() {
                startCamera = false;
                if (conn != null) {
                    conn.disconnect();
                }
                super.interrupt();
            }
        };
        getCamera.start();
    }

    /**
     * 用于将Bitmap绘制到Canvas上
     * @param canvas 画板
     * @param bmp 位图
     */
    private void drawCanvas(Canvas canvas, Bitmap bmp) {
        //读取屏幕大小,分配大小(此时为拉伸至全屏幕)
        float s_x = s_width;
        float s_y = s_height;
        float width = s_x;
        float height = s_y;
        float left = 0;
        float top = 0;
        float len_top = 0;
        float len_left = 0;
        //保持宽高比处理
        if (SpUtil.getInt(context, T.SHOW_TYPE, T.default_SHOW_TYPE) == T.SHOW_WRAP) {
            float p_x = bmp.getWidth();
            float p_y = bmp.getHeight();
            float x_y = p_x / p_y;

            float t_width = s_width;
            float t_height = width / x_y;
            len_top = (s_y - t_height) / 2;

            if (len_top < 0) {
                len_top = 0;
                height = s_y;
                width = height * x_y;
                len_left = (s_x - width) / 2;
            } else {
                len_left = 0;
                width = t_width;
                height = t_height;
            }
        }
        RectF rectF = new RectF(left + len_left, top + len_top, width + len_left, height + len_top);
        //绘制
        if(canvas != null) {
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(bmp, null, rectF, null);
        }
    }

    /**
     * 用于校验保存地址,设定保存图片,保存成功与否向主线程发送一条Message
     * @param fileDir 保存图片的目录
     */
    public void getPhoto(File fileDir) throws IOException {
        File savePath = null;
        //校验存储目录
        if(fileDir == null){
            handler.sendEmptyMessage(T.SAVE_FAILED);
        }
        if(!fileDir.isDirectory()){
            if(!fileDir.mkdirs()){
                handler.sendEmptyMessage(T.SAVE_FAILED);
            }
        }
        //构造存储文件
        savePath = new File(fileDir, System.currentTimeMillis() + ".jpg");
        if(savePath.exists()){
            deleteFile(savePath);
        }
        boolean hasSave = receivePhoto(savePath);
        //检查是否保存成功
        if(!hasSave){
            handler.sendEmptyMessage(T.SAVE_FAILED);
            return;
        }
        sendMessage(T.SAVE_SUCCEED, savePath);
    }

    /**
     * 从缓存文件中奖图片文件复制出来
     * @param savePath 保存图片地址
     * @return 是否保存成功
     */
    private boolean receivePhoto(File savePath) throws IOException {
        if(savePath == null || cacheFile == null){
            return false;
        }
        if(!cacheFile.exists()){
            return false;
        }
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cacheFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath));
        byte[] b = new byte[4096];
        int len;
        while((len = bis.read(b) ) > 0){
            bos.write(b, 0, len);
        }
        bos.flush();
        bos.close();
        bis.close();
        return true;
    }

    /**
     * 将图片从网络中下载到缓存并读取至内存
     * @param is 网络图片流
     * @return 读取到内存的Bitmap
     */
    private Bitmap getBitmap(InputStream is) throws IOException {
        //判断缓存地址是否正确
        if(!cache.isDirectory()){
            if(!cache.mkdirs()){
                return null;
            }
        }
        cacheFile = new File(cache,"cache");
        //判断是否存在同名文件或文件夹-->删除
        if(cacheFile.exists()){
            deleteFile(cacheFile);
        }
        //从流中保存图片到本地
        if(is == null){
            return null;
        }
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bof = new BufferedOutputStream(new FileOutputStream(cacheFile));
        byte[] b = new byte[4096];
        int len;
        while((len = bis.read(b) ) > 0){
            bof.write(b, 0, len);
        }
        bof.flush();
        bof.close();
        bis.close();
        //读取到内存
        return BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
    }

    /**
     * 调用递归删除文件/文件夹
     * @param file 要删除的文件/文件夹
     */
    private void deleteFile(File file) {
        if(file == null){
            return;
        }
        if(file.isFile()){
            file.delete();
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
            file.delete();
        }
    }

    /**
     * 向主线程发送一条消息
     * @param what 消息码
     * @param obj 消息内容
     */
    private void sendMessage(int what, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }
}
