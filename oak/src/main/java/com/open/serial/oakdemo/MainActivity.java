package com.open.serial.oakdemo;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.open.serial.liboak.JniSerialPort;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final Executor thread = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




    }

    @Override
    protected void onResume() {
        super.onResume();

//        hasPermission();
//
        thread.execute(new Runnable() {
            @Override
            public void run() {
                // 打开串口
                FileDescriptor mfd = JniSerialPort.getInstance().open("/dev/ttymxc1", 19200, 8, 1, JniSerialPort.Parity.NONE, JniSerialPort.FlowControl.NONE);
                // 收发数据演示
                try (FileInputStream inputStream = new FileInputStream(mfd);
                     FileOutputStream outputStream = new FileOutputStream(mfd);) {
                    // 发送数据
                    byte[] data = {0x01, 0x02, 0x03};
                    outputStream.write(data);
                    Log.d("serialport", "发送数据:" + JniSerialPort.bufferHexString(data, 0, 3));

                    // 接收数据
                    int n;
                    byte[] buffer = new byte[256];
                    for (int i = 0; i < 100; i++) {
                        SystemClock.sleep(1000);
                        n = inputStream.read(buffer, 0, buffer.length);
                        Log.d("serialport", "接收数据:" + JniSerialPort.bufferHexString(buffer, 0, n));
                    }
                } catch (IOException ignore) {
                }
                JniSerialPort.getInstance().close(); // 关闭串口
            }
        });

    }

    private void hasPermission() {
        // 有些设备需要进行授权，如果JniSerialPort.open()返回安全警告，需要执行该段授权代码。
        thread.execute(new Runnable() {
            @Override
            public void run() {
                /* Check access permission */
                Log.d("TAG", "检测串口");
                File device = new File("/dev/ttymxc1");
                if (!device.canRead() || !device.canWrite()) {
                    try {
                        /* Missing read/write permission, trying to chmod the file */
                        Process su;
                        File f = new File("/system/bin/su");
                        if (!f.exists()) {
                            f = new File("/system/xbin/su");
                            if (f.exists()) {
                                su = Runtime.getRuntime().exec(f.getAbsolutePath());
                                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                                        + "exit\n";
                                su.getOutputStream().write(cmd.getBytes());
                                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                                    Log.d("serialport", "串口无权限");
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d("serialport", "执行SHELL失败");
                    }
                }

            }
        });
    }
}