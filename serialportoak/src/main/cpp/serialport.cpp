/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "serialport.h"

static const char *TAG = "serial_port";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_open_serial_liboak_JniSerialPort_open(JNIEnv *env, jclass clazz, jstring path, jint baud,
                                               jint data_bits, jint stop_bits, jint parity,
                                               jint flow_control) {
    int fd_port;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudrate(baud);
        if (speed == -1) {
            LOGD("波特率不可用");
            return nullptr;
        }
    }

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = env->GetStringUTFChars(path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR);
        fd_port = open(path_utf, O_RDWR | O_NONBLOCK);
        LOGD("open() fd = %d", fd_port);
        env->ReleaseStringUTFChars(path, path_utf);
        if (fd_port == -1) {
            LOGE("Cannot open port");
            return nullptr;
        }
    }


    /* Configure device */
    {
        struct termios cfg{};
        LOGD("Configuring serial port");
        if (tcgetattr(fd_port, &cfg)) {
            LOGE("tcgetattr() failed");
            close(fd_port);
            return nullptr;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        // Apply additional parameters
        cfg.c_cflag &= ~(CSIZE | CSTOPB | PARENB | CRTSCTS); // Clear the fields first

        switch (data_bits) {
            case 5:
                cfg.c_cflag |= CS5;
                break;
            case 6:
                cfg.c_cflag |= CS6;
                break;
            case 7:
                cfg.c_cflag |= CS7;
                break;
            case 8:
                cfg.c_cflag |= CS8; // Default, typically used
                break;
            default:
                LOGE("Unsupported data bits setting:%d", data_bits);
                close(fd_port);
                return nullptr;
        }

        switch (stop_bits) {
            case 0:
            case 1:
                break; // Default, typically used
            case 2:
                cfg.c_cflag |= CSTOPB;
                break;
            default:
                LOGE("Unsupported stop bits setting:%d", stop_bits);
                close(fd_port);
                return nullptr;
        }

        switch (parity) {
            case 0:
                break; // NOPARITY
            case 1:
                cfg.c_cflag |= PARENB;
                break; // PARITY_EVEN (example)
            case 2:
                cfg.c_cflag |= PARENB | PARODD;
                break; // PARITY_ODD
            default:
                LOGE("Unsupported parity setting");
                close(fd_port);
                return nullptr;
        }

        // Flow control (assuming 'flow_control' is 0 for none, 1 for RTS/CTS)
        if (flow_control == 1) {
            cfg.c_cflag |= CRTSCTS;
        }

        if (tcsetattr(fd_port, TCSANOW, &cfg)) {
            LOGE("tcsetattr() failed");
            close(fd_port);
            return nullptr;
        }
    }


    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(mFileDescriptor, descriptorID, fd_port);
    }

    return mFileDescriptor;
}


/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_open_serial_liboak_JniSerialPort_close(JNIEnv *env, jobject thiz) {
    jclass SerialPortClass = env->GetObjectClass(thiz);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(thiz, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);
    return close(descriptor);
}
