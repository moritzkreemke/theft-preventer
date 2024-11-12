package com.theft_preventer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.theft_preventer.app.data.presentation.navigation.NavGraph
import com.theft_preventer.app.ui.theme.TheftpreventerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheftpreventerTheme() {
                NavGraph()
            }
        }
    }
}