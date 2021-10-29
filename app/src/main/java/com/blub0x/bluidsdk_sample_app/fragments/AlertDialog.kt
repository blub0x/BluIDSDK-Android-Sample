package com.blub0x.bluidsdk_sample_app.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.DialogFragment
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