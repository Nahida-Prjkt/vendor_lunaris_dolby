/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lunaris.dolby.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.lunaris.dolby.R
import java.net.HttpURLConnection
import java.net.URL

data class Contributor(
    val name: String,
    val githubUsername: String,
    val contribution: String,
    val isHighlighted: Boolean = false
)

data class Translator(
    val name: String,
    val githubUsername: String = name
)

data class TranslationEntry(
    val language: String,
    val translators: List<Translator>
)

private fun t(name: String, githubUsername: String = name) = Translator(name, githubUsername)

private fun translation(language: String, vararg translators: Translator) =
    TranslationEntry(language, translators.toList())

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreditsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val repoUrl = "https://github.com/Pong-Development/hardware_dolby"

    val mainContributors = listOf(
        Contributor(
            name = "Ghost",
            githubUsername = "Ghosuto",
            contribution = "Rewrite Dolby in Compose (Lunaris Dolby)",
            isHighlighted = true
        ),
        Contributor(
            name = "Adithya R",
            githubUsername = "adithya2306",
            contribution = "AOSPA Dolby Manager (Initial Code)",
            isHighlighted = true
        ),
        Contributor(
            name = "Kenway",
            githubUsername = "kenway214",
            contribution = "Base & Treble Changes, EQ Tuning",
            isHighlighted = true
        ),
        Contributor(
            name = "tranQuila",
            githubUsername = "MrTopia",
            contribution = "Adding per-device dolby state memory",
            isHighlighted = true
        ),
        Contributor(
            name = "Pablo Escobar",
            githubUsername = "pabloescobar-reborn",
            contribution = "AutoEQ headphone correction profiles",
            isHighlighted = true
        )
    )

    val translationEntries = listOf(
        translation("Spanish (Spain)", t("sm6150-dreams")),
        translation("Portuguese (Brazil)", t("SMarcosS", "S-Marcos-S")),
        translation("Indonesian", t("Alhaidar Latif", "zylhdrXP")),
        translation("Polish", t("Kacper", "ziomek3120"), t("rehork")),
        translation("Traditional Chinese", t("DenlNister", "nnn950711")),
        translation("Turkish", t("Ümit Taylan", "jinetty")),
        translation("Russian", t("Dmitry", "dkpost3")),
        translation("Persian", t("Arman Altafi", "Arman-ATI"))
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        )
                ) {
                    FilledTonalIconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.dolby_close)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Credits & Contributors",
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Thank you to all contributors who made this project possible!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    item {
                        val repoInteraction = remember { MutableInteractionSource() }
                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                                context.startActivity(intent)
                            },
                            interactionSource = repoInteraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressivePress(repoInteraction, scaleDown = 0.97f),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Code,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "View on GitHub",
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "hardware_dolby",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Main Contributors",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    items(mainContributors) { contributor ->
                        ContributorCard(contributor = contributor)
                    }

                    item {
                        Text(
                            text = "Translation Contributors",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    items(translationEntries.chunked(2)) { rowEntries ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowEntries.forEach { entry ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CompactTranslationCard(entry = entry)
                                }
                            }
                            if (rowEntries.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(42.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Translate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Missing your language?",
                                            style = MaterialTheme.typography.titleMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Help us make Lunaris Dolby better in your language! Submit or update translations on GitHub.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$repoUrl/pulls"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Submit a Pull Request",
                                        style = MaterialTheme.typography.labelLargeEmphasized
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "And all other contributors",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Check the GitHub repository for the complete list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(0.55f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.dolby_close),
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorCard(
    contributor: Contributor
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/${contributor.githubUsername}"
    val interactionSource = remember { MutableInteractionSource() }

    val onContainer = if (contributor.isHighlighted) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val onContainerMuted = if (contributor.isHighlighted) {
        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val linkColor = if (contributor.isHighlighted) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            context.startActivity(intent)
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .expressivePress(interactionSource, scaleDown = 0.97f),
        shape = if (contributor.isHighlighted) {
            MaterialTheme.shapes.large
        } else {
            MaterialTheme.shapes.medium
        },
        color = if (contributor.isHighlighted) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = onContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GithubAvatar(
                username = contributor.githubUsername,
                modifier = Modifier.size(48.dp),
                iconSize = 26.dp,
                shape = RoundedCornerShape(16.dp),
                isHighlighted = contributor.isHighlighted
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contributor.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = onContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = linkColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${contributor.githubUsername}",
                        style = MaterialTheme.typography.labelMedium,
                        color = linkColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contributor.contribution,
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainerMuted
                )
            }

            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = onContainerMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CompactTranslationCard(
    entry: TranslationEntry
) {
    val context = LocalContext.current
    val translators = entry.translators
    var currentIndex by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(translators.size) {
        if (translators.size > 1) {
            while (isActive) {
                delay(3500)
                currentIndex = (currentIndex + 1) % translators.size
            }
        }
    }

    val currentTranslator = translators.getOrElse(currentIndex) { translators.first() }
    val githubUrl = "https://github.com/${currentTranslator.githubUsername}"

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            context.startActivity(intent)
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .expressivePress(interactionSource, scaleDown = 0.96f),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StackedAvatars(
                    translators = translators,
                    currentIndex = currentIndex
                )
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val motionScheme = MaterialTheme.motionScheme
            AnimatedContent(
                targetState = currentTranslator,
                transitionSpec = {
                    (slideInVertically(motionScheme.defaultSpatialSpec()) { height -> height } +
                        fadeIn(motionScheme.defaultEffectsSpec()))
                        .togetherWith(
                            slideOutVertically(motionScheme.defaultSpatialSpec()) { height -> -height } +
                                fadeOut(motionScheme.fastEffectsSpec())
                        )
                },
                label = "TranslatorInfoAnimation"
            ) { target ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = target.name,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${target.githubUsername}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = entry.language,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StackedAvatars(
    translators: List<Translator>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val count = translators.size
    val avatarSize = 36.dp
    val stepSpacing = if (count <= 1) {
        0.dp
    } else {
        val maxAvailableSpread = 44.dp
        val calculated = maxAvailableSpread / (count - 1)
        if (calculated < 18.dp) calculated else 18.dp
    }
    val totalWidth = avatarSize + (stepSpacing * (count - 1).coerceAtLeast(0))

    Box(
        modifier = modifier
            .height(avatarSize)
            .width(totalWidth),
        contentAlignment = Alignment.CenterStart
    ) {
        translators.forEachIndexed { index, translator ->
            val isActive = index == currentIndex
            val animatedScale by animateFloatAsState(
                targetValue = if (isActive || count == 1) 1f else 0.82f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "avatarScale_$index"
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isActive || count == 1) 1f else 0.65f,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "avatarAlpha_$index"
            )
            val zIndex = if (isActive) 10f else (count - index).toFloat()
            val offsetX = stepSpacing * index

            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .zIndex(zIndex)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        alpha = animatedAlpha
                    }
                    .border(
                        width = if (count > 1) 1.5.dp else 0.dp,
                        color = ringColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                GithubAvatar(
                    username = translator.githubUsername,
                    modifier = Modifier.size(avatarSize),
                    iconSize = 20.dp,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

private val avatarCache = LruCache<String, Bitmap>(30)

private suspend fun fetchGithubAvatar(username: String): Bitmap? = withContext(Dispatchers.IO) {
    if (username.isBlank()) return@withContext null
    avatarCache.get(username)?.let { return@withContext it }

    var connection: HttpURLConnection? = null
    try {
        val url = URL("https://github.com/$username.png?size=96")
        connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 10000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Lunaris-Dolby/1.0")

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    avatarCache.put(username, bitmap)
                    bitmap
                } else null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    } finally {
        connection?.disconnect()
    }
}

@Composable
private fun GithubAvatar(
    username: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    shape: Shape = RoundedCornerShape(14.dp),
    isHighlighted: Boolean = false
) {
    val bitmapState = produceState<Bitmap?>(initialValue = avatarCache.get(username), username) {
        if (value == null) {
            value = fetchGithubAvatar(username)
        }
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = if (isHighlighted) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
