package com.d10ng.mqtt.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.d10ng.mqtt.constant.MqttConnectStatus
import com.d10ng.mqtt.demo.ui.theme.DLMqttUtilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLMqttUtilTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

@Composable
private fun MainView(
    model: MainViewModel = viewModel()
) {
    val username by model.usernameFlow.collectAsState()
    val password by model.passwordFlow.collectAsState()
    val host by model.hostFlow.collectAsState()
    val connectStatus by model.connectStatusFlow.collectAsState()
    val isStartConnect by model.isStartConnectFlow.collectAsState()
    val messageList by model.messageListFlow.collectAsState()
    Column {
        Text(text = "连接状态：$connectStatus")
        TextField(
            value = username,
            onValueChange = { model.usernameFlow.value = it },
            label = { Text(text = "账号") }
        )
        TextField(
            value = password,
            onValueChange = { model.passwordFlow.value = it },
            label = { Text(text = "密码") }
        )
        TextField(
            value = host,
            onValueChange = { model.hostFlow.value = it },
            label = { Text(text = "主机地址") }
        )
        Row {
            Button(onClick = {
                if (isStartConnect) {
                    model.onClickDisconnect()
                } else {
                    model.onClickConnect()
                }
            }) {
                Text(text = if (isStartConnect) "断开连接" else "连接")
            }
            Spacer(modifier = Modifier.width(32.dp))
            Button(
                enabled = connectStatus == MqttConnectStatus.CONNECTED,
                onClick = {
                    model.onClickPublish()
                }
            ) {
                Text(text = "发布消息")
            }
        }
        LazyColumn {
            items(messageList) {
                Text(text = "${it.topic}: ${it.message}")
            }
        }
    }
}