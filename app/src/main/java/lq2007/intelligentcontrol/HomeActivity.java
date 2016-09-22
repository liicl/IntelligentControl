package lq2007.intelligentcontrol;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import lq2007.intelligentcontrol.utils.SpUtil;

public class HomeActivity extends AppCompatActivity {

    //用于标识网络连接方式
    final int NO_WEB = -1;
    final int MOBILE = 1;
    final int OTHER = 2;
    //用于跳转各个页面
    Intent mIntent;
    //用于检查是否为第一次拒绝授权
    boolean isFirst = true;

    /**
     * 主要实现了检查是否需要密码(需要则调跳转密码界面),检查内存卡授权,打开控制服务
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //密码保护检查和跳转
        if(SpUtil.getBoolean(this, T.SAFE_OPEN, T.default_SAFE_OPEN)){
            Intent intent = new Intent(this, LockActivity.class);
            startActivityForResult(intent, 1);
        }
        //申请权限
        requestPermission();
    }

    /**
     * 用于检查授权是否成功
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //检查是否完成授权
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        } else {
            //失败--第一次,提示重新授权
            if(isFirst){
                isFirst = false;
                //构建提示授权的Dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("授权申请");
                builder.setMessage("我们需要读写内存卡的权限,用来保存您在视频监控功能中保存的图片,请赋予我们有关权限,以便您更好地使用本应用");
                builder.setPositiveButton("授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        builder.create().dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        builder.create().cancel();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        //失败--第二次,提示失败
                        Toast.makeText(HomeActivity.this, "授权失败,您将无法使用视频监控的图片保存功能,若想重新设置授权,请重启应用或在系统设置里手动设置", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            } else {
                //失败--第二次,提示失败
                Toast.makeText(this, "授权失败,您将无法使用视频监控的图片保存功能,若想重新设置授权,请重启应用或在系统设置里手动设置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查密码校验是否成功
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            //未返回Intent-->是通过取消校验框回来的-->退出程序
            finish();
        }
    }

    /**
     * 申请授权
     */
    private void requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    /**
     * 获取连接状态
     * @return 连接状态(NO_WEB,MOBILE,OTHER)
     */
    public int getConnectState() {
        //判断网络连接
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        //是否连接
        if(info == null || !info.isAvailable()){
            return NO_WEB;
        }
        //连接状态
        if(manager.TYPE_MOBILE == info.getType()){
            return MOBILE;
        }
        return OTHER;
    }

    /**
     * 跳转视频监控界面
     * 流程:检查网络状态 --> NO_WEB --> 无网络 Toast提示
     *                      OTHER  --> 有网络且非数据连接 直接跳转
     *                      MOBILE --> 有数据连接 提示会消耗大量流量(跳转-->视频监控/网络设置)
     *
     * @param v
     */
    public void camera(View v) {
        int state = getConnectState();
        mIntent = new Intent(HomeActivity.this, CameraActivity.class);
        //无网络连接
        if(state == NO_WEB){
            Toast.makeText(HomeActivity.this, "亲,请检查您的网络连接", Toast.LENGTH_SHORT).show();
            return;
        }
        //数据流量
        if(state == MOBILE){
            //Dialog提示
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("网络设置");
            builder.setMessage("当前网络情况为数据网络，打开远程摄像头会消耗大量流量，是否继续访问？");
            builder.setPositiveButton("继续访问", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(mIntent);;
                }
            });
            builder.setNegativeButton("设置网络", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        //根据Android版本不同,采用不同方法跳转到网络设置界面
                        if(Build.VERSION.SDK_INT > 10) {
                            mIntent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(mIntent);
                            builder.create().dismiss();
                        }else {
                            mIntent = new Intent();
                            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            mIntent.setComponent(comp);
                            mIntent.setAction("android.intent.action.VIEW");
                            startActivity(mIntent);
                            builder.create().dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            //取消--不跳转
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mIntent = null;
                }
            });
            builder.show();
        } else {
            //其他网络环境
            startActivity(mIntent);
            mIntent = null;
        }
    }

    /**
     * 跳转至环境控制界面
     * 流程:检查网络状态 --> NO_WEB --> 无网络 Toast提示
     *                      其他   --> 有网络 直接跳转(由于只是发送一些数字数据,消耗流量较小)
     * @param v
     */
    public void control(View v){
        int state = getConnectState();
        //无网络连接
        if(state == NO_WEB){
            Toast.makeText(HomeActivity.this, "亲,请检查您的网络连接", Toast.LENGTH_SHORT).show();
            return;
        }
        mIntent = new Intent(this, ControlActivity.class);
        startActivity(mIntent);
    }

    /**
     * 设置界面 无需联网直接跳转
     * @param v
     */
    public void setting(View v){
        mIntent = new Intent(this, SettingActivity.class);
        startActivity(mIntent);
    }

    /**
     * 退出 直接结束Activity
     * @param v
     */
    public void exit(View v){
        finish();
    }
}
