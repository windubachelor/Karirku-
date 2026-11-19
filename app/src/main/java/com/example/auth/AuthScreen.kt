package com.example.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.KarirKuBrandBadge
import com.example.ui.components.KarirKuCompactTextField
import com.example.ui.components.PulsingStatusDot
import com.example.ui.components.TactileRaisedButton
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily

private enum class AuthStep {
    WELCOME,
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

    var currentStep by rememberSaveable { mutableStateOf(AuthStep.WELCOME) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isEmailLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Email & Password Form State
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val anyLoading = isGoogleLoading || isEmailLoading

    BackHandler(enabled = currentStep != AuthStep.WELCOME && !anyLoading) {
        errorMessage = null
        infoMessage = null
        currentStep = AuthStep.WELCOME
    }

    fun triggerGoogleSignIn() {
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
    }

    fun submitEmailAuth() {
        focusManager.clearFocus()
        errorMessage = null
        infoMessage = null
        isEmailLoading = true

        if (currentStep == AuthStep.LOGIN) {
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

    // Silent auto sign-in runs non-blockingly so the user can always tap immediately
    LaunchedEffect(Unit) {
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
            .background(colors.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        // Subtle editorial geometric background arcs inspired by modern Behance onboarding
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentAmber.copy(alpha = if (colors.isDark) 0.14f else 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.92f, size.height * 0.06f),
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.92f, size.height * 0.06f)
            )
            drawCircle(
                color = colors.accentAmber.copy(alpha = if (colors.isDark) 0.08f else 0.09f),
                radius = size.width * 0.44f,
                center = Offset(size.width * 0.96f, size.height * 0.03f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentAmber.copy(alpha = if (colors.isDark) 0.08f else 0.07f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.06f, size.height * 0.92f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.06f, size.height * 0.92f)
            )
            drawCircle(
                color = colors.borderSubtle.copy(alpha = 0.55f),
                radius = size.width * 0.36f,
                center = Offset(size.width * 0.04f, size.height * 0.95f),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val forward = targetState != AuthStep.WELCOME
                if (forward) {
                    (slideInHorizontally(tween(260)) { it / 4 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { -it / 6 } + fadeOut(tween(180)))
                } else {
                    (slideInHorizontally(tween(260)) { -it / 4 } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(220)) { it / 6 } + fadeOut(tween(180)))
                }
            },
            label = "auth_step_transition",
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) { step ->
            when (step) {
                AuthStep.WELCOME -> {
                    WelcomeAuthPage(
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = errorMessage,
                        onLoginClick = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = AuthStep.LOGIN
                        },
                        onRegisterClick = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = AuthStep.SIGN_UP
                        },
                        onGoogleClick = ::triggerGoogleSignIn
                    )
                }

                AuthStep.LOGIN, AuthStep.SIGN_UP -> {
                    val isLogin = step == AuthStep.LOGIN
                    EmailAuthFormPage(
                        isLogin = isLogin,
                        fullName = fullName,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible,
                        isEmailLoading = isEmailLoading,
                        isGoogleLoading = isGoogleLoading,
                        errorMessage = errorMessage,
                        infoMessage = infoMessage,
                        onFullNameChange = { fullName = it.take(80); errorMessage = null },
                        onEmailChange = { email = it.take(120); errorMessage = null },
                        onPasswordChange = { password = it.take(72); errorMessage = null },
                        onConfirmPasswordChange = { confirmPassword = it.take(72); errorMessage = null },
                        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                        onBack = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = AuthStep.WELCOME
                        },
                        onSubmit = ::submitEmailAuth,
                        onForgotPassword = {
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
                        onSwitchMode = {
                            errorMessage = null
                            infoMessage = null
                            currentStep = if (isLogin) AuthStep.SIGN_UP else AuthStep.LOGIN
                        },
                        onGoogleClick = ::triggerGoogleSignIn
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeAuthPage(
    isGoogleLoading: Boolean,
    errorMessage: String?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Masthead
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KarirKuBrandBadge(size = 38.dp)
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = UnboundedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.editorial_dossier_tag),
                        style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                        color = colors.accentAmber
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.cardSurface,
                border = BorderStroke(1.dp, colors.borderSubtle),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PulsingStatusDot(color = colors.accentEmerald, size = 6.dp)
                    Text(
                        text = "CLOUD SYNC",
                        style = MonoEyebrowStyle.copy(fontSize = 8.5.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cute Vector Character Hero Illustration (Behance Onboarding Style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .modernColoredShadow(
                    color = colors.accentAmber.copy(alpha = if (colors.isDark) 0.16f else 0.14f),
                    cornerRadius = 32.dp,
                    blurRadius = 26.dp,
                    offsetY = 12.dp
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFFBF8F3),
            border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.8f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_auth_welcome_1795098523478),
                contentDescription = "Ilustrasi pencari kerja KarirKu",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(265.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bold Centered Headline & Warm Subtitle (Matching Reference Hierarchy)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Raih Karier\nImpianmu di Sini",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    lineHeight = 33.sp
                ),
                color = colors.accentAmber,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Catat setiap lamaran kerja, pantau jadwal interview, dan simpan portofoliomu dengan rapi di satu tempat.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        AnimatedVisibility(
            visible = !errorMessage.isNullOrBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
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

        Spacer(modifier = Modifier.height(26.dp))

        // Dual Action Row: Masuk (Terracotta Raised) + Daftar (Clean Tactile)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TactileRaisedButton(
                    onClick = onLoginClick,
                    enabled = !isGoogleLoading,
                    containerColor = colors.accentAmber,
                    contentColor = Color.White,
                    lipColor = Color(0xFFB14714),
                    glowColor = colors.accentAmber.copy(alpha = 0.36f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("auth_welcome_login_button")
                ) {
                    Text(
                        text = "Masuk",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .tactileClickable(enabled = !isGoogleLoading, onClick = onRegisterClick)
                        .testTag("auth_welcome_register_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.borderSubtle),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Daftar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Quick 1-Tap Google Option right on Welcome Screen too
            GoogleContinuePillButton(
                isLoading = isGoogleLoading,
                enabled = !isGoogleLoading,
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_google_sign_in_button")
            )
        }
    }
}

@Composable
private fun EmailAuthFormPage(
    isLogin: Boolean,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    isEmailLoading: Boolean,
    isGoogleLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onSwitchMode: () -> Unit,
    onGoogleClick: () -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    val anyLoading = isEmailLoading || isGoogleLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .modernColoredShadow(
                        color = Color(0xFF2B2118).copy(alpha = 0.08f),
                        cornerRadius = 21.dp,
                        blurRadius = 10.dp,
                        offsetY = 4.dp
                    )
                    .clip(CircleShape)
                    .background(colors.cardSurface)
                    .border(1.dp, colors.borderSubtle, CircleShape)
                    .tactileClickable(enabled = !anyLoading, onClick = onBack)
                    .testTag("auth_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            KarirKuBrandBadge(size = 36.dp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Centered Behance-Style Title & Warm Greeting
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isLogin) "Masuk di Sini" else "Buat Akun Baru",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = UnboundedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = colors.accentAmber,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isLogin) {
                    "Senang bertemu lagi!\nYuk lanjutkan progres lamaranmu."
                } else {
                    "Buat akun gratis supaya semua\ndata lamaranmu tersimpan aman."
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
        }

        // Error / Info Feedback Banners
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

        Spacer(modifier = Modifier.height(2.dp))

        // Clean Airy Compact 48dp Inputs (Matching Behance Cleanliness + KarirKu Tactile Polish)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = !isLogin,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("NAMA LENGKAP", style = MonoEyebrowStyle, color = colors.textMuted)
                    KarirKuCompactTextField(
                        value = fullName,
                        onValueChange = onFullNameChange,
                        placeholder = "Nama lengkapmu",
                        containerColor = colors.cardSurface,
                        cornerRadius = 15.dp,
                        elevatedShadow = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        enabled = !anyLoading,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_full_name_input")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("EMAIL", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "nama@email.com",
                    containerColor = colors.cardSurface,
                    cornerRadius = 15.dp,
                    elevatedShadow = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = !anyLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("KATA SANDI", style = MonoEyebrowStyle, color = colors.textMuted)
                KarirKuCompactTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "Minimal 6 karakter",
                    containerColor = colors.cardSurface,
                    cornerRadius = 15.dp,
                    elevatedShadow = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onTogglePasswordVisible),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Lihat kata sandi",
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !anyLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isLogin) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isLogin && !anyLoading) onSubmit()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )
            }

            AnimatedVisibility(
                visible = !isLogin,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("KONFIRMASI KATA SANDI", style = MonoEyebrowStyle, color = colors.textMuted)
                    KarirKuCompactTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        placeholder = "Ulangi kata sandi",
                        containerColor = colors.cardSurface,
                        cornerRadius = 15.dp,
                        elevatedShadow = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        enabled = !anyLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (!anyLoading) onSubmit() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_input")
                    )
                }
            }
        }

        // Right-aligned Forgot Password link (Login mode)
        if (isLogin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onForgotPassword,
                    enabled = !anyLoading,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.testTag("auth_forgot_password_button")
                ) {
                    Text(
                        text = "Lupa kata sandi?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.accentAmber
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Full-Width Primary Terracotta CTA Button
        TactileRaisedButton(
            onClick = onSubmit,
            enabled = !anyLoading,
            isLoading = isEmailLoading,
            containerColor = colors.accentAmber,
            contentColor = Color.White,
            lipColor = Color(0xFFB14714),
            glowColor = colors.accentAmber.copy(alpha = 0.38f),
            cornerRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_auth_submit_button")
        ) {
            Text(
                text = if (isLogin) "Masuk" else "Daftar Sekarang",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Centered Switch Mode Link (Reference style: "Create new account" / "Already have an account")
        TextButton(
            onClick = onSwitchMode,
            enabled = !anyLoading,
            modifier = Modifier.testTag(if (isLogin) "auth_mode_signup_tab" else "auth_mode_login_tab")
        ) {
            Text(
                text = if (isLogin) "Buat akun baru" else "Sudah punya akun? Masuk di sini",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // "Atau lanjutkan dengan" + Google Sign-In
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
                Text(
                    text = "Atau lanjutkan dengan",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.accentAmber
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderSubtle)
            }

            GoogleContinuePillButton(
                isLoading = isGoogleLoading,
                enabled = !anyLoading,
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_sign_in_button")
            )
        }

        // Subtle security note
        Row(
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = colors.accentEmerald,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Data lamaran tersimpan aman di cloud",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun GoogleContinuePillButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors
    Surface(
        modifier = modifier
            .height(52.dp)
            .modernColoredShadow(
                color = if (colors.isDark) Color.Black.copy(alpha = 0.35f) else Color(0xFF2B2118).copy(alpha = 0.09f),
                cornerRadius = 16.dp,
                blurRadius = 14.dp,
                offsetY = 5.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .tactileClickable(enabled = enabled && !isLoading, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardSurface,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.accentAmber,
                    strokeWidth = 2.4.dp
                )
            } else {
                GoogleGLogo(size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.cta_sign_in_google),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun GoogleGLogo(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val strokeW = this.size.minDimension * 0.21f
        val inset = strokeW / 2f
        val arcSize = Size(this.size.width - strokeW, this.size.height - strokeW)
        val topLeft = Offset(inset, inset)

        // Red top-left arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 72f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 40f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Butt)
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -5f,
            sweepAngle = 48f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Butt)
        )
        // Blue horizontal bar of 'G'
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(this.size.width * 0.52f, this.size.height * 0.50f),
            end = Offset(this.size.width * 0.96f, this.size.height * 0.50f),
            strokeWidth = strokeW,
            cap = StrokeCap.Butt
        )
    }
}
