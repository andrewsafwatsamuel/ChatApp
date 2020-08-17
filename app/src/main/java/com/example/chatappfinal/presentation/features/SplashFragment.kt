package com.example.chatappfinal.presentation.features

import android.os.Bundle
import android.os.Handler

import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.connectycube.auth.session.ConnectycubeSession
import com.connectycube.auth.session.ConnectycubeSessionManager
import com.connectycube.auth.session.ConnectycubeSettings
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.ConnectyCube
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference

class SplashFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed({
            if (getUserFromPreference()==null)
            {findNavController().navigate(R.id.action_splashFragment_to_loginFragment)}else{
                findNavController().navigate(R.id.action_global_dialogsFragment)
            }
        }, 2 * 1000)
    }
}
