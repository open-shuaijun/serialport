package com.open.serial.demo

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.open.serial.demo.ui.theme.SerialportTheme
import com.open.serial.lib.JniSerialPort
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SerialportTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Thread {
            JniSerialPort().open("/dev/ttymxc1", 19200)?.let {

                // 数据写入示例
                FileOutputStream(it).apply {
                    val buffer = ByteArray(16)
                    write(buffer, 0, 8)
                    Log.d(
                        "serial_port",
                        "写入数据:${
                            JniSerialPort.bufferHexString(
                                buffer,
                                0,
                                8
                            )
                        }"
                    )
                }


                // 数据读取示例
                FileInputStream(it).apply {
                    val buffer = ByteArray(128)
                    for (i in 0..10) {
                        SystemClock.sleep(1000)
                        val read = read(buffer, 0, 128)
                        Log.d(
                            "serial_port",
                            "读取数据长度$read  数据:${
                                JniSerialPort.bufferHexString(
                                    buffer,
                                    0,
                                    read
                                )
                            }"
                        )
                    }
                }
            }
            Log.d("serial_port", "thread start")
        }.start()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SerialportTheme {
        Greeting("Android")
    }
}
