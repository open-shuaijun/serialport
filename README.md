# Android SerialPort Library

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub Stars](https://img.shields.io/github/stars/yourusername/yourrepository.svg?style=social)](https://github.com/yourusername/yourrepository)

## 简介

本项目提供了一个简单易用的Android串口通信库，旨在帮助开发者快速在Android设备上实现与外部串行设备（如Arduino、ESP8266等）的通讯。该库封装了底层串口操作细节，暴露了高层API，支持跨Android版本的兼容性。

## 特性

- **简洁的API**：易于集成和使用，几行代码即可开始串口通信。
- **广泛的兼容性**：支持Android 4.4及以上版本，涵盖多种设备。
- **异步读写**：非阻塞的读写操作，提升应用响应性。
- **错误处理**：提供详细的错误回调，便于调试和异常处理。
- **示例代码**：包含详细使用示例，快速上手。

## 安装

### Gradle

在你的`build.gradle`文件中添加依赖：

```groovy
dependencies {
    implementation 'com.open.serial:serialport:1.0.0'
}