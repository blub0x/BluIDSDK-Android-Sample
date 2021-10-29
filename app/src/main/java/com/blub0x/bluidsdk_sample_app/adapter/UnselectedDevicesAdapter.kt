package com.blub0x.bluidsdk_sample_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.BluIDSDK.models.Device_Information
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.databinding.BleDeviceInfoBinding

class UnselectedDevicesAdapter(
    private val devices: ArrayList<Device_Information>,
    private val onClick: (Device_Information) -> Unit
) :
    RecyclerView.Adapter<UnselectedDevicesAdapter.ViewHolder>() {

    class ViewHolder(view: View, onClick: (Device_Information) -> Unit) :
        RecyclerView.ViewHolder(view) {
        var device: Device_Information?
        val binding = BleDeviceInfoBinding.bind(view)

        init {
            device = null
            view.setOnClickListener(View.OnClickListener {
                device?.let {
                    onClick(it)
                }
            })
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.ble_device_info, viewGroup, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        devices[position].let {
            viewHolder.device = it
            viewHolder.binding.bleName.text = it.name
            viewHolder.binding.bleFirmwareVersion.text = it.firmwareVersion
            viewHolder.binding.bleRSSI.text = it.signalStrength.toString()
        }
    }

    override fun getItemCount() = devices.size

}
