package lq2007.intelligentcontrol.utils;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import lq2007.intelligentcontrol.T;

/**
 * Created by lq200 on 2016/9/18.
 */
public class SocketUtil {
    private static final String TAG = "SocketUtil";

    /**
     * 关闭Socket连接
     * @param socket 需要关闭的Socket
     */
    public static void closeSocket(Socket socket){
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用于接收数据的方法
     * @param socket 接收数据需要的Socket对象.可以为new Socket = null,单独列出来主要是为了interrupt
     * @param ip 接收数据的ip地址
     * @return 接受的数据
     */
    public static String getMessage(Socket socket, InetAddress ip, Handler handler) {
        closeSocket(socket);
        try {
            socket = new Socket(ip, 10086);
            socket.setSoTimeout(5000);
            socket.setKeepAlive(true);
            SystemClock.sleep(500);
            InputStream is = socket.getInputStream();
            byte[] b = new byte[1024];
            int len;
            len = is.read(b);
            String str = new String(b, 0, len);
            Log.i(TAG, "getOriMessage: " + str);
            socket.close();
            return str.trim();
        } catch (SocketTimeoutException e){
            handler.sendEmptyMessage(T.CONNECTED_FAILED_GET_TIMEOUT);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            handler.sendEmptyMessage(T.CONNECTED_FAILED_GET_MESSAGE);
            e.printStackTrace();
            return null;
        } finally{
            closeSocket(socket);
        }
    }

    /**
     * 用于发送数据的方法
     * @param socket 发送数据需要的Socket对象.可以为new Socket = null,单独列出来主要是为了interrupt
     * @param ip 接收数据的ip地址
     * @param msg 要发送的数据
     */
    public static boolean sendMessage(Socket socket, InetAddress ip, int msg, Handler handler) {
        closeSocket(socket);
        if(msg != 0) {
            try {
                if(ip == null){
                    return false;
                }
                socket = new Socket(ip, 10086);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(5000);
                OutputStream os = socket.getOutputStream();
                os.write(msg);
                os.flush();
                Log.i(TAG, "sendMessage: send " + msg);
                if (handler != null) {
                    handler.sendEmptyMessage(T.CONNECTED_SENDED);
                }
                return true;
            } catch (SocketTimeoutException e){
                if (handler != null) {
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_SEND_TIMEOUT);
                }
                e.printStackTrace();
                return false;
            } catch (SocketException e) {
                if (handler != null) {
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_NO_HEADER);
                }
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                if (handler != null) {
                    handler.sendEmptyMessage(T.CONNECTED_FAILED_SEND_MESSAGE);
                }
                e.printStackTrace();
                return false;
            } finally{
                closeSocket(socket);
            }
        }
        return false;
    }
}
