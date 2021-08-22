package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ScreenSlidePageFragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_screen_slide_page_1, container, false)
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        return view
        //View = inflater.inflate(R.layout.fragment_screen_slide_page, container, false)
    }
}

class ScreenSlidePageFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_screen_slide_page_2, container, false)
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        return view
        //View = inflater.inflate(R.layout.fragment_screen_slide_page2, container, false)
    }
}

class ScreenSlidePageFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_screen_slide_page_3, container, false)
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        return view
        //View = inflater.inflate(R.layout.fragment_screen_slide_page2, container, false)
    }
}
