package com.blub0x.bluidsdk_sample_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.BluIDSDK.models.BluIDSDKError
import com.blub0x.BluIDSDK.models.BluIDSDKException
import com.blub0x.BluIDSDK.models.ScanFilter
import com.blub0x.BluIDSDK.models.Device_Information
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.adapter.SelectedDeviceAdapter
import com.blub0x.bluidsdk_sample_app.adapter.UnselectedDevicesAdapter
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareListBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareMultiDevicesBinding
import com.blub0x.bluidsdk_sample_app.model.SharedDataModel
import com.blub0x.bluidsdk_sample_app.utils.Utility
import kotlinx.coroutines.*

class FirmwareMultiDevices : Fragment() {
    private val m_TAG = "FirmwareMultiDevices"
    private val m_model: SharedDataModel by activityViewModels()

    @ObsoleteCoroutinesApi
    private val m_scope = CoroutineScope(newSingleThreadContext(m_TAG))
    private var m_selectedDevices: ArrayList<SelectedDevicesInfo> = ArrayList<SelectedDevicesInfo>()
    private var m_devices: ArrayList<Device_Information> = ArrayList<Device_Information>()
    private var _binding : FragmentFirmwareMultiDevicesBinding? = null
    private val binding get() = _binding

    fun updateMultiDeviceProgressHandler(deviceID: String, percent: Int, error: BluIDSDKError?) {

        if (error != null) {
            print("" + deviceID + error)
            return
        }

        val deviceUpdated = m_selectedDevices.filter {
            it.deviceDetails.id == deviceID
        }

        if (deviceUpdated.isNotEmpty()) {
            m_selectedDevices.remove(deviceUpdated[0])
            m_selectedDevices.add(
                SelectedDevicesInfo(
                    deviceUpdated[0].deviceDetails, deviceUpdated[0].deviceId, true,
                    (percent), error
                )
            )

        } else {
            print("Device not found")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirmwareMultiDevicesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var deviceSelected: Device_Information

        val unselectedDevicesId: ArrayList<String> = ArrayList<String>()
        val selectedDevicesId: ArrayList<String> = ArrayList<String>()

        binding?.scanDeviceRecyclerView?.layoutManager = LinearLayoutManager(view.context)

        binding?.selectedDeviceRecyclerView?.layoutManager = LinearLayoutManager(view.context)

        val deviceAdapter = UnselectedDevicesAdapter(m_devices) {
            deviceSelected = it
            m_selectedDevices.add(
                SelectedDevicesInfo(
                    deviceSelected,
                    deviceSelected.id,
                    false,
                    0,
                    null
                )
            )
            selectedDevicesId.add(deviceSelected.id)
            m_devices.remove(deviceSelected)
            unselectedDevicesId.remove(deviceSelected.id)
            binding?.scanDeviceRecyclerView?.adapter?.notifyDataSetChanged()
            binding?.selectedDeviceRecyclerView?.adapter?.notifyDataSetChanged()
            binding?.firmwareDownloadForSelectedDevices?.isEnabled = !m_selectedDevices.isEmpty()

        }
        binding?.scanDeviceRecyclerView?.adapter = deviceAdapter

        val selectedDeviceAdapter = SelectedDeviceAdapter(m_selectedDevices) {
            deviceSelected = it.deviceDetails
            m_selectedDevices.remove(
                SelectedDevicesInfo(
                    deviceSelected,
                    deviceSelected.id,
                    false,
                    0,
                    null
                )
            )
            m_devices.add(deviceSelected)
            selectedDevicesId.remove(deviceSelected.id)
            binding?.selectedDeviceRecyclerView?.adapter?.notifyDataSetChanged()
            binding?.firmwareDownloadForSelectedDevices?.isEnabled = !m_selectedDevices.isEmpty()

        }
        binding?.selectedDeviceRecyclerView?.adapter = selectedDeviceAdapter

        activity?.let { _ ->
            Unit
            try {
                Utility.m_BluIDSDK_Client?.startDeviceDiscovery(
                    ScanFilter(
                        -80,
                        3000
                    )
                ) { bleDevices ->
                    Unit
                    activity?.runOnUiThread {
                        for (device in bleDevices) {
                            val selectedIndex = selectedDevicesId.indexOf(device.id)
                            val unselectedIndex = unselectedDevicesId.indexOf(device.id)
                            if (selectedIndex == -1 && unselectedIndex == -1) {
                                m_devices.add(device)
                                unselectedDevicesId.add(device.id)
                                deviceAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                binding?.firmwareDownloadForSelectedDevices?.isEnabled = !m_selectedDevices.isEmpty()

                binding?.firmwareDownloadForSelectedDevices?.setOnClickListener(
                    View.OnClickListener {
                        Utility.m_BluIDSDK_Client?.stopDeviceDiscovery()
                        binding?.selectedDeviceRecyclerView?.isEnabled = false
                        if (binding?.firmwareDownloadForSelectedDevices?.isEnabled == true) {
                            binding?.returnBack?.isEnabled = false
                            binding?.firmwareDownloadForSelectedDevices?.isVisible = false
                            m_model.selectedFirmware.value?.let { firmwareSelected ->
                                Utility.m_BluIDSDK_Client?.let { BluIDSDK ->
                                    CoroutineScope(Dispatchers.Default).launch {
                                        activity?.runOnUiThread {
                                            binding?.done?.isEnabled = false
                                            binding?.done?.isVisible = true
                                        }
                                        BluIDSDK.updateMultipleDeviceFirmware(
                                            selectedDevicesId,
                                            firmwareSelected.version,
                                            progressHandler = { id, progress, updateError ->
                                                val index =
                                                    m_selectedDevices.indexOfFirst { _selectedDevice -> _selectedDevice.deviceId == id }
                                                m_selectedDevices[index].let { _selectedDevicesInfo ->
                                                    m_selectedDevices[index] =
                                                        SelectedDevicesInfo(
                                                            _selectedDevicesInfo.deviceDetails,
                                                            _selectedDevicesInfo.deviceId,
                                                            true,
                                                            (progress
                                                                ?: _selectedDevicesInfo.percent),
                                                            updateError
                                                        )
                                                    Log.d(
                                                        "Multi device Firmware : ",
                                                        "Data Changed"
                                                    )
                                                    activity?.runOnUiThread {
                                                        selectedDeviceAdapter.notifyDataSetChanged()
                                                    }
                                                }
                                            })
                                        activity?.let {
                                            it.runOnUiThread {
                                                binding?.done?.isEnabled = true
                                                binding?.done?.isVisible = true
                                                Toast.makeText(
                                                    it.applicationContext,
                                                    "Multidevice firmware update complete",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                )
                binding?.returnBack?.setOnClickListener {
                    findNavController().navigate(R.id.action_firmwareMultiDevices_to_firmwareListFragment)
                }

                binding?.done?.setOnClickListener {
                    findNavController().navigate(R.id.action_firmwareMultiDevices_to_homeScreenFragment)
                }


            } catch (exception: BluIDSDKException) {
                activity?.runOnUiThread {
                    Toast.makeText(view.context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

}

data class SelectedDevicesInfo(
    val deviceDetails: Device_Information,
    val deviceId: String,
    val isUpdating: Boolean,
    val percent: Int,
    val error: BluIDSDKError?
)
