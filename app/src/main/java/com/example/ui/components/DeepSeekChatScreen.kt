package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan
import com.example.ui.theme.DeepSeekPurple
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun DeepSeekChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(
        initialValue = if (uiState.isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Keep drawer state synced
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!uiState.isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen != uiState.isDrawerOpen) {
            viewModel.toggleDrawer(drawerState.isOpen)
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawerContent(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                onSelectSession = { viewModel.selectSession(it) },
                onNewChat = { viewModel.createNewSession() },
                onDeleteSession = { viewModel.deleteSession(it) },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                DeepSeekTopBar(
                    currentModel = uiState.activeModel,
                    onModelSelect = { viewModel.selectModel(it) },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNewChat = { viewModel.createNewSession() },
                    onOpenSettings = { viewModel.toggleSettings(true) }
                )
            },
            bottomBar = {
                DeepSeekBottomBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSendMessage = { prompt ->
                        viewModel.sendMessage(prompt)
                        inputText = ""
                    },
                    isDeepThinkActive = uiState.isDeepThink,
                    onToggleDeepThink = { viewModel.toggleDeepThink() },
                    isWebSearchActive = uiState.isWebSearch,
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    isGenerating = uiState.isGenerating,
                    onStopGeneration = { viewModel.stopGeneration() }
                )
            },
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.messages.isEmpty()) {
                    // Empty state: DeepSeek welcome & prompt starters
                    EmptyChatWelcome(
                        onPromptClick = { prompt, deepThink, webSearch ->
                            if (deepThink && !uiState.isDeepThink) viewModel.toggleDeepThink()
                            if (webSearch && !uiState.isWebSearch) viewModel.toggleWebSearch()
                            inputText = prompt
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { msg ->
                            if (msg.role == "user") {
                                UserMessageBubble(message = msg)
                            } else {
                                val sources = viewModel.deserializeSources(msg.webSourcesJson)
                                AssistantMessageCard(
                                    message = msg,
                                    sources = sources,
                                    showEfficiencyMetrics = uiState.showEfficiencyMetrics,
                                    onCopy = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("DeepSeek Response", msg.content))
                                        Toast.makeText(context, "تم نسخ الرد", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // Live generating / streaming thinking and token indicator
                        if (uiState.isGenerating) {
                            item {
                                GeneratingAssistantPlaceholder(
                                    isDeepThink = uiState.isDeepThink,
                                    thinkingText = uiState.currentThinkingStream,
                                    streamingAnswer = uiState.currentStreamingAnswer,
                                    liveSpeedTps = uiState.currentLiveSpeedTps
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            currentApiKey = uiState.apiKey,
            currentModel = uiState.activeModel,
            isUltraSpeedEnabled = uiState.isUltraSpeedEnabled,
            isSmartCacheEnabled = uiState.isSmartCacheEnabled,
            showEfficiencyMetrics = uiState.showEfficiencyMetrics,
            onSaveApiKey = { viewModel.updateApiKey(it) },
            onSelectModel = { viewModel.selectModel(it) },
            onSaveEfficiencySettings = { ultraSpeed, smartCache, showMetrics ->
                viewModel.updateEfficiencySettings(ultraSpeed, smartCache, showMetrics)
            },
            onClearChat = { viewModel.clearCurrentChat() },
            onDismiss = { viewModel.toggleSettings(false) }
        )
    }
}

@Composable
fun EmptyChatWelcome(
    onPromptClick: (String, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // DeepSeek stylized Logo / Icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(DeepSeekBlue.copy(alpha = 0.15f))
                .border(2.dp, DeepSeekBlue.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.deepseek_logo_icon),
                contentDescription = "DeepSeek Logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DeepSeek AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "مرحباً، أنا ديب سيك. كيف يمكنني مساعدتك اليوم؟\nاختر خيار التفكير العميق (R1) أو البحث في الويب من الأسفل.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Suggestions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PromptStarterCard(
                icon = Icons.Default.Psychology,
                iconColor = DeepSeekPurple,
                title = "تفكير عميق: حل مسألة منطقية وحسابية",
                subtitle = "تفعيل DeepThink R1 لتفكيك الفرضيات والوصول للحل خطوة بخطوة",
                onClick = {
                    onPromptClick("حل هذه المسألة الرياضية والمنطقية مع توضيح خطوات التفكير: إذا كان هناك 5 عمال ينهون 5 مهام في 5 ساعات، فكم ساعة يحتاج 10 عمال لإنهاء 10 مهام؟", true, false)
                }
            )

            PromptStarterCard(
                icon = Icons.Default.Language,
                iconColor = DeepSeekCyan,
                title = "بحث في الويب: أحدث أخبار الذكاء الاصطناعي",
                subtitle = "استعراض مصادر الويب الحية والأخبار الموثقة",
                onClick = {
                    onPromptClick("ابحث في الويب عن أحدث تطورات نماذج DeepSeek V3 و R1 وما يميزها تقنياً.", false, true)
                }
            )

            PromptStarterCard(
                icon = Icons.Default.Code,
                iconColor = DeepSeekBlue,
                title = "برمجة: كتابة كود كوتلن في أندرويد",
                subtitle = "طلب تصميم كود برمجي منظم مع الشرح والتفصيل",
                onClick = {
                    onPromptClick("اكتب دالة بلغة كوتلن في أندرويد تقوم بفحص النصوص وتوليد ملخص سريع بطريقة آمنة وفعالة.", false, false)
                }
            )
        }
    }
}

@Composable
fun PromptStarterCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessageEntity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Badges row for applied modes
            if (message.isDeepThink || message.isWebSearch) {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (message.isDeepThink) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DeepSeekPurple.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🧠 تفكير عميق",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = DeepSeekPurple,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (message.isWebSearch) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DeepSeekCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🌐 بحث الويب",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = DeepSeekCyan,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun AssistantMessageCard(
    message: ChatMessageEntity,
    sources: List<com.example.data.api.WebSource>,
    showEfficiencyMetrics: Boolean = true,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Assistant Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DeepSeekBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Model Badge & Efficiency Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = message.modelName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (message.modelName.contains("R1")) DeepSeekPurple else DeepSeekBlue
                    )
                )

                if (showEfficiencyMetrics && message.tokensPerSec > 0.0) {
                    EfficiencyMetricsPill(
                        tokensPerSec = message.tokensPerSec,
                        timeToFirstTokenMs = message.timeToFirstTokenMs,
                        totalTimeMs = message.totalTimeMs,
                        accelerationEngine = message.accelerationEngine
                    )
                }
            }

            // 1. Thinking Process Card (if available)
            if (!message.thinkingProcess.isNullOrBlank()) {
                ThinkingProcessCard(
                    thinkingText = message.thinkingProcess,
                    durationSeconds = message.thinkingDurationSeconds,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 2. Web Search Sources Card (if available)
            if (sources.isNotEmpty() || !message.webSearchQueries.isNullOrBlank()) {
                WebSearchSourcesCard(
                    sources = sources,
                    searchQueries = message.webSearchQueries,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 3. Main Text Content
            MarkdownText(
                text = message.content,
                textColor = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action Row: Copy, Thumbs up/down
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                var isLiked by remember { mutableStateOf<Boolean?>(null) }

                IconButton(
                    onClick = { isLiked = if (isLiked == true) null else true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Helpful",
                        tint = if (isLiked == true) DeepSeekBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { isLiked = if (isLiked == false) null else false },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Not Helpful",
                        tint = if (isLiked == false) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingAssistantPlaceholder(
    isDeepThink: Boolean,
    thinkingText: String?,
    streamingAnswer: String? = null,
    liveSpeedTps: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DeepSeekBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isDeepThink) {
                ThinkingProcessCard(
                    thinkingText = thinkingText ?: "جاري التفكير والتحليل المنطقي...",
                    isStreaming = streamingAnswer.isNullOrBlank(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Real-time incremental streaming answer rendering
            if (!streamingAnswer.isNullOrBlank()) {
                MarkdownText(
                    text = streamingAnswer,
                    textColor = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Live generation speed badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = DeepSeekBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (liveSpeedTps > 0.0) {
                        "DeepSeek يبث الرموز بسرعة ${String.format("%.1f", liveSpeedTps)} tps..."
                    } else if (isDeepThink) {
                        "DeepSeek يفكر ويحلل..."
                    } else {
                        "DeepSeek يقوم بالرد..."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
