package com.jasvir.simplelogbook.frag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jasvir.simplelogbook.MainActivity
import com.jasvir.simplelogbook.R
import com.jasvir.simplelogbook.adapter.NoteListAdapter
import com.jasvir.simplelogbook.databinding.FragmentNoteListBinding
import com.jasvir.simplelogbook.model.AppUser
import com.jasvir.simplelogbook.model.NoteModel
import com.jasvir.simplelogbook.uitls.NetworkUtils
import com.jasvir.simplelogbook.uitls.revokeAccess
import com.jasvir.simplelogbook.uitls.signOutUser
import com.jasvir.simplelogbook.vm.NoteListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment : Fragment() {

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient

    private val args: NoteListFragmentArgs? by navArgs()

    private val noteListViewModel: NoteListViewModel by activityViewModels()
    private lateinit var binding: FragmentNoteListBinding
    private var isMove = false
    private var prevNote: NoteModel? = null
    private var position = 0
    private val noteListAdapter = NoteListAdapter { _, note, isDelete ->
        if (isDelete) {
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_AlertDialog)
                .setTitle(getString(R.string.text_confirm))
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    noteListViewModel.deleteNote(note = note!!,
                        doOnSuccess = {},
                        doOnFailure = {})
                    noteListViewModel.loadNoteAllNote()
                }.setNegativeButton(getString(R.string.cancel), null).show()
            return@NoteListAdapter
        }

        if (!isMove) {
            navigateToNoteAdd(note)
        } else {
            if(note.title.equals(prevNote!!.title)){
                Toast.makeText(requireContext(),getString(R.string.you_cannot_move_to_same),Toast.LENGTH_SHORT).show()
                return@NoteListAdapter
            }


            note.subnotesList.add(prevNote?.subnotesList!!.get(position))
            update(note)
            prevNote!!.subnotesList.removeAt(position)
            update(prevNote!!)
            findNavController().popBackStack()
            findNavController().popBackStack()
            Toast.makeText(requireContext(),getString(R.string.moved_successfully),Toast.LENGTH_LONG).show()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isMove = args?.isMove ?: false
        prevNote = args?.note
        position = args?.position ?: 0

        if (noteListViewModel.getFirstInstall()) {
            val action = NoteListFragmentDirections.actionNoteListToSignIn()
            findNavController().navigate(action)
            noteListViewModel.setFirstInstall()
        }



        binding.toolbar.imgProfile.setOnClickListener {
            val profileDialog = ProfileDialogFragment {
                MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_AlertDialog)
                    .setTitle(getString(R.string.confirm))
                    .setMessage(getString(R.string.are_u_sure_logout))
                    .setPositiveButton(getString(R.string.singn_out)) { _, _ ->
                        signOutUser()
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }
            profileDialog.show(childFragmentManager, "")
        }

        binding.rvNote.apply {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = noteListAdapter
        }

        binding.fabNew.setOnClickListener {
            navigateToNoteAdd(null)
        }



        noteListViewModel.allNotes.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list ->
            if (list.isEmpty()) {
                binding.lblNoNote.visibility = View.VISIBLE
                binding.layoutNoteList.visibility = View.GONE
            } else {
                binding.lblNoNote.visibility = View.GONE
                binding.layoutNoteList.visibility = View.VISIBLE
            }
            noteListAdapter.updateNoteList(list)
        })

        mAuth.currentUser?.let {

            for (profile in it.providerData) {
                binding.toolbar.imgProfile.load(profile.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_account_circle_24)
                    transformations(CircleCropTransformation())
                }
            }
        }

        NetworkUtils.getNetworkLiveData(requireContext())
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                binding.lblNoInternet.visibility = if (it) View.GONE else View.VISIBLE
            })

        if (mAuth.currentUser == null) {
            binding.toolbar.imgProfile.visibility = View.GONE
        }

    }

    private fun navigateToNoteAdd(note: NoteModel?) {
        if (note == null) {
            val action = NoteListFragmentDirections.actionNoteListToNoteAdd(note)
            findNavController().navigate(action)
        } else {
            val action = NoteListFragmentDirections.actionNoteListToSublist(note)
            findNavController().navigate(action)
        }
    }


    private fun signOutUser() {
        lifecycleScope.launch(Dispatchers.Main) {
            mGoogleSignInClient.revokeAccess(
                doOnSuccess = {
                    mGoogleSignInClient.signOutUser(
                        doOnSuccess = {
                            mAuth.signOut()
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            requireActivity().startActivity(intent)
                            requireActivity().finishAffinity()
                        },
                        doOnFailed = { showErrorSignOutFailed() }
                    )
                },
                doOnFailed = { showErrorSignOutFailed() })

        }
    }

    private fun showErrorSignOutFailed() {
        Toast.makeText(context, "Sign out failed", Toast.LENGTH_SHORT).show()
    }

    fun update(note: NoteModel) {
        noteListViewModel.updateNote(
            noteId = note!!.id,
            noteData = mapOf(
                "title" to note!!.title,
                "note" to note!!.title, "subnotesList" to note.subnotesList,
                "modifiedDate" to Date(System.currentTimeMillis()),
                "receivers" to listOf<AppUser>()
            ),
            doOnSuccess = {},
            doOnFailure = {})
    }

}