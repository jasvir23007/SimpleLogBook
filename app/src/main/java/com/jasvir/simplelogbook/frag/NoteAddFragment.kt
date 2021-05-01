package com.jasvir.simplelogbook.frag

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.divyanshu.draw.activity.DrawingActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.kbeanie.multipicker.api.CacheLocation
import com.kbeanie.multipicker.api.ImagePicker
import com.kbeanie.multipicker.api.Picker
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.jasvir.simplelogbook.BuildConfig
import com.jasvir.simplelogbook.R
import com.jasvir.simplelogbook.adapter.ImageListAdapter
import com.jasvir.simplelogbook.databinding.FragmentNoteAddBinding
import com.jasvir.simplelogbook.model.AppUser
import com.jasvir.simplelogbook.model.NoteModel
import com.jasvir.simplelogbook.model.SubNotes
import com.jasvir.simplelogbook.uitls.DateFormatter
import com.jasvir.simplelogbook.uitls.ProgressDialog.Companion.hideProgress
import com.jasvir.simplelogbook.uitls.ProgressDialog.Companion.showProgress
import com.jasvir.simplelogbook.vm.NoteListViewModel
import com.kbeanie.multipicker.utils.FileUtils.getExternalFilesDir
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class NoteAddFragment : Fragment(), ImagePickerCallback, View.OnClickListener {
    private val args: NoteAddFragmentArgs by navArgs()
    private val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 100
    private val REQUEST_CODE_DRAW = 7
    private val viewModel: NoteListViewModel by activityViewModels()
    private lateinit var binding: FragmentNoteAddBinding
    private lateinit var imagePicker: ImagePicker
    private var drawingImage = ""
    private var isDrawing = false
    private val _listenImage = MutableLiveData<String>()


    @Inject
    lateinit var mAuth: FirebaseAuth
    private var note: NoteModel? = null
    private var isAddAction: Boolean = true
    private var isSublist: Boolean = false
    private var position: Int = 0
    private var imageList = ArrayList<String>()

    private val imageListAdapter = ImageListAdapter { _, position ->
        removeImage(position)
    }

    fun removeImage(position: Int) {
        imageList.removeAt(position)
        imageListAdapter.notifyDataSetChanged()
    }

    private fun isValidData(): Boolean {
        val tempTitle = binding.etTitle.text.toString().trim()
        val tempNote = binding.etNote.text.toString().trim()
        return (tempTitle.isNotEmpty() && if (isAddAction || !isSublist) true else {
            tempTitle != note!!.subnotesList.get(position).title || tempNote != note!!.subnotesList.get(
                position
            ).description
        })
    }

    private val noteTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateActionSave(isValidData())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            updateActionSave(isValidData())
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // no implement
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args.note
        isSublist = args.isSublist
        isAddAction = args.isAdd
        position = args.position
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_DRAW -> {
                    showProgress(requireContext())
                    val result = data?.getByteArrayExtra("bitmap")
                    val bitmap = BitmapFactory.decodeByteArray(result, 0, result!!.size)
                    viewModel.uploadImage(getImageUri(requireContext(), bitmap), _listenImage)
                }
                Picker.PICK_IMAGE_DEVICE -> {
                    imagePicker.submit(data)
                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askPermission()
        setUiAndText()
        setClicksListners()
        setRecyclerView()
        setOnTextChangeListners()
        setData()
        setObserver()
    }

    private fun setObserver() {
        _listenImage.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (!isDrawing) {
                imageList.add(it)
                imageListAdapter.updateImageList(imageList)
            } else {
                drawingImage = it
                if (drawingImage.isEmpty()) binding.layoutDrawing.clParent.visibility = View.GONE
                else binding.layoutDrawing.clParent.visibility = View.VISIBLE
                Glide.with(requireContext()).load(it).into(binding.layoutDrawing.ivImage);
            }
            hideProgress()
        })
    }

    private fun setData() {
        if (!isAddAction) {
            binding.etTitle.setText(note!!.subnotesList.get(position).title)
            binding.etNote.setText(note!!.subnotesList.get(position).description)
            drawingImage = note!!.subnotesList.get(position).drawingPath
            imageList = note!!.subnotesList.get(position).imgPath
            imageListAdapter.updateImageList(imageList)
        }

        if (drawingImage.isEmpty()) {
            binding.layoutDrawing.clParent.visibility = View.GONE
        } else {
            binding.layoutDrawing.clParent.visibility = View.VISIBLE
            Glide.with(requireContext()).load(drawingImage).into(binding.layoutDrawing.ivImage);
        }
    }

    private fun setClicksListners() {
        binding.layoutToolbar.imgBack.setOnClickListener(this)
        binding.layoutToolbar.imgPdf.setOnClickListener(this)
        binding.tvDrawing.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.layoutToolbar.imgSave.setOnClickListener(this)
        binding.layoutToolbar.imgDelete.setOnClickListener(this)
        binding.layoutDrawing.ivDelete.setOnClickListener(this)
    }

    private fun setRecyclerView() {
        binding.rvAddImage.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvAddImage.adapter = imageListAdapter
    }

    private fun setOnTextChangeListners() {
        binding.etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etTitle.addTextChangedListener(noteTextWatcher)
            } else {
                binding.etTitle.removeTextChangedListener(noteTextWatcher)
            }
        }
        binding.etNote.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etNote.addTextChangedListener(noteTextWatcher)
            } else {
                binding.etNote.removeTextChangedListener(noteTextWatcher)
            }
        }
    }

    private fun setUiAndText() {
        binding.layoutToolbar.imgSave.setVisibility(View.VISIBLE)
        updateActionSave(false)
        binding.lblDate.text =
            DateFormatter.formatDate(if (isAddAction) Date() else note!!.subnotesList.get(position).creationDate)
        binding.layoutToolbar.lblLabel.text =
            if (isAddAction) getString(R.string.add_notes) else getString(
                R.string.edit_notes
            )
        //   binding.layoutToolbar.imgDelete.visibility = if (isAddAction) View.GONE else View.VISIBLE
        when (isSublist) {
            false -> {
                binding.etNote.visibility = View.GONE
                binding.tvAddImage.visibility = View.GONE
                binding.rvAddImage.visibility = View.GONE
                binding.tvDrawing.visibility = View.GONE
                binding.layoutDrawing.clParent.visibility = View.GONE

            }
            else -> {
                if (!isAddAction) {
                    binding.layoutToolbar.imgDelete.visibility = View.VISIBLE
                    binding.layoutToolbar.imgPdf.visibility = View.VISIBLE
                    updateActionSave(true)
                }
            }
        }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    @Suppress("DEPRECATION")
    fun pickImageSingle() {
        imagePicker = ImagePicker(this)
        imagePicker.setDebugglable(true)
        imagePicker.setFolderName("Random")
        imagePicker.setRequestId(1234)
        imagePicker.ensureMaxSize(500, 500)
        imagePicker.shouldGenerateMetadata(true)
        imagePicker.shouldGenerateThumbnails(true)
        imagePicker.setImagePickerCallback(this)
        val bundle = Bundle()
        bundle.putInt("android.intent.extras.CAMERA_FACING", 1)
        imagePicker.setCacheLocation(CacheLocation.EXTERNAL_STORAGE_APP_DIR)
        imagePicker.pickImage()
    }

    private fun updateActionSave(isEnable: Boolean) {
        binding.layoutToolbar.imgSave.apply {
            isEnabled = isEnable
            imageAlpha = if (isEnable) 255 else 100
        }
    }

    private fun saveNote() {
        val tempTitle = binding.etTitle.text.toString()
        val tempNote = binding.etNote.text.toString()
        val currentMillis = System.currentTimeMillis()

        if (!isSublist) {

           if(!viewModel.filterNote(tempTitle).isEmpty()){
               Toast.makeText(requireActivity(), "Log book already exist", Toast.LENGTH_SHORT).show()
               return
           }

            val list = ArrayList<SubNotes>()
            viewModel.saveNote(note = NoteModel(
                title = tempTitle,
                note = tempNote,
                color = "#ffffff",
                creationDate = Date(),
                modifiedDate = Date(currentMillis), subnotesList = list
            ), doOnSuccess = {},
                doOnFailure = {
                    Toast.makeText(context, "Save note failed", Toast.LENGTH_SHORT).show()
                })

        } else {
            val list = note!!.subnotesList
            if (isAddAction) {
                val subNotes =
                    SubNotes(tempTitle, tempNote, Date(currentMillis), imageList, drawingImage)
                list.add(subNotes)
            } else {
                val subNotes =
                    SubNotes(tempTitle, tempNote, Date(currentMillis), imageList, drawingImage)
                list.set(position, subNotes)
            }

            update(list, currentMillis)

        }

        findNavController().popBackStack()
    }


    private fun update(subNotes: ArrayList<SubNotes>, currentMillis: Long) {

        viewModel.updateNote(
            noteId = note!!.id,
            noteData = mapOf(
                "title" to note!!.title,
                "note" to note!!.title, "subnotesList" to subNotes,
                "modifiedDate" to Date(currentMillis),
                "receivers" to listOf<AppUser>()
            ),
            doOnSuccess = {},
            doOnFailure = {
                Toast.makeText(context, "Update note failed", Toast.LENGTH_SHORT).show()
            })

    }

    private fun createPdf() {
        showProgress(requireContext())
        val document = Document()
        val root = File(requireContext().externalCacheDir!!.absolutePath, "Notes")
        if (!root.exists()) {
            root.mkdirs()
        }
        val mfile = File(root, note!!.title + ".pdf")
        PdfWriter.getInstance(document, FileOutputStream(mfile))

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            document.open()
            document.add(Paragraph(note!!.subnotesList.get(position).title))
            document.add(Paragraph(note!!.subnotesList.get(position).description))
            for (img in note!!.subnotesList.get(position).imgPath) {
                val image =
                    Image.getInstance(URL(img))
                image.scaleToFit(200.0.toFloat(), 49.0.toFloat())
                document.add(image)
            }
            document.close()

            val uri: Uri = FileProvider.getUriForFile(
                requireActivity(),
                BuildConfig.APPLICATION_ID + ".provider",
                mfile
            )

            openPdf(uri)
            hideProgress()
        }
    }


    private fun openPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    override fun onImagesChosen(p0: MutableList<ChosenImage>?) {
        showProgress(requireContext())
        viewModel.uploadImage(Uri.fromFile(File(p0?.get(0)?.originalPath)), _listenImage)
    }

    override fun onError(p0: String?) {
    }

    override fun onClick(view: View) {
        when (view) {
            binding.layoutToolbar.imgBack -> {
                findNavController().popBackStack()
            }

            binding.layoutDrawing.ivDelete -> {
                drawingImage = ""
                binding.layoutDrawing.clParent.visibility = View.GONE
            }

            binding.tvAddImage -> {
                isDrawing = false
                pickImageSingle()
            }

            binding.layoutToolbar.imgPdf -> {
                createPdf()
            }

            binding.layoutToolbar.imgSave -> {
                saveNote()
            }

            binding.tvDrawing -> {
                isDrawing = true
                val intent = Intent(requireContext(), DrawingActivity::class.java)
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_CODE_DRAW)
            }

            binding.layoutToolbar.imgDelete -> {
                MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_AlertDialog)
                    .setTitle(getString(R.string.text_confirm))
                    .setMessage(getString(R.string.are_you_sure))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        val list = note!!.subnotesList
                        list.removeAt(position)
                        update(list, System.currentTimeMillis())
                        findNavController().popBackStack()
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }


        }

    }
}