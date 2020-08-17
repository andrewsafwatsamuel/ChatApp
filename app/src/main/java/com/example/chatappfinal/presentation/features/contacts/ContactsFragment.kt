package com.example.chatappfinal.presentation.features.contacts

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import kotlinx.android.synthetic.main.error_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.toolbar.view.*

class ContactsFragment : Fragment() {

    private val adapter by lazy { ContactsAdapterDefault({},{}) }
    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(ContactsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_contacts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contacts_recyclerView.adapter = adapter
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer { drawContactStates(it) })
        contacts_error_sheet.retry_button.setOnClickListener { viewModel.getContacts() }
        drawToolbar()
    }

    private fun drawContactStates(state: ContactsState) = when (state) {
        is LoadingState -> onLoading()
        is EmptyContactState -> onEmptyContacts()
        is SuccessState -> onSuccess(state.Contacts)
        is ErrorState -> onError(state.message)
    }

    private fun onLoading() {
        contacts_error_sheet.hide()
        contacts_recyclerView.show()
        contacts_progressBar.show()
        contacts_empty_textView.hide()
    }

    private fun onEmptyContacts() {
        contacts_error_sheet.hide()
        contacts_recyclerView.hide()
        contacts_progressBar.hide()
        contacts_empty_textView.show()
    }

    private fun onSuccess(contacts: ArrayList<ConnectycubeUser>) {
        contacts_error_sheet.hide()
        contacts_recyclerView.show()
        contacts_progressBar.hide()
        contacts_empty_textView.hide()
        adapter.submitList(contacts)
    }

    private fun onError(message: String) {
        contacts_error_sheet.show()
        contacts_recyclerView.hide()
        contacts_progressBar.hide()
        contacts_empty_textView.hide()
        contacts_error_sheet.error_textView.text = message
    }

    private fun drawToolbar()=contacts_toolbar.main_toolbar.run{
        setupWithNavController(findNavController())
        navigationIcon = requireContext().getDrawable(R.drawable.ic_action_back)
        setTitleTextColor(Color.WHITE)
    }

}
