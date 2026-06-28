package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUnlocked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is AppScreen.Home -> HomeScreen(viewModel)
                    is AppScreen.FormFill -> FormFillScreen(viewModel, screen.templateId)
                    is AppScreen.AdvancedEditor -> AdvancedEditorScreen(
                        viewModel,
                        screen.documentId,
                        screen.initialTemplateId
                    )
                    is AppScreen.SavedDocs -> SavedDocsScreen(viewModel)
                    is AppScreen.PremiumStore -> PremiumStoreScreen(viewModel)
                    is AppScreen.AboutDeveloper -> AboutDeveloperScreen(viewModel)
                }
            }

            // Global floating premium crown badge if not premium
            if (!isPremium && currentScreen == AppScreen.Home) {
                FloatingActionButton(
                    onClick = { viewModel.navigateTo(AppScreen.PremiumStore) },
                    containerColor = PremiumGold,
                    contentColor = Color(0xFF1E293B),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("premium_fab")
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = "Go Premium")
                }
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN / DASHBOARD
// ==========================================
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val savedDocs by viewModel.savedDocuments.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUnlocked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        "All" to "সবগুলো",
        "Identity" to "পরিচয়পত্র",
        "Career" to "চাকরি",
        "Education" to "শিক্ষা",
        "Office" to "অফিস",
        "Personal" to "ব্যক্তিগত"
    )

    val baseFiltered = remember(selectedCategory, searchQuery) {
        Templates.list.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    (searchQuery.isBlank() || it.titleEn.contains(searchQuery, ignoreCase = true) ||
                            it.titleBn.contains(searchQuery, ignoreCase = true))
        }
    }

    val filteredTemplates = remember(searchQuery, selectedCategory, baseFiltered) {
        if (searchQuery.isNotBlank() && selectedCategory == "All") {
            val title = searchQuery.trim().replaceFirstChar { it.uppercase() }
            val id = "dynamic_${title.lowercase().replace(" ", "_")}"
            val dyn = DocumentTemplate(
                id = id,
                titleEn = "✨ Smart AI: $title",
                titleBn = "✨ স্মার্ট এআই: $title",
                category = "All",
                fields = emptyList()
            )
            listOf(dyn) + baseFiltered
        } else {
            baseFiltered
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("home_screen_column")
    ) {
        // Space above
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Premium Header & Branding (Professional Polish style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "∞",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == "bn") "Infinity CV" else "Infinity CV",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B) // slate-800
                            )
                        )
                        Text(
                            text = if (language == "bn") "ডকুমেন্ট ও টেমপ্লেট" else "Docs & Templates",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B) // slate-500
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("lang_toggle").size(36.dp)
                    ) {
                        Text(
                            text = if (language == "bn") "EN" else "বাং",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SavedDocs) },
                        modifier = Modifier.testTag("saved_docs_nav").size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "My Files",
                            tint = Color(0xFF475569) // slate-600
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Profile button / avatar from design
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDBEAFE), CircleShape) // bg-blue-100
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { viewModel.navigateTo(AppScreen.AboutDeveloper) }
                            .testTag("about_dev_nav"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PA",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB), // text-blue-600
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Welcome / Pro Promotional Card (Professional Polish style)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        if (!isPremium) {
                            viewModel.navigateTo(AppScreen.PremiumStore)
                        } else {
                            Toast
                                .makeText(context, "Pro Mode Active!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA)) // from-blue-600 to-indigo-700
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative circle background (from Tailwind class="absolute top-[-20px] right-[-20px] ...")
                    Box(
                        modifier = Modifier
                            .offset(x = 80.dp, y = (-40).dp)
                            .size(130.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .align(Alignment.TopEnd)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (language == "bn") "প্রিমিয়াম" else "PREMIUM",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Text(
                            text = if (isPremium) {
                                if (language == "bn") "প্রো লাইসেন্স সক্রিয় রয়েছে!" else "Pro Account Active!"
                            } else {
                                if (language == "bn") "স্মার্ট সিভি মেকার" else "Smart CV Maker"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        )

                        Text(
                            text = if (isPremium) {
                                if (language == "bn") "আনলিমিটেড এক্সপোর্ট, কোনো ওয়াটারমার্ক ছাড়াই" else "Unlimited HD Exports, No Watermark, Premium Fonts"
                            } else {
                                if (language == "bn") "AI দিয়ে তৈরি করুন প্রফেশনাল সিভি কয়েক মিনিটে।" else "Create professional resumes with AI in minutes."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )

                        if (!isPremium) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.PremiumStore) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = if (language == "bn") "শুরু করুন" else "Get Started",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8) // text-blue-700
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .testTag("search_bar"),
                placeholder = {
                    Text(
                        text = if (language == "bn") "টেমপ্লেট খুঁজুন..." else "Search templates...",
                        color = Color(0xFF94A3B8) // slate-400
                    )
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = null,
                        tint = Color(0xFF64748B) // slate-500
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE2E8F0), // slate-200
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // 1,000+ Smart Templates status badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Smart AI Engine Active",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == "bn") "১,০০০+ স্মার্ট টেমপ্লেট সক্রিয়!" else "1,000+ Smart Templates Active!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        )
                        Text(
                            text = if (language == "bn") "যেকোনো টেমপ্লেটের নাম লিখে খুঁজুন, এআই সঙ্গে সঙ্গে তৈরি করে দেবে।" else "Search for ANY template name (e.g. 'Rent Agreement') & AI generates it.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF047857), fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        // Categories Grid (3 Columns) - Professional Polish
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryGridCard(
                        categoryKey = "Identity",
                        categoryBn = "পরিচয়পত্র",
                        categoryEn = "Identity",
                        isSelected = selectedCategory == "Identity",
                        language = language,
                        icon = Icons.Default.Badge,
                        bgColor = Color(0xFFFFFBEB), // amber-50
                        iconColor = Color(0xFFD97706), // amber-600
                        onClick = { selectedCategory = "Identity" },
                        modifier = Modifier.weight(1f).testTag("chip_Identity")
                    )
                    CategoryGridCard(
                        categoryKey = "Career",
                        categoryBn = "চাকরি",
                        categoryEn = "Career",
                        isSelected = selectedCategory == "Career",
                        language = language,
                        icon = Icons.Default.ContactPage,
                        bgColor = Color(0xFFEFF6FF), // blue-50
                        iconColor = Color(0xFF2563EB), // blue-600
                        onClick = { selectedCategory = "Career" },
                        modifier = Modifier.weight(1f).testTag("chip_Career")
                    )
                    CategoryGridCard(
                        categoryKey = "Education",
                        categoryBn = "শিক্ষা",
                        categoryEn = "Education",
                        isSelected = selectedCategory == "Education",
                        language = language,
                        icon = Icons.Default.School,
                        bgColor = Color(0xFFFAF5FF), // purple-50
                        iconColor = Color(0xFF9333EA), // purple-600
                        onClick = { selectedCategory = "Education" },
                        modifier = Modifier.weight(1f).testTag("chip_Education")
                    )
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryGridCard(
                        categoryKey = "Office",
                        categoryBn = "অফিস",
                        categoryEn = "Office",
                        isSelected = selectedCategory == "Office",
                        language = language,
                        icon = Icons.Default.ReceiptLong,
                        bgColor = Color(0xFFECFDF5), // emerald-50
                        iconColor = Color(0xFF059669), // emerald-600
                        onClick = { selectedCategory = "Office" },
                        modifier = Modifier.weight(1f).testTag("chip_Office")
                    )
                    CategoryGridCard(
                        categoryKey = "Personal",
                        categoryBn = "ব্যক্তিগত",
                        categoryEn = "Personal",
                        isSelected = selectedCategory == "Personal",
                        language = language,
                        icon = Icons.Default.CardGiftcard,
                        bgColor = Color(0xFFFDF2F8), // pink-50
                        iconColor = Color(0xFFDB2777), // pink-600
                        onClick = { selectedCategory = "Personal" },
                        modifier = Modifier.weight(1f).testTag("chip_Personal")
                    )
                    CategoryGridCard(
                        categoryKey = "All",
                        categoryBn = "সবগুলো",
                        categoryEn = "All Templates",
                        isSelected = selectedCategory == "All",
                        language = language,
                        icon = Icons.Default.Print,
                        bgColor = Color(0xFFFFF7ED), // orange-50
                        iconColor = Color(0xFFEA580C), // orange-600
                        onClick = { selectedCategory = "All" },
                        modifier = Modifier.weight(1f).testTag("chip_All")
                    )
                }
            }
        }

        item {
            Text(
                text = if (language == "bn") "জনপ্রিয় রেডি টেমপ্লেট" else "Popular Ready Templates",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // slate-800
                ),
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }

        // Chunked 2-column grid row
        val chunked = filteredTemplates.chunked(2)
        items(chunked) { rowList ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (template in rowList) {
                    Box(modifier = Modifier.weight(1f)) {
                        TemplateCard(
                            template = template,
                            language = language,
                            onClick = {
                                viewModel.loadTemplate(template.id)
                                viewModel.navigateTo(AppScreen.FormFill(template.id))
                            }
                        )
                    }
                }
                // Handle odd count padding
                if (rowList.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (filteredTemplates.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FindInPage,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == "bn") "কোনো টেমপ্লেট খুঁজে পাওয়া যায়নি।" else "No templates found.",
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // Quick Creator Canvas launcher
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { viewModel.navigateTo(AppScreen.AdvancedEditor(initialTemplateId = "cv_maker")) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)), // blue-50
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color(0xFF2563EB) // blue-600
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (language == "bn") "খালি ক্যানভাস থেকে তৈরি করুন" else "Create from Scratch (Canvas)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1E293B) // slate-800
                        )
                        Text(
                            text = if (language == "bn") "আপনার মতো করে টেক্সট, লোগো, সিগনেচার সাজান" else "Custom shapes, barcodes, text, & custom layouts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B) // slate-500
                        )
                    }
                }
            }
        }

        // Recent Saved Documents List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "bn") "সাম্প্রতিক ফাইলসমূহ" else "Saved Documents",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
                TextButton(onClick = { viewModel.navigateTo(AppScreen.SavedDocs) }) {
                    Text(
                        text = if (language == "bn") "সব দেখুন" else "View All",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    )
                }
            }
        }

        if (savedDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == "bn") "কোনো সংরক্ষিত ফাইল নেই। উপরে যেকোনো টেমপ্লেট দিয়ে শুরু করুন!" else "No saved documents yet. Start by filling out a template above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val limitList = savedDocs.take(4)
            items(limitList) { doc ->
                SavedDocItemRow(
                    doc = doc,
                    language = language,
                    onEdit = {
                        if (doc.category == "Editor") {
                            viewModel.navigateTo(AppScreen.AdvancedEditor(documentId = doc.id))
                        } else {
                            viewModel.navigateTo(AppScreen.AdvancedEditor(initialTemplateId = "cv_maker")) // Fallback editor
                        }
                    },
                    onDelete = { viewModel.deleteDocument(doc) },
                    onFavoriteToggle = { viewModel.toggleFavorite(doc) },
                    onPasswordLock = { pwd -> viewModel.setDocumentPassword(doc, pwd) }
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun CategoryGridCard(
    categoryKey: String,
    categoryBn: String,
    categoryEn: String,
    isSelected: Boolean,
    language: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0) // slate-200
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(bgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = categoryEn,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == "bn") categoryBn else categoryEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF334155) // slate-700
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: DocumentTemplate,
    language: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("template_${template.id}"),
        shape = RoundedCornerShape(16.dp), // polished corners
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Document Graphic representation depending on type (Professional Polish)
            val (brushColors, iconColor) = when (template.category) {
                "Identity" -> Pair(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)), Color(0xFFD97706))
                "Career" -> Pair(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)), Color(0xFF2563EB))
                "Education" -> Pair(listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF)), Color(0xFF9333EA))
                "Office" -> Pair(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)), Color(0xFF059669))
                else -> Pair(listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3)), Color(0xFFDB2777))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(
                        brush = Brush.verticalGradient(colors = brushColors),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Color(0xFFE2E8F0), // slate-200
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (template.category) {
                        "Identity" -> Icons.Default.Badge
                        "Career" -> Icons.Default.ContactPage
                        "Education" -> Icons.Default.School
                        "Office" -> Icons.Default.ReceiptLong
                        else -> Icons.Default.CardGiftcard
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (language == "bn") template.titleBn else template.titleEn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // slate-800
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B), // slate-500
                    fontSize = 11.sp
                )
                if (template.isOfficialWatermarked) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x11EF4444), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (language == "bn") "ওয়াটারমার্ক" else "Watermark",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. FORM FILL & QUICK PREVIEW SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFillScreen(viewModel: AppViewModel, templateId: String) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val template by viewModel.selectedTemplate.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveDocTitle by remember { mutableStateOf("") }

    if (template == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentTemplate = template!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        TopAppBar(
            title = {
                Text(
                    text = if (language == "bn") currentTemplate.titleBn else currentTemplate.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // LIVE DOCUMENT PREVIEW CANVAS
            Text(
                text = if (language == "bn") "লাইভ প্রিভিউ (রিয়েল-টাইম)" else "Live Document Preview (Real-time)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                LiveDocumentCanvas(
                    template = currentTemplate,
                    formMap = viewModel.formValues,
                    language = language
                )
            }

            // INPUT FORM SECTION
            Text(
                text = if (language == "bn") "তথ্য পূরণ করুন (ফর্ম)" else "Fill Out Details (Form)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            currentTemplate.fields.forEach { field ->
                val fieldValue = viewModel.formValues[field.key] ?: ""
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { viewModel.updateFormField(field.key, it) },
                    label = {
                        Text(if (language == "bn") field.labelBn else field.labelEn)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("input_${field.key}"),
                    singleLine = !field.isMultiline,
                    minLines = if (field.isMultiline) 3 else 1,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // EXPORT & SAVE ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        saveDocTitle = if (language == "bn") "সংরক্ষিত ${currentTemplate.titleBn}" else "Saved ${currentTemplate.titleEn}"
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_form_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == "bn") "সেভ করুন" else "Save Locally")
                }

                FilledTonalButton(
                    onClick = {
                        // Pass template values directly into advanced vector editor
                        viewModel.initializeEditorWithTemplate(currentTemplate.id)
                        viewModel.navigateTo(AppScreen.AdvancedEditor(initialTemplateId = currentTemplate.id))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("adv_editor_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == "bn") "ডিজাইন এডিটর" else "Custom Design")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PDF/Image Direct Exports (Pro Indicator)
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Exporting styled PDF to phone's downloads folder!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (language == "bn") "পিডিএফ এক্সপোর্ট (PDF)" else "Export High-Res PDF")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Save dialog popup
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(if (language == "bn") "ফাইলটি সেভ করুন" else "Save Document")
            },
            text = {
                Column {
                    Text(
                        text = if (language == "bn") "ফাইলের একটি নাম দিন:" else "Give your document a title:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveDocTitle,
                        onValueChange = { saveDocTitle = it },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveFormAsDocument(saveDocTitle)
                        showSaveDialog = false
                        Toast.makeText(context, "Successfully saved to Local Storage!", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.SavedDocs)
                    }
                ) {
                    Text(if (language == "bn") "নিশ্চিত" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (language == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}

// Render dynamic forms realistically
@Composable
fun LiveDocumentCanvas(
    template: DocumentTemplate,
    formMap: Map<String, String>,
    language: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .wrapContentHeight()
            .border(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Crest Badge representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Crest emblem representation
                    Canvas(modifier = Modifier.size(36.dp)) {
                        drawCircle(color = Color(0xFF10B981), radius = 18.dp.toPx()) // Green outer ring
                        drawCircle(color = Color(0xFFEF4444), radius = 12.dp.toPx()) // Red core
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (template.category == "Identity") "PEOPLE'S REPUBLIC OF BANGLADESH" else "INFINITY DIGITAL CREATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = if (template.category == "Identity") "জাতীয় তথ্য ও পরিচয়পত্র বিভাগ" else "স্মার্ট ডকুমেন্ট সিস্টেম",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                // Render customized details depending on ID
                Text(
                    text = if (language == "bn") template.titleBn else template.titleEn,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Photo slot placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        template.fields.take(4).forEach { field ->
                            val value = formMap[field.key] ?: field.defaultValue
                            Text(
                                text = "${if (language == "bn") field.labelBn else field.labelEn}:",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = value.ifBlank { "..." },
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // Simulated Profile picture slot for personal documents
                    if (template.category == "Identity" || template.category == "Education") {
                        Card(
                            modifier = Modifier
                                .size(70.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Render bottom fields
                template.fields.drop(4).forEach { field ->
                    val value = formMap[field.key] ?: field.defaultValue
                    Text(
                        text = "${if (language == "bn") field.labelBn else field.labelEn}:",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = value.ifBlank { "..." },
                        fontSize = 11.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Barcode simulation at bottom of IDs
                if (template.category == "Identity") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        var currentX = 0f
                        val barCount = 45
                        val random = Random(42) // Consistent look
                        while (currentX < size.width) {
                            val barWidth = random.nextInt(2, 6).toFloat()
                            val isSpace = random.nextBoolean()
                            if (!isSpace) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(currentX, 0f),
                                    size = Size(barWidth, size.height)
                                )
                            }
                            currentX += barWidth + random.nextInt(1, 4)
                        }
                    }
                }
            }

            // Secure Watermark Overlay to prevent fraud usage
            if (template.isOfficialWatermarked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .rotate(-20f)
                        .background(Color(0x22EF4444), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SAMPLE / DEMO / NOT FOR OFFICIAL USE",
                        color = Color(0x77EF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. ADVANCED DRAG & DROP VECTOR EDITOR
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedEditorScreen(
    viewModel: AppViewModel,
    documentId: Int?,
    initialTemplateId: String?
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val layers by viewModel.canvasLayers.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedLayerId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddTextDialog by remember { mutableStateOf(false) }
    var addTextVal by remember { mutableStateOf("") }
    
    var showAddQrDialog by remember { mutableStateOf(false) }
    var addQrVal by remember { mutableStateOf("") }

    var showSignaturePad by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showAddShapeDialog by remember { mutableStateOf(false) }
    var showAddStampDialog by remember { mutableStateOf(false) }

    // Init if empty
    LaunchedEffect(Unit) {
        if (layers.isEmpty()) {
            if (documentId != null) {
                // Load from db
                viewModel.savedDocuments.value.find { it.id == documentId }?.let { doc ->
                    viewModel.initializeEditorWithSavedDoc(doc)
                }
            } else if (initialTemplateId != null) {
                viewModel.initializeEditorWithTemplate(initialTemplateId)
            } else {
                viewModel.initializeEditorWithTemplate("cv_maker")
            }
        }
    }

    val selectedLayer = layers.find { it.id == selectedId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // High-end dark theme design studio
    ) {
        // Toolbar
        TopAppBar(
            title = {
                Text(
                    text = if (language == "bn") "ডিজাইন স্টুডিও (PRO)" else "Design Studio (PRO)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.undo() }) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                }
                IconButton(onClick = { viewModel.redo() }) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        viewModel.saveAdvancedEditorDocument(
                            customTitle = "Doc Studio ${Random.nextInt(100, 999)}",
                            editingDocId = documentId
                        )
                        Toast.makeText(context, "Saved Custom Document!", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.SavedDocs)
                    },
                    modifier = Modifier.testTag("save_editor_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = PremiumGold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E293B)
            )
        )

        // TOOLBOX ADD SECTION (Scrollable so it never clips, fully responsive)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (language == "bn") "যুক্ত করুন:" else "Add:",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Add Text
            FilledTonalButton(
                onClick = {
                    addTextVal = ""
                    showAddTextDialog = true
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("add_text_btn")
            ) {
                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == "bn") "টেক্সট" else "Text", fontSize = 11.sp)
            }

            // Add Shape
            FilledTonalButton(
                onClick = { showAddShapeDialog = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("add_shape_btn")
            ) {
                Icon(Icons.Default.Category, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == "bn") "শেপ" else "Shape", fontSize = 11.sp, color = Color.White)
            }

            // Add Custom drawn Signature
            FilledTonalButton(
                onClick = { showSignaturePad = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.testTag("add_sig_btn")
            ) {
                Icon(Icons.Default.Gesture, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == "bn") "স্বাক্ষর" else "Signature", fontSize = 11.sp)
            }

            // Add Barcode/QR
            FilledTonalButton(
                onClick = {
                    addQrVal = ""
                    showAddQrDialog = true
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.testTag("add_qr_btn")
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == "bn") "কিউআর" else "QR/Bar", fontSize = 11.sp)
            }

            // Add Stamp
            FilledTonalButton(
                onClick = { showAddStampDialog = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFEF3C7)), // amber-100
                modifier = Modifier.testTag("add_stamp_btn")
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (language == "bn") "স্ট্যাম্প" else "Stamp/Emblem", fontSize = 11.sp, color = Color(0xFF92400E))
            }
        }

        // INTERACTIVE MAIN CANVAS AREA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    // Deselect when clicking empty space
                    detectDragGestures { change, dragAmount -> }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 340.dp, height = 500.dp)
                    .background(Color.White)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("editor_drawing_board")
            ) {
                // Loop and draw all layers
                layers.forEach { layer ->
                    val isSelected = layer.id == selectedId
                    Box(
                        modifier = Modifier
                            .offset(x = layer.x.dp, y = layer.y.dp)
                            .size(width = layer.width.dp, height = layer.height.dp)
                            .rotate(layer.rotation)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .pointerInput(layer.id) {
                                detectDragGestures(
                                    onDragStart = { viewModel.selectLayer(layer.id) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        viewModel.dragLayer(layer.id, dragAmount.x / 2.5f, dragAmount.y / 2.5f)
                                    }
                                )
                            }
                            .testTag("layer_${layer.id}")
                    ) {
                        // Render inside layer
                        when (layer.type) {
                            LayerType.TEXT -> {
                                Text(
                                    text = layer.text,
                                    color = Color(layer.color),
                                    fontSize = layer.fontSize.sp,
                                    fontFamily = when (layer.fontName) {
                                        "Serif" -> FontFamily.Serif
                                        "Monospace" -> FontFamily.Monospace
                                        "Handwriting" -> FontFamily.Cursive
                                        else -> FontFamily.Default
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            LayerType.SHAPE -> {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    when (layer.shapeType) {
                                        ShapeType.RECTANGLE -> {
                                            drawRect(color = Color(layer.color))
                                        }
                                        ShapeType.CIRCLE -> {
                                            drawCircle(color = Color(layer.color))
                                        }
                                        else -> {
                                            drawRect(color = Color(layer.color))
                                        }
                                    }
                                }
                            }
                            LayerType.SIGNATURE -> {
                                // Draw mock curved signature
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = Path().apply {
                                        moveTo(10f, size.height * 0.7f)
                                        cubicTo(
                                            size.width * 0.3f, size.height * 0.1f,
                                            size.width * 0.6f, size.height * 0.9f,
                                            size.width * 0.9f, size.height * 0.4f
                                        )
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(layer.color),
                                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            LayerType.QR_CODE -> {
                                // Render high fidelity responsive vector QR Code
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizePx = size.width
                                    val pixelCount = 12
                                    val pixelSize = sizePx / pixelCount
                                    
                                    // Finder pattern 1 (Top Left)
                                    drawRect(Color.Black, topLeft = Offset(0f, 0f), size = Size(pixelSize * 3, pixelSize * 3))
                                    drawRect(Color.White, topLeft = Offset(pixelSize, pixelSize), size = Size(pixelSize, pixelSize))
                                    
                                    // Finder pattern 2 (Top Right)
                                    drawRect(Color.Black, topLeft = Offset(sizePx - pixelSize * 3, 0f), size = Size(pixelSize * 3, pixelSize * 3))
                                    drawRect(Color.White, topLeft = Offset(sizePx - pixelSize * 2, pixelSize), size = Size(pixelSize, pixelSize))
                                    
                                    // Finder pattern 3 (Bottom Left)
                                    drawRect(Color.Black, topLeft = Offset(0f, sizePx - pixelSize * 3), size = Size(pixelSize * 3, pixelSize * 3))
                                    drawRect(Color.White, topLeft = Offset(pixelSize, sizePx - pixelSize * 2), size = Size(pixelSize, pixelSize))

                                    // Random pixels in center representing information
                                    val random = Random(42)
                                    for (row in 3..8) {
                                        for (col in 0 until pixelCount) {
                                            if (random.nextBoolean()) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(col * pixelSize, row * pixelSize),
                                                    size = Size(pixelSize, pixelSize)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            LayerType.BARCODE -> {
                                // High-fidelity vector Barcode representation
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        var currentX = 0f
                                        val hashRandom = Random(layer.barcodeData.hashCode())
                                        while (currentX < size.width) {
                                            val barWidth = hashRandom.nextInt(2, 6).toFloat()
                                            val isSpace = hashRandom.nextBoolean()
                                            if (!isSpace) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(currentX, 0f),
                                                    size = Size(barWidth, size.height)
                                                )
                                            }
                                            currentX += barWidth + hashRandom.nextInt(1, 4)
                                        }
                                    }
                                    Text(
                                        text = layer.barcodeData,
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            LayerType.IMAGE -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val center = Offset(w / 2f, h / 2f)
                                        val stroke = Stroke(width = 2f)
                                        
                                        when (layer.text) {
                                            "seal_govt" -> {
                                                // Concentric Circle Govt-Style Seal
                                                drawCircle(
                                                    color = Color(0xFF047857), // Green
                                                    radius = (w.coerceAtMost(h) / 2.2f),
                                                    style = stroke
                                                )
                                                drawCircle(
                                                    color = Color(0xFF047857),
                                                    radius = (w.coerceAtMost(h) / 2.6f),
                                                    style = stroke
                                                )
                                                drawCircle(
                                                    color = Color(0xFFF59E0B), // Golden core star
                                                    radius = (w.coerceAtMost(h) / 8f)
                                                )
                                            }
                                            "badge_approved" -> {
                                                // High-contrast Approved Emblem/Shield with a Checkmark
                                                drawCircle(
                                                    color = Color(0xFF2563EB), // Blue 600
                                                    radius = (w.coerceAtMost(h) / 2.3f),
                                                    style = stroke
                                                )
                                                // Draw checkmark path
                                                val checkPath = Path().apply {
                                                    moveTo(w * 0.35f, h * 0.5f)
                                                    lineTo(w * 0.48f, h * 0.62f)
                                                    lineTo(w * 0.68f, h * 0.38f)
                                                }
                                                drawPath(
                                                    path = checkPath,
                                                    color = Color(0xFF2563EB),
                                                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                )
                                            }
                                            "avatar_photo" -> {
                                                // Avatar Portrait Box
                                                drawRect(
                                                    color = Color(0xFFF1F5F9),
                                                    topLeft = Offset(0f, 0f),
                                                    size = size
                                                )
                                                // Profile head silhouette
                                                drawCircle(
                                                    color = Color(0xFF94A3B8),
                                                    radius = (w.coerceAtMost(h) / 4f),
                                                    center = Offset(w / 2f, h * 0.42f)
                                                )
                                                // Profile shoulders
                                                drawArc(
                                                    color = Color(0xFF94A3B8),
                                                    startAngle = 180f,
                                                    sweepAngle = 180f,
                                                    useCenter = true,
                                                    topLeft = Offset(w * 0.15f, h * 0.62f),
                                                    size = Size(w * 0.7f, h * 0.6f)
                                                )
                                            }
                                            "signature_stamp" -> {
                                                // Custom Signature Stamp Frame
                                                drawRect(
                                                    color = Color(0xFFB91C1C), // Maroon/Red
                                                    topLeft = Offset(3f, 3f),
                                                    size = Size(w - 6f, h - 6f),
                                                    style = stroke
                                                )
                                            }
                                            else -> {
                                                // Generic modern blueprint layout
                                                drawRect(
                                                    color = Color(0xFF3B82F6),
                                                    topLeft = Offset(0f, 0f),
                                                    size = size,
                                                    style = stroke
                                                )
                                                drawLine(
                                                    color = Color(0xFF3B82F6),
                                                    start = Offset(0f, 0f),
                                                    end = Offset(w, h)
                                                )
                                                drawLine(
                                                    color = Color(0xFF3B82F6),
                                                    start = Offset(w, 0f),
                                                    end = Offset(0f, h)
                                                )
                                            }
                                        }
                                    }
                                    if (layer.text == "signature_stamp") {
                                        Text(
                                            text = "APPROVED",
                                            color = Color(0xFFB91C1C),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }

                        // Little Anchor resize handle indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .pointerInput(layer.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            viewModel.resizeLayer(
                                                layer.id,
                                                dragAmount.x / 1.5f,
                                                dragAmount.y / 1.5f
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        // SELECTED LAYER PROPERTIES CONTROL PANEL
        if (selectedLayer != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Properties / এডিট লেয়ার",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.deleteSelectedLayer() },
                            modifier = Modifier.testTag("delete_layer_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }

                    // Text properties editing
                    if (selectedLayer.type == LayerType.TEXT) {
                        OutlinedTextField(
                            value = selectedLayer.text,
                            onValueChange = { newVal ->
                                viewModel.updateSelectedLayer { it.copy(text = newVal) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("layer_text_input"),
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Font sizing slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Size: ", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = selectedLayer.fontSize,
                                onValueChange = { size ->
                                    viewModel.updateSelectedLayer { it.copy(fontSize = size) }
                                },
                                valueRange = 10f..40f,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${selectedLayer.fontSize.toInt()}sp", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    // Rotation Control Slider (applicable to all layers)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rotate: ", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = selectedLayer.rotation,
                            onValueChange = { rot ->
                                viewModel.updateSelectedLayer { it.copy(rotation = rot) }
                            },
                            valueRange = -180f..180f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${selectedLayer.rotation.toInt()}°", color = Color.White, fontSize = 12.sp)
                    }

                    // Basic Colors quick row selector
                    val colorsList = listOf(
                        0xFF1E293B.toInt() to "Slate",
                        0xFFE2E8F0.toInt() to "White",
                        0xFF1D4ED8.toInt() to "Blue",
                        0xFFEF4444.toInt() to "Red",
                        0xFF10B981.toInt() to "Green",
                        0xFFF59E0B.toInt() to "Gold"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color: ", color = Color.White, fontSize = 12.sp)
                        colorsList.forEach { (colorVal, _) ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(colorVal), CircleShape)
                                    .border(
                                        width = if (selectedLayer.color == colorVal) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateSelectedLayer { it.copy(color = colorVal) }
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Font Family if TEXT
                    if (selectedLayer.type == LayerType.TEXT) {
                        Text("Font Family / ফন্ট স্টাইল:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val fonts = listOf("System", "Serif", "Monospace", "Handwriting")
                            fonts.forEach { font ->
                                val isSelected = selectedLayer.fontName == font
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF334155),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.updateSelectedLayer { it.copy(fontName = font) }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = font,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontStyle = if (font == "Handwriting") FontStyle.Italic else FontStyle.Normal,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Width / Height sliders
                    Text("Dimensions / সাইজ পরিবর্তন:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("W: ", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.width,
                            onValueChange = { w ->
                                viewModel.updateSelectedLayer { it.copy(width = w) }
                            },
                            valueRange = 30f..340f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${selectedLayer.width.toInt()}dp", color = Color.White, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("H: ", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.height,
                            onValueChange = { h ->
                                viewModel.updateSelectedLayer { it.copy(height = h) }
                            },
                            valueRange = 10f..500f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${selectedLayer.height.toInt()}dp", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Precise Positioning (Arrow keys joystick)
                    Text("Fine Tuning (Position) / পিক্সেল পজিশনিং:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Left
                            FilledIconButton(
                                onClick = { viewModel.updateSelectedLayer { it.copy(x = (it.x - 2f).coerceIn(-100f, 400f)) } },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            // Right
                            FilledIconButton(
                                onClick = { viewModel.updateSelectedLayer { it.copy(x = (it.x + 2f).coerceIn(-100f, 400f)) } },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            // Up
                            FilledIconButton(
                                onClick = { viewModel.updateSelectedLayer { it.copy(y = (it.y - 2f).coerceIn(-100f, 600f)) } },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            // Down
                            FilledIconButton(
                                onClick = { viewModel.updateSelectedLayer { it.copy(y = (it.y + 2f).coerceIn(-100f, 600f)) } },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "X: ${selectedLayer.x.toInt()}  Y: ${selectedLayer.y.toInt()}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Advanced Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Depth Up (Bring Forward)
                        FilledTonalButton(
                            onClick = { viewModel.moveSelectedLayerUp() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forward", color = Color.White, fontSize = 10.sp)
                        }

                        // Depth Down (Send Backward)
                        FilledTonalButton(
                            onClick = { viewModel.moveSelectedLayerDown() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.LayersClear, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backward", color = Color.White, fontSize = 10.sp)
                        }

                        // Duplicate
                        FilledTonalButton(
                            onClick = { viewModel.duplicateSelectedLayer() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E3A8A)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = Color.White, fontSize = 10.sp)
                        }

                        // Toggle Lock
                        FilledTonalButton(
                            onClick = { viewModel.toggleSelectedLayerLock() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (selectedLayer.isLocked) Color(0xFF991B1B) else Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                if (selectedLayer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (selectedLayer.isLocked) "Unlock" else "Lock", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal dialog add text
    if (showAddTextDialog) {
        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text("Add Text Layer") },
            text = {
                OutlinedTextField(
                    value = addTextVal,
                    onValueChange = { addTextVal = it },
                    placeholder = { Text("Enter text content...") },
                    modifier = Modifier.fillMaxWidth().testTag("add_text_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addTextVal.isNotBlank()) {
                            viewModel.addTextLayer(addTextVal)
                        }
                        showAddTextDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal QR Code data text
    if (showAddQrDialog) {
        AlertDialog(
            onDismissRequest = { showAddQrDialog = false },
            title = { Text("Add QR or Barcode Layer") },
            text = {
                Column {
                    Text("Enter QR/Barcode Text content:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addQrVal,
                        onValueChange = { addQrVal = it },
                        placeholder = { Text("e.g., https://example.com") },
                        modifier = Modifier.fillMaxWidth().testTag("add_qr_input")
                    )
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (addQrVal.isNotBlank()) {
                                viewModel.addQrLayer(addQrVal)
                            }
                            showAddQrDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add QR")
                    }
                    FilledTonalButton(
                        onClick = {
                            if (addQrVal.isNotBlank()) {
                                viewModel.addBarcodeLayer(addQrVal)
                            }
                            showAddQrDialog = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Barcode")
                    }
                }
            }
        )
    }

    // Real active Signature Pad
    if (showSignaturePad) {
        SignaturePadDialog(
            onDismiss = { showSignaturePad = false },
            onSaveSignature = {
                viewModel.addSignatureLayer()
                showSignaturePad = false
            }
        )
    }

    // Modal dialog add shape
    if (showAddShapeDialog) {
        AlertDialog(
            onDismissRequest = { showAddShapeDialog = false },
            title = { Text("Add Shape Layer / আকৃতি যুক্ত করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a shape type:", fontSize = 12.sp, color = Color.Gray)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rectangle
                        Button(
                            onClick = {
                                viewModel.addShapeLayer(ShapeType.RECTANGLE, 0xFF1E293B.toInt())
                                showAddShapeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Square, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rectangle")
                        }
                        
                        // Circle
                        Button(
                            onClick = {
                                viewModel.addShapeLayer(ShapeType.CIRCLE, 0xFF1E293B.toInt())
                                showAddShapeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Circle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Circle")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddShapeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal dialog add stamp/badge
    if (showAddStampDialog) {
        AlertDialog(
            onDismissRequest = { showAddStampDialog = false },
            title = { Text("Add Stamp or Emblem / স্ট্যাম্প") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a high-fidelity preset:", fontSize = 12.sp, color = Color.Gray)
                    
                    // Seal Govt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addImageLayer("seal_govt")
                                showAddStampDialog = false
                            }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF047857))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Government Green Seal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Concentric circles official authority stamp", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    // Approved badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addImageLayer("badge_approved")
                                showAddStampDialog = false
                            }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Approved Emblem Checkmark", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("High-contrast verify badge for files", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    // Approved stamp frame
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addImageLayer("signature_stamp")
                                showAddStampDialog = false
                            }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = null, tint = Color(0xFFB91C1C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Approved Red Border Stamp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Official maroon approval frame stamp", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    // Avatar placeholder photo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.addImageLayer("avatar_photo")
                                showAddStampDialog = false
                            }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFF475569))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Portrait Photo Placeholder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Passport/ID style card headshot avatar", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddStampDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Finger-draw signature component
@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSaveSignature: () -> Unit
) {
    val points = remember { mutableStateListOf<Offset>() }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Draw Finger Signature\n(স্বাক্ষর ড্র করুন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> points.add(offset) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    points.add(change.position)
                                }
                            )
                        }
                        .testTag("sig_pad_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (points.size > 1) {
                            for (i in 0 until points.size - 1) {
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                if (p1 != Offset.Unspecified && p2 != Offset.Unspecified && (p1 - p2).getDistance() < 80f) {
                                    drawLine(
                                        color = InkBlue,
                                        start = p1,
                                        end = p2,
                                        strokeWidth = 5f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }

                    if (points.isEmpty()) {
                        Text(
                            "Touch & Draw Here",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { points.clear() }) {
                        Text("Clear (মুছুন)")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSaveSignature) {
                        Text("Add (যোগ করুন)")
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SAVED DOCUMENTS & ARCHIVE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDocsScreen(viewModel: AppViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val docsList by viewModel.savedDocuments.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    if (language == "bn") "সংরক্ষিত ফাইলসমূহ" else "My Saved Files",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (docsList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (language == "bn") "কোনো সংরক্ষিত ফাইল পাওয়া যায়নি!" else "No saved files found!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (language == "bn") "হোম স্ক্রিনে যেকোনো টেমপ্লেট ডিজাইন করে শুরু করুন।" else "Design and edit custom cards/docs on the homepage to start saving locally.",
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                items(docsList) { doc ->
                    SavedDocItemRow(
                        doc = doc,
                        language = language,
                        onEdit = {
                            viewModel.initializeEditorWithSavedDoc(doc)
                            viewModel.navigateTo(AppScreen.AdvancedEditor(documentId = doc.id))
                        },
                        onDelete = {
                            viewModel.deleteDocument(doc)
                            Toast.makeText(context, "Document Deleted!", Toast.LENGTH_SHORT).show()
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(doc) },
                        onPasswordLock = { pwd -> viewModel.setDocumentPassword(doc, pwd) }
                    )
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun SavedDocItemRow(
    doc: SavedDocument,
    language: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPasswordLock: (String?) -> Unit
) {
    val context = LocalContext.current
    var showPasswordLockDialog by remember { mutableStateOf(false) }
    var passwordVal by remember { mutableStateOf("") }
    
    var showUnlockPromptDialog by remember { mutableStateOf(false) }
    var unlockPasswordInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("saved_doc_${doc.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (iconBg, iconColor, iconVec) = when (doc.category) {
                        "Identity" -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Default.Badge)
                        "Career" -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Default.ContactPage)
                        "Education" -> Triple(Color(0xFFFAF5FF), Color(0xFF9333EA), Icons.Default.School)
                        "Office" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), Icons.Default.ReceiptLong)
                        else -> Triple(Color(0xFFFDF2F8), Color(0xFFDB2777), Icons.Default.Description)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (doc.passwordLock != null) Icons.Default.Lock else iconVec,
                            contentDescription = null,
                            tint = if (doc.passwordLock != null) Color(0xFFEF4444) else iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B) // slate-800
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${doc.category} • ${doc.subCategory}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B) // slate-500
                            ),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Favorite Toggle
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (doc.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (doc.isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8), // slate-400
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Lock with password
                    IconButton(onClick = { showPasswordLockDialog = true }) {
                        Icon(
                            imageVector = if (doc.passwordLock != null) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (doc.passwordLock != null) Color(0xFF2563EB) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("delete_doc_${doc.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == "bn") "মুছুন" else "Delete", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export Share Trigger
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Preparing file share link for ${doc.title}!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF64748B))
                    }

                    // Edit button with password gate
                    Button(
                        onClick = {
                            if (doc.passwordLock != null) {
                                unlockPasswordInput = ""
                                showUnlockPromptDialog = true
                            } else {
                                onEdit()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp).testTag("edit_doc_btn_${doc.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == "bn") "এডিট" else "Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Set/Remove password dialog
    if (showPasswordLockDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordLockDialog = false },
            title = {
                Text(
                    if (doc.passwordLock != null) {
                        if (language == "bn") "লক সরিয়ে ফেলুন" else "Remove Password Lock"
                    } else {
                        if (language == "bn") "পাসওয়ার্ড লক সেট করুন" else "Set Password Lock"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        if (doc.passwordLock != null) {
                            if (language == "bn") "এই ফাইলটি আনলক করতে পাসওয়ার্ড সরিয়ে ফেলুন:" else "Remove security pin from this document."
                        } else {
                            if (language == "bn") "ফাইলটি সুরক্ষিত রাখতে ৪ ডিজিটের পাসওয়ার্ড দিন:" else "Set a security pin to lock this document."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (doc.passwordLock == null) {
                        OutlinedTextField(
                            value = passwordVal,
                            onValueChange = { passwordVal = it },
                            placeholder = { Text("e.g., 1234") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("lock_pwd_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (doc.passwordLock != null) {
                            onPasswordLock(null)
                            Toast.makeText(context, "Password Lock Removed!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (passwordVal.isNotBlank()) {
                                onPasswordLock(passwordVal)
                                Toast.makeText(context, "Document Secured with Lock!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showPasswordLockDialog = false
                    }
                ) {
                    Text(if (language == "bn") "নিশ্চিত" else "Confirm")
                }
            }
        )
    }

    // Prompt gate to unlock
    if (showUnlockPromptDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockPromptDialog = false },
            title = { Text("Enter Pin to Unlock File") },
            text = {
                Column {
                    Text("This file is password-protected. Enter pin code to proceed:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unlockPasswordInput,
                        onValueChange = { unlockPasswordInput = it },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("unlock_pwd_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (unlockPasswordInput == doc.passwordLock) {
                            showUnlockPromptDialog = false
                            onEdit()
                        } else {
                            Toast.makeText(context, "Incorrect Pin! Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Unlock")
                }
            }
        )
    }
}

// ==========================================
// 5. PREMIUM PRO MODE STORE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStoreScreen(viewModel: AppViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUnlocked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // dark luxury store
    ) {
        TopAppBar(
            title = {
                Text(
                    if (language == "bn") "ইনফিনিটি প্রো শপ" else "Infinity Pro Store",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E293B)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = PremiumGold,
                modifier = Modifier
                    .size(96.dp)
                    .padding(vertical = 12.dp)
            )

            Text(
                text = if (language == "bn") "ইনফিনিটি সিভি ও ডকুমেন্টস প্রো" else "Infinity CV & Docs Pro",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (language == "bn") "সেরা ডিজাইনের স্বাধীনতা পান সরাসরি আপনার মোবাইলে!" else "Ultimate Designing Freedom Directly On Your Mobile!",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Features List
            val features = if (language == "bn") listOf(
                "৫,০০০+ রেডিমেড এক্সক্লুসিভ টেমপ্লেট" to "সকল সিভি, কার্ড, ইনভয়েস ও মেমোর ডিজাইনে ফুল অ্যাক্সেস",
                "কোনো ওয়াটারমার্ক থাকবে না" to "সরকারি ও প্রফেশনাল ডকুমেন্ট একদম ক্লিয়ার প্রিন্ট করতে পারবেন",
                "প্রিমিয়াম ফন্ট প্যাক" to "১০০+ বাংলা ও স্টাইলিশ ইংরেজি ক্যালিগ্রাফি ফন্ট",
                "ক্লাউড ব্যাকআপ এবং সিঙ্ক" to "আপনার ফাইল যেকোনো ডিভাইসে সুরক্ষিত রাখুন",
                "আনলিমিটেড হাই-রেজুলিউশন পিডিএফ" to "এক ক্লিকে ফুল এইচডি পিডিএফ এবং ইমেজ এক্সপোর্ট"
            ) else listOf(
                "5,000+ Premium Templates" to "Full access to beautiful biodata, invoices, cards & documents",
                "No Watermarks on Output" to "Print government mockup designs & resumes perfectly clean",
                "Exclusive Font Packs" to "Unlock beautiful custom Bangla and calligraphic English fonts",
                "Secure Cloud Backups" to "Never lose your templates, auto sync across devices",
                "Unlimited High-Res Exports" to "One-tap full-bleed crisp PDF or PNG sharing"
            )

            features.forEach { (title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(desc, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing Package Mockup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PremiumGold, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (language == "bn") "লাইফটাইম মেম্বারশিপ" else "Lifetime Access Plan",
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (language == "bn") "৳ ৪৯০" else "$4.99",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == "bn") "/ এককালীন" else "/ one-time purchase",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = if (language == "bn") "কোনো মাসিক সাবস্ক্রিপশন ফি নেই!" else "No monthly recurring charges. Cancel anytime.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buy Now Trigger
            Button(
                onClick = {
                    viewModel.togglePremium()
                    Toast.makeText(
                        context,
                        if (isPremium) "Premium Mode Disabled!" else "Congratulations! Premium Mode Unlocked!",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("buy_pro_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPremium) Color.Red else PremiumGold,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text(
                    text = if (isPremium) {
                        if (language == "bn") "প্রো লাইসেন্স নিষ্ক্রিয় করুন" else "Deactivate Pro Mode"
                    } else {
                        if (language == "bn") "প্রিমিয়াম আনলক করুন" else "Upgrade To Pro Now"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDeveloperScreen(viewModel: AppViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openLink = { url: String ->
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Could not open link: $url", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == "bn") "ডেভেলপার ও কোম্পানি" else "Developer & Brand",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        },
        containerColor = Color(0xFFF8FAFC) // Slate 50 background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Brand Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)) // Dark Blue to Light Blue
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "∞",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NexVora Lab's Ofc",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        )
                        Text(
                            text = if (language == "bn") "উদ্ভাবনী এন্ড্রয়েড সলিউশন" else "Innovative Android Solutions",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Developer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF2563EB))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == "bn") "ডেভেলপার পরিচিতি" else "About Developer",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Prince AR Abdur Rahman",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569), lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == "bn") "সরাসরি যোগাযোগ ও সামাজিক মাধ্যম:" else "Quick Contacts & Socials:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // WhatsApp Contacts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openLink("https://api.whatsapp.com/send?phone=8801707424006") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp: 01707424006", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(if (language == "bn") "চ্যাট করুন" else "Chat", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openLink("https://api.whatsapp.com/send?phone=8801796951709") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp: 01796951709", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(if (language == "bn") "চ্যাট করুন" else "Chat", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Social buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openLink("https://www.facebook.com/share/1BNn32qoJo/") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Facebook", color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = { openLink("https://www.instagram.com/ur___abdur____rahman__2008") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Instagram", color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // About Company Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF0FDF4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF16A34A))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == "bn") "প্রতিষ্ঠান পরিচিতি" else "About Company",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "NexVora Lab's Ofc",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569), lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (language == "bn") "আমাদের মিশন:" else "Our Mission:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569), fontSize = 13.sp)
                            )
                        }
                    }
                }
            }

            // Technical details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == "bn") "কারিগরি তথ্য" else "Technical Information",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Version", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                        Text("1.0.0", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Framework", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                        Text("Jetpack Compose (M3)", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Local DB", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                        Text("Room SQLite", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Credits Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (language == "bn") "কৃতজ্ঞতা স্বীকার" else "Credits & Copyright",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Developed by Prince AR Abdur Rahman",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    )
                    Text(
                        text = "Published by NexVora Lab's Ofc",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
