package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.network.OnlyhavenCreator
import com.example.network.OnlyhavenSavedPost
import com.example.ui.MainViewModel
import com.example.ui.ScreenState
import com.example.ui.components.AppLottieLoadingAnimation
import com.example.ui.components.AppTophead
import com.example.ui.components.NavIconType
import com.example.ui.components.OnlyhavenPreviewOverlay
import com.example.ui.components.ShimmerBrush
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalVaultPalette

enum class SavedTab {
    CREATORS,
    POSTS
}

@Composable
fun SavedCreatorsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalVaultPalette.current
    val accent = LocalAccentColor.current
    val savedCreators by viewModel.savedOnlyhavenCreators.collectAsStateWithLifecycle()
    val savedKeys by viewModel.savedCreatorKeys.collectAsStateWithLifecycle()
    val savedPosts by viewModel.savedOnlyhavenPosts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(SavedTab.CREATORS) }

    // Preview state for saved posts
    var selectedPreviewPost by remember { mutableStateOf<OnlyhavenSavedPost?>(null) }
    var initialPreviewIndex by remember { mutableIntStateOf(0) }

    BackHandler {
        if (selectedPreviewPost != null) {
            selectedPreviewPost = null
        } else {
            viewModel.navigateBack()
        }
    }

    val isLight = palette.name.equals("light", ignoreCase = true)
    val circleBorderColor = if (isLight) Color.Black else Color.White

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = palette.bg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AppTophead(
                    title = "Saved",
                    navIconType = NavIconType.BACK,
                    onNavClick = { viewModel.navigateBack() },
                    showBorder = false
                )

                // Tab Switcher Row (Creators vs Posts) in exact style of CreatorDetailsScreen tabs
                val indicatorBias by animateFloatAsState(
                    targetValue = when (selectedTab) {
                        SavedTab.CREATORS -> -1f
                        SavedTab.POSTS -> 1f
                    },
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                    label = "saved_tab_indicator_bias"
                )

                val creatorsTabColor by animateColorAsState(
                    targetValue = if (selectedTab == SavedTab.CREATORS) accent else palette.textSecondary,
                    animationSpec = tween(durationMillis = 180),
                    label = "creators_tab_color"
                )
                val postsTabColor by animateColorAsState(
                    targetValue = if (selectedTab == SavedTab.POSTS) accent else palette.textSecondary,
                    animationSpec = tween(durationMillis = 180),
                    label = "posts_tab_color"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { selectedTab = SavedTab.CREATORS }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Creators (${savedCreators.size})",
                                    fontWeight = if (selectedTab == SavedTab.CREATORS) FontWeight.Bold else FontWeight.SemiBold,
                                    color = creatorsTabColor,
                                    fontSize = 14.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { selectedTab = SavedTab.POSTS }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Posts (${savedPosts.size})",
                                    fontWeight = if (selectedTab == SavedTab.POSTS) FontWeight.Bold else FontWeight.SemiBold,
                                    color = postsTabColor,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(1f / 2f)
                                    .fillMaxHeight()
                                    .align(BiasAlignment(indicatorBias, 0f))
                                    .background(accent)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                SavedTab.CREATORS -> {
                    if (savedCreators.isEmpty()) {
                        EmptyCreatorsState(accent = accent, palette = palette, onExplore = { viewModel.navigateTo(ScreenState.Onlyhaven) })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 116.dp, start = 14.dp, end = 14.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(savedCreators, key = { "saved_${it.service}_${it.id}" }) { creator ->
                                val isSaved = savedKeys.contains("${creator.service}_${creator.id}")
                                SavedCreatorItemRow(
                                    creator = creator,
                                    palette = palette,
                                    accent = accent,
                                    isLight = isLight,
                                    circleBorderColor = circleBorderColor,
                                    isSaved = isSaved,
                                    onToggleSave = { viewModel.toggleSaveOnlyhavenCreator(creator) },
                                    onClick = { viewModel.navigateTo(ScreenState.OnlyhavenCreatorDetails(creator)) }
                                )
                            }
                        }
                    }
                }
                SavedTab.POSTS -> {
                    if (savedPosts.isEmpty()) {
                        EmptyPostsState(accent = accent, palette = palette, onExplore = { viewModel.navigateTo(ScreenState.Onlyhaven) })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 116.dp, start = 2.dp, end = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(savedPosts, key = { "saved_post_${it.id}" }) { post ->
                                SavedPostGridItem(
                                    post = post,
                                    palette = palette,
                                    onClick = {
                                        initialPreviewIndex = 0
                                        selectedPreviewPost = post
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Preview Overlay for Saved Posts
    OnlyhavenPreviewOverlay(
        parentPost = selectedPreviewPost?.mediaItem,
        creator = OnlyhavenCreator(
            id = selectedPreviewPost?.creatorId ?: "",
            name = selectedPreviewPost?.creatorName ?: "",
            displayName = selectedPreviewPost?.creatorDisplayName ?: selectedPreviewPost?.creatorName ?: "",
            service = selectedPreviewPost?.creatorService ?: "",
            profileUrl = "",
            avatarUrl = selectedPreviewPost?.creatorAvatarUrl ?: "",
            headerUrl = ""
        ),
        initialIndex = initialPreviewIndex,
        onDismiss = { selectedPreviewPost = null },
        viewModel = viewModel
    )
}

@Composable
fun EmptyCreatorsState(accent: Color, palette: com.example.ui.theme.VaultPalette, onExplore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B5C).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart_filled),
                    contentDescription = null,
                    tint = Color(0xFFFF3B5C),
                    modifier = Modifier.size(46.dp)
                )
            }
            Text(
                text = "No saved creators yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
                fontSize = 18.sp
            )
            Text(
                text = "Browse creators on Onlyhaven and tap the heart icon on any creator to save them here for quick access.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onExplore,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Explore Creators", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyPostsState(accent: Color, palette: com.example.ui.theme.VaultPalette, onExplore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B5C).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart_filled),
                    contentDescription = null,
                    tint = Color(0xFFFF3B5C),
                    modifier = Modifier.size(46.dp)
                )
            }
            Text(
                text = "No saved posts yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
                fontSize = 18.sp
            )
            Text(
                text = "Browse posts in creator profiles on Onlyhaven and tap the heart icon in preview to save posts here.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onExplore,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Explore Onlyhaven", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.SavedPostGridItem(
    post: OnlyhavenSavedPost,
    palette: com.example.ui.theme.VaultPalette,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val item = post.mediaItem
    val thumbRequest = remember(item.thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(item.thumbnailUrl.ifEmpty { item.originalUrl })
            .crossfade(true)
            .crossfade(350)
            .build()
    }
    var isLoaded by remember(item.thumbnailUrl) { mutableStateOf(false) }
    var isError by remember(item.thumbnailUrl) { mutableStateOf(false) }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isLoaded && !isError) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "saved_post_img_alpha"
    )
    val imageScale by animateFloatAsState(
        targetValue = if (isLoaded && !isError) 1f else 0.93f,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "saved_post_img_scale"
    )

    Box(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .background(palette.cardBg)
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
    ) {
        if (!isLoaded && !isError) {
            val shimmerBrush = ShimmerBrush(targetValue = 900f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.cardBg)
                    .background(shimmerBrush),
                contentAlignment = Alignment.Center
            ) {
                AppLottieLoadingAnimation(modifier = Modifier.size(28.dp))
            }
        }

        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.cardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock_error),
                    contentDescription = "Failed to load thumbnail",
                    tint = palette.textSecondary.copy(alpha = 0.45f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AsyncImage(
            model = thumbRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onSuccess = {
                isLoaded = true
                isError = false
            },
            onError = {
                isLoaded = true
                isError = true
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = imageAlpha
                    scaleX = imageScale
                    scaleY = imageScale
                }
        )

        if (item.isMultiMedia) {
            Icon(
                painter = painterResource(id = R.drawable.ic_instagram_carousel),
                contentDescription = "Multiple Items",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(20.dp)
            )
        } else if (item.kind == "video") {
            Icon(
                painter = painterResource(id = R.drawable.ic_player_play),
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun LazyGridItemScope.SavedCreatorItemRow(
    creator: OnlyhavenCreator,
    palette: com.example.ui.theme.VaultPalette,
    accent: Color,
    isLight: Boolean,
    circleBorderColor: Color,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val avatarRequest = remember(creator.avatarUrl) {
        ImageRequest.Builder(context)
            .data(creator.avatarUrl)
            .crossfade(true)
            .crossfade(350)
            .build()
    }

    var isPopAnimating by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (isPopAnimating) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_pop",
        finishedListener = { isPopAnimating = false }
    )

    Card(
        modifier = modifier
            .animateItem()
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("saved_creator_row_${creator.id}"),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLight) 2.5.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(palette.surface),
                contentAlignment = Alignment.Center
            ) {
                if (creator.avatarUrl.isNotEmpty()) {
                    var isAvatarLoaded by remember(creator.avatarUrl) { mutableStateOf(false) }
                    val avatarAlpha by animateFloatAsState(
                        targetValue = if (isAvatarLoaded) 1f else 0f,
                        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
                        label = "avatar_alpha"
                    )
                    val avatarScale by animateFloatAsState(
                        targetValue = if (isAvatarLoaded) 1f else 0.92f,
                        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
                        label = "avatar_scale"
                    )

                    if (!isAvatarLoaded) {
                        val shimmerBrush = ShimmerBrush(targetValue = 900f)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(palette.surface)
                                .background(shimmerBrush)
                        )
                    }
                    AsyncImage(
                        model = avatarRequest,
                        contentDescription = creator.name,
                        contentScale = ContentScale.Crop,
                        onSuccess = { isAvatarLoaded = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = avatarAlpha
                                scaleX = avatarScale
                                scaleY = avatarScale
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.cardBg.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = creator.name.take(2).uppercase(),
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.5.dp, circleBorderColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val label = creator.displayName.ifBlank { creator.name }
                Text(
                    text = label,
                    color = palette.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "@${creator.name}",
                    color = palette.textSecondary,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (creator.postCount > 0 || creator.imageCount > 0 || creator.videoCount > 0) {
                    val statsText = buildString {
                        if (creator.postCount > 0) append("${creator.postCount} posts")
                        if (creator.videoCount > 0) {
                            if (isNotEmpty()) append(" • ")
                            append("${creator.videoCount} videos")
                        }
                    }
                    if (statsText.isNotEmpty()) {
                        Text(
                            text = statsText,
                            color = palette.textSecondary.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(heartScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isPopAnimating = true
                        onToggleSave()
                    }
                    .testTag("toggle_save_creator_${creator.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isSaved) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    ),
                    contentDescription = if (isSaved) "Unsave Creator" else "Save Creator",
                    tint = if (isSaved) Color(0xFFFF3B5C) else palette.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
