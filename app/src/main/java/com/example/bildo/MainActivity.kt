package com.example.bildo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bildo.data.LiteRtLandmarkClassifier
import com.example.bildo.domain.Classification
import com.example.bildo.presentation.CameraPreview
import com.example.bildo.presentation.LandmarkImageAnalyzer
import com.example.bildo.ui.theme.BildoTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
        setContent {
            BildoTheme {
                var classifications by remember { mutableStateOf(emptyList<Classification>()) }

                val analyzer = remember {
                    LandmarkImageAnalyzer(
                        classifier = LiteRtLandmarkClassifier(context = applicationContext),
                        onResults = { classifications = it }
                    )
                }

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(controller, Modifier.fillMaxSize())
                    //CameraPreview(controller, Modifier.fillMaxSize())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    ) {
                        classifications.forEach {
                            LandmarkText(it.name)

                        }
                    }
                }

            }
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    @Composable
    fun LandmarkText(landmark: String) {
        Column (
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            VignetteText(
                text = "$landmark",
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp, vertical = 60.dp),
                tailPosition = TailPosition.BOTTOM
            )
        }
    }

    @Composable
    fun VignetteText(
        text: String,
        modifier: Modifier = Modifier,
        tailPosition: TailPosition = TailPosition.BOTTOM,
        contentAlignment: Alignment = Alignment.Center
    ) {
        var textSize by remember {mutableStateOf(IntSize.Zero)}
        Box(
            modifier = modifier
                .onGloballyPositioned { textSize = it.size },
//                .drawBehind {
//                    drawVignette(size, tailPosition = tailPosition)
//                },
            contentAlignment = contentAlignment
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .drawBehind {
                        drawVignette(
                            size = Size( textSize.width.toFloat() +10, textSize.height.toFloat() +80),
                            tailPosition = tailPosition
                        )
                    }
                    //.padding(16.dp)
            )
        }
    }





    fun DrawScope.drawVignette(
        size: Size,
        cornerRadius: Dp = 10.dp,
        tailWidth: Dp = 20.dp,
        tailHeight: Dp = 30.dp,
        tailOffset: Dp = 30.dp,
        tailPosition: TailPosition = TailPosition.BOTTOM,

    ) {
        val cornerRadiusPx = cornerRadius.toPx()
        val tailWidthPx = tailWidth.toPx()
        val tailHeightPx = tailHeight.toPx()
        val tailOffsetPx = tailOffset.toPx()

        val path = Path().apply {
            when (tailPosition) {
                TailPosition.BOTTOM -> {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(Offset.Zero, Size(size.width, size.height - tailHeightPx)),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                    moveTo(tailOffsetPx, size.height - tailHeightPx)
                    lineTo(tailOffsetPx + (tailWidthPx / 2), size.height)
                    lineTo(tailOffsetPx + tailWidthPx, size.height - tailHeightPx)
                }

                TailPosition.TOP -> {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                Offset(0f, tailHeightPx),
                                Size(size.width, size.height - tailHeightPx)
                            ),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                    moveTo(tailOffsetPx, tailHeightPx)
                    lineTo(tailOffsetPx + (tailWidthPx / 2), 0f)
                    lineTo(tailOffsetPx + tailWidthPx, tailHeightPx)
                }

                TailPosition.LEFT -> {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                Offset(tailHeightPx, 0f),
                                Size(size.width - tailHeightPx, size.height)
                            ),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                    moveTo(tailHeightPx, tailOffsetPx)
                    lineTo(0f, tailOffsetPx + (tailWidthPx / 2))
                    lineTo(tailHeightPx, tailOffsetPx + tailWidthPx)
                }

                TailPosition.RIGHT -> {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(Offset.Zero, Size(size.width - tailHeightPx, size.height)),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                    moveTo(size.width - tailHeightPx, tailOffsetPx)
                    lineTo(size.width, tailOffsetPx + (tailWidthPx / 2))
                    lineTo(size.width - tailHeightPx, tailOffsetPx + tailWidthPx)
                }
            }
        }
        drawPath(path = path, color = Color.White)
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
        )
    }
}

enum class TailPosition {
    TOP, BOTTOM, LEFT, RIGHT
}
// @Composable
// fun Greeting(name: String, modifier: Modifier = Modifier) {
//     Text(
//         text = "Hello $name!",
//         modifier = modifier
//     )
// }

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview() {
//     BildoTheme {
//         Greeting("Android")
//     }
// }

