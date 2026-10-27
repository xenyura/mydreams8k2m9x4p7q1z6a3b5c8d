package com.xenyura.mydreams

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale

val dt: DreamTheme
    @Composable get() = LocalDT.current

fun rp(n: Long): String = "Rp" + NumberFormat.getInstance(Locale("id", "ID")).format(n)

fun pct(d: Dream): Int = when {
    d.done -> 100
    d.target > 0 -> minOf(100L, d.saved * 100 / d.target).toInt()
    else -> 0
}

// URL (http/https) dipakai apa adanya; path file lokal (diawali "/") dibungkus File
fun imgModel(src: String): Any = if (src.startsWith("/")) File(src) else src

// ---------- Root + navigasi ----------

@Composable
fun AppRoot(s: Store) {
    val t = themeById(s.themeId)
    MyDreamsTheme(t) {
        val view = LocalView.current
        SideEffect {
            (view.context as? Activity)?.window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !t.dark
            }
        }
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val items = listOf(
            "Profile" to Icons.Rounded.Person,
            "Impian" to Icons.Rounded.AutoAwesome,
            "Kemajuan" to Icons.Rounded.BarChart,
            "Keuangan" to Icons.Rounded.AccountBalanceWallet,
            "Pengaturan" to Icons.Rounded.Tune
        )
        Scaffold(
            containerColor = t.bg,
            bottomBar = {
                NavigationBar(containerColor = t.card.compositeOver(t.bg)) {
                    items.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = t.onBtn,
                                indicatorColor = t.btn,
                                unselectedIconColor = t.muted,
                                selectedTextColor = t.text,
                                unselectedTextColor = t.muted
                            )
                        )
                    }
                }
            }
        ) { pad ->
            // Tab Profile: banner memanjang sampai ke belakang status bar, jadi tanpa padding atas
            Box(Modifier.padding(if (tab == 0) PaddingValues(bottom = pad.calculateBottomPadding()) else pad)) {
                when (tab) {
                    0 -> ProfileScreen(s)
                    1 -> DreamsScreen(s)
                    2 -> ProgressScreen(s)
                    3 -> MoneyScreen(s)
                    else -> SettingsScreen(s)
                }
            }
        }
    }
}

// ---------- Komponen ----------

@Composable
fun Page(title: String, content: LazyListScope.() -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = dt.text) }
        content()
    }
}

@Composable
fun DCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = dt.card,
        border = BorderStroke(1.dp, dt.text.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun Gap(h: Int = 8) = Spacer(Modifier.height(h.dp))

@Composable
fun Field(v: String, onV: (String) -> Unit, hint: String, num: Boolean = false) {
    OutlinedTextField(
        value = v,
        onValueChange = onV,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(hint) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (num) KeyboardType.Number else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = dt.text,
            unfocusedTextColor = dt.text,
            focusedBorderColor = dt.btn,
            unfocusedBorderColor = dt.text.copy(alpha = 0.2f),
            cursorColor = dt.btn,
            focusedPlaceholderColor = dt.muted,
            unfocusedPlaceholderColor = dt.muted
        )
    )
}

@Composable
fun Btn(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = dt.btn, contentColor = dt.onBtn)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Bar(p: Float) {
    Box(
        Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(9.dp))
            .background(dt.text.copy(alpha = 0.1f))
    ) {
        Box(
            Modifier.fillMaxWidth(p.coerceIn(0f, 1f)).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(dt.a, dt.b)))
        )
    }
}

@Composable
fun Stat(m: Modifier, v: String, l: String) {
    DCard(m) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(v, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = dt.text)
            Text(l, fontSize = 13.sp, color = dt.muted)
        }
    }
}

@Composable
fun Hero(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(dt.hero)).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

// ---------- Profile ----------

@Composable
fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = dt.muted, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
fun SmallBtn(text: String, modifier: Modifier = Modifier, filled: Boolean = true, onClick: () -> Unit) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = dt.btn, contentColor = dt.onBtn)
        ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, dt.text.copy(alpha = 0.25f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = dt.text)
        ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1) }
    }
}

@Composable
fun GlowRing() {
    val t = rememberInfiniteTransition(label = "glow")
    val a by t.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "glowAlpha"
    )
    Box(Modifier.fillMaxSize().border(3.dp, dt.btn.copy(alpha = a), CircleShape))
}

// Kolom URL: kosong = hapus URL lama, berawalan http = pakai URL baru, lainnya diabaikan
private fun applyUrl(draft: String, current: String, set: (String) -> Unit) {
    val d = draft.trim()
    if (d.isEmpty()) {
        if (current.startsWith("http")) set("")
    } else if (d.startsWith("http", ignoreCase = true) && d != current) {
        set(d)
    }
}

@Composable
fun ProfileScreen(s: Store) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val focus = LocalFocusManager.current

    // Mode edit: tap 3x di foto profil untuk membuka, tap 1x di area luar untuk mengunci
    var editing by rememberSaveable { mutableStateOf(false) }
    var n by rememberSaveable(editing) { mutableStateOf(s.name) }
    var b by rememberSaveable(editing) { mutableStateOf(s.bio) }
    var bu by rememberSaveable(editing) { mutableStateOf(if (s.banner.startsWith("http")) s.banner else "") }
    var au by rememberSaveable(editing) { mutableStateOf(if (s.avatar.startsWith("http")) s.avatar else "") }

    val lock: () -> Unit = {
        if (editing) {
            s.updateProfile(n, b)
            applyUrl(bu, s.banner) { s.setBanner(it) }
            applyUrl(au, s.avatar) { s.setAvatar(it) }
            focus.clearFocus()
            editing = false
        }
    }
    val lockNow by rememberUpdatedState(lock)

    // Efek tap 3x: avatar membal + dua gelombang cincin + getar
    val pop = remember { Animatable(1f) }
    val ring1 = remember { Animatable(0f) }
    val ring2 = remember { Animatable(0f) }
    val onTriple: () -> Unit = {
        editing = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            pop.animateTo(1.22f, tween(110))
            pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        }
        scope.launch { ring1.snapTo(0f); ring1.animateTo(1f, tween(750)) }
        scope.launch { delay(160); ring2.snapTo(0f); ring2.animateTo(1f, tween(750)) }
    }
    val tripleNow by rememberUpdatedState(onTriple)
    var taps by remember { mutableIntStateOf(0) }
    var lastTap by remember { mutableLongStateOf(0L) }

    // Pilih gambar dari HP (disalin ke penyimpanan aplikasi)
    val pickBanner = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val p = withContext(Dispatchers.IO) { s.copyImage(uri, "banner") }
            if (p != null) { s.setBanner(p); bu = "" }
        }
    }
    val pickAvatar = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val p = withContext(Dispatchers.IO) { s.copyImage(uri, "avatar") }
            if (p != null) { s.setAvatar(p); au = "" }
        }
    }

    val d = s.dreams
    val avg = if (d.isEmpty()) 0 else d.sumOf { pct(it) } / d.size
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bannerH = 140.dp + topInset
    val avatarSize = 96.dp
    val ringColor = dt.btn

    Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { lockNow() } }) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ----- Banner + foto profil + nama -----
            item {
                Box(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().height(bannerH)) {
                        Box(Modifier.matchParentSize().background(Brush.linearGradient(dt.hero)))
                        if (s.banner.isNotBlank()) {
                            AsyncImage(
                                model = imgModel(s.banner),
                                contentDescription = "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        // pudar ke warna latar di bagian bawah
                        Box(
                            Modifier.matchParentSize().background(
                                Brush.verticalGradient(
                                    0.35f to Color.Transparent,
                                    0.72f to dt.bg.copy(alpha = 0.85f),
                                    1f to dt.bg
                                )
                            )
                        )
                    }
                    Row(
                        Modifier.padding(start = 22.dp, end = 16.dp, top = bannerH - avatarSize / 2),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Foto profil: separuh di atas (masuk banner), separuh di bawah
                        Box(
                            Modifier.size(avatarSize)
                                .drawBehind {
                                    val base = size.minDimension / 2
                                    listOf(ring1.value, ring2.value).forEach { p ->
                                        if (p > 0f && p < 1f) {
                                            drawCircle(
                                                color = ringColor.copy(alpha = (1f - p) * 0.7f),
                                                radius = base * (1f + 0.5f * p),
                                                style = Stroke(width = 3.dp.toPx())
                                            )
                                        }
                                    }
                                }
                                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        val now = System.currentTimeMillis()
                                        taps = if (now - lastTap < 450) taps + 1 else 1
                                        lastTap = now
                                        if (taps >= 3) { taps = 0; tripleNow() }
                                    }
                                }
                        ) {
                            Box(
                                Modifier.fillMaxSize().clip(CircleShape).background(dt.card.compositeOver(dt.bg)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s.name.take(1).uppercase(), fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = dt.text)
                                if (s.avatar.isNotBlank()) {
                                    AsyncImage(
                                        model = imgModel(s.avatar),
                                        contentDescription = "Foto profil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Box(Modifier.fillMaxSize().border(4.dp, dt.bg, CircleShape))
                            if (editing) {
                                GlowRing()
                                Box(
                                    Modifier.align(Alignment.BottomEnd).size(30.dp).clip(CircleShape)
                                        .background(dt.btn).border(2.dp, dt.bg, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Edit, null, tint = dt.onBtn, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        // Nama: bagian atasnya sejajar dengan bagian atas foto profil
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.name,
                                fontSize = 30.sp,
                                lineHeight = 34.sp,
                                letterSpacing = (-0.5).sp,
                                fontWeight = FontWeight.Black,
                                color = dt.text,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                            if (s.bio.isNotBlank()) {
                                Text(
                                    s.bio,
                                    fontSize = 13.sp,
                                    color = dt.muted,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ----- Statistik -----
            item {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat(Modifier.weight(1f), "${d.size}", "Impian")
                    Stat(Modifier.weight(1f), "${d.count { it.done }}", "Tercapai")
                    Stat(Modifier.weight(1f), "$avg%", "Kemajuan")
                }
            }

            // ----- Panel edit (muncul setelah tap 3x di foto profil) -----
            if (editing) {
                item {
                    DCard(
                        Modifier.padding(horizontal = 16.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = {}) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Edit, null, tint = dt.btn, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Mode edit aktif · ketuk di luar untuk mengunci",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = dt.text
                            )
                        }
                        Gap(12)
                        FieldLabel("Nama")
                        Field(n, { n = it }, "Nama kamu")
                        Gap(12)
                        FieldLabel("Kata-kata impianmu")
                        Field(b, { b = it }, "Kata-kata impianmu")
                        Gap(12)
                        FieldLabel("Banner (URL gambar)")
                        Field(bu, { bu = it }, "https://…")
                        Gap(6)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallBtn("Pilih dari HP", Modifier.weight(1f)) {
                                pickBanner.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            SmallBtn("Hapus", Modifier.width(96.dp), filled = false) {
                                s.setBanner(""); bu = ""
                            }
                        }
                        Gap(12)
                        FieldLabel("Foto profil (URL gambar)")
                        Field(au, { au = it }, "https://…")
                        Gap(6)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallBtn("Pilih dari HP", Modifier.weight(1f)) {
                                pickAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            SmallBtn("Hapus", Modifier.width(96.dp), filled = false) {
                                s.setAvatar(""); au = ""
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Impian ----------

@Composable
fun DreamsScreen(s: Store) {
    var t by remember { mutableStateOf("") }
    var m by remember { mutableStateOf("") }
    Page("Impian") {
        item {
            DCard {
                Field(t, { t = it }, "Apa impianmu?")
                Gap()
                Field(m, { m = it.filter { c -> c.isDigit() } }, "Target dana (Rp), opsional", true)
                Gap()
                Btn("Tambah impian") {
                    s.addDream(t, m.toLongOrNull() ?: 0L)
                    t = ""; m = ""
                }
            }
        }
        if (s.dreams.isEmpty()) {
            item { Text("Belum ada impian. Tulis impian pertamamu di atas.", color = dt.muted) }
        }
        items(s.dreams, key = { it.id }) { d ->
            DCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { s.toggle(d.id) }) {
                        Icon(
                            if (d.done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null, tint = dt.btn
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            d.title, fontWeight = FontWeight.Bold, color = dt.text,
                            textDecoration = if (d.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            if (d.target > 0) "Target " + rp(d.target) else "Tanpa target dana",
                            fontSize = 13.sp, color = dt.muted
                        )
                    }
                    IconButton(onClick = { s.remove(d.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = dt.muted)
                    }
                }
            }
        }
    }
}

// ---------- Kemajuan ----------

@Composable
fun ProgressScreen(s: Store) {
    val d = s.dreams
    val avg = if (d.isEmpty()) 0 else d.sumOf { pct(it) } / d.size
    Page("Kemajuan") {
        item {
            Hero {
                Text("$avg%", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = dt.text)
                Text("Rata-rata kemajuan impian", color = dt.text.copy(alpha = 0.8f))
            }
        }
        if (d.isEmpty()) {
            item { Text("Tambahkan impian untuk melihat kemajuannya.", color = dt.muted) }
        }
        items(d, key = { it.id }) { x ->
            DCard {
                Row {
                    Text(x.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = dt.text)
                    Text("${pct(x)}%", fontWeight = FontWeight.Bold, color = dt.text)
                }
                Gap()
                Bar(pct(x) / 100f)
            }
        }
    }
}

// ---------- Keuangan ----------

@Composable
fun MoneyScreen(s: Store) {
    val d = s.dreams
    Page("Keuangan") {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(Modifier.weight(1f), rp(d.sumOf { it.saved }), "Terkumpul")
                Stat(Modifier.weight(1f), rp(d.sumOf { it.target }), "Total target")
            }
        }
        if (d.isEmpty()) {
            item { Text("Tambahkan impian dulu untuk mencatat tabungan.", color = dt.muted) }
        }
        items(d, key = { it.id }) { x -> MoneyItem(s, x) }
    }
}

@Composable
fun MoneyItem(s: Store, d: Dream) {
    var a by remember(d.id) { mutableStateOf("") }
    DCard {
        Row {
            Text(d.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = dt.text)
            Text(
                rp(d.saved) + if (d.target > 0) " / " + rp(d.target) else "",
                fontSize = 13.sp, color = dt.muted
            )
        }
        if (d.target > 0) {
            Gap()
            Bar(d.saved.toFloat() / d.target)
        }
        Gap()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { Field(a, { a = it.filter { c -> c.isDigit() } }, "Jumlah (Rp)", true) }
            Button(
                onClick = { s.addMoney(d.id, a.toLongOrNull() ?: 0L); a = "" },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = dt.btn, contentColor = dt.onBtn)
            ) { Text("Tambah", fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ---------- Pengaturan ----------

@Composable
fun ModeBtn(m: Modifier, label: String, icon: ImageVector, on: Boolean, onClick: () -> Unit) {
    Row(
        m.clip(RoundedCornerShape(10.dp))
            .background(if (on) dt.btn else Color.Transparent)
            .clickable(onClick = onClick).padding(10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (on) dt.onBtn else dt.muted)
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = if (on) dt.onBtn else dt.muted)
    }
}

@Composable
fun SettingsScreen(s: Store) {
    val cur = themeById(s.themeId)
    Page("Pengaturan") {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, null, tint = dt.btn)
                Spacer(Modifier.width(8.dp))
                Text("Tema aplikasi", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = dt.text)
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(dt.card)
                    .border(1.dp, dt.text.copy(alpha = 0.1f), RoundedCornerShape(14.dp)).padding(4.dp)
            ) {
                ModeBtn(Modifier.weight(1f), "Terang", Icons.Rounded.LightMode, !cur.dark) {
                    if (cur.dark) s.setTheme(Themes.first { !it.dark }.id)
                }
                ModeBtn(Modifier.weight(1f), "Gelap", Icons.Rounded.DarkMode, cur.dark) {
                    if (!cur.dark) s.setTheme(Themes.first { it.dark }.id)
                }
            }
        }
        items(Themes.filter { it.dark == cur.dark }, key = { it.id }) { th ->
            val on = th.id == cur.id
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(dt.card)
                    .border(2.dp, if (on) dt.btn else dt.text.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { s.setTheme(th.id) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(th.bg, th.a, th.b).forEach { col ->
                        Box(Modifier.size(26.dp).clip(CircleShape).background(col).border(1.dp, dt.text.copy(alpha = 0.2f), CircleShape))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(th.name, fontWeight = FontWeight.Bold, color = dt.text)
                    val hex = listOf(th.bg, th.a, th.b).joinToString(" · ") { "#%06X".format(it.toArgb() and 0xFFFFFF) }
                    Text(if (th.id == "pastel-sky") "Default · $hex" else hex, fontSize = 12.sp, color = dt.muted)
                }
                if (on) Icon(Icons.Rounded.Check, null, tint = dt.btn)
            }
        }
    }
}
