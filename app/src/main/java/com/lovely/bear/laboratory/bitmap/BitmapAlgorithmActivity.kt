package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.example.myapplication2.ui.theme.MyApplication2Theme
import com.lovely.bear.laboratory.bitmap.data.AdaptiveIconImage
import com.lovely.bear.laboratory.bitmap.data.AppIconLoader
import com.lovely.bear.laboratory.bitmap.data.AppInfo
import com.lovely.bear.laboratory.bitmap.data.IconImage
import com.lovely.bear.laboratory.bitmap.data.Image
import com.lovely.bear.laboratory.bitmap.data.LocalIconLoader
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.mono.Mono
import com.lovely.bear.laboratory.util.dpToPx
import com.lovely.bear.laboratory.util.pxToDp


class BitmapAlgorithmActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appIconImages = AppIconLoader.load()
        val localImages = LocalIconLoader.load()
//        val images = resImages + iconImages
        val images = appIconImages

        setContent {
            MyApplication2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray
                ) {
                    Column {

                        RoundCorner()
                        galley(images)
                    }
                }
            }
        }

    }


}

@Composable
fun RoundCorner() {
    androidx.compose.foundation.Image(
        painter = ColorPainter(Color.Transparent),
        "",
        modifier = Modifier
            .size(100.dp, 100.dp)
            .drawWithContent {
                drawRoundRect(
                    Color.Green,
                    cornerRadius = CornerRadius(dpToPx(50F).toFloat(), dpToPx(25F).toFloat())
                )
            },
    )
}

@Composable
fun galley(images: List<Image>) {
    LazyColumn {
        items(images) { image ->

            when {
                image is IconImage && image.appInfo != null -> {
                    App(app = image.appInfo!!, image)
                }

                else -> {
                    // todo single image

                }
            }

            Spacer(
                modifier = Modifier
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    }
}

@Composable
fun App(app: AppInfo, image: IconImage) {
    Column() {
        Text(text = app.label, style = MaterialTheme.typography.headlineMedium)

        Text(
            text = " ${if (app.isIconAdaptive) "已" else "未"}适配",
            style = MaterialTheme.typography.bodyMedium
        )

        if (image is AdaptiveIconImage) {
            AdaptiveIconImage(image)
        } else {
            LegacyIconImage(image)
        }
    }
}

@Composable
fun LegacyIconImage(image: IconImage) {
    SingleImage("系统返回", image = image)

    val mono = image.mono

    Spacer(modifier = Modifier.width(if (mono == null) 16.dp else 8.dp))

    if (mono != null) {
        AMono(mono = mono)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun AdaptiveIconImage(image: AdaptiveIconImage) {
    Column(modifier = Modifier.padding(all = 8.dp)) {

//        Text(text = image.label)
//        Spacer(modifier = Modifier.width(8.dp))

        SingleImage(
            label = "系统返回",
            image,
            Color.Green
        )

        Spacer(modifier = Modifier.width(8.dp))

        SingleImage(
            label = "背景",
            image.bgBitmap,
            Color.Green
        )

        Spacer(modifier = Modifier.width(8.dp))

        SingleImage(
            label = "前景",
            image.fgBitmap,
            Color.Green
        )

        val mono = image.mono

        Spacer(modifier = Modifier.width(if (mono == null) 16.dp else 8.dp))

        if (mono != null) {
            AMono(mono = mono)
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun AMono(label: String = "mono", mono: Mono) {
    val bg = Color(
        red = 0.1f, green = 0.1f, blue = 0.1f, alpha = 1f,
        ColorSpaces.Srgb
    )
    val fg = Color(
        red = 1f, green = 1f, blue = 1f, alpha = 1f,
        ColorSpaces.Srgb
    )
    val monoFgColorFilter = ColorFilter.tint(fg, BlendMode.SrcIn)
    val monoBgColorFilter = ColorFilter.tint(bg, BlendMode.SrcIn)

    Row(modifier = Modifier.padding(all = Dp(8F)), verticalAlignment = CenterVertically) {

        Text(
            text = label,
            modifier = Modifier.width(80.dp)
        )

        // 生成的mono wrap大小
        Column() {
            val monoBitmap = mono.bitmap
            val monoRadius = mono.size.width / 2F
            androidx.compose.foundation.Image(
                painter = ColorPainter(Color.Transparent),
                modifier = Modifier
                    .size(pxToDp(mono.size.width).dp, pxToDp(mono.size.height).dp)
                    .drawWithContent {
                        // bg
                        drawRect(bg, colorFilter = monoBgColorFilter)
                        drawImage(
                            monoBitmap.asImageBitmap(),
                            colorFilter = monoFgColorFilter,
                        )
                        // 辅助线，图标中心的方形mono区域，边长为 IconConfig.monoSizeDp = 42
                        drawRect(
                            color = Color.Red,
                            style = Stroke(width = 1F),
                            topLeft = center.plus(
                                Offset(
                                    -IconConfig.monoSizePx.toFloat() / 2,
                                    -IconConfig.monoSizePx.toFloat() / 2
                                )
                            ),
                            size = Size(
                                IconConfig.monoSizePx.toFloat(),
                                IconConfig.monoSizePx.toFloat()
                            ),
                            blendMode = BlendMode.Plus
                        )
                    },
                contentDescription = "mono"
            )

            Text(
                text = if (mono is Mono.Original) "原始mono" else "生成",
                modifier = Modifier.width(80.dp)
            )
        }
        Column() {

            Box(contentAlignment = Alignment.Center) {


                val scale = IconConfig.iconSizePx * 1F / mono.size.width
                val bitmap = if (scale < 1F) {
                    mono.bitmap.scale(IconConfig.iconSizePx, IconConfig.iconSizePx)
                } else mono.bitmap

                androidx.compose.foundation.Image(
                    painter = BitmapPainter(mono.bitmap.asImageBitmap()),
                    modifier = Modifier
                        .size(IconConfig.iconSizeDp.dp, IconConfig.iconSizeDp.dp)
                        // mono尺寸大于图标尺寸时，是系统原始mono，包含了透明间距，需要缩放到icon大小
                        .scale(if (scale < 1F) scale else 1F)
                        .drawWithContent {
                            drawRect(bg, colorFilter = monoBgColorFilter)
                            drawImage(
                                bitmap.asImageBitmap(),
                                colorFilter = monoFgColorFilter,
                                topLeft = Offset(
                                    (IconConfig.iconSizePx - bitmap.width) / 2F,
                                    (IconConfig.iconSizePx - bitmap.height) / 2F
                                )
                            )
                        },
                    contentDescription = "mono"
                )

                IconMonoBound()
            }
            Text(
                text = "适配效果",
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
fun SingleImage(label: String = "导入的图像", image: Image, imageBackground: Color? = null) {
    Row(modifier = Modifier.padding(all = Dp(8F)), verticalAlignment = CenterVertically) {


        Text(
            text = label,
            modifier = Modifier.width(80.dp)
        )

        ABitmap(
            label = "原始图像${if (image is IconImage) image.iconType else ""}",
            bitmap = image.bitmap,
            imageBackground = imageBackground
        )

        val edgeBitmap by remember {
            Log.d("TRACK_ICON", "remember")
            mutableStateOf(image.edgeBitmap)
        }

        if (edgeBitmap != null) {
            Log.d("TRACK_ICON", "going to show edge")
            Spacer(modifier = Modifier.width(8.dp))
            ABitmap(label = "边缘图像", bitmap = edgeBitmap!!.bitmap)
        }
    }
}

@Composable
fun ABitmap(
    label: String,
    bitmap: Bitmap?,
    emptySize: Float = IconConfig.iconSizeDp,
    imageBackground: Color? = null
) {
    Column(horizontalAlignment = CenterHorizontally) {

        val modifier =
            if (bitmap == null) {
                Modifier
                    .size(emptySize.dp)
            } else {
                Modifier
                    .wrapContentSize()
            }

        imageBackground?.let {
            modifier.background(it)
        }

        Image(
            painter = if (bitmap == null) ColorPainter(Color.Gray) else BitmapPainter(bitmap.asImageBitmap()),
            contentDescription = null,
            modifier = modifier
                .background(Color.Green)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            modifier = Modifier.wrapContentSize()
        )
    }
}

@Composable
fun IconMonoBound() {
    androidx.compose.foundation.Image(
        painter = ColorPainter(Color.Transparent), contentDescription = "",
        modifier = Modifier
            .size(IconConfig.iconSizeDp.dp, IconConfig.iconSizeDp.dp)
            .drawWithContent {
//                drawContent()
                drawRect(Color.Red, style = Stroke(width = 1F))
                drawRect(
                    Color.Red, style = Stroke(width = 1F),
                    topLeft = center.plus(
                        Offset(
                            -IconConfig.monoSizePx.toFloat() / 2,
                            -IconConfig.monoSizePx.toFloat() / 2
                        )
                    ),
                    size = Size(
                        IconConfig.monoSizePx.toFloat(),
                        IconConfig.monoSizePx.toFloat()
                    ),
                )
                drawCircle(
                    color = Color.Red, style = Stroke(width = 1F),
                    radius = IconConfig.monoOuterCircleRadiusPx.toFloat(),
                )
            }
    )
}

@Preview("Light Mode")
//@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun PreviewGreeting() {
//    MyApplication2Theme {
//        // A surface container using the 'background' color from the theme
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            conversation(SampleData.conversationSample)
//        }
//    }
}