package com.buvettefacile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import kotlin.math.min
import kotlin.math.roundToInt

private class Geometry(canvas: Size, imageWidth: Int, imageHeight: Int) {
    val scale = min(canvas.width / imageWidth, canvas.height / imageHeight)
    val width = imageWidth * scale
    val height = imageHeight * scale
    val left = (canvas.width - width) / 2f
    val top = (canvas.height - height) / 2f

    fun toNormalized(p: Offset) = Offset(
        ((p.x - left) / width).coerceIn(0f, 1f),
        ((p.y - top) / height).coerceIn(0f, 1f),
    )

    fun toScreen(l: Float, t: Float, r: Float, b: Float) =
        Rect(left + l * width, top + t * height, left + r * width, top + b * height)
}

@Composable
fun PosterCanvas(
    bitmap: ImageBitmap,
    boxes: List<PriceBox>,
    modifier: Modifier = Modifier,
    highlightedId: Long? = null,
    counts: Map<Long, Int> = emptyMap(),
    draft: Rect? = null,
    onTap: ((Offset) -> Unit)? = null,
    onDragStart: ((Offset) -> Unit)? = null,
    onDrag: ((Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val labelStyle = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        shadow = Shadow(Color.Black, blurRadius = 6f),
    )
    val badgeStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    val badgeRadius = with(LocalDensity.current) { 12.dp.toPx() }

    Canvas(
        modifier
            .background(Color(0xFF202020))
            .pointerInput(bitmap) {
                detectTapGestures { pos ->
                    currentOnTap?.invoke(Geometry(size.toSize(), bitmap.width, bitmap.height).toNormalized(pos))
                }
            }
            .pointerInput(bitmap) {
                detectDragGestures(
                    onDragStart = { pos ->
                        currentOnDragStart?.invoke(Geometry(size.toSize(), bitmap.width, bitmap.height).toNormalized(pos))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentOnDrag?.invoke(Geometry(size.toSize(), bitmap.width, bitmap.height).toNormalized(change.position))
                    },
                    onDragEnd = { currentOnDragEnd?.invoke() },
                    onDragCancel = { currentOnDragEnd?.invoke() },
                )
            }
    ) {
        val geom = Geometry(size, bitmap.width, bitmap.height)
        drawImage(
            bitmap,
            dstOffset = IntOffset(geom.left.roundToInt(), geom.top.roundToInt()),
            dstSize = IntSize(geom.width.roundToInt(), geom.height.roundToInt()),
        )

        for (box in boxes) {
            val r = geom.toScreen(box.left, box.top, box.right, box.bottom)
            val fill = if (box.id == highlightedId) Color(0xCCFFC107) else Color(0x6633B5E5)
            drawRect(fill, r.topLeft, r.size)
            drawRect(Color.White, r.topLeft, r.size, style = Stroke(3f))
            val layout = textMeasurer.measure(box.displayText(), labelStyle)
            drawText(
                layout,
                topLeft = Offset(r.center.x - layout.size.width / 2f, r.center.y - layout.size.height / 2f),
            )
            val count = counts[box.id] ?: 0
            if (count > 0) {
                val center = Offset(r.left + badgeRadius, r.top + badgeRadius)
                drawCircle(Color(0xFFD32F2F), badgeRadius, center)
                drawCircle(Color.White, badgeRadius, center, style = Stroke(2f))
                val badge = textMeasurer.measure(count.toString(), badgeStyle)
                drawText(badge, topLeft = Offset(center.x - badge.size.width / 2f, center.y - badge.size.height / 2f))
            }
        }

        draft?.let { d ->
            val r = geom.toScreen(d.left, d.top, d.right, d.bottom)
            drawRect(Color(0x33FFFFFF), r.topLeft, r.size)
            drawRect(
                Color.Yellow, r.topLeft, r.size,
                style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
            )
        }
    }
}
