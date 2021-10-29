package com.blub0x.bluidsdk_sample_app.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.databinding.MultiDeviceFirmwareUpgradeBinding
import com.blub0x.bluidsdk_sample_app.fragments.SelectedDevicesInfo


class SelectedDeviceAdapter(
    private val devices: ArrayList<SelectedDevicesInfo>,
    private var onClick: (SelectedDevicesInfo) -> Unit
) :
    RecyclerView.Adapter<SelectedDeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View,onClick: (SelectedDevicesInfo) -> Unit) :
        RecyclerView.ViewHolder(view) {
        var selectedDevice: SelectedDevicesInfo?
        val binding = MultiDeviceFirmwareUpgradeBinding.bind(view)

        init {
            selectedDevice = null

            view.setOnClickListener(View.OnClickListener {
                    selectedDevice?.let {
                        onClick(it)
                    }
                })

        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.multi_device_firmware_upgrade, viewGroup, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        devices[position].let {
            Log.d("Multi Device Firmware Adapter Position : ", position.toString())
            viewHolder.selectedDevice = it
            viewHolder.binding.deviceForFirmwareUpgrade.text = it.deviceDetails.name
            if(it.isUpdating)
            {
                if(it.error == null)
                {
                    viewHolder.binding.progressDisplayLabel.isVisible = true
                    viewHolder.binding.deviceFirmwareUpgradeProgress.isVisible = true
                    viewHolder.binding.errorOrSuccess.isVisible = false
                    viewHolder.binding.progressDisplayLabel.text = it.percent.toString() + "%"
                    viewHolder.binding.deviceFirmwareUpgradeProgress.progress = it.percent
                }

                else
                {
                    viewHolder.binding.errorOrSuccess.isVisible = true
                    viewHolder.binding.errorOrSuccess.text = it.error.toString()
                    viewHolder.binding.errorOrSuccess.setTextColor(Color.RED)
                }

            }
            else if(!it.isUpdating)
            {
                viewHolder.binding.progressDisplayLabel.isVisible = false
                viewHolder.binding.deviceFirmwareUpgradeProgress.isVisible = false

                if(it.percent == 0)
                {
                    if(it.error == null)
                    {
                        viewHolder.binding.errorOrSuccess.isVisible = false
                    }

                    else
                    {
                        viewHolder.binding.errorOrSuccess.isVisible = true
                        viewHolder.binding.errorOrSuccess.text = it.error.toString()
                        viewHolder.binding.errorOrSuccess.setTextColor(Color.RED)
                    }
                }

                else if(it.percent > 0)
                {
                    if(it.error == null)
                    {
                        viewHolder.binding.errorOrSuccess.isVisible = true
                        viewHolder.binding.errorOrSuccess.text = "Successful"
                        viewHolder.binding.errorOrSuccess.setTextColor(Color.GREEN)
                    }

                    else
                    {
                        viewHolder.binding.errorOrSuccess.isVisible = true
                        viewHolder.binding.errorOrSuccess.text = it.error.toString()
                        viewHolder.binding.errorOrSuccess.setTextColor(Color.RED)
                    }
                }
            }

        }

    }

    override fun getItemCount() = devices.size
}
