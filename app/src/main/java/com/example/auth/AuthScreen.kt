package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.AtmosphericDarkCard
import com.example.ui.components.KarirKuBrandBadge
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.TactileRaisedCard
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily

private enum class EmailAuthMode {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var isEmailLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var contentVisible by remember { mutableStateOf(false) }

    // Email & Password Form State
    var authMode by rememberSaveable { mutableStateOf(EmailAuthMode.LOGIN) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val anyLoading = isGoogleLoading || isEmailLoading

    fun submitEmailAuth() {
        focusManager.clearFocus()
        errorMessage = null
        infoMessage = null
        isEmailLoading = true

        if (authMode == EmailAuthMode.LOGIN) {
            signInWithEmailPassword(
                email = email,
                password = password,
                onAuthSuccess = {
                    isEmailLoading = false
                    onAuthSuccess()
                },
                onAuthError = { msg ->
                    isEmailLoading = false
                    errorMessage = msg
                },
                scope = scope
            )
        } else {
            signUpWithEmailPassword(
                fullName = fullName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                onAuthSuccess = {
                    isEmailLoading = false
                    onAuthSuccess()
                },
                onAuthError = { msg ->
                    isEmailLoading = false
                    errorMessage = msg
                },
                scope = scope
            )
        }
    }

    // Silent auto sign-in runs non-blockingly so the user can always tap Google or Email immediately
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
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    cornerRadius = 26.dp
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                lineHeight = 29.sp
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PulsingStatusDot(color = colors.emeraldBright, size = 7.dp)
                                Text(
                                    text = "Google & Email Aktif",
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

            // Raised Tactile Feature Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            // Raised 3D Primary Google Sign-In CTA
            TactileRaisedButton(
                onClick = {
                    focusManager.clearFocus()
                    errorMessage = null
                    infoMessage = null
                    isGoogleLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isGoogleLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isGoogleLoading = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isGoogleLoading = false
                        }
                    )
                },
                enabled = !anyLoading,
                isLoading = isGoogleLoading,
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

            // Editorial Divider between Google & Email/Password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.borderSubtle
                )
                Text(
                    text = "ATAU DENGAN EMAIL",
                    style = MonoEyebrowStyle.copy(fontSize = 9.5.sp),
                    color = colors.textMuted
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.borderSubtle
                )
            }

            // Raised 3D Email & Password Login / Sign-Up Card
            TactileRaisedCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.cardSurface,
                borderColor = colors.borderSubtle,
                cornerRadius = 24.dp,
                lipDepth = 3.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Segmented Login / Sign Up Switcher
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.recessedSurface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AuthModeSegmentPill(
                                label = "Masuk",
                                selected = authMode == EmailAuthMode.LOGIN,
                                onClick = {
                                    authMode = EmailAuthMode.LOGIN
                                    errorMessage = null
                                    infoMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_mode_login_tab")
                            )
                            AuthModeSegmentPill(
                                label = "Daftar",
                                selected = authMode == EmailAuthMode.SIGN_UP,
                                onClick = {
                                    authMode = EmailAuthMode.SIGN_UP
                                    errorMessage = null
                                    infoMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_mode_signup_tab")
                            )
                        }
                    }

                    // Feedback banners (Error / Success)
                    AnimatedVisibility(
                        visible = !errorMessage.isNullOrBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.crimsonSoftBg,
                            border = BorderStroke(1.dp, colors.crimsonBorder)
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.accentCrimson,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !infoMessage.isNullOrBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.emeraldSoftBg,
                            border = BorderStroke(1.dp, colors.emeraldBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.accentEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = infoMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.accentEmerald
                                )
                            }
                        }
                    }

                    // Full Name Field (Sign-Up mode only)
                    AnimatedVisibility(
                        visible = authMode == EmailAuthMode.SIGN_UP,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it.take(80)
                                errorMessage = null
                            },
                            label = { Text("Nama Lengkap") },
                            placeholder = { Text("Contoh: Dita Pratama") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = colors.textMuted
                                )
                            },
                            singleLine = true,
                            enabled = !anyLoading,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = authFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_full_name_input")
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it.take(120)
                            errorMessage = null
                        },
                        label = { Text("Alamat Email") },
                        placeholder = { Text("nama@email.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        singleLine = true,
                        enabled = !anyLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = authFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it.take(72)
                            errorMessage = null
                        },
                        label = { Text("Kata Sandi") },
                        placeholder = { Text("Minimal 6 karakter") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Lihat kata sandi",
                                    tint = colors.textMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !anyLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (authMode == EmailAuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (authMode == EmailAuthMode.LOGIN && !anyLoading) {
                                    submitEmailAuth()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = authFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    // Confirm Password Field (Sign-Up mode only)
                    AnimatedVisibility(
                        visible = authMode == EmailAuthMode.SIGN_UP,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it.take(72)
                                errorMessage = null
                            },
                            label = { Text("Konfirmasi Kata Sandi") },
                            placeholder = { Text("Ulangi kata sandi") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = colors.textMuted
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !anyLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!anyLoading) submitEmailAuth()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = authFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    // Forgot Password Link (Login mode only)
                    if (authMode == EmailAuthMode.LOGIN) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null
                                    infoMessage = null
                                    sendPasswordReset(
                                        email = email,
                                        onResetSent = { msg -> infoMessage = msg },
                                        onResetError = { msg -> errorMessage = msg },
                                        scope = scope
                                    )
                                },
                                enabled = !anyLoading,
                                modifier = Modifier.testTag("auth_forgot_password_button")
                            ) {
                                Text(
                                    text = "Lupa kata sandi?",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.accentAmber
                                )
                            }
                        }
                    }

                    // Raised 3D Email Login / Sign-Up CTA Button
                    TactileRaisedButton(
                        onClick = { submitEmailAuth() },
                        enabled = !anyLoading,
                        isLoading = isEmailLoading,
                        containerColor = if (authMode == EmailAuthMode.LOGIN) colors.accentAmber else colors.accentEmerald,
                        contentColor = Color.White,
                        lipColor = if (authMode == EmailAuthMode.LOGIN) Color(0xFFB35406) else Color(0xFF0A5C43),
                        glowColor = (if (authMode == EmailAuthMode.LOGIN) colors.accentAmber else colors.accentEmerald).copy(alpha = 0.34f),
                        cornerRadius = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_auth_submit_button")
                    ) {
                        Icon(
                            imageVector = if (authMode == EmailAuthMode.LOGIN) Icons.Rounded.Login else Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (authMode == EmailAuthMode.LOGIN) {
                                stringResource(R.string.cta_sign_in_email)
                            } else {
                                stringResource(R.string.cta_sign_up_email)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Bottom mode toggle helper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authMode == EmailAuthMode.LOGIN) "Belum punya akun?" else "Sudah punya akun?",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                        TextButton(
                            onClick = {
                                authMode = if (authMode == EmailAuthMode.LOGIN) {
                                    EmailAuthMode.SIGN_UP
                                } else {
                                    EmailAuthMode.LOGIN
                                }
                                errorMessage = null
                                infoMessage = null
                            },
                            enabled = !anyLoading
                        ) {
                            Text(
                                text = if (authMode == EmailAuthMode.LOGIN) "Daftar sekarang" else "Masuk di sini",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Security footer note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
                    text = "Data lamaran terenkripsi & tersinkronisasi di cloud",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun AuthModeSegmentPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    val bg by animateColorAsState(
        targetValue = if (selected) colors.heroDarkSurface else Color.Transparent,
        animationSpec = tween(200),
        label = "auth_seg_bg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) colors.heroOnDark else colors.textSecondary,
        animationSpec = tween(200),
        label = "auth_seg_fg"
    )

    Box(
        modifier = modifier
            .then(
                if (selected) {
                    Modifier.modernColoredShadow(
                        color = colors.heroDarkSurface.copy(alpha = 0.25f),
                        cornerRadius = 11.dp,
                        blurRadius = 10.dp,
                        offsetY = 4.dp
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .tactileClickable(pressedScale = 0.96f, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = fg
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = KarirKuThemeTokens.colors.recessedSurface,
    unfocusedContainerColor = KarirKuThemeTokens.colors.recessedSurface,
    focusedBorderColor = KarirKuThemeTokens.colors.textPrimary,
    unfocusedBorderColor = KarirKuThemeTokens.colors.borderSubtle,
    focusedTextColor = KarirKuThemeTokens.colors.textPrimary,
    unfocusedTextColor = KarirKuThemeTokens.colors.textPrimary,
    focusedLabelColor = KarirKuThemeTokens.colors.textPrimary,
    unfocusedLabelColor = KarirKuThemeTokens.colors.textMuted
)

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
