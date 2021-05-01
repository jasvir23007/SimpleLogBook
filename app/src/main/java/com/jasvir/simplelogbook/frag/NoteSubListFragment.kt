package com.jasvir.simplelogbook.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jasvir.simplelogbook.R
import com.jasvir.simplelogbook.adapter.NoteSubListAdapter
import com.jasvir.simplelogbook.databinding.FragmentNoteSubListBinding
import com.jasvir.simplelogbook.model.NoteModel
import com.jasvir.simplelogbook.uitls.NetworkUtils
import com.jasvir.simplelogbook.vm.NoteListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteSubListFragment : Fragment() {

    @Inject
    lateinit var mAuth: FirebaseAuth
    private var note: NoteModel? = null
    private val args: NoteAddFragmentArgs by navArgs()
    private val noteListViewModel: NoteListViewModel by activityViewModels()
    private lateinit var binding: FragmentNoteSubListBinding

    private val noteListAdapter = NoteSubListAdapter { _, pos, isMove ->

        if (isMove) {
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_AlertDialog)
                .setTitle(getString(R.string.move))
                .setMessage(getString(R.string.are_u_sure_move))
                .setPositiveButton(getString(R.string.move)) { _, _ ->
                    navigateToListMove(pos, true)
                }.setNegativeButton(getString(R.string.cancel), null).show()


        } else {
            navigateToNoteAdd(pos, true, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteSubListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args.note
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.lblLabel.setText(getString(R.string.entries))
        binding.rvNoteSublist.apply {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = noteListAdapter
        }

        binding.toolbar.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.fabNew.setOnClickListener {
            navigateToNoteAdd(0, true, true)
        }

        noteListAdapter.updateNoteList(note!!.subnotesList)


        NetworkUtils.getNetworkLiveData(requireContext()).observe(viewLifecycleOwner) {
            binding.lblNoInternet.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

    private fun navigateToNoteAdd(position: Int, isSublist: Boolean, isAdd: Boolean) {
        val action =
            NoteSubListFragmentDirections.actionSublistToAdd(note, position, isSublist, isAdd)
        findNavController().navigate(action)
    }

    private fun navigateToListMove(position: Int, isMove: Boolean) {
        val action = NoteSubListFragmentDirections.actionSublistToMove(note, position, isMove)
        findNavController().navigate(action)
    }


}