package com.blub0x.bluidsdk_sample_app.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.blub0x.BluIDSDK.models.BluIDSDKErrorType
import com.blub0x.bluidsdk_sample_app.MainActivity
import com.blub0x.bluidsdk_sample_app.R

class AlertDialog(private val mActivity: MainActivity) : DialogFragment() {

    private lateinit var m_isDialog: AlertDialog
    private val m_builder = AlertDialog.Builder(mActivity)

    init {
        mActivity.runOnUiThread {
            m_builder.setCancelable(false)
            m_isDialog = m_builder.create()
        }
    }

    fun show(message:String) {
        mActivity.runOnUiThread {
            m_builder.setTitle("Error")
            m_builder.setMessage(message)
            m_builder.setPositiveButton("Okay",) { isDialog, _ ->
                isDialog.dismiss()
            }
            m_isDialog = m_builder.create()
            m_isDialog.show()
        }
    }

    fun show(message:String, err : BluIDSDKErrorType) {
        mActivity.runOnUiThread {
            m_builder.setTitle("Error")
            m_builder.setMessage(message)
            m_builder.setPositiveButton("Enable") { isDialog, _ ->
                openSettings(err)
                isDialog.dismiss()
            }
        }
        m_isDialog = m_builder.create()
        m_isDialog.show()
    }

    fun openSettings(err :BluIDSDKErrorType) {
        if(err == BluIDSDKErrorType.BLUETOOTH_POWER_OFF) {
            mActivity.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }
        if(err == BluIDSDKErrorType.LOCATION_POWER_OFF) {
            mActivity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        if(err == BluIDSDKErrorType.BLUETOOTH_UNAUTHORIZED || err == BluIDSDKErrorType.LOCATION_UNAUTHORIZED)
        {
            mActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", "com.blub0x.bluidsdk_sample_app" ,null) })
        }
    }

    fun showBatteryOptimizationOptions(){
        mActivity.let {
            val name = mActivity.resources.getString(R.string.app_name)
            m_builder.setMessage("Battery optimization is on in the device. This will affect the working of the application in background mode. To fix it, go to Battery optimization -> All apps -> $name -> Don't optimize")
                .setPositiveButton("Okay",
                    DialogInterface.OnClickListener { _, _ ->
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        mActivity.startActivity(intent)
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                    })
            m_isDialog = m_builder.create()
            m_isDialog.show()
        }
    }
}