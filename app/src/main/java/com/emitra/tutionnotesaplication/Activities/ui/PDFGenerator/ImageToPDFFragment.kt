package com.emitra.tutionnotesaplication.Activities.ui.PDFGenerator

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.emitra.tutionnotesaplication.Constants
import com.emitra.tutionnotesaplication.R
import com.emitra.tutionnotesaplication.databinding.FragmentImageToPDFBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ImageToPDFFragment : Fragment() {

    private lateinit var binding : FragmentImageToPDFBinding
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageToPDFBinding.inflate(layoutInflater)
        val root = binding.root

        binding.actionBar.activityName.text ="Image to PDF Activity"
        binding.actionBar.back.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.selectImageButton.setOnClickListener {
            openFileChooser()
        }

        binding.downloadAsPdfButton.setOnClickListener {
            if (selectedImageUri != null) {
                generateAndDownloadPdf()
            } else {
                Constants.error(context, "Please select the image")
            }
        }

        return root
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(requireContext()).load(selectedImageUri).into(binding.image)
            binding.downloadAsPdfButton.visibility = View.VISIBLE
        }
    }

    private fun generateAndDownloadPdf() {
        selectedImageUri?.let { imageUri ->
            val resolver = requireContext().contentResolver
            val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(imageUri))
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(directory, "${UUID.randomUUID().toString()}.pdf")
            try {
                val outputStream = FileOutputStream(pdfFile)
                document.writeTo(outputStream)
                Constants.success(context, "PDF saved at ${pdfFile.absolutePath}")
            } catch (e: IOException) {
                e.printStackTrace()
               Constants.error(context, "Failed to save PDF")
            }
            document.close()
        }
    }
}