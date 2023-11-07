package com.what3words.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.what3words.sample.compose.ui.screen.W3WTextFieldInConstraintLayoutScreen
import com.what3words.sample.compose.ui.theme.What3WordsComponentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            What3WordsComponentsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    W3WTextFieldInConstraintLayoutScreen()
                }
            }
        }
    }
}