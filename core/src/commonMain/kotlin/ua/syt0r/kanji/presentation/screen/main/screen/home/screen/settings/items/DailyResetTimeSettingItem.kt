package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.CommonTimeFormat
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.setting_daily_reset_time_item

class DailyResetTimeSettingItem(
    private val appPreferences: PreferencesContract.AppPreferences
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var dailyResetTime: MutableState<LocalTime>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        dailyResetTime = mutableStateOf(
            value = appPreferences.dailyResetTime.get()
        )
        appPreferences.dailyResetTime.onModified
            .onEach { if (dailyResetTime.value != it) dailyResetTime.value = it }
            .launchIn(coroutineScope)
        snapshotFlow { dailyResetTime.value }
            .drop(1)
            .onEach { appPreferences.dailyResetTime.set(it) }
            .launchIn(coroutineScope)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun content(mainNavigationState: MainNavigationState) {

        var showPicker by remember { mutableStateOf(false) }
        val value = dailyResetTime.value

        ListItem(
            headlineContent = {
                Text(text = stringResource(Res.string.setting_daily_reset_time_item))
            },
            supportingContent = { Text(value.format(CommonTimeFormat)) },
            modifier = Modifier.clip(MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .clickable { showPicker = true },
        )


        if (showPicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = value.hour,
                initialMinute = value.minute,
                is24Hour = true
            )

            TimePickerDialog(
                onDismissRequest = { showPicker = false },
                onConfirm = {
                    val time = LocalTime(timePickerState.hour, timePickerState.minute)
                    dailyResetTime.value = time
                    showPicker = false
                },
                state = timePickerState
            )
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TimePickerDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit,
        state: TimePickerState
    ) {
        MultiplatformDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = stringResource(Res.string.setting_daily_reset_time_item))
            },
            content = {
                TimeInput(
                    state = state,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            },
            buttons = {
                TextButton(onClick = onDismissRequest) {
                    Text(resolveString { settings.pickerDialogCancel })
                }
                TextButton(onClick = onConfirm) {
                    Text(resolveString { settings.pickerDialogApply })
                }
            }
        )
    }
}
