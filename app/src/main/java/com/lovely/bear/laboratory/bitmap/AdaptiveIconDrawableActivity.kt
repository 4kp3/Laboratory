package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication2.ui.theme.MyApplication2Theme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.lovely.bear.laboratory.bitmap.data.AppIconLoader
import com.lovely.bear.laboratory.bitmap.data.LocalIconLoader
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.mono.BitmapMono
import com.lovely.bear.laboratory.bitmap.mono.DrawableMono
import com.lovely.bear.laboratory.bitmap.mono.Mono
import com.lovely.bear.laboratory.bitmap.utils.toSize
import com.lovely.bear.laboratory.util.pxToDp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


class AdaptiveIconDrawableActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appIconImages = AppIconLoader.loadSystemIcon()
//        val localImages = LocalIconLoader.load()
//        val images = resImages + iconImages
        val images = appIconImages

        setContent {
            MyApplication2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray
                ) {
                    LazyColumn (Modifier.fillMaxSize()){
                        items(images){
                            IconDrawableAnalyseView(icon = it)
                        }
                    }
                }
            }
        }

    }


}


@Composable
fun AdaptiveIconDrawableActivity.IconDrawableAnalyseView(icon: IconDrawableAnalyse) {
    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = icon.system.label, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        val scroll = rememberScrollState()

        Row(modifier = Modifier.horizontalScroll(scroll), horizontalArrangement = Arrangement.Start) {
            SingleImage(
                label = "系统返回",
                icon.system,
                Color.Transparent
            )


            icon.fg?.let {
                Spacer(modifier = Modifier.width(8.dp))
                SingleImage(
                    label = it.label,
                    it,
                    Color.Transparent
                )
            }


            icon.bg?.let {
                Spacer(modifier = Modifier.width(8.dp))
                SingleImage(
                    label = it.label,
                    it,
                    Color.Transparent
                )
            }

//            val mono = image.mono

            icon.userMono?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Mono(mono = DrawableMono(drawable = it, size = it.toSize(), "用户版本"))
            }
        }
    }
}

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

@Composable
fun AdaptiveIconDrawableActivity.IconWithMono(mono: Mono, bound: Boolean = false) {

}

@Composable
fun AdaptiveIconDrawableActivity.Mono(mono: Mono, bound: Boolean = false) {
    Column() {
        val monoBitmap: Bitmap?
        val monoDrawable: Drawable?
        when (mono) {
            is BitmapMono -> {
                monoBitmap = mono.bitmap
                monoDrawable = null
            }

            is DrawableMono -> {
                monoBitmap = null
                monoDrawable = mono.drawable
            }

            else -> {
                monoBitmap = null
                monoDrawable = null
                trackIcon(this, "mono全部为空！")
                return@Column
            }
        }

        Image(
            painter = if (monoBitmap != null) ColorPainter(Color.Transparent) else rememberDrawablePainter(
                drawable = monoDrawable
            ),
            modifier = Modifier
                .size(pxToDp(mono.size.width).dp, pxToDp(mono.size.height).dp)
                .drawWithContent {
                    // bg
                    drawRect(bg, colorFilter = monoBgColorFilter)
                    if (monoBitmap != null)
                        drawImage(
                            monoBitmap.asImageBitmap(),
                            colorFilter = monoFgColorFilter,
                        )
                    else drawContent()
                    // 辅助线，图标中心的方形mono区域，边长为 IconConfig.monoSizeDp = 42
                    if (bound)
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

        mono.label?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

@Composable
fun AdaptiveIconDrawableActivity.SingleImage(
    label: String = "",
    iconDrawable: IconDrawable,
    imageBackground: Color? = null
) {
    Column(modifier = Modifier.padding(all = Dp(8F)), horizontalAlignment =Alignment.CenterHorizontally ) {

        val modifier = Modifier.wrapContentSize()

        imageBackground?.let {
            modifier.background(it)
        }

        Image(
            painter = rememberDrawablePainter(drawable = iconDrawable.drawable),
            contentDescription = "content description",
            modifier = Modifier.drawWithContent {
                drawContent()
                drawRect(Color.Red,style= Stroke(width=1F))
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = iconDrawable.sizeString(),
            fontSize=12.sp ,
            lineHeight=12.sp,
            modifier = Modifier.width(100.dp)
        )
    }
}
//
//@Composable
//fun ABitmap(
//    label: String,
//    bitmap: Bitmap?,
//    emptySize: Float = IconConfig.iconSizeDp,
//    imageBackground: Color? = null
//) {
//    Column(horizontalAlignment = CenterHorizontally) {
//
//        val modifier =
//            if (bitmap == null) {
//                Modifier
//                    .size(emptySize.dp)
//            } else {
//                Modifier
//                    .wrapContentSize()
//            }
//
//        imageBackground?.let {
//            modifier.background(it)
//        }
//
//        Image(
//            painter = if (bitmap == null) ColorPainter(Color.Gray) else BitmapPainter(bitmap.asImageBitmap()),
//            contentDescription = null,
//            modifier = modifier
//                .background(Color.Green)
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = label,
//            modifier = Modifier.wrapContentSize()
//        )
//    }
//}

//@Composable
//fun IconMonoBound() {
//    androidx.compose.foundation.Image(
//        painter = ColorPainter(Color.Transparent), contentDescription = "",
//        modifier = Modifier
//            .size(IconConfig.iconSizeDp.dp, IconConfig.iconSizeDp.dp)
//            .drawWithContent {
////                drawContent()
//                drawRect(Color.Red, style = Stroke(width = 1F))
//                drawRect(
//                    Color.Red, style = Stroke(width = 1F),
//                    topLeft = center.plus(
//                        Offset(
//                            -IconConfig.monoSizePx.toFloat() / 2,
//                            -IconConfig.monoSizePx.toFloat() / 2
//                        )
//                    ),
//                    size = Size(
//                        IconConfig.monoSizePx.toFloat(),
//                        IconConfig.monoSizePx.toFloat()
//                    ),
//                )
//                drawCircle(
//                    color = Color.Red, style = Stroke(width = 1F),
//                    radius = IconConfig.monoOuterCircleRadiusPx.toFloat(),
//                )
//            }
//    )
//}
