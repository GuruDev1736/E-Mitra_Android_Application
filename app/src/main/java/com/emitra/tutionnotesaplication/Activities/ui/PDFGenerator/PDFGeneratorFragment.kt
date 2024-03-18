package com.emitra.tutionnotesaplication.Activities.ui.PDFGenerator

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.emitra.tutionnotesaplication.R
import com.emitra.tutionnotesaplication.databinding.FragmentPDFGeneratorBinding

class PDFGeneratorFragment : Fragment() {

    private lateinit var binding : FragmentPDFGeneratorBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPDFGeneratorBinding.inflate(layoutInflater)
        val root = binding.root

        binding.imgPdf.setOnClickListener {
           Navigation.findNavController(it).navigate(R.id.imageToPDF)
        }

        binding.textPdf.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.textToPDF)
        }

        return root
    }
}