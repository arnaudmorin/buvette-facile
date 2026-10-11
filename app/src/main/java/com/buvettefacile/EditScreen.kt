package com.buvettefacile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private sealed interface EditDialog {
    data class New(val rect: Rect) : EditDialog
    data class Existing(val box: PriceBox) : EditDialog
}

private const val MIN_BOX_SIZE = 0.03f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    bitmap: ImageBitmap,
    initialBoxes: List<PriceBox>,
    canGoBack: Boolean,
    onSave: (List<PriceBox>) -> Unit,
    onBack: () -> Unit,
) {
    var boxes by remember { mutableStateOf(initialBoxes) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }
    var dialog by remember { mutableStateOf<EditDialog?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noBoxesMessage = stringResource(R.string.no_boxes)

    if (canGoBack) BackHandler(onBack = onBack)

    val draft = dragStart?.let { s ->
        dragCurrent?.let { c -> Rect(min(s.x, c.x), min(s.y, c.y), max(s.x, c.x), max(s.y, c.y)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_title)) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (boxes.isEmpty()) scope.launch { snackbar.showSnackbar(noBoxesMessage) } else onSave(boxes)
                    }) { Text(stringResource(R.string.save)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                stringResource(R.string.edit_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PosterCanvas(
                bitmap = bitmap,
                boxes = boxes,
                draft = draft,
                modifier = Modifier.fillMaxWidth().weight(1f),
                onTap = { p -> boxes.lastOrNull { it.contains(p) }?.let { dialog = EditDialog.Existing(it) } },
                onDragStart = { p -> dragStart = p; dragCurrent = p },
                onDrag = { p -> dragCurrent = p },
                onDragEnd = {
                    draft?.let { r ->
                        if (abs(r.width) >= MIN_BOX_SIZE && abs(r.height) >= MIN_BOX_SIZE) dialog = EditDialog.New(r)
                    }
                    dragStart = null
                    dragCurrent = null
                },
            )
        }
    }

    when (val d = dialog) {
        null -> Unit
        is EditDialog.New -> PriceDialog(
            title = stringResource(R.string.dialog_new_title),
            initialCents = null,
            initialLabel = "",
            onConfirm = { cents, label ->
                boxes = boxes + PriceBox(System.currentTimeMillis(), d.rect.left, d.rect.top, d.rect.right, d.rect.bottom, cents, label)
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        is EditDialog.Existing -> PriceDialog(
            title = stringResource(R.string.dialog_edit_title),
            initialCents = d.box.cents,
            initialLabel = d.box.label,
            onConfirm = { cents, label ->
                boxes = boxes.map { if (it.id == d.box.id) it.copy(cents = cents, label = label) else it }
                dialog = null
            },
            onDismiss = { dialog = null },
            onDelete = {
                boxes = boxes.filter { it.id != d.box.id }
                dialog = null
            },
        )
    }
}
