package lq2007.intelligentcontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lq2007.intelligentcontrol.utils.MD5Util;
import lq2007.intelligentcontrol.utils.SpUtil;

public class LockActivity extends AppCompatActivity {

    /**
     * 本界面用于判断密码是否正确
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //提示密码框
        setContentView(R.layout.activity_lock);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this, R.layout.dialog_pwd_check, null);
        Button check = (Button) view.findViewById(R.id.btn_pwd_set);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPwd(((EditText)view.findViewById(R.id.et_pwd_pwd1)).getText().toString())){
                    //密码错误--Toast提示
                    Toast.makeText(LockActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                } else {
                    //密码正确--创建一个Intent返回,表示通过密码回到主界面
                    setResult(1, new Intent());
                    finish();
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //若通过返回回到主界面,带回的Intent为null,据此可使主界面也退出
                finish();
            }
        });
        builder.setView(view);
        builder.show();
    }

    /**
     * 用于检查密码是否正确
     * @param pwd1 输入的密码
     * @return
     */
    private boolean checkPwd(String pwd1) {
        return SpUtil.getString(this, T.SAFE_PWD, T.default_SAFE_PWD).equals(MD5Util.getMD5(pwd1));
    }
}
