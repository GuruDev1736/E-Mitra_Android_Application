package com.emitra.tutionnotesaplication.Activities.ui.PDFGenerator

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emitra.tutionnotesaplication.Constants
import com.emitra.tutionnotesaplication.R
import com.emitra.tutionnotesaplication.databinding.FragmentTextToPDFBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


class TextToPDFFragment : Fragment() {

    private lateinit var binding : FragmentTextToPDFBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTextToPDFBinding.inflate(layoutInflater)
        val root = binding.root

        binding.actionBar.back.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.actionBar.activityName.text = "Text to PDF Activity"

        binding.convertToPdfButton.setOnClickListener {
            val text = binding.textInput.text.toString()
            if (text.isNotEmpty()) {
                generateAndDownloadPdf(text)
            } else {
                Constants.error(context, "Please enter some text")
            }
        }

        return root
    }


    private fun generateAndDownloadPdf(text: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val textWidth = pageInfo.pageWidth - 40 // 20dp margin on each side
        val textHeight = pageInfo.pageHeight - 40 // 20dp margin on top and bottom

        // Write text to the PDF
        val paint = Paint()
        paint.textSize = 12f
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)

        val lineHeight = textBounds.height()

        // Split the text into lines
        val textLines = text.split("\n")

        // Write text to the PDF
        var yPos = 20f + lineHeight // Starting yPos
        for (line in textLines) {
            if (yPos + lineHeight <= textHeight) {
                canvas.drawText(line, 20f, yPos, paint)
                yPos += lineHeight // Move to next line
            } else {
                break // Stop writing if reached end of page
            }
        }

        document.finishPage(page)

        // Save the PDF
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