package com.open.serial.liboak;


import android.util.Log;

import java.io.FileDescriptor;

public class JniSerialPort {
    static {
        System.loadLibrary("serialportoak");
    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();


    public static String bufferHexString(byte[] buffer, int off, int len) {
        StringBuilder sb = new StringBuilder(len * 2);
        int v;
        for (int i = 0; i < len; i++) {
            v = buffer[i + off];
            sb.append(HEX[v >> 4 & 0xF]).append(HEX[v & 0xF]);
        }
        return sb.toString();
    }


    public enum Parity {
        NONE, // 无校验
        ODD,  // 奇校验
        EVEN, // 偶校验
        MARK, // 标志位校验
        RTSCTS_IN, SPACE // 空位校验
    }

    public enum FlowControl {
        NONE,           // 无流控制
        RTSCTS_IN,          // RTS/CTS硬件流控制（输入）
        RTSCTS_OUT,         // RTS/CTS硬件流控制（输出）
        DSRDTR_IN,          // DSR/DTR硬件流控制（输入）
        DSRDTR_OUT          // DSR/DTR硬件流控制（输出）
    }

    /**
     * 开启串口
     *
     * @param path        串口设备路径  示例: "/dev/ttyS0"
     * @param baud        波特率       示例: 115200
     * @param dataBits    数据位       示例: 8
     * @param stopBits    停止位       示例: 1 2
     * @param parity      校验方式
     * @param flowControl 流控
     */
    public FileDescriptor open(
            String path,
            int baud,
            int dataBits,
            int stopBits,
            Parity parity,
            FlowControl flowControl
    ) {

        int parityInt;
        switch (parity) {
            case ODD:
                parityInt = 1;
                break;
            case EVEN:
                parityInt = 2;
                break;
            case MARK:
                parityInt = 3;
                break;
            case SPACE:
                parityInt = 4;
                break;
            default:
                parityInt = 0;
                break;
        }

        int flowControlInt;
        switch (flowControl) {
            case RTSCTS_IN:
                flowControlInt = 1;
                break;
            case RTSCTS_OUT:
                flowControlInt = 2;
                break;
            case DSRDTR_IN:
                flowControlInt = 3;
                break;
            case DSRDTR_OUT:
                flowControlInt = 4;
                break;
            default:
                flowControlInt = 0;
                break;
        }
        return open(path, baud, dataBits, stopBits, parityInt, flowControlInt);
    }

    private native FileDescriptor open(
            String path,
            int baud,
            int dataBits,
            int stopBits,
            int parity,
            int flowControl
    );

    /**
     * 关闭串口
     */
    public native int close();

    private JniSerialPort() {

    }

    private static final class H {
        private static final JniSerialPort i = new JniSerialPort();
    }

    /**
     * 单例可选
     */
    public static JniSerialPort getInstance() {
        return H.i;
    }
}

