package com.example.chatappfinal.presentation.features.startGroupChat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.contactsRepository
import com.example.chatappfinal.domain.dataSources.runtimeCache
import com.example.chatappfinal.presentation.*
import kotlinx.android.synthetic.main.fragment_start_group_chat.*

class StartGroupChatFragment : Fragment() {

    private val horizontalAdapter by lazy {
        StartGroupAdapterDefault(R.layout.item_contact_horizontal) {}
    }
    private val verticalAdapter by lazy {
        StartGroupAdapterDefault(R.layout.item_contact, horizontalAdapter::submitList)
    }

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(StartGroupViewModel::class.java)
    }

    private var isPublic = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_start_group_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selected_contacts_recyclerView.adapter = horizontalAdapter
        create_group_recyclerView.adapter = verticalAdapter
        verticalAdapter.submitList(contactsRepository.retrieveData())
        setPublicGroup()
        create_group_button.setOnClickListener {
            viewModel.starDialog(isPublic, dialog_name_editText.text.toString(), getOccupants())
        }
        back_imageView.setOnClickListener { findNavController().navigateUp() }
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer { drawStates(it) })
    }

    private fun getOccupants() = horizontalAdapter.items.toList().map { it.id }

    private fun setPublicGroup() =
        public_group_switch.setOnCheckedChangeListener { button, isPublic ->
            button.text = if (isPublic) "Public" else "Group"
            this.isPublic = isPublic
        }

    private fun drawStates(state: StartGroupState) = when (state) {
        is LoadingState -> loading()
        is SuccessState -> success(state.dialog)
        is ErrorState -> error(state.message)
    }

    private fun loading() {
        join_chat_button.disable()
        create_group_button.disable()
        back_imageView.disable()
        start_group_progressBar.show()
    }

    private fun success(dialog: ConnectycubeChatDialog?) = dialog
        ?.also { runtimeCache.updateDialog(it) }
        ?.let { StartGroupChatFragmentDirections.actionGlobalChatFragment(it) }
        ?.let { findNavController().navigate(it) }

    private fun error(message: String) {
        join_chat_button.enable()
        create_group_button.enable()
        back_imageView.enable()
        start_group_progressBar.hide()
        start_group_layout.createSnackBar(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.hideKeyboard()
    }
}
