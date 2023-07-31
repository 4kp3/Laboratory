package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.MyApplication2Theme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.lovely.bear.laboratory.bitmap.data.AppIconLoader
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.mono.BitmapMono
import com.lovely.bear.laboratory.bitmap.mono.DrawableMono
import com.lovely.bear.laboratory.bitmap.mono.Mono
import com.lovely.bear.laboratory.bitmap.utils.dpSize
import com.lovely.bear.laboratory.bitmap.utils.toSize
import com.lovely.bear.laboratory.bitmap.utils.typeDesc
import com.lovely.bear.laboratory.util.pxToDp


class AdaptiveIconDrawableActivity : ComponentActivity() {

    val fg = Color(
        red = 0.1f, green = 0.1f, blue = 0.1f, alpha = 1f,
        ColorSpaces.Srgb
    )
    val bg = Color(
        red = 1f, green = 1f, blue = 1f, alpha = 1f,
        ColorSpaces.Srgb
    )
    val monoFgColorFilter = ColorFilter.tint(fg, BlendMode.SrcIn)
    val monoBgColorFilter = ColorFilter.tint(bg, BlendMode.SrcIn)

    @OptIn(ExperimentalFoundationApi::class)
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
                    MainList(images, this)

//                    Box(modifier = Modifier.fillMaxSize().background(Color.White)){
//                        Image(
//                            painter = ColorPainter(Color.Gray),
//                            contentDescription = "",
//                            modifier = Modifier.size(100.dp,100.dp).drawWithContent {
//                                drawRect(Color.Black)
//                                drawRect(Color.Gray)
//                                ///drawRect(Color.Transparent,size = Size(this.size.width/2,this.size.height/2),blendMode=BlendMode.SrcIn)
//                                val p = androidx.compose.ui.graphics.Paint()
//                                p.colorFilter =  PorterDuffColorFilter(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.SRC_IN)
//                                drawIntoCanvas {
//                                    it.drawRect(0F,0F,100F,100F,p)
//                                }
//                            }
//                        )
//                    }

                }
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainList(images: List<IconDrawableAnalyse>, scope: AdaptiveIconDrawableActivity) {

    val onlyNotAdaptive = remember {
        mutableStateOf(false)
    }
    val totalList = remember {
        mutableStateOf(images.filter {
            if (onlyNotAdaptive.value) {
                !it.hasMonochrome
            } else true
        })
    }

    LazyColumn(Modifier.fillMaxSize()) {

        stickyHeader {
            Row {
                Text(text = "只显示未适配monochrome应用")
                Checkbox(checked = onlyNotAdaptive.value, onCheckedChange = {
                    onlyNotAdaptive.value = it
                })
            }
        }

        items(images.filter {
            if (onlyNotAdaptive.value) {
                !it.hasMonochrome
            } else true
        }) {
            scope.IconDrawableAnalyseView(icon = it)
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

        trackIcon(this, "开始绘制${icon.system.label}")

        Spacer(modifier = Modifier.height(8.dp))

        val scroll = rememberScrollState()

        Row(
            modifier = Modifier.horizontalScroll(scroll),
            horizontalArrangement = Arrangement.Start
        ) {

            icon.system.let {
                CoupleImage(label = "系统返回\n${it.drawable.typeDesc()}", icon = it)
            }

            icon.circleGreyMaterial?.let {
                CoupleImage(label = "mono原料\n${it.drawable.typeDesc()}", icon = it)
            }

            val monos = mutableListOf<Pair<String, SingleImage>>()
            icon.devMono?.let { b ->
                monos.add(Pair("开发版本", SingleImage.Drawable(b)))
            }
            icon.userMono?.let { b ->
                monos.add(Pair("用户版本", SingleImage.Drawable(b)))
            }
            if (monos.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                ImageRow(
                    label = "mono",
                    bitmaps = monos,
                )
            }
            icon.fg?.let {
                CoupleImage(label = "前景\n${it.drawable.typeDesc()}", icon = it)
            }
            icon.bg?.let {
                CoupleImage(label = "背景\n${it.drawable.typeDesc()}", icon = it)
            }
        }
    }
}

sealed class SingleImage {
    class Bitmap(val b: android.graphics.Bitmap) : SingleImage()
    class Drawable(val d: android.graphics.drawable.Drawable) : SingleImage()
}

@Composable
fun AdaptiveIconDrawableActivity.CoupleImage(
    label: String = "",
    icon: IconDrawable,
) {
    val bitmaps = mutableListOf<Pair<String, SingleImage>>()
    icon.sizedBitmap?.let { b ->
        bitmaps.add(Pair("调整尺寸", SingleImage.Bitmap(b)))
    }
    icon.originSizeBitmap?.let { b ->
        bitmaps.add(Pair("原始尺寸", SingleImage.Bitmap(b)))
    }
    if (bitmaps.isNotEmpty()) {
        Spacer(modifier = Modifier.width(8.dp))
        ImageRow(
            label = label,
            bitmaps = bitmaps,
        )
    }

}

@Composable
fun AdaptiveIconDrawableActivity.ImageRow(
    label: String = "",
    bitmaps: List<Pair<String, SingleImage>>,
    imageBackground: Color? = null
) {

    if (bitmaps.isEmpty()) return

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            modifier = Modifier.wrapContentSize(),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(modifier = Modifier.wrapContentSize()) {
            bitmaps.forEach {
                if (it.second is SingleImage.Bitmap) {
                    val b = (it.second as SingleImage.Bitmap).b
                    SingleBitmapImage(bitmap = b, label = it.first)
                } else {
                    val d = (it.second as SingleImage.Drawable).d
                    SingleImage(label = it.first, drawable = d)
                }
            }
        }
    }

}

@Composable
fun AdaptiveIconDrawableActivity.SingleBitmapImage(
    label: String = "",
    bitmap: Bitmap,
    size: android.util.Size? = null,
    imageBackground: Color? = null
) {
    Column(
        modifier = Modifier.padding(all = Dp(8F)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var drawSize = size

        if (drawSize == null)
            drawSize = bitmap.toSize()

        val modifier = Modifier.size(
            width = pxToDp(drawSize.width).dp, height = pxToDp(drawSize.height).dp
        )

        imageBackground?.let {
            modifier.background(it)
        }

        Image(
            painter = BitmapPainter(image = bitmap.asImageBitmap()),
            contentDescription = "content description",
            modifier = modifier.drawWithContent {
                drawContent()
                drawRect(Color.Red, style = Stroke(width = 1F))
            }
        )

        Text(
            text = label,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = drawSize.dpSize(),
            fontSize = 12.sp,
            lineHeight = 12.sp,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun AdaptiveIconDrawableActivity.SingleImage(
    label: String = "",
    drawable: Drawable,
    size: android.util.Size? = null,
    imageBackground: Color? = null
) {
    Column(
        modifier = Modifier.padding(all = Dp(8F)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var drawSize = size

        if (drawSize == null)
            drawSize =
                if (drawable.bounds.width() > 0 && drawable.bounds.height() > 0) {
                    android.util.Size(
                        drawable.bounds.width(),
                        drawable.bounds.height()
                    )
                } else if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                    android.util.Size(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight
                    )
                } else {
                    android.util.Size(IconConfig.iconSizePx, IconConfig.iconSizePx)
                }

        val modifier =
            Modifier.size(
                width = pxToDp(drawSize.width).dp, height = pxToDp(drawSize.height).dp
            )

        imageBackground?.let {
            modifier.background(it)
        }

        Image(
            painter = ColorPainter(Color.Transparent),
            contentDescription = "content description",
            modifier = modifier.drawWithContent {
                drawIntoCanvas {
                    drawable.setBounds(0, 0, drawSize.width, drawSize.height)
                    drawable.draw(it.nativeCanvas)
                }
                drawContent()
                drawRect(Color.Red, style = Stroke(width = 1F))
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = drawSize.dpSize(),
            fontSize = 12.sp,
            lineHeight = 12.sp,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun AdaptiveIconDrawableActivity.Mono(mono: Mono, bound: Boolean = false) {
    Column(modifier = Modifier.padding(8.dp)) {
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
