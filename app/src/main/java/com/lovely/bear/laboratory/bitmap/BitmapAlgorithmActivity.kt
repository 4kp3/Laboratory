package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication2.ui.theme.MyApplication2Theme
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.bitmap.data.AdaptiveIconImage
import com.lovely.bear.laboratory.bitmap.data.AppIcon
import com.lovely.bear.laboratory.bitmap.data.IconImage
import com.lovely.bear.laboratory.bitmap.data.Image
import com.lovely.bear.laboratory.bitmap.data.ResImage


class BitmapAlgorithmActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val resImages = listOf(R.mipmap.icon_ios_cloud_192, R.mipmap.icon_ios_letter).map {
//            ResImage(it).also {
//                makeEdgeBitmap(it)
//            }
//        }

        val iconImages = AppIcon.getAllImages().apply {
            forEach {
//                makeEdgeBitmap(it)
                if (it is AdaptiveIconImage) {
//                    makeEdgeBitmap(it.bgBitmap)
                    makeEdgeBitmap(it.fgBitmap)
                }
            }
        }

//        val images = resImages + iconImages
        val images =  iconImages

        setContent {
            MyApplication2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    galley(images)
                }
            }
        }

    }


    private fun makeEdgeBitmap(image: Image) {
        image.edgeBitmap = ContentBounding.get(image.bitmap)
    }

}


@Composable
fun galley(images: List<Image>) {
    LazyColumn {
        items(images) {
            if (it is AdaptiveIconImage) {
                AdaptiveIconImage(it)
            } else {
                SingleImage(image = it)
            }
        }
    }
}

@Composable
fun AdaptiveIconImage(image: AdaptiveIconImage) {
    Column(modifier = Modifier.padding(all = 8.dp)) {

        Text(text = image.label)
        Spacer(modifier = Modifier.width(8.dp))

        SingleImage(
            label = "原始效果",
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

        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun SingleImage(label: String = "导入的图像", image: Image, imageBackground: Color? = null) {
    Column {

        val edgeBitmap by remember {
            mutableStateOf(image.edgeBitmap)
        }

        Row(modifier = Modifier.padding(all = Dp(8F))) {

            if (image is IconImage) {
                Text(
                    text = image.icon::class.simpleName?:"",
                    modifier = Modifier.width(100.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            ABitmap(label = label, bitmap = image.bitmap, imageBackground = imageBackground)

            if (edgeBitmap != null) {
                Spacer(modifier = Modifier.width(8.dp))
                ABitmap(label = "边缘图像", bitmap = edgeBitmap!!.bitmap)
            }

        }

        if (edgeBitmap!=null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = edgeBitmap.toString())
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