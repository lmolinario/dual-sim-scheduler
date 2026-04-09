package com.lello.dualsimscheduler

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lello.dualsimscheduler.automation.AutomationState
import com.lello.dualsimscheduler.automation.AutomationStateStore
import com.lello.dualsimscheduler.automation.PendingAction
import com.lello.dualsimscheduler.automation.ProfileType
import com.lello.dualsimscheduler.ui.SettingsNavigator
import com.lello.dualsimscheduler.ui.theme.DualSIMSchedulerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var store: AutomationStateStore
    private lateinit var settingsNavigator: SettingsNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = AutomationStateStore(applicationContext)
        settingsNavigator = SettingsNavigator(applicationContext)
        enableEdgeToEdge()

        setContent {
            DualSIMSchedulerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DebugPanel(
                        modifier = Modifier.padding(innerPadding),
                        store = store,
                        settingsNavigator = settingsNavigator,
                        isAccessibilityEnabled = { isAccessibilityServiceEnabled() },
                        openUserSwitcher = {
                            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                        }
                    )
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceName =
            ComponentName(this, ProfileSwitchAccessibilityService::class.java).flattenToString()

        return enabled.split(':').any { it.equals(serviceName, ignoreCase = true) }
    }
}

@Composable
private fun DebugPanel(
    modifier: Modifier = Modifier,
    store: AutomationStateStore,
    settingsNavigator: SettingsNavigator,
    isAccessibilityEnabled: () -> Boolean,
    openUserSwitcher: () -> Unit,
) {
    var workSimLabel by remember { mutableStateOf(store.getWorkSimLabel()) }
    var state by remember { mutableStateOf(store.getState()) }
    var pendingAction by remember { mutableStateOf(store.getPendingAction()) }
    var profile by remember { mutableStateOf(store.getCurrentProfile()) }
    var lastError by remember { mutableStateOf(store.getLastError()) }
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityEnabled()) }

    fun refresh() {
        state = store.getState()
        pendingAction = store.getPendingAction()
        profile = store.getCurrentProfile()
        lastError = store.getLastError()
        accessibilityEnabled = isAccessibilityEnabled()
        workSimLabel = store.getWorkSimLabel()
    }

    LaunchedEffect(Unit) {
        while (true) {
            refresh()
            delay(1_000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Dual SIM Scheduler - Debug Panel")

        OutlinedTextField(
            value = workSimLabel,
            onValueChange = {
                workSimLabel = it
                store.setWorkSimLabel(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Work SIM label") },
            singleLine = true,
        )

        Text("Current profile")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileType.entries.forEach { option ->
                Row {
                    RadioButton(
                        selected = profile == option,
                        onClick = {
                            store.setCurrentProfile(option)
                            refresh()
                        },
                    )
                    Text(text = option.name)
                }
            }
        }

        Divider()

        Button(onClick = {
            store.setPendingAction(PendingAction.GO_PRIVATE)
            store.setState(AutomationState.START_GO_PRIVATE)
            store.setLastError("")
            refresh()
        }) { Text("Test switch to Privato") }

        Button(onClick = {
            store.setPendingAction(PendingAction.GO_WORK)
            store.setState(AutomationState.START_GO_WORK)
            store.setLastError("")
            refresh()
        }) { Text("Test switch to Lavoro") }

        Button(onClick = { settingsNavigator.openSimSettings() }) {
            Text("Test open SIM settings")
        }

        Button(onClick = openUserSwitcher) {
            Text("Test open user switcher")
        }

        Button(onClick = {
            store.setPendingAction(PendingAction.DISABLE_WORK_SIM)
            store.setState(AutomationState.DISABLING_WORK_SIM)
            refresh()
        }) { Text("Test disable work SIM") }

        Button(onClick = {
            store.setPendingAction(PendingAction.ENABLE_WORK_SIM)
            store.setState(AutomationState.ENABLING_WORK_SIM)
            refresh()
        }) { Text("Test enable work SIM") }

        Divider()

        Text("Current state: ${state.name}")
        Text("Pending action: ${pendingAction.name}")
        Text("Last error: ${lastError.ifBlank { "(none)" }}")
        Text("Accessibility enabled: $accessibilityEnabled")
    }
}