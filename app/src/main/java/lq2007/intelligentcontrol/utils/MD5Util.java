package lq2007.intelligentcontrol.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lq200 on 2016/9/10.
 */
public class MD5Util {

    private static String MD5(String pwd){
        try {
            if(TextUtils.isEmpty(pwd)){
                return "";
            }
            //1 获取摘要
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(pwd.getBytes());
            //2 加密:将byte与255进行与运算
            StringBuilder sb = new StringBuilder();
            for(byte b : digest) {
                int result = b & 0xff;
                String res = Integer.toHexString(result);
                if (res.length() == 1) {
                    sb.append("0");
                }
                sb.append(res);
            }
            //3 返回
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMD5(String password){
        /*
        加密方法:
        1 不规则加密(加盐)
        2 将密码进行10-30次MD5加密
         */
        if(TextUtils.isEmpty(password)){
            return "";
        }
        String salt1 = "asdfqwe";
        String salt2 = "asdf111";
        String psd1 = MD5(password);
        String psd2 = MD5(salt1 + psd1);
        return psd2 + salt2;
    }
}
