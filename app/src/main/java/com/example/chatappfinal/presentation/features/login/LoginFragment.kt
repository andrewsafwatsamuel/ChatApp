package com.example.chatappfinal.presentation.features.login

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
import com.example.chatappfinal.presentation.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.login_button
import kotlinx.android.synthetic.main.fragment_login.login_edit_text
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class LoginFragment : Fragment(),AuthStates {

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(LoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.main_toolbar.title = "Login"
        register_button_textView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        viewModel.loginStates.observe(viewLifecycleOwner, Observer { drawStates(it) })
        login_button.setOnClickListener { viewModel.login(login_edit_text.text(),password_edit_text.text()) }
    }

    override fun loading() {
        login_error_textView.hide()
        login_progressBar.show()
        login_button.disable()
    }

    override fun success() {
        login_error_textView.hide()
        login_progressBar.hide()
        login_button.enable()
        findNavController().navigate(R.id.action_global_dialogsFragment)
    }

    override fun error(message: String) {
        login_error_textView.show()
        login_progressBar.hide()
        login_button.enable()
        login_error_textView.text = message
    }

}
