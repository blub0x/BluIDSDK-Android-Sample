package com.blub0x.bluidsdk_sample_app.fragments

import android.app.Activity
import android.app.AlertDialog
import android.widget.ProgressBar
import android.widget.TextView
import com.blub0x.bluidsdk_sample_app.R

class DownloadingDialog(private val mActivity: Activity) {

    private lateinit var isDialog: AlertDialog
    private val m_inflater = mActivity.layoutInflater
    val m_dialogView = m_inflater.inflate(R.layout.download_progress_bar, null)
    private val m_builder = AlertDialog.Builder(mActivity)

    var m_progressDisplay = m_dialogView.findViewById<TextView>(R.id.progressDisplay)
    private var m_progressBar : ProgressBar = m_dialogView.findViewById(R.id.progressBarDownload)

    init {
        mActivity.runOnUiThread {
            m_builder.setView(m_dialogView)
            m_builder.setCancelable(false)
            isDialog = m_builder.create()
        }
    }

    fun startDownloading() {

        mActivity.runOnUiThread {
            isDialog.show()
        }
    }

    fun dismiss() {
        mActivity.runOnUiThread {
            isDialog.dismiss()
        }
    }

    fun setProgress(percent : Int)
    {
        mActivity.runOnUiThread {
            m_progressBar.setProgress(percent)
            m_progressDisplay.setText(percent.toString() + "%")
        }
    }


}