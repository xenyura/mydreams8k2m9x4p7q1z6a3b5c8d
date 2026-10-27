package com.xenyura.mydreams

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
            Box(Modifier.padding(pad)) {
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
fun ProfileScreen(s: Store) {
    var n by remember(s.name) { mutableStateOf(s.name) }
    var b by remember(s.bio) { mutableStateOf(s.bio) }
    val d = s.dreams
    val avg = if (d.isEmpty()) 0 else d.sumOf { pct(it) } / d.size
    Page("Profile") {
        item {
            Hero {
                Box(
                    Modifier.size(78.dp).clip(CircleShape).background(dt.card).border(3.dp, dt.btn, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.name.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = dt.text)
                }
                Gap()
                Text(s.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dt.text)
                Text(s.bio, color = dt.text.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(Modifier.weight(1f), "${d.size}", "Impian")
                Stat(Modifier.weight(1f), "${d.count { it.done }}", "Tercapai")
                Stat(Modifier.weight(1f), "$avg%", "Kemajuan")
            }
        }
        item { Text("Ubah profil", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = dt.text) }
        item { Field(n, { n = it }, "Nama kamu") }
        item { Field(b, { b = it }, "Kata-kata impianmu") }
        item { Btn("Simpan profil") { s.updateProfile(n, b) } }
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
