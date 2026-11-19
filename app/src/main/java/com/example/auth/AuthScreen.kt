package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.KarirKuBrandBadge
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var contentVisible by remember { mutableStateOf(false) }
    var showAuthHelpSheet by remember { mutableStateOf(false) }

    // Silent auto sign-in runs non-blockingly so the user can always tap the button immediately
    LaunchedEffect(Unit) {
        contentVisible = true
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Editorial Masthead
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KarirKuBrandBadge(size = 44.dp)
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = UnboundedFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.editorial_dossier_tag),
                            style = MonoEyebrowStyle,
                            color = colors.accentAmber
                        )
                    }
                }
                TactileRaisedCard(
                    cornerRadius = 12.dp,
                    lipDepth = 2.dp,
                    modifier = Modifier.width(106.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = colors.accentEmerald, size = 6.dp)
                        Text(
                            text = "CLOUD",
                            style = MonoEyebrowStyle,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Atmospheric Obsidian Hero Dossier Card with tactile 3D shadow
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
            ) {
                AtmosphericDarkCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.accentAmber.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "[ PIPELINE KARIER // 2026 ]",
                                style = MonoEyebrowStyle,
                                color = colors.accentAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Text(
                            text = "Arsitektur Kariermu\ndalam Satu Dossier.",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = UnboundedFontFamily,
                                lineHeight = 30.sp
                            ),
                            color = colors.heroOnDark
                        )

                        Text(
                            text = "Pantau funnel lamaran, jadwal interview HR & User, negosiasi offer, dan portofolio kariermu dalam satu ruang kerja modern.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.heroOnDarkMuted
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PulsingStatusDot(color = colors.emeraldBright, size = 7.dp)
                                Text(
                                    text = "1-Ketuk Masuk Aman",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.heroOnDark
                                )
                            }
                            Text(
                                text = "SINKRONISASI CLOUD",
                                style = MonoEyebrowStyle.copy(fontSize = 9.sp),
                                color = colors.heroOnDarkMuted
                            )
                        }
                    }
                }
            }

            // Raised Tactile Feature Cards (Gojek-style tactile icons & shadows)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthFeatureRow(
                    icon = Icons.Outlined.Insights,
                    iconTint = colors.accentAmber,
                    iconBg = colors.amberSoftBg,
                    title = "Pipeline Seleksi Realtime",
                    subtitle = "Pantau rasio konversi dari Wishlist, Screening, Interview hingga Offer."
                )
                AuthFeatureRow(
                    icon = Icons.Outlined.CalendarMonth,
                    iconTint = colors.accentBlue,
                    iconBg = colors.blueSoftBg,
                    title = "Agenda Interview Terstruktur",
                    subtitle = "Jadwal wawancara, tautan meeting, pewawancara, dan catatan persiapan."
                )
                AuthFeatureRow(
                    icon = Icons.Outlined.FolderShared,
                    iconTint = colors.accentEmerald,
                    iconBg = colors.emeraldSoftBg,
                    title = "Dossier Pelamar & Portofolio",
                    subtitle = "Kelola versi CV ATS, link portofolio, sertifikasi, dan ekspektasi gaji."
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                TactileRaisedCard(
                    containerColor = colors.crimsonSoftBg,
                    borderColor = colors.crimsonBorder,
                    lipColor = colors.crimsonBorder,
                    cornerRadius = 16.dp,
                    lipDepth = 2.dp
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accentCrimson,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Raised 3D Primary Google Sign-In CTA
            TactileRaisedButton(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_sign_in_button"),
                containerColor = colors.heroDarkSurface,
                contentColor = colors.heroOnDark,
                lipColor = Color(0xFF040507),
                glowColor = colors.heroDarkSurface.copy(alpha = 0.36f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.accentAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Login,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.cta_sign_in_google),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Friendly explanation & help sheet button about sign-in options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.accentEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tanpa perlu mengingat kata sandi • Data terenkripsi",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { showAuthHelpSheet = true },
                    modifier = Modifier.testTag("auth_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Tentang Metode Masuk & Keamanan Akun",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }

    if (showAuthHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAuthHelpSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.cardSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.emeraldSoftBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = colors.accentEmerald
                        )
                    }
                    Column {
                        Text("KEAMANAN AKUN", style = MonoEyebrowStyle, color = colors.accentAmber)
                        Text(
                            text = "Masuk Cepat dengan Google",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                    }
                }
                Text(
                    text = "KarirKu menggunakan Google Sign-In terverifikasi agar data lamaran, kompensasi, dan jadwal wawancara kamu tersinkronisasi otomatis ke cloud dan terisolasi secara privat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.recessedSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Text(
                        text = "Catatan Pengembang: Penyedia autentikasi Email/Password memerlukan aktivasi manual melalui Firebase Console (Authentication → Sign-in method) pada proyek Firebase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                TactileRaisedButton(
                    onClick = { showAuthHelpSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mengerti", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    val colors = KarirKuThemeTokens.colors
    TactileRaisedCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        lipDepth = 2.5.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(iconBg, iconBg.copy(alpha = 0.7f))
                        )
                    )
                    .border(1.dp, iconTint.copy(alpha = 0.22f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}
