package com.cmatuteortega.monoburro

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cmatuteortega.monoburro.ui.MonoburroApp
import com.cmatuteortega.monoburro.ui.AppViewModel

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MonoburroApp(vm, onDarkChanged = ::applySystemBars, onSubscribe = { vm.subscribe(this) })
        }
    }

    /** Picks up renewals, cancellations and purchases made outside the app. */
    override fun onResume() {
        super.onResume()
        vm.refreshBilling()
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
