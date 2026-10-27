package com.xenyura.mydreams

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class Dream(
    val id: Long,
    val title: String,
    val target: Long,
    val saved: Long,
    val done: Boolean
)

class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("mydreams", Context.MODE_PRIVATE)

    var name by mutableStateOf(sp.getString("name", "Pemimpi") ?: "Pemimpi")
        private set
    var bio by mutableStateOf(sp.getString("bio", "Setiap impian dimulai dari satu langkah.") ?: "")
        private set
    var themeId by mutableStateOf(sp.getString("theme", "pastel-sky") ?: "pastel-sky")
        private set
    val dreams = mutableStateListOf<Dream>()

    init {
        runCatching {
            val a = JSONArray(sp.getString("dreams", "[]"))
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                dreams.add(Dream(o.getLong("id"), o.getString("t"), o.getLong("target"), o.getLong("saved"), o.getBoolean("done")))
            }
        }
    }

    private fun save() {
        val a = JSONArray()
        dreams.forEach {
            a.put(JSONObject().put("id", it.id).put("t", it.title).put("target", it.target).put("saved", it.saved).put("done", it.done))
        }
        sp.edit().putString("name", name).putString("bio", bio).putString("theme", themeId)
            .putString("dreams", a.toString()).apply()
    }

    private fun update(id: Long, f: (Dream) -> Dream) {
        val i = dreams.indexOfFirst { it.id == id }
        if (i >= 0) { dreams[i] = f(dreams[i]); save() }
    }

    fun updateProfile(n: String, b: String) {
        name = n.trim().ifEmpty { name }
        bio = b.trim()
        save()
    }

    fun setTheme(id: String) { themeId = id; save() }

    fun addDream(title: String, target: Long) {
        if (title.isBlank()) return
        dreams.add(0, Dream(System.currentTimeMillis(), title.trim(), target, 0, false))
        save()
    }

    fun toggle(id: Long) = update(id) { it.copy(done = !it.done) }

    fun remove(id: Long) { dreams.removeAll { it.id == id }; save() }

    fun addMoney(id: Long, amount: Long) {
        if (amount > 0) update(id) { it.copy(saved = it.saved + amount) }
    }
}
