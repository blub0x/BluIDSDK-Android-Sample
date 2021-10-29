package com.blub0x.bluidsdk_sample_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.blub0x.BluIDSDK.models.UserCredentials
import com.blub0x.bluidsdk_sample_app.Database.DBController
import com.blub0x.bluidsdk_sample_app.Database.UserCreds
import com.blub0x.bluidsdk_sample_app.R
import com.blub0x.bluidsdk_sample_app.databinding.FragmentFirmwareListBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentHomeScreenBinding
import com.blub0x.bluidsdk_sample_app.databinding.FragmentUserLoginBinding
import com.blub0x.bluidsdk_sample_app.model.SharedDataModel
import com.blub0x.bluidsdk_sample_app.utils.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@ObsoleteCoroutinesApi
class UserLoginFragment : Fragment() {
    private val m_TAG = "UserLogin"
    @ObsoleteCoroutinesApi
    private val m_scope = CoroutineScope(newSingleThreadContext(m_TAG))
    private val m_model: SharedDataModel by activityViewModels()
    private var _binding : FragmentUserLoginBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(m_TAG, "back pressed")
                    findNavController().navigate(R.id.action_userLoginFragment_to_homeScreenFragment)
                }
            })
        _binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(m_TAG, "onViewCreated called")

        DBController().getUserData(requireActivity()).let {
            Log.d(m_TAG, "getUserData called")
            Log.d(m_TAG, "UserName: " + it?.username.toString())
            if (it?.rememberMe == true) {
                binding?.usernameEditText?.setText(it.username)
                binding?.passwordEditText?.setText(it.password)
                binding?.rememberCreds?.isChecked = true
            }
        }

        binding?.submitLoginButton?.setOnClickListener(
            View.OnClickListener {
                if (binding?.usernameEditText?.text?.isEmpty() == true || binding?.passwordEditText?.text?.isEmpty() == true) {
                    Utility.m_AlertDialog?.show("Fields cannot be empty!")
                    return@OnClickListener
                }
                it.isEnabled = false

                m_scope.launch {
                    Utility.m_BluIDSDK_Client?.let { sdkClient ->
                        Utility.m_ProgressBar?.show()
                        val loginResponse = sdkClient.login(
                            UserCredentials(
                                binding?.usernameEditText?.text.toString(),
                                binding?.passwordEditText?.text.toString()
                            )
                        )
                        Utility.m_ProgressBar?.dismiss()
                        activity?.runOnUiThread {
                            it.isEnabled = true
                            loginResponse.error?.let { loginError ->
                                Utility.m_AlertDialog?.show("${loginError.type} Code: ${loginError.code}\n${loginError.message}")
                                return@runOnUiThread
                            }
                            if (binding?.rememberCreds?.isChecked == true) {
                                DBController().saveUserData(
                                    requireActivity(),
                                    UserCreds(
                                        binding?.usernameEditText?.text.toString(),
                                        binding?.passwordEditText?.text.toString(),
                                        true
                                    )
                                )
                            } else {
                                DBController().saveUserData(
                                    requireActivity(),
                                    UserCreds("", "", false)
                                )
                            }
                            m_model.updateUserData(loginResponse.userData)
                            findNavController().navigate(R.id.action_userLoginFragment_to_homeScreenFragment)
                            return@runOnUiThread
                        }
                    }
                }
            }
        )
        view.findViewById<Toolbar>(R.id.userLoginToolbar)?.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_userLoginFragment_to_homeScreenFragment)
        }


    }

}