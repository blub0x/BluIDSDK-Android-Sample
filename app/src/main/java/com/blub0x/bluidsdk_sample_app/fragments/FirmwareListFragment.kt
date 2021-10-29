package com.blub0x.bluidsdk_sample_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blub0x.BluIDSDK.models.Device_Firmware
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.adapter.FirmwareListAdapter
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareListBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentHomeScreenBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentUserLoginBinding
import com.blub0x.bluidsdk_sample_app.model.SharedDataModel
import com.blub0x.bluidsdk_sample_app.utils.Utility
import kotlinx.coroutines.*


@ObsoleteCoroutinesApi
class FirmwareListFragment : Fragment() {
    private val m_TAG = "FirmwareDownload"
    private val m_model: SharedDataModel by activityViewModels()

    private val m_scope = CoroutineScope(newSingleThreadContext(m_TAG))
    private var _binding: FragmentFirmwareListBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(m_TAG, "back pressed")
                    findNavController().navigate(R.id.action_firmwareListFragment_to_homeScreenFragment)
                }
            })

        _binding = FragmentFirmwareListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = runBlocking {
        super.onViewCreated(view, savedInstanceState)
        val firmwares: ArrayList<Device_Firmware> =
            Utility.m_BluIDSDK_Client?.listAvailableFirmwareVersions()!!
        val cancelButton = view.findViewById<ImageButton>(R.id.returnBackFromFirmwareList)

        val firmwareAdapter = FirmwareListAdapter(firmwares) {
            m_model.selectedFirmware.value = it
            binding?.firmwareDownload?.visibility = View.VISIBLE
        }
        val firmwaresRecyclerView =
            view.findViewById<View>(R.id.firmwareListRecyclerView) as RecyclerView
        firmwaresRecyclerView.layoutManager = LinearLayoutManager(view.context)
        firmwaresRecyclerView.adapter = firmwareAdapter

        binding?.firmwareDownload?.setOnClickListener {
            val firmwareSelected = m_model.selectedFirmware.value
            binding?.firmwareDownload?.isEnabled = false
            firmwareSelected?.let {
                Utility.m_ProgressBar?.show()
                m_scope.launch {
                    val version = it.version
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        if (!firmwareSelected.isDownloaded) {
                            Utility.m_ProgressBar?.showTitle("Downloading")
                            val downloadResponse =
                                sdkClient.downloadFirmware(it.version)
                            downloadResponse?.let {
                                Utility.m_ProgressBar?.dismiss()
                                Utility.m_ProgressBar?.showTitle("")
                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        view.context,
                                        "Download Failed",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    findNavController().navigate(R.id.action_firmwareListFragment_to_homeScreenFragment)
                                }
                                return@launch
                            }
                            Utility.m_ProgressBar?.dismiss()
                            Utility.m_ProgressBar?.showTitle("")
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    view.context,
                                    "Download Completed",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                        if (m_model.selectedDeviceFirmwareMode.value == "single") {
                            activity?.let { activity ->
                                val progressIndicator = DownloadingDialog(activity)
                                Utility.progressDialog = progressIndicator
                                progressIndicator.startDownloading()
                                Log.d("Updating ......", "single Device")
                                m_model.selectedDevice.value?.id?.let { id ->
                                    val firmwareUpdateResponse =
                                        Utility.m_BluIDSDK_Client?.updateSingleDeviceFirmware(
                                            id,
                                            version,
                                            onProgress = { _, progress ->
                                                activity.runOnUiThread {
                                                    val progressBar =
                                                        progressIndicator.m_dialogView?.findViewById<ProgressBar>(
                                                            R.id.progressBarDownload
                                                        )
                                                    val progressDisplay =
                                                        progressIndicator.m_dialogView?.findViewById<TextView>(
                                                            R.id.progressDisplay
                                                        )
                                                    progressBar?.progress = progress
                                                    progressDisplay?.text =
                                                        progress.toString() + "%"
                                                }
                                            }
                                        )
                                    progressIndicator.dismiss()
                                    Utility.progressDialog = null
                                    activity.runOnUiThread {
                                        if (firmwareUpdateResponse == null) {
                                            m_model.updatedFirmwareForSingleDevice.value = true
                                            Toast.makeText(
                                                view.context,
                                                "Firmware update completed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            m_model.updatedFirmwareForSingleDevice.value = false
                                            Utility.m_AlertDialog?.show("$firmwareUpdateResponse")
                                        }
                                        Utility.m_ProgressBar?.dismiss()
                                        findNavController().navigate(R.id.action_firmwareListFragment_to_homeScreenFragment)
                                    }
                                }
                            }
                        } else if (m_model.selectedDeviceFirmwareMode.value == "multi") {
                            activity?.runOnUiThread {
                                Utility.m_ProgressBar?.dismiss()
                                Utility.m_ProgressBar?.showTitle("")
                                findNavController().navigate(R.id.action_firmwareListFragment_to_firmwareMultiDevices)
                            }
                        }
                    }
                }
            }

        }

        binding?.returnBackFromFirmwareList?.setOnClickListener {
            findNavController().navigate(R.id.action_firmwareListFragment_to_homeScreenFragment)
        }
    }
}





