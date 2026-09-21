package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.FileMetadata

data class ModelStats(
    val polygonCount: Int = 0,
    val textureMemoryBytes: Long = 0,
    val formatVersion: String = "Unknown"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ModelViewScreen(modelUri: Uri, cachedMetadata: FileMetadata? = null) {
    val context = LocalContext.current
    var modelStats by remember { mutableStateOf<ModelStats?>(null) }
    
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webViewClient = WebViewClient()
            
            // Add bridge for JS to communicate stats
            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun updateStats(polys: Int, mem: Long, ver: String) {
                    modelStats = ModelStats(polys, mem, ver)
                }
            }, "AndroidBridge")
            
            // Load local HTML
            loadUrl("file:///android_asset/3d_viewer.html")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
        
        // Stats Overlay
        modelStats?.let { stats ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Text("Polygons: ${stats.polygonCount}", color = Color.White)
                Text("Texture Memory: ${stats.textureMemoryBytes / 1024} KB", color = Color.White)
                Text("Format Version: ${stats.formatVersion}", color = Color.White)
            }
        }
    }
}
