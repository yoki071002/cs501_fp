// File: app/src/main/java/com/example/cs501_fp/ui/pages/community/CommunityScreen.kt
// The social feed screen allowing users to view, search, like, and comment on public events.

package com.example.cs501_fp.ui.pages.community

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cs501_fp.data.local.entity.UserEvent
import com.example.cs501_fp.ui.components.OnCoreCard
import com.example.cs501_fp.ui.components.StaggeredEntry
import com.example.cs501_fp.ui.components.TheatricalTopBar
import com.example.cs501_fp.ui.theme.Gold
import com.example.cs501_fp.ui.theme.TicketInkColor
import com.example.cs501_fp.ui.theme.TicketPaperColor
import com.example.cs501_fp.viewmodel.CommunityViewModel
import com.example.cs501_fp.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = viewModel(),
    onProfileClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    // State
    val posts by viewModel.publicPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Bottom Sheet for Comments
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var selectedPostOwnerId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Image Zoom Logic
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }


    // --- Main UI Structure ---
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TheatricalTopBar(
                title = "Stage Door",
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Gold, modifier = Modifier.size(28.dp))
                    }
                }
            )
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchPosts(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search show or venue...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )

                FilterChip(
                    selected = sortOption == SortOption.NEWEST,
                    onClick = { viewModel.setSortOption(SortOption.NEWEST) },
                    label = { Text("Newest") },
                    leadingIcon = if (sortOption == SortOption.NEWEST) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = chipColors
                )
                FilterChip(
                    selected = sortOption == SortOption.TRENDING,
                    onClick = { viewModel.setSortOption(SortOption.TRENDING) },
                    label = { Text("Trending") },
                    leadingIcon = if (sortOption == SortOption.TRENDING) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = chipColors
                )
                FilterChip(
                    selected = sortOption == SortOption.MINE,
                    onClick = { viewModel.setSortOption(SortOption.MINE) },
                    label = { Text("My Posts") },
                    leadingIcon = if (sortOption == SortOption.MINE) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = chipColors
                )
            }
            // Feed Content
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (posts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No posts found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (sortOption == SortOption.MINE) {
                            Text("Go to your Ticket Wallet to post!", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("Be the first to share!", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(items = posts, key = { _, post -> post.id }) { index, post ->
                        StaggeredEntry(index = index) {
                            CommunityPostCard(
                                post = post,
                                viewModel = viewModel,
                                onImageClick = { url -> viewingImageUrl = url },
                                onCommentClick = {
                                    selectedPostId = post.id
                                    selectedPostOwnerId = post.ownerId
                                    showBottomSheet = true
                                },
                                onUserClick = { onUserClick(post.ownerId) }
                            )
                        }
                    }
                }
            }
        }
        // Comment Bottom Sheet
        if (showBottomSheet && selectedPostId != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                CommentSection(
                    eventId = selectedPostId!!,
                    postOwnerId = selectedPostOwnerId ?: "",
                    viewModel = viewModel
                )
            }
        }
        // Full Screen Image Dialog
        if (viewingImageUrl != null) {
            Dialog(onDismissRequest = { viewingImageUrl = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewingImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewingImageUrl = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }

                    AsyncImage(
                        model = viewingImageUrl,
                        contentDescription = "Full Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}


// --- Post Card Components (single social feed item) ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommunityPostCard(
    post: UserEvent,
    viewModel: CommunityViewModel = viewModel(),
    onImageClick: (String) -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val currentUserId = viewModel.currentUserId
    val isLiked = post.likedBy.contains(currentUserId)
    val likeCount = post.likedBy.size

    OnCoreCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.ownerAvatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.ownerAvatarUrl)
                            .crossfade(true)
                            .size(100, 100)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onUserClick() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onUserClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.ownerName.take(1).uppercase().ifBlank { "?" },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.clickable { onUserClick() }) {
                    Text(
                        text = post.ownerName.ifBlank { "Anonymous" },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getRelativeTimeDisplay(post.dateText),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "@ ${post.venue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(8.dp))

            val content = post.publicReview.ifBlank { post.notes }
            if (content.isNotBlank()) {
                ExpandableText(text = content)
            }

            Spacer(Modifier.height(12.dp))

            val displayImage = post.publicImageUrls.firstOrNull() ?: post.officialImageUrl
            if (displayImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .size(600, 400)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(displayImage) },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.toggleLike(post) }) {
                    Icon(
                        if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (likeCount > 0) "$likeCount" else "Like")
                }
                Spacer(Modifier.width(16.dp))
                TextButton(onClick = onCommentClick) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    if (post.commentCount > 0) {
                        Text("${post.commentCount}")
                    } else {
                        Text("Comment")
                    }
                }
            }
        }
    }
}


// --- Helper UI Components ---
@Composable
fun ExpandableText(text: String) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    isOverflowing = true
                }
            }
        )
        if (isOverflowing || isExpanded) {
            Text(
                text = if (isExpanded) "Show less" else "See more",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 4.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getRelativeTimeDisplay(dateText: String): String {
    return try {
        val eventDate = LocalDate.parse(dateText)
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(eventDate, today)

        when {
            daysDiff == 0L -> "Today"
            daysDiff == 1L -> "Yesterday"
            daysDiff == -1L -> "Tomorrow"
            daysDiff > 0 -> "$daysDiff days ago"
            else -> "In ${-daysDiff} days"
        }
    } catch (e: Exception) {
        dateText
    }
}


// --- Comment System ---
@Composable
fun CommentSection(
    eventId: String,
    postOwnerId: String,
    viewModel: CommunityViewModel
) {
    val commentsFlow = remember(eventId) { viewModel.getComments(eventId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    val currentUserId = viewModel.currentUserId
    var inputContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp)
            .padding(bottom = 20.dp)
    ) {
        Text(
            "Comments (${comments.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Text(
                        "No comments yet. Say something!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        currentUserId = currentUserId,
                        postOwnerId = postOwnerId,
                        onDeleteClick = {
                            viewModel.deleteComment(comment, postOwnerId)
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputContent,
                onValueChange = { inputContent = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.sendComment(eventId, postOwnerId, inputContent)
                    inputContent = ""
                },
                enabled = inputContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: com.example.cs501_fp.data.model.Comment,
    currentUserId: String,
    postOwnerId: String,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val canDelete = (currentUserId == comment.userId) || (currentUserId == postOwnerId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (comment.avatarUrl != null) {
            AsyncImage(
                model = comment.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username.ifBlank { "Anonymous" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dateFormat.format(Date(comment.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (canDelete) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Comment",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}