package com.cmatuteortega.monoburro

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cmatuteortega.monoburro.ui.MonoburroApp
import com.cmatuteortega.monoburro.ui.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private val vm: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MonoburroApp(vm, onDarkChanged = ::applySystemBars)
        }
    }

    /** Status bar icons follow the in-app theme toggle, not just the system one. */
    private fun applySystemBars(dark: Boolean) {
        val style = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
