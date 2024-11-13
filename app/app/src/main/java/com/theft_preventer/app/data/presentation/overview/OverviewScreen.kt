package com.theft_preventer.app.data.presentation.overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theft_preventer.app.data.remote.ApiService
import java.util.Date

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = hiltViewModel(),
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogout()
        }
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            viewModel.refreshEvents()
            isRefreshing = false
        }
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ),
        topBar = {
            TopAppBar(
                title = { Text("Events Overview") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (state.deviceStatus) {
                    DeviceStatus.LOADING -> {
                        CircularProgressIndicator()
                    }
                    DeviceStatus.REGISTERED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Device Registered")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Registered", tint = Color.Green)
                        }
                    }
                    DeviceStatus.NOT_REGISTERED -> {
                        Button(onClick = { viewModel.registerDevice() }) {
                            Text("Register Device")
                        }
                    }
                    DeviceStatus.ESP_NOT_AVAILABLE -> {
                        Text("ESP32 not available", color = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.logout() }) {
                    Text("Logout")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.error != null) {
                    Text(state.error!!, color = Color.Red)
                } else {
                    LazyColumn {
                        items(state.events) { event ->
                            EventItem(event)
                        }
                    }
                }
            }

            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}


@Composable
fun EventItem(event: ApiService.Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${event.id}")
            Text("State: ${event.state}")
            Text("Lux: ${event.lux}")
            Text("Temperature: ${event.temp}°C")
            Text("Phone: ${event.phone}")
            Text("Timestamp: ${Date(event.timestamp * 1000)}")
        }
    }
}