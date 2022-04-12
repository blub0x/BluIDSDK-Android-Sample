package com.blub0x.bluidsdk_sample_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.BluIDSDK.models.BluIDSDKException
import com.blub0x.BluIDSDK.models.ScanFilter
import com.blub0x.BluIDSDK.models.Device_Information
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.adapter.BLEDevicesAdapter
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareListBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareMultiDevicesBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentScanDevicesBinding
import com.blub0x.bluidsdk_sample_app.model.SharedDataModel
import com.blub0x.bluidsdk_sample_app.utils.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ScanDevicesFragment : Fragment() {
    private val m_TAG = "ScanDevicesFragment"
    private val m_model: SharedDataModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private var _binding : FragmentScanDevicesBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(m_TAG, "back pressed")
                    findNavController().navigate(R.id.action_scanDevicesFragment_to_homeScreenFragment)
                }
            })
        _binding = FragmentScanDevicesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val devices: ArrayList<Device_Information> = ArrayList<Device_Information>()
        val deviceAdapter = BLEDevicesAdapter(devices) {
            m_model.selectedDevice.value = it
            if (Utility.m_BluIDSDK_Client?.m_isAuthenticatorStarted == false) {
                Utility.m_BluIDSDK_Client?.stopDeviceDiscovery()
            }
            findNavController().navigate(R.id.action_scanDevicesFragment_to_homeScreenFragment)
        }
        binding?.scanDeviceRecyclerView?.layoutManager = LinearLayoutManager(view.context)
        binding?.scanDeviceRecyclerView?.adapter = deviceAdapter
        activity?.let { _ ->
            Unit
            Utility.m_ProgressBar?.show()
            Utility.m_IsScanningStarted = true
            CoroutineScope(Dispatchers.Default).launch {
                val scanError = Utility.m_BluIDSDK_Client?.startDeviceDiscovery(
                    ScanFilter(
                        -80,
                        1000
                    )
                ) { bleDevices ->
                    Unit
                    Log.d(m_TAG, bleDevices.map { it.name }.toString())
                    activity?.runOnUiThread {
                        devices.clear()
                        devices.addAll(bleDevices)
                        devices.sortByDescending { it.signalStrength }
                        deviceAdapter.notifyDataSetChanged()
                    }
                }
                if (scanError != null) {
                    activity?.runOnUiThread {
                        Utility.m_ProgressBar?.dismiss()
                        Toast.makeText(view.context, scanError.message, Toast.LENGTH_SHORT).show()
                        scanError.message?.let { Utility.m_AlertDialog?.show(it) }
                        findNavController().navigate(R.id.action_scanDevicesFragment_to_homeScreenFragment)
                    }
                }
            }
                Utility.m_ProgressBar?.dismiss()
            }
        }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding = null
    }
}