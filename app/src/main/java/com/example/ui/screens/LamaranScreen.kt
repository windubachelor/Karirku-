package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.data.ApplicationStatus
import com.example.data.JobApplication
import com.example.ui.components.ExpressiveEmptyStateCard
import com.example.ui.components.JobApplicationCard
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.ui.theme.UnboundedFontFamily
import com.example.viewmodel.KarirKuDashboardState

@Composable
fun LamaranScreen(
    state: KarirKuDashboardState,
    searchQuery: String,
    selectedFilter: ApplicationStatus?,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (ApplicationStatus?) -> Unit,
    onOpenApplicationDetail: (JobApplication) -> Unit,
    onToggleStar: (JobApplication) -> Unit,
    onAddNewDossierClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KarirKuThemeTokens.colors

    val filteredApps = remember(state.applications, searchQuery, selectedFilter) {
        val q = searchQuery.trim().lowercase()
        state.applications.filter { app ->
            val matchesStage = selectedFilter == null || app.statusEnum == selectedFilter
            val matchesQuery = q.isEmpty() ||
                app.roleTitle.lowercase().contains(q) ||
                app.companyName.lowercase().contains(q) ||
                app.location.lowercase().contains(q) ||
                app.notes.lowercase().contains(q)
            matchesStage && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lamaran_lazy_column"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Section Header + Quick Add Dossier Button
        item(key = "lamaran_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DOSSIER LAMARAN",
                        style = MonoEyebrowStyle,
                        color = colors.accentAmber
                    )
                    Text(
                        text = "Semua Lamaran",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = UnboundedFontFamily,
                            fontSize = 21.sp
                        ),
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.heroDarkSurface,
                    contentColor = colors.heroOnDark,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                    modifier = Modifier
                        .modernColoredShadow(
                            color = colors.heroDarkSurface.copy(alpha = 0.30f),
                            cornerRadius = 14.dp,
                            blurRadius = 14.dp,
                            offsetY = 6.dp
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .tactileClickable(onClick = onAddNewDossierClick)
                        .testTag("lamaran_add_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = colors.accentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Tambah",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.heroOnDark
                        )
                    }
                }
            }
        }

        // Editorial Search Input with soft ambient shadow
        item(key = "lamaran_search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .modernColoredShadow(
                        color = if (colors.isDark) Color.Black.copy(alpha = 0.38f) else Color(0xFF2B2118).copy(alpha = 0.08f),
                        cornerRadius = 16.dp,
                        blurRadius = 14.dp,
                        offsetY = 6.dp
                    )
                    .testTag("lamaran_search_input"),
                placeholder = {
                    Text(
                        text = "Cari peran, perusahaan, atau lokasi...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colors.textMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Hapus pencarian",
                                tint = colors.textMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardSurface,
                    unfocusedContainerColor = colors.cardSurface,
                    focusedBorderColor = colors.textPrimary,
                    unfocusedBorderColor = colors.borderSubtle,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )
        }

        // Stage Filter Strip
        item(key = "lamaran_filters") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isAll = selectedFilter == null
                    Surface(
                        modifier = Modifier
                            .then(
                                if (isAll) {
                                    Modifier.modernColoredShadow(
                                        color = colors.heroDarkSurface.copy(alpha = 0.24f),
                                        cornerRadius = 12.dp,
                                        blurRadius = 10.dp,
                                        offsetY = 4.dp
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { onSelectFilter(null) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAll) colors.heroDarkSurface else colors.cardSurface,
                        border = BorderStroke(1.dp, if (isAll) colors.heroDarkSurface else colors.borderSubtle)
                    ) {
                        Text(
                            text = "Semua (${state.applications.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (isAll) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
                items(ApplicationStatus.entries) { st ->
                    val count = state.applications.count { it.statusEnum == st }
                    val selected = selectedFilter == st
                    Surface(
                        modifier = Modifier
                            .then(
                                if (selected) {
                                    Modifier.modernColoredShadow(
                                        color = colors.heroDarkSurface.copy(alpha = 0.24f),
                                        cornerRadius = 12.dp,
                                        blurRadius = 10.dp,
                                        offsetY = 4.dp
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .tactileClickable { onSelectFilter(if (selected) null else st) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) colors.heroDarkSurface else colors.cardSurface,
                        border = BorderStroke(1.dp, if (selected) colors.heroDarkSurface else colors.borderSubtle)
                    ) {
                        Text(
                            text = "${st.shortLabel} ($count)",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (selected) colors.heroOnDark else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        // Application List
        if (filteredApps.isEmpty()) {
            item(key = "lamaran_empty") {
                ExpressiveEmptyStateCard(
                    eyebrow = "PIPELINE BERKAS",
                    title = if (searchQuery.isNotBlank()) "Pencarian Tidak Ditemukan" else "Belum Ada Dossier Lamaran",
                    description = if (searchQuery.isNotBlank())
                        "Tidak ada lamaran yang cocok dengan \"$searchQuery\". Coba kata kunci lain."
                    else
                        "Buka formulir Dossier untuk mencatat posisi baru, rentang gaji, jadwal respon, dan berkas lamaranmu.",
                    ctaLabel = if (searchQuery.isNotBlank()) "Reset Pencarian" else "+ Buka Formulir Dossier",
                    onCtaClick = {
                        if (searchQuery.isNotBlank()) onSearchQueryChange("") else onAddNewDossierClick()
                    }
                )
            }
        } else {
            items(filteredApps, key = { it.id }) { app ->
                JobApplicationCard(
                    application = app,
                    onClick = { onOpenApplicationDetail(app) },
                    onToggleStar = { onToggleStar(app) }
                )
            }
        }
    }
}
