package com.ainewsstudio

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ainewsstudio.data.AppViewModel
import com.ainewsstudio.data.Prefs
import com.ainewsstudio.ui.AppRoot
import com.ainewsstudio.ui.theme.AppFonts
import com.ainewsstudio.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(applicationContext)
        AppFonts.init(assets)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { AppTheme { AppRoot(vm) } }
    }
}
