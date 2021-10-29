package com.blub0x.bluidsdk_sample_app.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.BluIDSDK.models.Device_Firmware
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.databinding.FirmwareListBinding

class FirmwareListAdapter(
    private val firmwares: ArrayList<Device_Firmware>,
    private val onClick: (Device_Firmware) -> Unit
) :
    RecyclerView.Adapter<FirmwareListAdapter.ViewHolder>() {

    var selectedItemPosition = -1

    class ViewHolder(view: View, onClick: (Device_Firmware) -> Unit) :
        RecyclerView.ViewHolder(view) {
        val binding = FirmwareListBinding.bind(view)
        var firmware: Device_Firmware?

        init {
            // Define click listener for the ViewHolder's View.
            firmware = null
            view.setOnClickListener(View.OnClickListener {
                firmware?.let {
                    onClick(it)
                    if (it.isDownloaded == true) {
                        binding.downloadFirmware.isVisible = false
                        Toast.makeText(
                            view.context,
                            "Firmware is already downloaded.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        binding.downloadFirmware.isVisible = true

                    }
                    Log.i("firmware version", it.version)
                }
            })
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.firmware_list, viewGroup, false)

        return ViewHolder(view, onClick)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        firmwares[position].let { blu ->
            viewHolder.binding.firmwareVersion.text = blu.version
            viewHolder.binding.firmwareVersion.setOnClickListener(
                View.OnClickListener {
                    onClick(blu)
                    selectedItemPosition = position
                    notifyDataSetChanged()
                }
            )

            if(blu.isDownloaded)
            {
                viewHolder.binding.downloadFirmware.isVisible = false
            }
        }
        if (selectedItemPosition == position) {
            viewHolder.binding.firmwareVersion.setBackgroundColor(Color.GRAY)
            viewHolder.binding.downloadFirmware.setBackgroundColor(Color.GRAY)
        }
        else {
            viewHolder.binding.firmwareVersion.setBackgroundColor(Color.parseColor("#0151BA"))
            viewHolder.binding.downloadFirmware.setBackgroundColor(Color.parseColor("#0151BA"))
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = firmwares.size
}
