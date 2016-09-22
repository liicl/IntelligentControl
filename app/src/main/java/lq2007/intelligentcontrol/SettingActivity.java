package lq2007.intelligentcontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import lq2007.intelligentcontrol.utils.MD5Util;
import lq2007.intelligentcontrol.utils.SpUtil;

/**
 * 设置界面
 */
public class SettingActivity extends AppCompatActivity {

    EditText et_ca_path,et_en_timeout,et_en_delay;
    RadioGroup rg_showType;
    Switch sw_pwd;

    /**
     * 设置界面
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        et_ca_path = (EditText) findViewById(R.id.et_camera_save_path);
        et_en_timeout = (EditText) findViewById(R.id.et_en_timeout);
        et_en_delay = (EditText) findViewById(R.id.et_en_refresh_time);
        rg_showType = (RadioGroup) findViewById(R.id.rg_camera_show_type);
        sw_pwd = (Switch) findViewById(R.id.sw_password);
        //读取参数
        et_ca_path.setText(SpUtil.getString(this, T.SAVE_PATH, T.default_SAVE_PATH));
        et_en_delay.setText(Long.toString(SpUtil.getLong(this, T.EN_DELAY, T.default_EN_DELAY)));
        et_en_timeout.setText(Long.toString(SpUtil.getLong(this, T.EN_TIMEOUT, T.default_EN_TIMEOUT)));
        int type = SpUtil.getInt(this, T.SHOW_TYPE, T.default_SHOW_TYPE);
        rg_showType.check(type == T.SHOW_FILL? R.id.rb_camera_show_scr: R.id.rb_camera_show_org);
        sw_pwd.setChecked(SpUtil.getBoolean(this, T.SAFE_OPEN, T.default_SAFE_OPEN));
        //设置密码
        sw_pwd.setOnClickListener(new View.OnClickListener() {
            View view;
            @Override
            public void onClick(View v) {
                //选中时,打开Dialog
                if(sw_pwd.isChecked()){
                    SpUtil.putBoolean(SettingActivity.this, T.SAFE_OPEN, true);
                    String pwd = SpUtil.getString(SettingActivity.this, T.SAFE_PWD, T.default_SAFE_PWD);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    final AlertDialog dialog = builder.create();
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //取消时--取消选中,更新数据
                            sw_pwd.setChecked(false);
                            SpUtil.putBoolean(SettingActivity.this, T.SAFE_OPEN, false);
                        }
                    });
                    //未保存密码信息--生成设置密码提示框
                    if(TextUtils.isEmpty(pwd)){
                        view = View.inflate(SettingActivity.this, R.layout.dialog_pwd_add, null);
                        Button set = (Button) view.findViewById(R.id.btn_pwd_set);
                        set.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String pwd1 = ((EditText) view.findViewById(R.id.et_pwd_pwd1)).getText().toString();
                                String pwd2 = ((EditText) view.findViewById(R.id.et_pwd_pwd2)).getText().toString();
                                //校验两次密码
                                if(!savePwd(pwd1, pwd2)){
                                    //未通过
                                    Toast.makeText(SettingActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                                } else {
                                    //通过
                                    Toast.makeText(SettingActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                        });
                        Button cancel = (Button) view.findViewById(R.id.btn_pwd_cancel);
                        //取消--cancel
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                    } else {
                        //有密码数据--加载修改密码提示框
                        view = View.inflate(SettingActivity.this, R.layout.dialog_pwd_set, null);
                        Button set = (Button) view.findViewById(R.id.btn_pwd_set);
                        set.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String pwd1 = ((EditText)view.findViewById(R.id.et_pwd_pwd1)).getText().toString();
                                String pwd2 = ((EditText)view.findViewById(R.id.et_pwd_pwd2)).getText().toString();
                                //校验密码
                                if(!SpUtil.getString(SettingActivity.this, T.SAFE_PWD, T.default_SAFE_PWD).equals(MD5Util.getMD5(pwd1))){
                                    Toast.makeText(SettingActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                } else {
                                    //通过--校验新密码(位数大于6或空通过)
                                    if(pwd2.length() >= 6 || TextUtils.isEmpty(pwd2)) {
                                        SpUtil.putString(SettingActivity.this, T.SAFE_PWD, MD5Util.getMD5(pwd2));
                                        if (TextUtils.isEmpty(pwd2)) {
                                            //空--取消密码
                                            sw_pwd.setChecked(false);
                                            SpUtil.putBoolean(SettingActivity.this, T.SAFE_OPEN, false);
                                        }
                                        dialog.dismiss();
                                    } else {
                                        //校验未通过
                                        Toast.makeText(SettingActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        Button cancel = (Button) view.findViewById(R.id.btn_pwd_cancel);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                    }
                    dialog.setView(view);
                    dialog.show();
                }
            }
        });
    }

    /**
     * 用来校验两次输入的密码,并保存
     * @param pwd1
     * @param pwd2
     * @return
     */
    private boolean savePwd(String pwd1, String pwd2) {
        //空--失败
        if(TextUtils.isEmpty(pwd1) || TextUtils.isEmpty(pwd2)){
            return false;
        }
        //长度小于6--失败
        if(pwd1.length() < 6){
            return false;
        }
        //两次密码不一致--失败
        if(!pwd1.equals(pwd2)){
            return false;
        }
        //保存
        SpUtil.putString(SettingActivity.this, T.SAFE_PWD, MD5Util.getMD5(pwd1));
        return true;
    }

    /**
     * 打开选择文件夹,用于手动指定视频监控照片保存位置
     * @param view
     */
    public void choose(View view) {
        Intent intent = new Intent(this, FileDialogActivity.class);
        startActivityForResult(intent, 1);
    }

    /**
     * 保存数据
     * 除了安全密码,其他参数在此处一并保存
     * @param view
     */
    public void save(View view) {
        SpUtil.putString(this, T.SAVE_PATH, et_ca_path.getText().toString());
        long delay = Long.parseLong(et_en_delay.getText().toString());
        SpUtil.putLong(this, T.EN_DELAY, delay);
        long timeout = Long.parseLong(et_en_timeout.getText().toString());
        if(timeout < 3000){
            timeout = 3000;
        }
        SpUtil.putLong(this, T.EN_TIMEOUT, timeout);
        int type = rg_showType.getCheckedRadioButtonId() == R.id.rb_camera_show_org?T.SHOW_WRAP:T.SHOW_FILL;
        SpUtil.putInt(this, T.SHOW_TYPE, type);
        SpUtil.putBoolean(this, T.SAFE_OPEN, sw_pwd.isChecked());
        finish();
    }

    /**
     * 取消--放弃修改的参数
     * @param view
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * 处理返回的照片保存位置
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            et_ca_path.setText(data.getStringExtra("path"));
        }
    }
}