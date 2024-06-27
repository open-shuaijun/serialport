package com.open.serial.lib

import java.io.FileDescriptor

class JniSerialPort {

    enum class Parity {
        NONE, // 无校验
        ODD,  // 奇校验
        EVEN, // 偶校验
        MARK, // 标志位校验
        SPACE // 空位校验
    }

    enum class FlowControl {
        NONE,               // 无流控制
        RTSCTS_IN,          // RTS/CTS硬件流控制（输入）
        RTSCTS_OUT,         // RTS/CTS硬件流控制（输出）
        DSRDTR_IN,          // DSR/DTR硬件流控制（输入）
        DSRDTR_OUT          // DSR/DTR硬件流控制（输出）
    }

    /**
     * 开启串口
     * @param path        串口设备路径  示例: "/dev/ttyS0"
     * @param baud        波特率       示例: 115200
     * @param dataBits    数据位       示例: 8
     * @param stopBits    停止位       示例: 0
     * @param parity      校验方式
     * @param flowControl 流控
     * */
    fun open(
        path: String,
        baud: Int,
        dataBits: Int = 8,
        stopBits: Int = 0,
        parity: Parity = Parity.NONE,
        flowControl: FlowControl = FlowControl.NONE
    ): FileDescriptor? {
        val parityInt = when (parity) {
            Parity.NONE -> 0
            Parity.ODD -> 1
            Parity.EVEN -> 2
            Parity.MARK -> 3
            Parity.SPACE -> 4
        }
        val flowControlInt = when (flowControl) {
            FlowControl.NONE -> 0
            FlowControl.RTSCTS_IN -> 1
            FlowControl.RTSCTS_OUT -> 2
            FlowControl.DSRDTR_IN -> 3
            FlowControl.DSRDTR_OUT -> 4
        }
        return open(path, baud, dataBits, stopBits, parityInt, flowControlInt)
    }

    private external fun open(
        path: String,
        baud: Int,
        dataBits: Int = 8,
        stopBits: Int = 0,
        parity: Int = 0,
        flowControl: Int = 0
    ): FileDescriptor?

    /**
     * 关闭串口
     * */
    external fun close(): Int


    companion object {

        private val HEX = "0123456789ABCDEF".toCharArray()

        init {
            System.loadLibrary("serialport")
        }

        fun bufferHexString(buffer: ByteArray, off: Int = 0, len: Int = buffer.size): String {
            val sb = StringBuilder(len * 2)
            for (i in 0..off + len) {
                (buffer[i].toInt() and 0xFF).let { b ->
                    sb.append(HEX[b ushr 4]).append(HEX[b and 0xF])
                }
            }
            return sb.toString()
        }
    }

}