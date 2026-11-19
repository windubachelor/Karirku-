package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import kotlinx.coroutines.delay

/**
 * Modern Aesthetic In-App Browser powered by Android WebView with a floating
 * pill address header, security indicator, action menu, and floating glass bottom dock.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppWebBrowserDialog(
    title: String,
    url: String,
    onDismiss: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val normalizedUrl = remember(url) {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
        else "https://$trimmed"
    }

    var pageTitle by remember(title) { mutableStateOf(title.ifBlank { "Pratinjau Dokumen" }) }
    var currentUrl by remember(normalizedUrl) { mutableStateOf(normalizedUrl) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var copiedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(copiedBanner) {
        if (copiedBanner) {
            delay(2000)
            copiedBanner = false
        }
    }

    val isSecure = currentUrl.startsWith("https://", ignoreCase = true)
    val displayHost = remember(currentUrl) {
        try {
            Uri.parse(currentUrl).host?.removePrefix("www.") ?: currentUrl
        } catch (_: Exception) {
            currentUrl
        }
    }

    fun openExternal() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)))
        } catch (_: Exception) {
        }
    }

    fun shareLink() {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, pageTitle)
                putExtra(Intent.EXTRA_TEXT, currentUrl)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Tautan"))
        } catch (_: Exception) {
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler {
            val wv = webViewRef
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
            } else {
                onDismiss()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                webViewRef?.stopLoading()
                webViewRef?.destroy()
                webViewRef = null
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("in_app_browser_dialog"),
            color = colors.canvas
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern Rounded Editorial Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.canvas)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Subtle Drag Indicator Pill
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.borderSubtle)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tactile Close Button
                            BrowserCircleButton(
                                icon = Icons.Rounded.Close,
                                contentDescription = "Tutup pratinjau",
                                onClick = onDismiss,
                                modifier = Modifier.testTag("in_app_browser_close")
                            )

                            // Sleek Floating Address Pill
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.cardSurface,
                                border = BorderStroke(1.dp, colors.borderSubtle),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSecure) colors.emeraldSoftBg else colors.recessedSurface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSecure) Icons.Rounded.Lock else Icons.Rounded.Public,
                                            contentDescription = null,
                                            tint = if (isSecure) colors.accentEmerald else colors.textMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = pageTitle,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = displayHost,
                                            style = MonoEyebrowStyle.copy(fontSize = 9.sp, letterSpacing = 0.2.sp),
                                            color = colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // More Actions Menu Button
                            Box {
                                BrowserCircleButton(
                                    icon = Icons.Rounded.MoreVert,
                                    contentDescription = "Menu lainnya",
                                    onClick = { showMoreMenu = true },
                                    modifier = Modifier.testTag("in_app_browser_menu")
                                )
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    containerColor = colors.cardSurface,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Salin Tautan", color = colors.textPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            clipboard.setText(AnnotatedString(currentUrl))
                                            copiedBanner = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Bagikan Tautan", color = colors.textPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Share, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            shareLink()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Muat Ulang", color = colors.textPrimary) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            webViewRef?.reload()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Buka di Browser Luar", color = colors.accentAmber, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = colors.accentAmber, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            openExternal()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Slim Progress Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(colors.borderSubtle.copy(alpha = 0.5f))
                    ) {
                        if (loadProgress in 1..99) {
                            LinearProgressIndicator(
                                progress = { loadProgress / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = colors.accentAmber,
                                trackColor = Color.Transparent
                            )
                        }
                    }

                    // Embedded WebView inside softly rounded frame
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(colors.cardSurface)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false

                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val target = request?.url?.toString() ?: return false
                                            return if (target.startsWith("http://") || target.startsWith("https://")) {
                                                false
                                            } else {
                                                try {
                                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                                                } catch (_: Exception) {
                                                }
                                                true
                                            }
                                        }

                                        override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                                            super.onPageFinished(view, loadedUrl)
                                            if (!loadedUrl.isNullOrBlank()) currentUrl = loadedUrl
                                            canGoBack = view?.canGoBack() == true
                                            canGoForward = view?.canGoForward() == true
                                        }
                                    }

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            loadProgress = newProgress
                                        }

                                        override fun onReceivedTitle(view: WebView?, receivedTitle: String?) {
                                            if (!receivedTitle.isNullOrBlank() && title.isBlank()) {
                                                pageTitle = receivedTitle
                                            }
                                        }
                                    }

                                    loadUrl(normalizedUrl)
                                    webViewRef = this
                                }
                            }
                        )

                        // Tasteful Initial Loading Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = loadProgress in 1..20,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.cardSurface.copy(alpha = 0.96f),
                                border = BorderStroke(1.dp, colors.borderSubtle),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = colors.accentAmber,
                                        strokeWidth = 2.4.dp
                                    )
                                    Text(
                                        text = "Memuat $displayHost...",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Subtle bottom fade gradient behind the floating dock
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(72.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, colors.canvas.copy(alpha = 0.85f))
                                    )
                                )
                        )

                        // Copied Link Toast Banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = copiedBanner,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 80.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.heroDarkSurface,
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.emeraldBright,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Tautan berhasil disalin",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.heroOnDark
                                    )
                                }
                            }
                        }

                        // Floating Modern Control Dock
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 14.dp)
                                .modernColoredShadow(
                                    color = if (colors.isDark) Color.Black.copy(alpha = 0.55f) else Color(0xFF2B2118).copy(alpha = 0.16f),
                                    cornerRadius = 999.dp,
                                    blurRadius = 20.dp,
                                    offsetY = 8.dp
                                ),
                            shape = RoundedCornerShape(999.dp),
                            color = colors.cardSurface.copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BrowserDockIcon(
                                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Halaman sebelumnya",
                                    enabled = canGoBack,
                                    onClick = { webViewRef?.goBack() }
                                )
                                BrowserDockIcon(
                                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "Halaman berikutnya",
                                    enabled = canGoForward,
                                    onClick = { webViewRef?.goForward() }
                                )
                                BrowserDockIcon(
                                    icon = Icons.Outlined.Refresh,
                                    contentDescription = "Muat ulang",
                                    enabled = true,
                                    onClick = { webViewRef?.reload() }
                                )
                                BrowserDockIcon(
                                    icon = Icons.Outlined.ContentCopy,
                                    contentDescription = "Salin tautan",
                                    enabled = true,
                                    onClick = {
                                        clipboard.setText(AnnotatedString(currentUrl))
                                        copiedBanner = true
                                    }
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .tactileClickable(onClick = ::openExternal),
                                    shape = RoundedCornerShape(999.dp),
                                    color = colors.heroDarkSurface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.OpenInNew,
                                            contentDescription = null,
                                            tint = colors.accentAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Browser",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.heroOnDark,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowserCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.cardSurface)
            .border(1.dp, colors.borderSubtle, CircleShape)
            .tactileClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun BrowserDockIcon(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) colors.textPrimary else colors.textMuted.copy(alpha = 0.35f),
            modifier = Modifier.size(19.dp)
        )
    }
}
