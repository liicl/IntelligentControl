package lq2007.intelligentcontrol;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import lq2007.intelligentcontrol.utils.SpUtil;

/**
 * 设置界面
 */
public class ICSettingActivity extends AppCompatActivity {

    Switch sw_ic_1,sw_ic_2,sw_t_1,sw_t_2;
    TimePicker tp_open_1,tp_open_2,tp_close_1,tp_close_2;
    LinearLayout ll_time_1,ll_time_2;

    /**
     * 设置界面
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ic_setting);
        
        sw_ic_1 = (Switch) findViewById(R.id.sw_ic_1);
        sw_ic_2 = (Switch) findViewById(R.id.sw_ic_2);
        sw_t_1 = (Switch) findViewById(R.id.sw_time_1);
        sw_t_2 = (Switch) findViewById(R.id.sw_time_2);
        tp_open_1 = (TimePicker) findViewById(R.id.tp_open_1);
        tp_open_1.setIs24HourView(true);
        tp_open_2 = (TimePicker) findViewById(R.id.tp_open_2);
        tp_open_2.setIs24HourView(true);
        tp_close_1 = (TimePicker) findViewById(R.id.tp_close_1);
        tp_close_1.setIs24HourView(true);
        tp_close_2 = (TimePicker) findViewById(R.id.tp_close_2);
        tp_close_2.setIs24HourView(true);
        ll_time_1 = (LinearLayout) findViewById(R.id.layout_settime_1);
        ll_time_2 = (LinearLayout) findViewById(R.id.layout_settime_2);

        sw_t_1.setChecked(SpUtil.getBoolean(this, T.TIME_1, T.default_TIME_1));
        sw_t_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ll_time_1.setVisibility(View.VISIBLE);
                } else {
                    ll_time_1.setVisibility(View.GONE);
                }
            }
        });

        sw_t_2.setChecked(SpUtil.getBoolean(this, T.TIME_2, T.default_TIME_2));
        sw_t_2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ll_time_2.setVisibility(View.VISIBLE);
                } else {
                    ll_time_2.setVisibility(View.GONE);
                }
            }
        });
    }

    public void btn_time_1(View view) {
        ll_time_1.setVisibility(View.GONE);
        int openHour = tp_open_1.getCurrentHour();
        int openMinute = tp_open_1.getCurrentMinute();
        int closeHour = tp_close_1.getCurrentHour();
        int closeMinute = tp_close_1.getCurrentMinute();
        SpUtil.putInt(this, T.TIME_HOUR_OPEN_1, openHour);
        SpUtil.putInt(this, T.TIME_HOUR_CLOSE_1, closeHour);
        SpUtil.putInt(this, T.TIME_MINUTE_OPEN_1, openMinute);
        SpUtil.putInt(this, T.TIME_MINUTE_CLOSE_1, closeMinute);
        SpUtil.putBoolean(this, T.TIME_1, sw_ic_1.isChecked());
    }

    public void btn_time_2(View view) {
        ll_time_2.setVisibility(View.GONE);
        int openHour = tp_open_2.getCurrentHour();
        int openMinute = tp_open_2.getCurrentMinute();
        int closeHour = tp_close_2.getCurrentHour();
        int closeMinute = tp_close_2.getCurrentMinute();
        SpUtil.putInt(this, T.TIME_HOUR_OPEN_2, openHour);
        SpUtil.putInt(this, T.TIME_HOUR_CLOSE_2, closeHour);
        SpUtil.putInt(this, T.TIME_MINUTE_OPEN_2, openMinute);
        SpUtil.putInt(this, T.TIME_MINUTE_CLOSE_2, closeMinute);
        SpUtil.putBoolean(this, T.TIME_2, sw_ic_2.isChecked());
    }
}