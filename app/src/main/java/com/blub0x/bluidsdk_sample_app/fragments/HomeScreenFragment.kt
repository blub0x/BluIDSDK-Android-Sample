package com.blub0x.bluidsdk_sample_app.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blub0x.BluIDSDK.BluIDSDK
import com.blub0x.BluIDSDK.models.*
import com.blub0x.bluidsdk_sample_app.Database.*
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.model.SharedDataModel
import com.blub0x.bluidsdk_sample_app.utils.Utility
import com.blub0x.bluidsdk_sample_app.utils.Utility.deviceStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.text.SimpleDateFormat
import com.blub0x.bluidsdk_sample_app.databinding.FragmentHomeScreenBinding
import com.blub0x.bluidsdk_sample_app.utils.Utility.m_BluIDSDK_Client


@ObsoleteCoroutinesApi
class HomeScreenFragment : Fragment() {
    private val m_TAG = "HomeScreen"
    private val m_model: SharedDataModel by activityViewModels()
    private val m_scope = CoroutineScope(newSingleThreadContext(m_TAG))
    var m_userSettings: UserPreferences? = null
    private var m_blePower = 1
    private val m_minBlePower = 1
    private val m_maxBlePower = 7
    var m_idleLEDStateSelected: String = "Off"
    var m_inUseLEDStateSelected: String = "Off"
    private var m_deviceiOSBackgroundRangeValue = -50
    private var m_deviceiOSForegroundRangeValue = -50
    private var m_deviceAndroidRangeValue = -50
    private var m_deviceTapValue = -50
    private var m_deviceTwistValue = -50
    private var m_deviceAppSpecificValue = -50
    private var m_deviceAppleWatchValue = -50
    private var m_deviceAIValue = -50
    private var m_deviceInRangeValue = -50
    private var m_deviceBluREMOTEValue = -50
    private var m_deviceWaveValue = -50

    private val m_ledIntensity1: Byte = 0xFF.toByte()
    private val m_ledIntensity2: Byte = 0xBF.toByte()
    private val m_ledIntensity3: Byte = 0x80.toByte()

    private val m_diagnosticLogsStringBuilder = StringBuilder()
    private var m_isGetLEDCalled = false
    private var m_isUnlockClicked = false

    private var _binding : FragmentHomeScreenBinding? = null
    private val binding get() = _binding

    val m_deviceStateLEDColors: HashMap<String, Device_LED_Color> = hashMapOf(
        "Red" to Device_LED_Color(m_ledIntensity1, 0, 0),
        "Green" to Device_LED_Color(0, m_ledIntensity1, 0),
        "Blue" to Device_LED_Color(0, 0, m_ledIntensity1),
        "Amber" to Device_LED_Color(m_ledIntensity1, m_ledIntensity2, 0),
        "Cyan" to Device_LED_Color(0, m_ledIntensity1, m_ledIntensity1),
        "Magenta" to Device_LED_Color(m_ledIntensity1, 0, m_ledIntensity1),
        "White" to Device_LED_Color(m_ledIntensity1, m_ledIntensity1, m_ledIntensity1),
        "HostControlled" to Device_LED_Color(m_ledIntensity3, m_ledIntensity3, m_ledIntensity3),
        "Off" to Device_LED_Color(0, 0, 0)
    )

    private val m_minBluPointGestureStrength = -90
    private val m_maxBluPointGestureStrength = -20
    var m_sdkVersion: String = ""
    var m_deviceId: String = ""

    val m_envTypeList = arrayListOf<String>("PRODUCTION", "QA", "QA_TEST_2", "QA_TEST_3")
    val m_ledColorsList = arrayListOf<String>(
        "Off",
        "White",
        "Cyan",
        "HostControlled",
        "Magenta",
        "Amber",
        "Blue",
        "Green",
        "Red"
    )
    val m_accessTypesList = arrayListOf<String>("foreground", "phoneUnlocked", "always")
    private var m_unlockedDevice = false

    fun enableDisableView(view: View, enabled: Boolean) {
        view.setEnabled(enabled)
        if (view is ViewGroup) {
            val group = view
            for (idx in 0 until group.childCount) {
                enableDisableView(group.getChildAt(idx), enabled)
            }
        }
    }

    private fun decreaseValue(currentVal: Int, minVal: Int): Boolean {
        return currentVal - 1 >= minVal
    }

    private fun increaseValue(currentVal: Int, maxVal: Int): Boolean {
        return currentVal + 1 <= maxVal
    }

    private fun getBlePower(blePowerResponse: GetStrengthResponse?) {
        activity?.runOnUiThread {
            blePowerResponse?.let { _blePowerResponse ->
                _blePowerResponse.error?.let {
                    Utility.m_AlertDialog?.show("Error : $it")
                    return@runOnUiThread
                }
                _blePowerResponse.data?.let {
                    m_blePower = it
                    binding?.blePowerValue?.text = it.toString()
                    Utility.m_ProgressBar?.dismiss()
                    return@runOnUiThread
                }
            }
            Utility.m_AlertDialog?.show("Error : No response ")
        }
    }

    private fun getDeviceStateLEDColour(
        getDeviceStateLedColorResponse: GetDeviceStateLedColorResponse?,
        deviceState: DeviceLEDState
    ) {
        activity?.runOnUiThread {
            getDeviceStateLedColorResponse?.let { _getDeviceStateLedColorResponse ->
                _getDeviceStateLedColorResponse.error?.let {
                    Utility.m_AlertDialog?.show("Error : $it")
                    return@runOnUiThread
                }
                _getDeviceStateLedColorResponse.ledColor?.let { ledColour ->
                    val bluPointLEDColour =
                        m_deviceStateLEDColors.filterValues { it == ledColour }.keys.firstOrNull()
                    val ledColorIndex = m_ledColorsList.indexOf(bluPointLEDColour)
                    if (deviceState == DeviceLEDState.idle) {
                        m_isGetLEDCalled = true
                        binding?.idleLEDList?.setSelection(ledColorIndex)
                        m_model.idleLED.value = bluPointLEDColour
                    } else {
                        m_isGetLEDCalled = true
                        binding?.inUseLEDList?.setSelection(ledColorIndex)
                        m_model.inUseLED.value = bluPointLEDColour
                    }
                    return@runOnUiThread
                }
            }
            Utility.m_AlertDialog?.show("Error : No response ")
        }
    }

    fun setDeviceStateLEDColour(
        deviceState: DeviceLEDState,
        ledColour: Device_LED_Color,
        deviceId: String
    ) {
        Log.i(m_TAG, "setDeviceLedColour isGetLEDCalled $m_isGetLEDCalled ")
        if (m_isGetLEDCalled) {
            m_isGetLEDCalled = false
            return
        }
        Log.i(m_TAG, "call sdkClient setDeviceLedColour isGetLEDCalled $m_isGetLEDCalled ")
        m_scope.launch {
            Utility.m_BluIDSDK_Client?.let { sdkClient ->
                val deviceLEDResponse =
                    sdkClient.setDeviceStateLEDColor(deviceId, deviceState, ledColour)
                activity?.runOnUiThread {
                    deviceLEDResponse?.let {
                        Utility.m_AlertDialog?.show("Error : $it")
                        return@runOnUiThread
                    }
                    Toast.makeText(
                        view?.context,
                        "LED colour set",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun enhancedTapSettingsEnable() {
        binding?.decreaseIOSBackgroundRange?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(
                        m_deviceiOSBackgroundRangeValue,
                        m_minBluPointGestureStrength
                    )
                ) {
                    m_deviceiOSBackgroundRangeValue--
                    binding?.showIOSBackgroundRange?.text =
                        m_deviceiOSBackgroundRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Background Range decreased",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding?.increaseIOSBackgroundRange?.isEnabled?.let { increaseiosBackgroundRangeIsEnabled ->
                        if (!increaseiosBackgroundRangeIsEnabled) {
                            binding?.increaseIOSBackgroundRange?.isEnabled = true
                        }
                    }

                    if (m_deviceiOSBackgroundRangeValue == m_minBluPointGestureStrength) {
                        binding?.decreaseIOSBackgroundRange?.isEnabled = false
                    }

                } else {
                    binding?.decreaseIOSBackgroundRange?.isEnabled = false
                }


            }
        )

        binding?.increaseIOSBackgroundRange?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(
                        m_deviceiOSBackgroundRangeValue,
                        m_maxBluPointGestureStrength
                    )
                ) {
                    m_deviceiOSBackgroundRangeValue++
                    binding?.showIOSBackgroundRange?.text =
                        m_deviceiOSBackgroundRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Background Range increased",
                        Toast.LENGTH_SHORT
                    ).show()


                    binding?.decreaseIOSBackgroundRange?.isEnabled?.let { decreaseiosBackgroundRangeIsEnbaled ->
                        if (!decreaseiosBackgroundRangeIsEnbaled) {
                            binding?.decreaseIOSBackgroundRange?.isEnabled = true
                        }
                    }

                    if (m_deviceiOSBackgroundRangeValue == m_maxBluPointGestureStrength) {
                        binding?.increaseIOSBackgroundRange?.isEnabled = false
                    }
                } else {
                    binding?.increaseIOSBackgroundRange?.isEnabled = false
                }

            }
        )


        binding?.decreaseIOSForegroundRange?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(
                        m_deviceiOSForegroundRangeValue,
                        m_minBluPointGestureStrength
                    )
                ) {
                    m_deviceiOSForegroundRangeValue--
                    binding?.showIOSForegroundRange?.text =
                        m_deviceiOSForegroundRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Foreground Range decreased",
                        Toast.LENGTH_SHORT
                    ).show()


                    binding?.increaseIOSForegroundRange?.isEnabled?.let { increaseiosForegroundRangeIsEnabled ->
                        if (!increaseiosForegroundRangeIsEnabled) {
                            binding?.increaseIOSForegroundRange?.isEnabled = true
                        }
                    }

                    if (m_deviceiOSForegroundRangeValue == m_minBluPointGestureStrength) {
                        binding?.decreaseIOSForegroundRange?.isEnabled = false
                    }

                } else {
                    binding?.decreaseIOSForegroundRange?.isEnabled = false
                }


            }
        )

        binding?.increaseIOSForegroundRange?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(
                        m_deviceiOSForegroundRangeValue,
                        m_maxBluPointGestureStrength
                    )
                ) {
                    m_deviceiOSForegroundRangeValue++
                    binding?.showIOSForegroundRange?.text =
                        m_deviceiOSForegroundRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Foreground Range increased",
                        Toast.LENGTH_SHORT
                    ).show()


                    binding?.decreaseIOSForegroundRange?.isEnabled?.let { decreaseiOSForegroundRangeIsEnabled ->
                        if (!decreaseiOSForegroundRangeIsEnabled) {
                            binding?.decreaseIOSForegroundRange?.isEnabled = true
                        }
                    }

                    if (m_deviceiOSForegroundRangeValue == m_maxBluPointGestureStrength) {
                        binding?.increaseIOSForegroundRange?.isEnabled = false
                    }
                } else {
                    binding?.increaseIOSForegroundRange?.isEnabled = false
                }

            }
        )

        binding?.decreaseAndroidRange?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceAndroidRangeValue, m_minBluPointGestureStrength)) {
                    m_deviceAndroidRangeValue--
                    binding?.showAndroidRange?.text = m_deviceAndroidRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Android Range decreased",
                        Toast.LENGTH_SHORT
                    ).show()


                    binding?.increaseAndroidRange?.isEnabled?.let { increaseAndroidRangeIsEnabled ->
                        if (!increaseAndroidRangeIsEnabled) {
                            binding?.increaseAndroidRange?.isEnabled = true
                        }
                    }

                    if (m_deviceAndroidRangeValue == m_minBluPointGestureStrength) {
                        binding?.decreaseAndroidRange?.isEnabled = false
                    }

                } else {
                    binding?.decreaseAndroidRange?.isEnabled = false

                }


            }
        )

        binding?.increaseAndroidRange?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceAndroidRangeValue, m_maxBluPointGestureStrength)) {
                    m_deviceAndroidRangeValue++
                    binding?.showAndroidRange?.text = m_deviceAndroidRangeValue.toString()
                    Toast.makeText(
                        view?.context,
                        "iOS Android Range increased",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding?.decreaseAndroidRange?.isEnabled?.let { decreaseAndroidRangeIsEnable ->
                        if (!decreaseAndroidRangeIsEnable) {
                            binding?.decreaseAndroidRange?.isEnabled = true
                        }
                    }

                    if (m_deviceAndroidRangeValue == m_maxBluPointGestureStrength) {
                        binding?.increaseAndroidRange?.isEnabled = false
                    }

                } else {
                    binding?.increaseAndroidRange?.isEnabled = false
                }

            }
        )
    }

    private fun getDiagnosticsLogs(diagnosticLogs: DiagnosticLog) {
        val deviceTitle = diagnosticLogs.title
        val timeEpoch = diagnosticLogs.time
        val timeFormat = SimpleDateFormat("HH:mm:ss z")
        val deviceTime = timeFormat.format(timeEpoch)
        val deviceDetails = diagnosticLogs.userDeviceInfo
        m_diagnosticLogsStringBuilder.clear()
        m_diagnosticLogsStringBuilder.append(deviceTitle + "\n").append(deviceTime + "\n")
            .append(deviceDetails + "\n").append("Logs : \n")
        m_diagnosticLogsStringBuilder.append(diagnosticLogs.logs)
        val logs = diagnosticLogs.logs
        Log.d("Logs in UI : ", logs)

        activity?.runOnUiThread {
            binding?.showDiagnosticLogs?.movementMethod = ScrollingMovementMethod()
            binding?.showDiagnosticLogs?.setText(m_diagnosticLogsStringBuilder.toString())
        }
    }

    private fun updateUserPreferences(setting: UserPreferences) {
        Utility.m_BluIDSDK_Client?.saveUserPreferences(setting)
        m_model.updateUserSettings(setting)
    }

    fun logout() {
        m_scope.launch {
            Utility.m_BluIDSDK_Client?.logout()
        }
    }

    private fun enableOrDisableIncreaseDecreaseButtons() {
        activity?.runOnUiThread {
            if (m_blePower == m_maxBlePower) binding?.increasePower?.isEnabled = false
            if (m_blePower == m_minBlePower) binding?.decreasePower?.isEnabled = false
            if (m_deviceiOSBackgroundRangeValue == m_minBluPointGestureStrength) binding?.decreaseIOSBackgroundRange?.isEnabled =
                false
            if (m_deviceiOSBackgroundRangeValue == m_maxBluPointGestureStrength) binding?.increaseIOSBackgroundRange?.isEnabled =
                false
            if (m_deviceiOSForegroundRangeValue == m_minBluPointGestureStrength) binding?.decreaseIOSForegroundRange?.isEnabled =
                false
            if (m_deviceiOSForegroundRangeValue == m_maxBluPointGestureStrength) binding?.increaseIOSForegroundRange?.isEnabled =
                false
            if (m_deviceAndroidRangeValue == m_minBluPointGestureStrength) binding?.decreaseAndroidRange?.isEnabled =
                false
            if (m_deviceAndroidRangeValue == m_maxBluPointGestureStrength) binding?.increaseAndroidRange?.isEnabled =
                false
            if (m_deviceTapValue == m_minBluPointGestureStrength) binding?.decreaseTap?.isEnabled = false
            if (m_deviceTapValue == m_maxBluPointGestureStrength) binding?.increaseTap?.isEnabled = false
            if (m_deviceTwistValue == m_minBluPointGestureStrength) binding?.decreaseTwist?.isEnabled = false
            if (m_deviceTwistValue == m_maxBluPointGestureStrength) binding?.increaseTwist?.isEnabled = false
            if (m_deviceAppSpecificValue == m_minBluPointGestureStrength) binding?.decreaseAppSpecific?.isEnabled =
                false
            if (m_deviceAppSpecificValue == m_maxBluPointGestureStrength) binding?.increaseAppSpecific?.isEnabled =
                false
            if (m_deviceWaveValue == m_minBluPointGestureStrength) binding?.decreaseWave?.isEnabled = false
            if (m_deviceWaveValue == m_maxBluPointGestureStrength) binding?.increaseWave?.isEnabled = false
            if (m_deviceAIValue == m_minBluPointGestureStrength) binding?.decreaseAI?.isEnabled = false
            if (m_deviceAIValue == m_maxBluPointGestureStrength) binding?.increaseAI?.isEnabled = false
            if (m_deviceAppleWatchValue == m_minBluPointGestureStrength) binding?.decreaseAppleWatch?.isEnabled =
                false
            if (m_deviceAppleWatchValue == m_maxBluPointGestureStrength) binding?.increaseAppleWatch?.isEnabled =
                false
            if (m_deviceInRangeValue == m_minBluPointGestureStrength) binding?.decreaseInRange?.isEnabled = false
            if (m_deviceInRangeValue == m_maxBluPointGestureStrength) binding?.increaseInRange?.isEnabled = false
            if (m_deviceBluREMOTEValue == m_minBluPointGestureStrength) binding?.decreaseBluREMOTE?.isEnabled =
                false
            if (m_deviceBluREMOTEValue == m_maxBluPointGestureStrength) binding?.increaseBluREMOTE?.isEnabled =
                false
        }
    }

    private fun updateSelectedDeviceDetailsInUI() {
        m_model.selectedDevice.value?.let {
            m_deviceTapValue = it.tapSettings?.power ?: -50
            m_deviceTwistValue = it.twistSettings?.power ?: -50
            m_deviceInRangeValue = it.rangeSettings?.power ?: -50
            m_deviceAppSpecificValue = it.appSpecificSettings?.power ?: -50
            m_deviceWaveValue = it.waveSettings?.power ?: -50
            m_deviceAIValue = it.aiSettings?.power ?: -50
            m_deviceAppleWatchValue = it.appleWatchSettings?.power ?: -50
            m_deviceBluREMOTEValue = it.BluREMOTESettings?.power ?: -50
            m_deviceiOSBackgroundRangeValue = it.enhancedTapSettings?.iosBackgroundPower ?: -50
            m_deviceiOSForegroundRangeValue = it.enhancedTapSettings?.iosForegroundPower ?: -50
            m_deviceAndroidRangeValue = it.enhancedTapSettings?.androidPower ?: -50

            binding?.tapValue?.text = m_deviceTapValue.toString()
            binding?.twistValue?.text = m_deviceTwistValue.toString()
            binding?.inRangeValue?.text = m_deviceInRangeValue.toString()
            binding?.appSpecificValue?.text = m_deviceAppSpecificValue.toString()
            binding?.waveValue?.text = m_deviceWaveValue.toString()
            binding?.AIValue?.text = m_deviceAIValue.toString()
            binding?.appleWatchValue?.text = m_deviceAppleWatchValue.toString()
            binding?.bluREMOTEValue?.text = m_deviceBluREMOTEValue.toString()
            binding?.showIOSBackgroundRange?.text = m_deviceiOSBackgroundRangeValue.toString()
            binding?.showIOSForegroundRange?.text = m_deviceiOSForegroundRangeValue.toString()
            binding?.showAndroidRange?.text = m_deviceAndroidRangeValue.toString()

            binding?.bluPOINTTap?.isChecked = it.tapSettings?.enabled ?: false
            binding?.bluPOINTTwist?.isChecked = it.twistSettings?.enabled ?: false
            binding?.bluPOINTINRange?.isChecked = it.rangeSettings?.enabled ?: false
            binding?.bluPOINTAppSpecific?.isChecked = it.appSpecificSettings?.enabled ?: false
            binding?.bluPOINTWave?.isChecked = it.waveSettings?.enabled ?: false
            binding?.bluPOINTAI?.isChecked = it.aiSettings?.enabled ?: false
            binding?.bluPOINTAppleWatch?.isChecked = it.appleWatchSettings?.enabled ?: false
            binding?.bluPOINTBluREMOTE?.isChecked = it.BluREMOTESettings?.enabled ?: false
            binding?.toggleDeviceEnhancedTap?.isChecked = it.enhancedTapSettings?.enabled ?: false

            binding?.nameValue?.setText(it.name)

            m_deviceId = it.id
            binding?.unlockBluPointButton?.isEnabled = true
            binding?.blePowerValue?.text = m_blePower.toString()
        }

        if (m_model.userLoginData.value == null) {
            binding?.unlockBluPointButton?.isEnabled = false
            binding?.flashFirmwareMultiDevice?.isEnabled = false
        }

        if (m_model.selectedDevice.value == null) {
            binding?.tapValue?.text = m_deviceTapValue.toString()
            binding?.twistValue?.text = m_deviceTwistValue.toString()
            binding?.inRangeValue?.text = m_deviceInRangeValue.toString()
            binding?.appSpecificValue?.text = m_deviceAppSpecificValue.toString()
            binding?.waveValue?.text = m_deviceWaveValue.toString()
            binding?.AIValue?.text = m_deviceAIValue.toString()
            binding?.appleWatchValue?.text = m_deviceAppleWatchValue.toString()
            binding?.bluREMOTEValue?.text = m_deviceBluREMOTEValue.toString()
        }

        if (m_model.updatedFirmwareForSingleDevice.value == true) {
            m_model.updatedFirmwareForSingleDevice.value = false
            m_model.deviceUnlocked.value = false
            binding?.deviceFunctionality?.let { deviceFunctionality -> onDisconnect(deviceFunctionality) }
        }

        if (m_model.deviceUnlocked.value == true) {
            binding?.unlockBluPointButton?.isEnabled = false
            binding?.toggleAutoTransfer?.isEnabled = false
        }

        if( m_model.syncPersonCardsPersonID.value != null)
        {
            binding?.userIDValue?.setText(m_model.syncPersonCardsPersonID.value)
        }


    }

    private fun onDeviceUnlock(
        deviceId: String
    ) {
        m_scope.launch {

            val blePowerResponse: GetStrengthResponse? =
                Utility.m_BluIDSDK_Client?.getBLETxPowerLevel(deviceId)
            getBlePower(blePowerResponse)
            val getDeviceStateLedColorResponse =
                Utility.m_BluIDSDK_Client?.getDeviceStateLEDColor(deviceId, DeviceLEDState.idle)

            getDeviceStateLEDColour(getDeviceStateLedColorResponse, DeviceLEDState.idle)

            val getDeviceStateLedColorResponse1 =
                Utility.m_BluIDSDK_Client?.getDeviceStateLEDColor(deviceId, DeviceLEDState.in_use)

            getDeviceStateLEDColour(getDeviceStateLedColorResponse1, DeviceLEDState.in_use)

            m_unlockedDevice = true
            enableOrDisableIncreaseDecreaseButtons()
           // binding?.toggleAutoTransfer?.isEnabled = false
        }
    }

    private fun getDiagnosticLogs()
    {
        m_scope.launch {
            val diagnosticLogs = Utility.m_BluIDSDK_Client?.getDiagnosticInfo()
            if (diagnosticLogs != null) {
                getDiagnosticsLogs(diagnosticLogs)
            }
        }
    }

    fun getUserGesturePreferences() {
        binding?.toggleTap?.isChecked = m_userSettings?.enableTap ?: false
        binding?.toggleTwist?.isChecked = m_userSettings?.enableTwist ?: false
        binding?.toggleInRange?.isChecked = m_userSettings?.enableRange ?: false
        binding?.toggleAppSpecific?.isChecked = m_userSettings?.enableAppSpecific ?: false
        binding?.toggleWave?.isChecked = m_userSettings?.enableWave ?: false
        binding?.toggleAI?.isChecked = m_userSettings?.enableAI ?: false
        binding?.toggleAppleWatch?.isChecked = m_userSettings?.enableAppleWatch ?: false
        binding?.toggleBluRemote?.isChecked = m_userSettings?.enableBluREMOTE ?: false
        binding?.toggleEnhancedTap?.isChecked = m_userSettings?.enhancedTap ?: false
        binding?.toggleVibrate?.isChecked = m_userSettings?.enableVibrate ?: false
        val accessTypeSelected = m_userSettings?.allowAccess?.accessType ?: 0
        binding?.accessType?.setSelection(accessTypeSelected)
    }

    fun disconnectDevice() {
        m_model.selectedDevice.value?.let { _ ->
            m_scope.launch {
                Utility.m_BluIDSDK_Client?.let { sdkClient ->
                    val response = sdkClient.lockDeviceAccess(m_deviceId)
                    activity?.runOnUiThread {
                        if (response == null) {
                            Toast.makeText(
                                view?.context,
                                "Device disconnected",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding?.deviceFunctionality?.let { onDisconnect(it) }
                            m_model.deviceUnlocked.value = false

                        } else {
                            Utility.m_AlertDialog?.show("Error : " + response)
                        }

                    }
                }
            }
        }
    }

    fun onDisconnect(deviceFunctions: View) {
        enableDisableView(deviceFunctions, false)
        m_model.selectedDevice.value = null
        m_model.deviceUnlocked.value = null
        binding?.selectedDeviceLabel?.setText("")
        binding?.unlockBluPointButton?.isEnabled = false
        binding?.flashFirmwareMultiDevice?.isEnabled = true
        binding?.nameValue?.setText("")
        binding?.blePowerValue?.setText("1")
        binding?.tapValue?.text = "-50"
        binding?.twistValue?.text = "-50"
        binding?.appSpecificValue?.text = "-50"
        binding?.appleWatchValue?.text = "-50"
        binding?.waveValue?.text = "-50"
        binding?.AIValue?.text = "-50"
        binding?.bluREMOTEValue?.text = "-50"
        binding?.inRangeValue?.text = "-50"
        binding?.showIOSBackgroundRange?.text = "-50"
        binding?.showIOSForegroundRange?.text = "-50"
        binding?.showAndroidRange?.text = "-50"
        binding?.bluPOINTTap?.isChecked = false
        binding?.bluPOINTTwist?.isChecked = false
        binding?.bluPOINTINRange?.isChecked = false
        binding?.bluPOINTAppSpecific?.isChecked = false
        binding?.bluPOINTWave?.isChecked = false
        binding?.bluPOINTAI?.isChecked = false
        binding?.bluPOINTAppleWatch?.isChecked = false
        binding?.bluPOINTBluREMOTE?.isChecked = false
        binding?.toggleDeviceEnhancedTap?.isChecked = false
        binding?.toggleRingBuzzer?.isChecked = false
        binding?.showDiagnosticLogs?.setText("")
        m_isUnlockClicked = false
        m_model.deviceUnlocked.value = false
        m_isUnlockClicked = false
        binding?.toggleAutoTransfer?.isEnabled = true

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(m_TAG, "back pressed")
                }
            })

        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.unlockBluPointButton?.isEnabled = false

        binding?.deviceFunctionality?.let { enableDisableView(it, false) }
        if (m_model.viewInit.value == null) {
            m_model.viewInit.value = true
            m_model.deviceUnlocked.value = false
            binding?.flashFirmwareMultiDevice?.isEnabled = false
            binding?.getCardsByID?.isEnabled = false
            binding?.userIDValue?.isEnabled = false
        }

        m_model.userLoginData.observe(viewLifecycleOwner, Observer<UserData?> { item ->
            item?.userName?.let {
                binding?.loginNavigationButton?.text = "Logout"
                binding?.userNameLabel?.text = it
                binding?.flashFirmwareMultiDevice?.isEnabled = true
                binding?.userIDValue?.isEnabled = true
                binding?.getCardsByID?.isEnabled = true
                return@Observer
            }
            binding?.userNameLabel?.text = "none"
            binding?.flashFirmwareMultiDevice?.isEnabled = false
            binding?.userIDValue?.isEnabled = false
            binding?.getCardsByID?.isEnabled = false
            m_model.syncPersonCardsPersonID.value = null
            binding?.userIDValue?.setText("")
           binding?.loginNavigationButton?.text = "Login"
        })

        m_model.selectedDevice.observe(viewLifecycleOwner, Observer<Device_Information?> {
            it?.let {
                binding?.selectedDeviceLabel?.text = "${it.name}\nv${it.firmwareVersion ?: ""}"
                binding?.flashFirmwareMultiDevice?.isEnabled = true
                return@Observer
            }
            binding?.selectedDeviceLabel?.text = ""
        })
        binding?.loginNavigationButton?.setOnClickListener(
            View.OnClickListener {
                if (binding?.loginNavigationButton?.text == "Logout") {
                    binding?.loginNavigationButton?.text = "Login"
                    m_model.userLoginData.value = null
                    binding?.deviceFunctionality?.let { deviceFunctionality -> enableDisableView(deviceFunctionality, false) }
                    binding?.userNameLabel?.text = "(none)"
                    m_model.selectedDevice.value?.let { _ ->
                        m_scope.launch {
                            Utility.m_BluIDSDK_Client?.lockDeviceAccess(m_deviceId)
                        }
                    }
                    logout()
                    Toast.makeText(
                        view.context,
                        "Logged Out",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    findNavController().navigate(R.id.action_homeScreenFragment_to_userLoginFragment)
                }
            }
        )

        binding?.bleDeviceScanNavigationButton?.setOnClickListener(
            View.OnClickListener {
                findNavController().navigate(R.id.action_homeScreenFragment_to_scanDevicesFragment)
            }
        )
        val envSelected = DBController().getEnvironment(requireActivity())?.env
            ?: BluIDSDK_Environment.valueOf("QA").toString()
        val environmentAdapter = ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.environment,
            android.R.layout.simple_spinner_item
        )

        updateSelectedDeviceDetailsInUI()

        binding?.toggleTap?.setOnCheckedChangeListener(null)
        binding?.toggleTwist?.setOnCheckedChangeListener(null)
        binding?.toggleInRange?.setOnCheckedChangeListener(null)
        binding?.toggleAppSpecific?.setOnCheckedChangeListener(null)
        binding?.toggleWave?.setOnCheckedChangeListener(null)
        binding?.toggleAI?.setOnCheckedChangeListener(null)
        binding?.toggleAppleWatch?.setOnCheckedChangeListener(null)
        binding?.toggleBluRemote?.setOnCheckedChangeListener(null)
        binding?.toggleEnhancedTap?.setOnCheckedChangeListener(null)
        binding?.toggleVibrate?.setOnCheckedChangeListener(null)
        binding?.enableFileLogging?.setOnCheckedChangeListener(null)
        environmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.environmentList?.adapter = environmentAdapter
        val index = m_envTypeList.indexOf(envSelected)
        binding?.environmentList?.setSelection(index)

        binding?.environmentList?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                val env = m_envTypeList[position]
                if (m_model.selectedEnvironment.value != env) {
                    Utility.m_BluIDSDK_Client =
                        BluIDSDK(
                            BluIDSDK_Environment.valueOf(env),
                            requireActivity(),
                            deviceStateObserver
                        )
                    m_model.selectedEnvironment.value = env
                    DBController().saveEnvironment(
                        requireActivity(),
                        Environment(env)
                    )

                    if(Utility.m_BluIDSDK_Client?.isBatteryOptimizationEnabled(requireActivity().packageName) != false){
                        Utility.m_AlertDialog?.showBatteryOptimizationOptions()
                    }

                    binding?.loginNavigationButton?.text = "Login"
                    binding?.deviceFunctionality?.let { enableDisableView(it, false) }
                    binding?.userNameLabel?.text = "(none)"
                    m_model.selectedDevice.value?.let { _ ->
                        m_scope.launch {
                            Utility.m_BluIDSDK_Client?.lockDeviceAccess(m_deviceId)
                        }
                    }
                    binding?.deviceFunctionality?.let { onDisconnect(it) }
                    logout()
                }
                m_userSettings = Utility.m_BluIDSDK_Client?.getUserPreferences()
                m_model.updateUserSettings(m_userSettings)
                getUserGesturePreferences()
                m_sdkVersion = Utility.m_BluIDSDK_Client?.getSDKVersion().toString()
                binding?.sdkVersionDisplay?.text = m_sdkVersion
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        if (m_model.generalUserSettings.value != null) {
            binding?.toggleTap?.isChecked = m_model.generalUserSettings.value?.enableTap ?: false
            binding?.toggleTwist?.isChecked = m_model.generalUserSettings.value?.enableTwist ?: false
            binding?.toggleInRange?.isChecked = m_model.generalUserSettings.value?.enableRange ?: false
            binding?.toggleAppSpecific?.isChecked =
                m_model.generalUserSettings.value?.enableAppSpecific ?: false
            binding?.toggleWave?.isChecked = m_model.generalUserSettings.value?.enableWave ?: false
            binding?.toggleAI?.isChecked = m_model.generalUserSettings.value?.enableAI ?: false
            binding?.toggleAppleWatch?.isChecked =
                m_model.generalUserSettings.value?.enableAppleWatch ?: false
            binding?.toggleBluRemote?.isChecked = m_model.generalUserSettings.value?.enableBluREMOTE ?: false
            binding?.toggleEnhancedTap?.isChecked = m_model.generalUserSettings.value?.enhancedTap ?: false
            binding?.toggleVibrate?.isChecked = m_model.generalUserSettings.value?.enableVibrate ?: false
            val accessTypeSelected = m_model.generalUserSettings.value?.allowAccess?.accessType ?: 0
            binding?.accessType?.setSelection(accessTypeSelected)
        }

        binding?.diagnosticLogsButton?.setOnClickListener(
            View.OnClickListener {
                getDiagnosticLogs()
            }
        )

        binding?.transferCredsButton?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                        m_model.selectedDevice.value?.let { device ->
                            val error = Utility.m_BluIDSDK_Client?.transferCredential(device.id)

                            if (error == null) {
                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        view.context,
                                        "ACCESS GRANTED",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            error?.let { bluIDSDKError ->
                                activity?.runOnUiThread {
                                    Utility.m_AlertDialog?.show("${bluIDSDKError.message} type:${bluIDSDKError.type}")
                                }
                            }
                        }
                }
            }
        )


        binding?.toggleAutoTransfer?.isChecked = m_model.autoTransfer.value ?: false

        binding?.toggleAutoTransfer?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (!b) {
                Utility.m_BluIDSDK_Client?.stopGestureBasedAuthentication()
                m_model.autoTransfer.value = false
            } else {
                Utility.m_BluIDSDK_Client?.startGestureBasedAuthentication()
                m_model.autoTransfer.value = true

                    Utility.m_IsScanningStarted = true
                    m_scope.launch {
                        val scanError = Utility.m_BluIDSDK_Client?.startDeviceDiscovery(
                            ScanFilter(
                                -80,
                                1000
                            )
                        ) { bleDevices ->
                            Unit
                            Log.d(m_TAG, bleDevices.map { it.name }.toString())
                        }
                        if (scanError != null) {
                            activity?.runOnUiThread {
                                Toast.makeText(view.context, scanError.message, Toast.LENGTH_LONG)
                                    .show()
                                m_model.autoTransfer.value = false
                                scanError.message?.let { Utility.m_AlertDialog?.show(it) }
                            }
                        }
                    }
            }
        })

        val adapter = ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.AllowAccessType,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.accessType?.adapter = adapter

        if (m_model.selectedAllowAccessType.value != null) {
            binding?.accessType?.setSelection(m_model.selectedAllowAccessType.value!!)
        }

        binding?.accessType?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                val access = m_accessTypesList[position]
                m_userSettings?.allowAccess =
                    AllowAccessType.valueOf(access)
                if (m_userSettings != null) {
                    Utility.m_BluIDSDK_Client?.saveUserPreferences(m_userSettings!!)
                }
                m_model.selectedAllowAccessType.value = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                m_userSettings?.allowAccess?.accessType = 0
                if (m_userSettings != null) {
                    Utility.m_BluIDSDK_Client?.saveUserPreferences(m_userSettings!!)
                }
            }
        }

        binding?.toggleTap?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableTap = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }
        binding?.toggleTwist?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableTwist = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.toggleInRange?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableRange = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.toggleAppSpecific?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableAppSpecific = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.toggleWave?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableWave = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.toggleAI?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableAI = isChecked
            m_userSettings?.let { setting ->
                updateUserPreferences(setting)
            }
        }

        binding?.toggleAppleWatch?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableAppleWatch = isChecked
            m_userSettings?.let { setting ->
                updateUserPreferences(setting)
            }
        }

        binding?.toggleBluRemote?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableBluREMOTE = isChecked
            m_userSettings?.let { setting ->
                updateUserPreferences(setting)
            }
        }

        binding?.toggleEnhancedTap?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings = m_userSettings?.copy(enhancedTap = isChecked)
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.toggleVibrate?.setOnCheckedChangeListener { _, isChecked ->
            m_userSettings?.enableVibrate = isChecked
            m_userSettings.let { setting ->
                if (setting != null) {
                    updateUserPreferences(setting)
                }
            }
        }

        binding?.enableFileLogging?.setOnCheckedChangeListener { _, isChecked ->
                m_BluIDSDK_Client?.enableFileLogging(isChecked)
        }
        m_model.selectedDevice.value?.let {
            updateSelectedDeviceDetailsInUI()
        }

        if (m_model.deviceUnlocked.value == true) {
            binding?.unlockBluPointButton?.isEnabled = false
            Log.d("Device is unlocked button: ", binding?.unlockBluPointButton?.isEnabled.toString())
            binding?.deviceFunctionality?.let { enableDisableView(it, true) }
            m_isUnlockClicked = true
            onDeviceUnlock(m_deviceId)
            if (binding?.toggleDeviceEnhancedTap?.isChecked == true) {
                binding?.enhancedTapMobile?.let { enableDisableView(it, true) }
                enhancedTapSettingsEnable()
            } else {
                binding?.enhancedTapMobile?.let { enableDisableView(it, false) }
            }

            updateSelectedDeviceDetailsInUI()
        } else {
            updateSelectedDeviceDetailsInUI()
            binding?.deviceFunctionality?.let { enableDisableView(it, false) }
        }

        binding?.unlockBluPointButton?.setOnClickListener(
            View.OnClickListener {
                Log.d(m_TAG, "unlock blupoint called")

                Utility.m_BluIDSDK_Client?.stopGestureBasedAuthentication()
                Utility.m_BluIDSDK_Client?.stopDeviceDiscovery()

                var errorCheck: BluIDSDKError? = null
                var expiryTimeCheck: Long? = null

                m_model.selectedDevice.value?.let { selectedDevice ->
                    Utility.m_ProgressBar?.show()
                    m_scope.launch {
                        if (m_isUnlockClicked) {
                            Utility.m_ProgressBar?.dismiss()
                            Log.d(m_TAG, "unlocked blupoint")

                            return@launch
                        }
                        m_model.selectedDevice.value?.let { _ ->
                            val deviceId = selectedDevice.id
                            m_isUnlockClicked = true
                            Utility.m_BluIDSDK_Client?.unlockDeviceAccess(
                                deviceId,
                                onResponse = fun(error: BluIDSDKError?, expiryTime: Long?) {
                                    Log.d(m_TAG, "got response from unlock blupoint")
                                    m_isUnlockClicked = false
                                    activity?.runOnUiThread {
                                        Utility.m_ProgressBar?.dismiss()
                                        error?.let { bluIDSDKError ->
                                            errorCheck = bluIDSDKError
                                            Log.e(m_TAG, "Couldn't unlock device")
                                            if (bluIDSDKError.type == BluIDSDKErrorType.DEVICE_IN_BLUBOOT) {
                                                binding?.firmwareUpdateLayout?.let { updateLayout ->
                                                    enableDisableView(
                                                        updateLayout, true)
                                                }
                                                binding?.deviceConnectionLayout?.let { connectionLayout ->
                                                    enableDisableView(
                                                        connectionLayout, true)
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Device is in BluBOOT mode",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                return@runOnUiThread
                                            }
                                            Utility.m_AlertDialog?.show("${bluIDSDKError.message} type:${bluIDSDKError.type} code:${bluIDSDKError.code}")
                                            return@runOnUiThread
                                        }

                                        expiryTime?.let {
                                            expiryTimeCheck = expiryTime
                                            binding?.unlockBluPointButton?.isEnabled = false
                                            Log.e(m_TAG, "Expiry Time: $expiryTime")
                                            Log.d(m_TAG, "unlocked blupoint")
                                            Utility.m_ProgressBar?.dismiss()
                                            m_isUnlockClicked = true
                                            m_model.deviceUnlocked.value = true
                                            Toast.makeText(
                                                context,
                                                "Device Unlocked",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            binding?.deviceFunctionality?.let { deviceFunctionality ->
                                                enableDisableView(
                                                    deviceFunctionality, true)
                                            }
                                            onDeviceUnlock(deviceId)
                                            binding?.toggleAutoTransfer?.isChecked = false
                                            if (binding?.toggleDeviceEnhancedTap?.isChecked == true) {
                                                binding?.enhancedTapMobile?.let { enhancedTapMobile ->
                                                    enableDisableView(
                                                        enhancedTapMobile, true)
                                                }
                                                enhancedTapSettingsEnable()
                                            } else {
                                                binding?.enhancedTapMobile?.let { enhancedTapMobile ->
                                                    enableDisableView(
                                                        enhancedTapMobile, false)
                                                }
                                            }
                                            return@runOnUiThread
                                        }
                                        Log.e(m_TAG, "Expiry time not found")
                                    }
                                },
                                onDisconnect = { _: String ->
                                    Utility.progressDialog?.dismiss()
                                    if(errorCheck == null && expiryTimeCheck == null){
                                        Utility.m_ProgressBar?.dismiss()
                                        m_isUnlockClicked = false
                                        Log.e(m_TAG, "Device disconnected during device unlock")

                                        activity?.runOnUiThread {
                                            m_model.selectedDevice.value = null
                                            binding?.unlockBluPointButton?.isEnabled = true
                                            binding?.deviceFunctionality?.let { deviceFunctionality ->
                                                onDisconnect(
                                                    deviceFunctionality
                                                )
                                            }
                                            Utility.m_AlertDialog?.show("Device disconnected")
                                        }
                                    }

                                    if (m_isUnlockClicked) {
                                        return@unlockDeviceAccess
                                    }

                                    Utility.m_ProgressBar?.dismiss()
                                    m_isUnlockClicked = false
                                    Log.e(m_TAG, "Device disconnected during device unlock")
                                    activity?.runOnUiThread {
                                        m_model.selectedDevice.value = null
                                        binding?.unlockBluPointButton?.isEnabled = true
                                        binding?.deviceFunctionality?.let { deviceFunctionality -> onDisconnect(deviceFunctionality) }
                                        Utility.m_AlertDialog?.show("Device disconnected")
                                    }
                                })
                            return@launch
                        }
                        activity?.runOnUiThread {
                            Utility.m_ProgressBar?.dismiss()
                            Utility.m_AlertDialog?.show("Please select the device to unlock")
                        }
                    }
                }
            }
        )

        binding?.reboot?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val rebootResponse = sdkClient.rebootDevice(m_deviceId, 3)
                        activity?.runOnUiThread {
                            rebootResponse.error?.let {
                                Utility.m_AlertDialog?.show("Error : " + rebootResponse)
                                return@runOnUiThread
                            }
                            m_model.selectedDevice.value = null
                            binding?.deviceFunctionality?.let { deviceFunctionality -> onDisconnect(deviceFunctionality) }
                        }
                    }
                }
            }
        )

        binding?.factoryReset?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val factoryResetResponse = sdkClient.factoryResetDevice(m_deviceId)
                        activity?.runOnUiThread {
                            factoryResetResponse?.let {
                                Utility.m_AlertDialog?.show("Error : $factoryResetResponse")
                                return@runOnUiThread
                            }
                            m_model.selectedDevice.value = null
                            binding?.deviceFunctionality?.let { deviceFunctionality -> onDisconnect(deviceFunctionality) }
                        }
                    }
                }
            }
        )

        binding?.disconnect?.setOnClickListener(
            View.OnClickListener {
                disconnectDevice()
            }
        )
        binding?.flashFirmwareSingle?.setOnClickListener(
            View.OnClickListener {
                m_model.selectedDeviceFirmwareMode.value = "single"
                findNavController().navigate(R.id.action_homeScreenFragment_to_firmwareListFragment)
            }
        )
        binding?.flashFirmwareMultiDevice?.setOnClickListener(
            View.OnClickListener {
                m_model.userLoginData.value?.let {
                    m_model.selectedDeviceFirmwareMode.value = "multi"
                    findNavController().navigate(R.id.action_homeScreenFragment_to_firmwareListFragment)
                    return@OnClickListener
                }

                Utility.m_AlertDialog?.show("Login Please")
            }
        )

        binding?.getCardsByID?.setOnClickListener(
            View.OnClickListener {
                if (binding?.userIDValue?.text?.isEmpty() == true) {
                    Utility.m_AlertDialog?.show("Field cannot be empty!")
                    return@OnClickListener
                }
                else {
                    Utility.m_ProgressBar?.show()
                    m_scope.launch {
                        Utility.m_BluIDSDK_Client?.let { sdkClient ->
                            val response = sdkClient.syncPersonCardsByID(
                                binding?.userIDValue?.text.toString(),
                                CredentialType.BluB0XMobile
                            )
                            activity?.runOnUiThread {
                                if (response.error == null) {
                                    m_model.syncPersonCardsPersonID.value = binding?.userIDValue?.text.toString()
                                    Toast.makeText(
                                        view.context,
                                        "Total cards: ${response.personCardDetails?.count()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Utility.m_AlertDialog?.show("Error : " + response.error)
                                }
                            }
                        }
                        Utility.m_ProgressBar?.dismiss()
                    }
                }

            }
        )


        binding?.updateName?.setOnClickListener(
            View.OnClickListener {
                Log.d(m_TAG, "DeviceName is called")
                if (binding?.nameValue?.text?.isEmpty() == true) {
                    Utility.m_AlertDialog?.show("Field cannot be empty!")
                    return@OnClickListener
                } else if (binding?.nameValue?.text?.length!! > 8) {
                    Utility.m_AlertDialog?.show("Invalid Device Name : Name should be upto 8 character and non empty")
                    return@OnClickListener
                } else {
                    Utility.m_ProgressBar?.show()
                    m_scope.launch {
                        Utility.m_BluIDSDK_Client?.let { sdkClient ->
                            val response =
                                sdkClient.updateDeviceName(m_deviceId, binding?.nameValue?.text.toString())
                            activity?.runOnUiThread {
                                if (response == null) {
                                    Toast.makeText(
                                        view.context,
                                        "Device Name updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    m_model.selectedDevice.value?.name =
                                        binding?.nameValue?.text.toString()


                                } else {
                                    Utility.m_AlertDialog?.show("Error : " + response)
                                }

                            }

                        }
                        Utility.m_ProgressBar?.dismiss()
                    }
                }

            }
        )


        binding?.decreasePower?.setOnClickListener(
            View.OnClickListener {
                if (binding?.decreasePower?.isEnabled != true) {
                    return@OnClickListener
                }
                binding?.decreasePower?.isEnabled = false
                if (decreaseValue(m_blePower, m_minBlePower)) {
                    m_scope.launch {
                        Utility.m_BluIDSDK_Client?.let { sdkClient ->
                            m_blePower--
                            sdkClient.updateDeviceBLETxPowerLevel(m_deviceId, m_blePower)?.let { setStrengthResponse ->
                                val blePowerResponse: GetStrengthResponse? =
                                    sdkClient.getBLETxPowerLevel(m_deviceId)
                                activity?.runOnUiThread {
                                    binding?.decreasePower?.isEnabled = true
                                    setStrengthResponse.error?.let {
                                        Utility.m_AlertDialog?.show("Error : ${it}")

                                        return@runOnUiThread
                                    }
                                    setStrengthResponse.data?.let {
                                        Toast.makeText(
                                            view.context,
                                            "BLE Power decreased",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        getBlePower(blePowerResponse)
                                        if (m_blePower == m_minBlePower) {
                                            binding?.decreasePower?.isEnabled = false
                                        }
                                        return@runOnUiThread
                                    }
                                }
                            }
                        }
                    }

                    if (binding?.increasePower?.isEnabled != true) {
                        binding?.increasePower?.isEnabled = true
                    }
                } else {
                    binding?.decreasePower?.isEnabled = false
                }
            }
        )

        binding?.increasePower?.setOnClickListener(
            View.OnClickListener {
                if (binding?.increasePower?.isEnabled != true) {
                    return@OnClickListener
                }
                binding?.increasePower?.isEnabled = false
                if (increaseValue(m_blePower, m_maxBlePower)) {
                    m_scope.launch {
                        m_blePower++
                        Utility.m_BluIDSDK_Client?.let { sdkClient ->
                            sdkClient.updateDeviceBLETxPowerLevel(m_deviceId, m_blePower)?.let { setStrengthResponse ->
                                val blePowerResponse: GetStrengthResponse? =
                                    sdkClient.getBLETxPowerLevel(m_deviceId)
                                activity?.runOnUiThread {
                                    binding?.increasePower?.isEnabled = true
                                    setStrengthResponse.error?.let {
                                        Utility.m_AlertDialog?.show("Error : ${it}")
                                        return@runOnUiThread
                                    }
                                    setStrengthResponse.data?.let {
                                        Toast.makeText(
                                            view.context,
                                            "BLE Power increased",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        getBlePower(blePowerResponse)
                                        if (m_blePower == m_maxBlePower) {
                                            binding?.increasePower?.isEnabled = false
                                        }
                                        return@runOnUiThread
                                    }
                                }
                            }
                        }
                    }
                    if (binding?.decreasePower?.isEnabled != true) {
                        binding?.decreasePower?.isEnabled = true
                    }
                } else {
                    binding?.increasePower?.isEnabled = false
                }

            }
        )

        binding?.ledToggle?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (!b) {
                return@OnCheckedChangeListener
            }
            m_model.selectedDevice.value?.let { selectedDevice ->
                m_scope.launch {
                    val ledColor = Device_LED_Color(0, 0xFF.toByte(), 0)
                    val bluPointLED = Device_LED(ledColor, 5)
                    val response =
                        Utility.m_BluIDSDK_Client?.identifyDevice(
                            bluPointLED,
                            selectedDevice.id
                        )
                    activity?.runOnUiThread {
                        binding?.ledToggle?.isChecked = false
                        response?.let {
                            Utility.m_AlertDialog?.show("${it.message ?: ""}")
                        }
                    }
                }
                return@let
            }
        })

        binding?.toggleRingBuzzer?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, b ->
            if (!b) {
                return@OnCheckedChangeListener
            }
            Utility.m_ProgressBar?.show()
            m_model.selectedDevice.value?.let { selectedDevice ->
                m_scope.launch {
                    val response =
                        Utility.m_BluIDSDK_Client?.identifyDevice(selectedDevice.id, 5)
                    Utility.m_ProgressBar?.dismiss()
                    activity?.runOnUiThread {
                        binding?.toggleRingBuzzer?.isChecked = false
                        if (response == null) {
                            Toast.makeText(
                                view.context,
                                "Buzzer Rang",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Utility.m_AlertDialog?.show("Error : " + response)
                        }

                    }
                }
                return@let
            }
        })

        val idleLEDAdapter = ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.LEDColorList,
            android.R.layout.simple_spinner_item
        )

        idleLEDAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.idleLEDList?.adapter = idleLEDAdapter

        if (m_model.idleLED.value == null) {
            m_model.idleLED.value = m_idleLEDStateSelected
        }
        val idleLedIndex = m_ledColorsList.indexOf(m_idleLEDStateSelected)
        binding?.idleLEDList?.setSelection(idleLedIndex)
        binding?.idleLEDList?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (m_deviceId.isNullOrEmpty())
                    return
                val idleLed = m_ledColorsList[position]
                if (idleLed != m_idleLEDStateSelected) {
                    m_model.idleLED.value = idleLed
                    m_deviceStateLEDColors[idleLed]?.let {
                        setDeviceStateLEDColour(
                            DeviceLEDState.idle,
                            it, m_deviceId
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val inUseLEDAdapter = ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.LEDColorList,
            android.R.layout.simple_spinner_item
        )

        inUseLEDAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding?.inUseLEDList?.adapter = inUseLEDAdapter


        m_model.inUseLED.value = m_inUseLEDStateSelected
        val inUseLedIndex = m_ledColorsList.indexOf(m_inUseLEDStateSelected)
        binding?.inUseLEDList?.setSelection(inUseLedIndex)

        binding?.inUseLEDList?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {

                if (m_deviceId.isNullOrEmpty())
                    return
                val inUseLed = m_ledColorsList[position]
                if (inUseLed != m_inUseLEDStateSelected) {
                    m_model.inUseLED.value = inUseLed
                    m_deviceStateLEDColors[inUseLed]?.let {
                        setDeviceStateLEDColour(
                            DeviceLEDState.in_use,
                            it, m_deviceId
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding?.toggleDeviceEnhancedTap?.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                binding?.enhancedTapMobile?.let { enableDisableView(it, true) }
                enhancedTapSettingsEnable()
            } else {
                binding?.enhancedTapMobile?.let { enableDisableView(it, false) }
            }

        }

        binding?.decreaseTap?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceTapValue, m_minBluPointGestureStrength)) {
                    m_deviceTapValue--
                    binding?.tapValue?.text = m_deviceTapValue.toString()

                    if (binding?.increaseTap?.isEnabled == false) {
                        binding?.increaseTap?.isEnabled = true
                    }
                    if (m_deviceTapValue == m_minBluPointGestureStrength) {
                        binding?.decreaseTap?.isEnabled = false
                    }

                } else {
                    binding?.decreaseTap?.isEnabled = false
                }
            }
        )

        binding?.increaseTap?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceTapValue, m_maxBluPointGestureStrength)) {
                    m_deviceTapValue++
                    binding?.tapValue?.text = m_deviceTapValue.toString()

                    if (binding?.decreaseTap?.isEnabled == false) {
                        binding?.decreaseTap?.isEnabled = true
                    }

                    if (m_deviceTapValue == m_maxBluPointGestureStrength) {
                        binding?.increaseTap?.isEnabled = false
                    }

                } else {
                    binding?.increaseTap?.isEnabled = false
                }

            }
        )


        binding?.setBluPOINTTapRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateTapValue =
                        binding?.bluPOINTTap?.isChecked?.let { bluPOINTTap ->
                            GenericBLESettings(m_deviceTapValue,
                                bluPOINTTap
                            )
                        }
                    Log.d("updateTapValue : ", "" + m_deviceTapValue)
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response = updateTapValue?.let { updateTapValue ->
                            sdkClient.updateDeviceSettings(m_deviceId, GestureType.tap,
                                updateTapValue
                            )
                        }
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Tap Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.tapSettings =
                                    binding?.bluPOINTTap?.isChecked?.let { bluPOINTTap ->
                                        GenericBLESettings(m_deviceTapValue,
                                            bluPOINTTap
                                        )
                                    }

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseTwist?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceTwistValue, m_minBluPointGestureStrength)) {
                    m_deviceTwistValue -= 1
                    binding?.twistValue?.text = m_deviceTwistValue.toString()
                    if (binding?.increaseTwist?.isEnabled == false) {
                        binding?.increaseTwist?.isEnabled = true
                    }

                    if (m_deviceTwistValue == m_minBluPointGestureStrength) {
                        binding?.decreaseTwist?.isEnabled = false
                    }

                } else {
                    binding?.decreaseTwist?.isEnabled = false
                }
            }
        )

        binding?.increaseTwist?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceTwistValue, m_maxBluPointGestureStrength)) {
                    m_deviceTwistValue += 1
                    binding?.twistValue?.text = m_deviceTwistValue.toString()

                    if (binding?.decreaseTwist?.isEnabled == false) {
                        binding?.decreaseTwist?.isEnabled = true
                    }

                    if (m_deviceTwistValue == m_maxBluPointGestureStrength) {
                        binding?.increaseTwist?.isEnabled = false
                    }

                } else {
                    binding?.increaseTwist?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTTwistRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateTwistValue =
                        binding?.bluPOINTTwist?.isChecked?.let { _bluPOINTTwist ->
                            GenericBLESettings(m_deviceTwistValue,
                                _bluPOINTTwist
                            )
                        }
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            updateTwistValue?.let { updateTwistValue ->
                                sdkClient.updateDeviceSettings(m_deviceId,GestureType.twist,
                                    updateTwistValue
                                )
                            }
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Twist Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.twistSettings = GenericBLESettings(
                                    m_deviceTwistValue,
                                    binding?.bluPOINTTwist?.isChecked != false
                                )
                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseAppSpecific?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceAppSpecificValue, m_minBluPointGestureStrength)) {
                    m_deviceAppSpecificValue -= 1
                    binding?.appSpecificValue?.setText(m_deviceAppSpecificValue.toString())

                    if (binding?.increaseAppSpecific?.isEnabled == false) {
                        binding?.increaseAppSpecific?.isEnabled = true
                    }

                    if (m_deviceAppSpecificValue == m_minBluPointGestureStrength) {
                        binding?.decreaseAppSpecific?.isEnabled = false
                    }

                } else {
                    binding?.decreaseAppSpecific?.isEnabled = false
                }
            }
        )

        binding?.increaseAppSpecific?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceAppSpecificValue, m_maxBluPointGestureStrength)) {
                    m_deviceAppSpecificValue += 1
                    binding?.appSpecificValue?.text = m_deviceAppSpecificValue.toString()

                    if (binding?.decreaseAppSpecific?.isEnabled == false) {
                        binding?.decreaseAppSpecific?.isEnabled = true
                    }

                    if (m_deviceAppSpecificValue == m_maxBluPointGestureStrength) {
                        binding?.increaseAppSpecific?.isEnabled = false
                    }

                } else {
                    binding?.increaseAppSpecific?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTAppSpecificRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateAppSpecificValue = GenericBLESettings(
                        m_deviceAppSpecificValue,
                        binding?.bluPOINTAppSpecific?.isChecked == true
                    )
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(
                                m_deviceId,
                                GestureType.appSpecific,
                                updateAppSpecificValue
                            )
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "App Specific Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.appSpecificSettings = GenericBLESettings(
                                    m_deviceAppSpecificValue,
                                    binding?.bluPOINTAppSpecific?.isChecked == true
                                )

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseWave?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceWaveValue, m_minBluPointGestureStrength)) {
                    m_deviceWaveValue -= 1
                    binding?.waveValue?.text = m_deviceWaveValue.toString()

                    if (binding?.increaseWave?.isEnabled == false) {
                        binding?.increaseWave?.isEnabled = true
                    }

                    if (m_deviceWaveValue == m_minBluPointGestureStrength) {
                        binding?.decreaseWave?.isEnabled = false
                    }

                } else {
                    binding?.decreaseWave?.isEnabled = false
                }
            }
        )

        binding?.increaseWave?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceWaveValue, m_maxBluPointGestureStrength)) {
                    m_deviceWaveValue += 1
                    binding?.waveValue?.text = m_deviceWaveValue.toString()

                    if (binding?.decreaseWave?.isEnabled == false) {
                        binding?.decreaseWave?.isEnabled = true
                    }

                    if (m_deviceWaveValue == m_maxBluPointGestureStrength) {
                        binding?.increaseWave?.isEnabled = false
                    }

                } else {
                    binding?.increaseWave?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTWaveRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateWaveValue =
                        GenericBLESettings(m_deviceWaveValue, binding?.bluPOINTWave?.isChecked == true)
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(m_deviceId, GestureType.wave, updateWaveValue)
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Wave Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.waveSettings =
                                    GenericBLESettings(m_deviceWaveValue, binding?.bluPOINTWave?.isChecked == true)

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseAI?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceAIValue, m_minBluPointGestureStrength)) {
                    m_deviceAIValue -= 1
                    binding?.AIValue?.text = m_deviceAIValue.toString()

                    if (binding?.increaseAI?.isEnabled == false) {
                        binding?.increaseAI?.isEnabled = true
                    }

                    if (m_deviceAIValue == m_minBluPointGestureStrength) {
                        binding?.decreaseAI?.isEnabled = false
                    }

                } else {
                    binding?.decreaseAI?.isEnabled = false
                }
            }
        )

        binding?.increaseAI?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceAIValue, m_maxBluPointGestureStrength)) {
                    m_deviceAIValue += 1
                    binding?.AIValue?.text = m_deviceAIValue.toString()

                    if (binding?.decreaseAI?.isEnabled == false) {
                        binding?.decreaseAI?.isEnabled = true
                    }

                    if (m_deviceAIValue == m_maxBluPointGestureStrength) {
                        binding?.increaseAI?.isEnabled = false
                    }

                } else {
                    binding?.increaseAI?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTAIRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateAIValue =
                        GenericBLESettings(m_deviceAIValue, binding?.bluPOINTAI?.isChecked == true)
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response = sdkClient.updateDeviceSettings(m_deviceId, GestureType.ai, updateAIValue)
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "AI Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.aiSettings =
                                    GenericBLESettings(m_deviceAIValue, binding?.bluPOINTAI?.isChecked == true)
                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )
        binding?.decreaseAppleWatch?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceAppleWatchValue, m_minBluPointGestureStrength)) {
                    m_deviceAppleWatchValue -= 1
                    binding?.appleWatchValue?.text = m_deviceAppleWatchValue.toString()

                    if (binding?.increaseAppleWatch?.isEnabled == false) {
                        binding?.increaseAppleWatch?.isEnabled = true
                    }

                    if (m_deviceAppleWatchValue == m_minBluPointGestureStrength) {
                        binding?.decreaseAppleWatch?.isEnabled = false
                    }

                } else {
                    binding?.decreaseAppleWatch?.isEnabled = false
                }
            }
        )

        binding?.increaseAppleWatch?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceAppleWatchValue, m_maxBluPointGestureStrength)) {
                    m_deviceAppleWatchValue += 1
                    binding?.appleWatchValue?.text = m_deviceAppleWatchValue.toString()

                    if (binding?.decreaseAppleWatch?.isEnabled == false) {
                        binding?.decreaseAppleWatch?.isEnabled = true
                    }

                    if (m_deviceAppleWatchValue == m_maxBluPointGestureStrength) {
                        binding?.increaseAppleWatch?.isEnabled = false
                    }

                } else {
                    binding?.increaseAppleWatch?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTAppleWatchRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateAppleWatchValue =
                        GenericBLESettings(
                            m_deviceAppleWatchValue,
                            binding?.bluPOINTAppleWatch?.isChecked == true
                        )
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(
                                m_deviceId,GestureType.appleWatch,
                                updateAppleWatchValue
                            )
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Apple Watch Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.appleWatchSettings = GenericBLESettings(
                                    m_deviceAppleWatchValue,
                                    binding?.bluPOINTAppleWatch?.isChecked == true
                                )

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseInRange?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceInRangeValue, m_minBluPointGestureStrength)) {
                    m_deviceInRangeValue -= 1
                    binding?.inRangeValue?.text = m_deviceInRangeValue.toString()

                    if (binding?.increaseInRange?.isEnabled == false) {
                        binding?.increaseInRange?.isEnabled = true
                    }

                    if (m_deviceInRangeValue == m_minBluPointGestureStrength) {
                        binding?.decreaseInRange?.isEnabled = false
                    }

                } else {
                    binding?.decreaseInRange?.isEnabled = false
                }
            }
        )

        binding?.increaseInRange?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceInRangeValue, m_maxBluPointGestureStrength)) {
                    m_deviceInRangeValue += 1
                    binding?.inRangeValue?.text = m_deviceInRangeValue.toString()

                    if (binding?.decreaseInRange?.isEnabled == false) {
                        binding?.decreaseInRange?.isEnabled = true
                    }

                    if (m_deviceInRangeValue == m_maxBluPointGestureStrength) {
                        binding?.increaseInRange?.isEnabled = false
                    }

                } else {
                    binding?.increaseInRange?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTInRangeRange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateInRangeValue =
                        GenericBLESettings(
                            m_deviceInRangeValue,
                            binding?.bluPOINTINRange?.isChecked == true
                        )
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(m_deviceId, GestureType.inRange, updateInRangeValue)
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Range Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.rangeSettings = GenericBLESettings(
                                    m_deviceInRangeValue,
                                    binding?.bluPOINTINRange?.isChecked == true
                                )

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.decreaseBluREMOTE?.setOnClickListener(
            View.OnClickListener {
                if (decreaseValue(m_deviceBluREMOTEValue, m_minBluPointGestureStrength)) {
                    m_deviceBluREMOTEValue -= 1
                    binding?.bluREMOTEValue?.text = m_deviceBluREMOTEValue.toString()

                    if (binding?.increaseBluREMOTE?.isEnabled == false) {
                        binding?.increaseBluREMOTE?.isEnabled = true
                    }

                    if (m_deviceBluREMOTEValue == m_minBluPointGestureStrength) {
                        binding?.decreaseBluREMOTE?.isEnabled = false
                    }

                } else {
                    binding?.decreaseBluREMOTE?.isEnabled = false
                }
            }
        )

        binding?.increaseBluREMOTE?.setOnClickListener(
            View.OnClickListener {
                if (increaseValue(m_deviceBluREMOTEValue, m_maxBluPointGestureStrength)) {
                    m_deviceBluREMOTEValue += 1
                    binding?.bluREMOTEValue?.text = m_deviceBluREMOTEValue.toString()

                    if (binding?.decreaseBluREMOTE?.isEnabled == false) {
                        binding?.decreaseBluREMOTE?.isEnabled = true
                    }

                    if (m_deviceBluREMOTEValue == m_maxBluPointGestureStrength) {
                        binding?.increaseBluREMOTE?.isEnabled = false
                    }

                } else {
                    binding?.increaseBluREMOTE?.isEnabled = false
                }
            }
        )

        binding?.setBluPOINTBluREMOTERange?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateBluREMOTEValue =
                        GenericBLESettings(
                            m_deviceBluREMOTEValue,
                            binding?.bluPOINTBluREMOTE?.isChecked == true
                        )
                    Utility.m_ProgressBar?.show()
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(
                                m_deviceId,
                                GestureType.BluREMOTE,
                                updateBluREMOTEValue
                            )
                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "BluREMOTE Value updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                m_model.selectedDevice.value?.BluREMOTESettings = GenericBLESettings(
                                    m_deviceBluREMOTEValue,
                                    binding?.bluPOINTBluREMOTE?.isChecked == true
                                )

                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }

                        }
                    }
                    Utility.m_ProgressBar?.dismiss()
                }
            }
        )

        binding?.setDeviceEnhancedTap?.setOnClickListener(
            View.OnClickListener {
                m_scope.launch {
                    val updateEnhancedTapValue = EnhancedTapSettings(
                        binding?.toggleDeviceEnhancedTap?.isChecked == true,
                        m_deviceiOSBackgroundRangeValue,
                        m_deviceiOSForegroundRangeValue,
                        m_deviceAndroidRangeValue
                    )
                    m_model.selectedDevice.value?.enhancedTapSettings = EnhancedTapSettings(
                        binding?.toggleDeviceEnhancedTap?.isChecked == true,
                        m_deviceiOSBackgroundRangeValue,
                        m_deviceiOSForegroundRangeValue,
                        m_deviceAndroidRangeValue
                    )
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        val response =
                            sdkClient.updateDeviceSettings(
                                m_deviceId,
                                updateEnhancedTapValue
                            )
                        Log.d("enhancedTap response : ", response.toString())

                        activity?.runOnUiThread {
                            if (response == null) {
                                Toast.makeText(
                                    view.context,
                                    "Enhanced Tap, iOS Background Range,iOS Foreground Range,Android Range values updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Utility.m_AlertDialog?.show("Error : " + response)
                            }
                        }
                    }
                }
            }
        )
    }
}




