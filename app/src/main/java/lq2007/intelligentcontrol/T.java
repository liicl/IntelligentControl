package lq2007.intelligentcontrol;

import android.os.Environment;

import java.io.File;

/**
 * 常量集
 * Created by lq200 on 2016/9/12.
 */
public class T {

    /**
     * 常量列表
     */

    //CONNECTED_    连接服务器是否成功
    public static final int CONNECTED_START = 10;
    public static final int CONNECTED_SUCCEED = 11;
    public static final int CONNECTED_RECEIVED = 12;
    public static final int CONNECTED_SENDED = 13;
    public static final int CONNECTED_FAILED_IP = 14;
    public static final int CONNECTED_FAILED_URL = 15;
    public static final int CONNECTED_FAILED_CONNECT = 16;
    public static final int CONNECTED_FAILED_NO_HEADER = 17;
    public static final int CONNECTED_FAILED_SEND_MESSAGE = 18;
    public static final int CONNECTED_FAILED_GET_MESSAGE = 19;
    public static final int CONNECTED_FAILED_SEND_TIMEOUT = -10;
    public static final int CONNECTED_FAILED_GET_TIMEOUT = -11;
    //SAVE_ 截图保存
    public static final int SAVE_FAILED = -21;
    public static final int SAVE_SUCCEED = 21;
    //SHOW_ 视频监控显示方式
    public static final int SHOW_WRAP = 30;
    public static final int SHOW_FILL = 31;
    //type_ 控制代码
    public static final int send_tem_command = 0x81;
    public static final int send_air_command = 0x82;
    public static final int send_light_command = 0x83;
    public static final int send_PIR_command = 0x84;
    public static final int send_sw1_On_command = 0x10;
    public static final int send_sw1_Off_command = 0x11;
    public static final int send_sw2_On_command = 0x20;
    public static final int send_sw2_Off_command = 0x21;
    /**
     * 参数列表
     */
    public static final String HOST = "host";
    public static final String URL = "url";
    public static final String SHOW_TYPE = "showType";
    public static final String FIRST_USE_CAMERA = "firstUseCamera";
    public static final String SAVE_PATH = "savePath";
    public static final String EN_DELAY = "en_delay";
    public static final String EN_TIMEOUT = "en_timeout";
    public static final String SAFE_PWD = "password";
    public static final String SAFE_OPEN = "use_pwd";
    public static final String TIME_HOUR_OPEN_1 = "hour_open_a";
    public static final String TIME_HOUR_OPEN_2 = "hour_open_b";
    public static final String TIME_HOUR_CLOSE_1 = "hour_close_a";
    public static final String TIME_HOUR_CLOSE_2 = "hour_close_b";
    public static final String TIME_MINUTE_OPEN_1 = "minute_open_a";
    public static final String TIME_MINUTE_OPEN_2 = "minute_open_b";
    public static final String TIME_MINUTE_CLOSE_1 = "minute_close_a";
    public static final String TIME_MINUTE_CLOSE_2 = "minute_close_b";
    public static final String TIME_1 = "time_1";
    public static final String TIME_2 = "time_2";

    /**
     * 默认参数列表
     */
    public static final String default_HOST = "mozzielx.picp.io";
    public static final String default_URL = "http://mozzielx.picp.io:10010/?action=snapshot";
    public static final String default_SAVE_PATH = (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "IC")).getAbsolutePath();
    public static final String default_SAFE_PWD = "";
    public static final boolean default_FIRST_USE_CAMERA = true;
    public static final boolean default_SAFE_OPEN = false;
    public static final boolean default_TIME_1 = false;
    public static final boolean default_TIME_2 = false;
    public static final long default_EN_TIMEOUT = 3000;
    public static final long default_EN_DELAY = 2000;
    public static final int default_SHOW_TYPE = SHOW_WRAP;
}
