package com.blub0x.bluidsdk_sample_app.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blub0x.BluIDSDK.models.*

class SharedDataModel : ViewModel() {
    var userLoginData = MutableLiveData<UserData?>()
    var selectedDevice = MutableLiveData<Device_Information?>()
    var generalUserSettings = MutableLiveData<UserPreferences?>()
    var selectedFirmware = MutableLiveData<Device_Firmware?>()
    var selectedEnvironment = MutableLiveData<String>()
    var selectedAllowAccessType = MutableLiveData<Int>()
    var viewInit = MutableLiveData<Boolean>()
    var deviceUnlocked = MutableLiveData<Boolean>()
    var selectedDeviceFirmwareMode = MutableLiveData<String>()
    var updatedFirmwareForSingleDevice = MutableLiveData<Boolean>()

    var idleLED = MutableLiveData<String>()
    var inUseLED = MutableLiveData<String>()

    fun updateUserData(userData: UserData?) {
        userLoginData.value = userData
    }

    fun updateUserSettings(userSettings: UserPreferences?) {
        generalUserSettings.value = userSettings
    }

    var autoTransfer = MutableLiveData<Boolean>()
}
