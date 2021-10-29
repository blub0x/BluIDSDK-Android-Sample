package com.blub0x.bluidsdk_sample_app.fragments

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import androidx.core.view.isVisible
import com.blub0x.bluidsdk_sample_app.R

class ProgressDialog(var mActivity: Activity) {
    private var m_isDialog: AlertDialog
    var m_dialogView = mActivity.layoutInflater.inflate(R.layout.progress_bar, null)
    private val m_builder = AlertDialog.Builder(mActivity,R.style.CustomDialog)
    var m_progressBarTitle = m_dialogView.findViewById<TextView>(R.id.progressBarTitle)

    init {
        m_builder.setView(m_dialogView)
        m_builder.setCancelable(false)
        m_isDialog = m_builder.create()
        m_progressBarTitle.isVisible = false
    }

    fun show() {
        mActivity.runOnUiThread {
            m_isDialog.show()
        }
    }

    fun dismiss() {
        mActivity.runOnUiThread {
            m_isDialog.dismiss()
        }
    }

    fun showTitle(title:String)
    {
        mActivity.runOnUiThread {
            if(title != "")
            {
                m_progressBarTitle.isVisible = true
                m_progressBarTitle.setText(title)
            }

            else {
                m_progressBarTitle.isVisible = false
            }


        }
    }


}