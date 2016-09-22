package lq2007.intelligentcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import lq2007.intelligentcontrol.utils.SocketUtil;
import lq2007.intelligentcontrol.utils.SpUtil;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ControlActivity";
    TextView tv_r1,tv_r2,tv_r3,tv_r4,tv_r5;
    //用来保存控制按钮
    Button[] buttons;
    //用于循环链接开关
    boolean isConnectedSend = false;
    boolean isConnectedGet = false;
    //发送的指令
    int msg;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case T.CONNECTED_FAILED_SEND_MESSAGE:
                    Toast.makeText(ControlActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
                    break;
                case T.CONNECTED_FAILED_GET_TIMEOUT:
                    Log.i(TAG, "handleMessage: 连接超时");
                    break;
                case T.CONNECTED_RECEIVED:
                    if(msg.obj == null){
                        break;
                    }
                    String t = (String) msg.obj;
                    switch (t.length()){
                        case 1:
                            t = t.equals("Y")?"有人通过":(t.equals("N")?"无人通过":t);
                            tv_r3.setText("红外感应 : " + t);
                            break;
                        case 10:
                            double value_t1 = Double.parseDouble(t.substring(0, 5));
                            double value_t2 = Double.parseDouble(t.substring(5));
                            tv_r4.setText("湿度 : " + value_t1 + "%");
                            tv_r5.setText("温度 : " + value_t2 + "°C");
                            break;
                        case 5:
                            double value = Double.parseDouble(t);
                            Log.i(TAG, "handleMessage: " + value);
                            if(value >= 18000){
                                value = Double.parseDouble(t) - 20000;
                                tv_r1.setText("空气质量 : " + value);
                                break;
                            } else {
                                value = Double.parseDouble(t) - 10000;
                                tv_r2.setText("光照强度 : " + value + " lx");
                                break;
                            }
                    }
            }
        }
    };

    Thread sendThread = null;
    Thread getThread = null;
    Socket socket = null;

    /**
     * 本Activity主要用来完成环境控制
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        tv_r1 = (TextView) findViewById(R.id.tv_r1);
        tv_r2 = (TextView) findViewById(R.id.tv_r2);
        tv_r3 = (TextView) findViewById(R.id.tv_r3);
        tv_r4 = (TextView) findViewById(R.id.tv_r4);
        tv_r5 = (TextView) findViewById(R.id.tv_r5);

        buttons = new Button[]{
                (Button) findViewById(R.id.btn_sw1_Off),
                (Button) findViewById(R.id.btn_sw1_On),
                (Button) findViewById(R.id.btn_sw2_Off),
                (Button) findViewById(R.id.btn_sw2_On)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
    }

    /**
     * 显示界面时,开启线程
     */
    @Override
    protected void onStart() {
        startGet();
        super.onStart();
    }

    /**
     * 隐藏界面时,关闭线程
     */
    @Override
    protected void onStop() {
        if(getThread != null) {
            getThread.interrupt();
        }
        super.onStop();
    }

    /**
     * 处理点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sw1_Off:
                msg = T.send_sw1_Off_command;
                break;
            case R.id.btn_sw2_Off:
                msg = T.send_sw2_Off_command;
                break;
            case R.id.btn_sw1_On:
                msg = T.send_sw1_On_command;
                break;
            case R.id.btn_sw2_On:
                msg = T.send_sw2_On_command;
                break;
        }
        if(getThread != null){
            getThread.interrupt();
        }
        startSend();
        Toast.makeText(ControlActivity.this, "指令已发送", Toast.LENGTH_SHORT).show();
    }

    /**
     * 开启一个线程接收服务器内容
     */
    private void startGet(){
        getThread = new Thread(){
            @Override
            public void run() {
                Log.i(TAG, "run: run");
                //获得地址
                try {
                    String url = SpUtil.getString(ControlActivity.this, T.HOST, T.default_HOST);
                    InetAddress address = InetAddress.getByName(url);
                    //连接标识
                    while(isConnectedGet) {
                        Log.i(TAG, "run: get");
                        //接收四个参数并发送Message
                        sendState(socket, address, T.send_air_command);
                        SystemClock.sleep(SpUtil.getLong(ControlActivity.this, T.EN_DELAY, T.default_EN_DELAY));
                        sendState(socket, address, T.send_light_command);
                        SystemClock.sleep(SpUtil.getLong(ControlActivity.this, T.EN_DELAY, T.default_EN_DELAY));
                        sendState(socket, address, T.send_PIR_command);
                        SystemClock.sleep(SpUtil.getLong(ControlActivity.this, T.EN_DELAY, T.default_EN_DELAY));
                        sendState(socket, address, T.send_tem_command);
                        SystemClock.sleep(SpUtil.getLong(ControlActivity.this, T.EN_DELAY, T.default_EN_DELAY));
                    }
                } catch (UnknownHostException e) {
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_IP);
                    e.printStackTrace();
                } finally {
                    SocketUtil.closeSocket(socket);
                }
                super.run();
            }
            //退出--修改标签,关闭连接
            @Override
            public void interrupt() {
                Log.i(TAG, "interrupt: stop");
                isConnectedGet = false;
                SocketUtil.closeSocket(socket);
                super.interrupt();
            }
            //启动--修改标签
            @Override
            public synchronized void start() {
                isConnectedGet = true;
                super.start();
            }
        };
        getThread.start();
    }

    /**
     * 开启一个线程发送服务器内容
     */
    private void startSend(){
        sendThread = new Thread(){
            @Override
            public void run() {
                //获得地址
                try {
                    String url = SpUtil.getString(ControlActivity.this, T.HOST, T.default_HOST);
                    InetAddress address = InetAddress.getByName(url);
                    //连接标识
                    if(isConnectedSend) {
                        SocketUtil.sendMessage(socket, address, msg, handler);
                        interrupt();
                        startGet();
                    }
                } catch (UnknownHostException e) {
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_IP);
                    e.printStackTrace();
                } finally {
                    SocketUtil.closeSocket(socket);
                }
                super.run();
            }
            //退出--修改标签,关闭连接
            @Override
            public void interrupt() {
                isConnectedSend = false;
                SocketUtil.closeSocket(socket);
                super.interrupt();
            }
            //启动--修改标签
            @Override
            public synchronized void start() {
                isConnectedSend = true;
                super.start();
            }
        };
        sendThread.start();
    }
    
    /**
     * 发送指定数据到服务器，接受服务器数据，并返回至hander的流程
     * @param socket
     * @param ip
     * @param type
     */
    private void sendState(Socket socket, InetAddress ip, int type){
        if(SocketUtil.sendMessage(socket, ip, type, handler)) {
            String str = SocketUtil.getMessage(socket, ip, handler);
            Message msg = Message.obtain();
            msg.what = T.CONNECTED_RECEIVED;
            msg.obj = str;
            handler.sendMessage(msg);
        }
    }

    public void en_control(View view) {
        Intent intent = new Intent(this, ICSettingActivity.class);
        startActivity(intent);
    }
}
