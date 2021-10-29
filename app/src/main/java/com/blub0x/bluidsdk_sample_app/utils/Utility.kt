package com.blub0x.bluidsdk_sample_app.utils

import android.app.Activity
import android.content.Intent
import com.blub0x.BluIDSDK.BluIDSDK
import com.blub0x.BluIDSDK.utils.DeviceStateObserver
import com.blub0x.bluidsdk_sample_app.fragments.AlertDialog
import com.blub0x.bluidsdk_sample_app.fragments.DownloadingDialog
import com.blub0x.bluidsdk_sample_app.fragments.ProgressDialog

object Utility {
    var m_BluIDSDK_Client:BluIDSDK? = null
    var m_ProgressBar: ProgressDialog? = null
    var m_IsScanningStarted: Boolean = false
    var m_ServiceIntent: Intent? = null
    var globalActivity: Activity? = null
    var m_AlertDialog : AlertDialog? = null
    var deviceStateObserver = DeviceStateObserver()
    var progressDialog: DownloadingDialog? = null
}