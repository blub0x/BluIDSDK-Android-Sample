package com.blub0x.bluidsdk_sample_app

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blub0x.BluIDSDK.utils.BLECentral
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.fragments.AlertDialog
import com.blub0x.bluidsdk_sample_app.fragments.ProgressDialog
import com.blub0x.bluidsdk_sample_app.utils.Utility
import com.blub0x.bluidsdk_sample_app.utils.Utility.deviceStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    val appPermissions_s = arrayOf<String>(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Utility.m_ProgressBar = ProgressDialog(this)
        Utility.m_AlertDialog = AlertDialog(this)
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_USER_BACKGROUND)
        filter.addAction(Intent.ACTION_USER_FOREGROUND)
        filter.addAction(Intent.ACTION_USER_UNLOCKED)
        filter.addAction(Intent.ACTION_USER_INITIALIZE)
        this.registerReceiver(deviceStateObserver, filter)
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ), PERMISSION_REQUEST_CODE
                )
            }
        }else{
            ActivityCompat.requestPermissions(
                this, appPermissions_s, PERMISSION_REQUEST_CODE)
        }

        startService()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utility.m_BluIDSDK_Client?.stopDeviceDiscovery()
        this.unregisterReceiver(deviceStateObserver)
        CoroutineScope(Dispatchers.Default).launch{ Utility.m_BluIDSDK_Client?.disconnectAllDevices() }
        if (Utility.m_BluIDSDK_Client?.m_isAuthenticatorStarted == true) {
            Utility.m_BluIDSDK_Client?.stopGestureBasedAuthentication()
        }
        stopService()
    }

    override fun onStop() {
        super.onStop()
    }
    private fun startService() {
        Utility.globalActivity = this
        val serviceIntent = Intent(this, BLECentral::class.java)
        startService(serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(this, BLECentral::class.java)
        stopService(serviceIntent)
    }

    fun getActivity(): MainActivity {
        return this
    }
}