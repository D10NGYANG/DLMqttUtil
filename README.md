# DLMqttUtil

android MQTT client，基于[paho.mqtt.android](https://github.com/eclipse/paho.mqtt.android)进行封装开发；

*最新版本`0.0.5`*

# 特性
- [x] 以Flow形式去接收订阅消息
- [x] 以Flow形式展示连接状态

## 安装说明
1 添加Maven仓库
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://raw.githubusercontent.com/D10NGYANG/maven-repo/main/repository'}
  }
}
```

2 添加依赖
```gradle
dependencies {
    implementation 'com.github.D10NGYANG:DLMqttUtil:$ver'
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}
```

3 添加属性配置，打开`gradle.properties`文件添加以下内容：
```properties
android.useAndroidX=true
android.enableJetifier=true
```

4 添加服务，打开`AndroidManifest.xml`文件添加以下内容：
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application>
        <service android:name="org.eclipse.paho.android.service.MqttService" />
    </application>

</manifest>
```

5 混淆
```properties
-keep class com.d10ng.mqtt.** {*;}
-dontwarn com.d10ng.mqtt.**
```

## 使用说明
1 连接MQTT
```kotlin
MqttManager.connect(
    context,
    MqttClientOptions(
        username = "admin",
        password = "123456",
        host = "tcp://192.168.1.111:1883",
        topics = listOf(
            MqttClientOptions.Topic("topic0", MqttClientOptions.Topic.Qos.QOS_0),
            MqttClientOptions.Topic("topic1", MqttClientOptions.Topic.Qos.QOS_0),
        )
    )
)
```

MQTT客户端配置:
```kotlin
data class MqttClientOptions(
    // 客户端ID
    var clientId: String = "android_client",
    // 服务器地址，如：tcp://localhost:1883
    var host: String,
    // 用户名
    var username: String = "",
    // 密码
    var password: String = "",
    // 是否清除会话
    var isCleanSession: Boolean = true,
    // 连接超时时间，单位秒
    var connectionTimeout: Int = 10,
    // 心跳时间，单位秒
    var keepAliveInterval: Int = 20,
    // 自动重连间隔时间，单位秒
    var autoReconnectInterval: Int = 10,
    // 订阅主题列表
    var topics: List<Topic> = listOf(),
)
```
> - 启动连接后，会自动订阅`topics`中的主题；
> - 只要不执行`MqttManager.disconnect()`，连接会一直保持，如果由于网络问题或其他问题导致连接断开了，也会自动重连，并且就算第一次连接时没有连接成功也会不断尝试重连；

2 断开连接
```kotlin
MqttManager.disconnect()
```

3 发布消息
```kotlin
val isSuccess = MqttManager.publish("topic0", "Hello World! ${curTime.toDateStr()}")
```

4 接收订阅消息
```kotlin
MqttManager.getMessageFlow().collect {
    Log.d("MqttManager", "收到消息：${it.topic} -> ${it.message}")
}
```

5 连接状态
```kotlin
// 监听连接状态
MqttManager.getConnectStatusFlow().collect {
    // 连接中、已连接、已断开
    // MqttConnectStatus.CONNECTING, MqttConnectStatus.CONNECTED, MqttConnectStatus.DISCONNECTED
    Log.d("MqttManager", "连接状态：$it")
}

// 或者获取当前状态
val status = MqttManager.getCurrentConnectStatus()
```

6 设置日志输出
```kotlin
MqttManager.setDebug()
```