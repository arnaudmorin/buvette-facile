package com.buvettefacile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private sealed interface Screen {
    data object Home : Screen
    data class Edit(val fromRegister: Boolean) : Screen
    data object Register : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = PosterStore(applicationContext)
        setContent {
            MaterialTheme { App(store) }
        }
    }
}

@Composable
private fun App(store: PosterStore) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(store.loadBitmap()?.asImageBitmap()) }
    var boxes by remember { mutableStateOf(store.loadBoxes()) }
    var screen by remember {
        mutableStateOf<Screen>(if (store.hasSavedRegister() && bitmap != null) Screen.Register else Screen.Home)
    }

    val image = bitmap
    if (image == null || screen is Screen.Home) {
        HomeScreen(store) {
            bitmap = store.loadBitmap()?.asImageBitmap()
            boxes = emptyList()
            screen = Screen.Edit(fromRegister = false)
        }
        return
    }

    when (val s = screen) {
        is Screen.Edit -> EditScreen(
            bitmap = image,
            initialBoxes = boxes,
            canGoBack = s.fromRegister,
            onSave = {
                boxes = it
                store.saveBoxes(it)
                screen = Screen.Register
            },
            onBack = { screen = Screen.Register },
        )
        Screen.Register -> RegisterScreen(
            bitmap = image,
            boxes = boxes,
            onEdit = { screen = Screen.Edit(fromRegister = true) },
            onRestart = {
                store.clear()
                bitmap = null
                boxes = emptyList()
                screen = Screen.Home
            },
        )
        Screen.Home -> Unit
    }
}
