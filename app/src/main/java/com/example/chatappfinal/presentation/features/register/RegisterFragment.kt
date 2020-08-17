package com.example.chatappfinal.presentation.features.register

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.RegParams
import com.example.chatappfinal.presentation.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.toolbar.view.*

class RegisterFragment : Fragment(), AuthStates {

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(RegisterViewModel::class.java)
    }

    private var avatar = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_register, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        include.main_toolbar.title = "Registration"
        viewModel.registerStates.observe(viewLifecycleOwner, Observer { drawStates(it) })
        register_button.setOnClickListener { viewModel.register(createRegisterParams(),register_repeat_password_edit_text.text()) }
    }

    override fun loading() {
        register_error_textView.hide()
        register_progressBar.show()
        register_button.disable()
    }

    override fun success() {
        register_error_textView.hide()
        register_progressBar.hide()
        register_button.enable()
        findNavController().navigate(R.id.action_global_dialogsFragment)
    }

    override fun error(message: String) {
        register_error_textView.show()
        register_progressBar.hide()
        register_button.enable()
        register_error_textView.text = message
    }

    private fun createRegisterParams() = RegParams(
        register_login_edit_text.text(),
        register_password_edit_text.text(),
        mail_edit_text.text(),
        "${register_login_edit_text.text()} ${last_name_edit_text.text()}",
        phone_edit_text.text(),
        avatar
    )
}
