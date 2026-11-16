package com.example.ciferki

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Ciferki(this) }
    }
}

private val ink = Color(0xFF17213B)
private val purple = Color(0xFF7157E8)

private data class Player(
    val id: String,
    val name: String,
    val salt: String,
    val passwordHash: String,
    val score: Int = 0,
    val best: Int = 0,
    val level: Int = 1
)

private object AccountStore {
    private const val PREFS = "ciferki_accounts"
    private const val USERS = "users"
    private const val SESSION = "session"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readUsers(context: Context): MutableMap<String, Player> {
        val raw = prefs(context).getString(USERS, "{}") ?: "{}"
        val json = JSONObject(raw)
        val users = mutableMapOf<String, Player>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val id = keys.next()
            val item = json.getJSONObject(id)
            users[id] = Player(
                id, item.optString("name"), item.optString("salt"),
                item.optString("hash"), item.optInt("score"), item.optInt("best"),
                item.optInt("level", 1)
            )
        }
        return users
    }

    private fun saveUsers(context: Context, users: Map<String, Player>) {
        val json = JSONObject()
        users.values.forEach { p ->
            json.put(p.id, JSONObject().put("name", p.name).put("salt", p.salt)
                .put("hash", p.passwordHash).put("score", p.score)
                .put("best", p.best).put("level", p.level))
        }
        prefs(context).edit().putString(USERS, json.toString()).apply()
    }

    private fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun register(context: Context, name: String, password: String): Player {
        val users = readUsers(context)
        val id = "CIF-" + UUID.randomUUID().toString().replace("-", "")
            .take(8).uppercase()
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val player = Player(id, name.trim(), Base64.encodeToString(salt, Base64.NO_WRAP),
            hash(password, salt))
        users[id] = player
        saveUsers(context, users)
        prefs(context).edit().putString(SESSION, id).apply()
        return player
    }

    fun login(context: Context, idInput: String, password: String): Player? {
        val users = readUsers(context)
        val id = idInput.trim().uppercase()
        val player = users[id] ?: return null
        val salt = Base64.decode(player.salt, Base64.NO_WRAP)
        if (hash(password, salt) != player.passwordHash) return null
        prefs(context).edit().putString(SESSION, id).apply()
        return player
    }

    fun current(context: Context): Player? {
        val id = prefs(context).getString(SESSION, null) ?: return null
        return readUsers(context)[id]
    }

    fun update(context: Context, player: Player) {
        val users = readUsers(context)
        if (users.containsKey(player.id)) {
            users[player.id] = player
            saveUsers(context, users)
        }
    }

    fun logout(context: Context) {
        prefs(context).edit().remove(SESSION).apply()
    }
}

@Composable
fun Ciferki(context: Context) {
    var player by remember { mutableStateOf(AccountStore.current(context)) }
    var authMode by remember { mutableIntStateOf(0) }
    var authError by remember { mutableStateOf("") }
    var newId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var page by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(player?.score ?: 0) }
    var best by remember { mutableIntStateOf(player?.best ?: 0) }
    var level by remember { mutableIntStateOf(player?.level ?: 1) }
    var chosen by remember { mutableStateOf<Int?>(null) }
    var note by remember { mutableStateOf("Найди одинаковые цифры или сумму 10") }
    var nums by remember { mutableStateOf(List(25) { Random.nextInt(1, 10) }) }
    val colors = listOf(Color(0xFFFFD166), Color(0xFF72D6C9), Color(0xFFFF8FA3), Color(0xFF8EB8FF), Color(0xFFC6A0F6))

    MaterialTheme(colorScheme = lightColorScheme(primary = purple)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF8F6FF), Color(0xFFECE8FF)))).padding(18.dp)) {
            if (player == null) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("🔢", fontSize = 58.sp)
                    Text("ЦИФЫРКИ", fontSize = 34.sp, fontWeight = FontWeight.Black, color = ink)
                    Text("Твой игровой аккаунт", color = Color.Gray)
                    Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { authMode = 0; authError = "" }, modifier = Modifier.weight(1f)) { Text("Войти") }
                        OutlinedButton(onClick = { authMode = 1; authError = "" }, modifier = Modifier.weight(1f)) { Text("Регистрация") }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (authMode == 0) {
                        OutlinedTextField(value = loginId, onValueChange = { loginId = it }, label = { Text("Игровой ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = loginPassword, onValueChange = { loginPassword = it }, label = { Text("Пароль") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            val result = AccountStore.login(context, loginId, loginPassword)
                            if (result == null) authError = "Неверный ID или пароль" else {
                                player = result; score = result.score; best = result.best; level = result.level; authError = ""
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("ВОЙТИ В ИГРУ") }
                    } else {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Имя игрока") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль (от 6 символов)") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            if (name.trim().length < 2) authError = "Имя должно быть не короче 2 символов"
                            else if (password.length < 6) authError = "Пароль должен содержать минимум 6 символов"
                            else {
                                val result = AccountStore.register(context, name, password)
                                player = result; score = 0; best = 0; level = 1
                                newId = result.id; authError = ""
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("СОЗДАТЬ АККАУНТ") }
                        if (newId.isNotBlank()) {
                            Text("ТВОЙ ID: $newId", color = purple, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("Сохрани ID — он понадобится для входа на этом устройстве", color = Color.DarkGray)
                        }
                    }
                    if (authError.isNotBlank()) Text(authError, color = Color(0xFFB3261E), modifier = Modifier.padding(top = 8.dp))
                }
            } else if (page == 2) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("👤", fontSize = 56.sp)
                    Text(player!!.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = ink)
                    Text("ID: ${player!!.id}", color = purple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(18.dp))
                    Text("🏆 Лучший результат: $best", fontSize = 19.sp, color = ink)
                    Text("⭐ Уровень: $level", fontSize = 19.sp, color = ink)
                    Text("🎯 Очки в текущей игре: $score", fontSize = 19.sp, color = ink)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { page = 0 }, modifier = Modifier.fillMaxWidth()) { Text("В ИГРУ") }
                    OutlinedButton(onClick = { AccountStore.logout(context); player = null; page = 0; newId = "" }, modifier = Modifier.fillMaxWidth()) { Text("ВЫЙТИ ИЗ АККАУНТА") }
                }
            } else if (page == 0) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        Text("👤 ${player!!.name}", color = purple, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { page = 2 })
                    }
                    Text("🔢", fontSize = 64.sp)
                    Text("ЦИФЫРКИ", fontSize = 36.sp, fontWeight = FontWeight.Black, color = ink)
                    Text("Игра на логику и скорость", color = Color.Gray)
                    Spacer(Modifier.height(28.dp))
                    Button(onClick = { mode = 0; score = 0; level = 1; chosen = null; nums = List(25) { Random.nextInt(1, 10) }; note = "Найди пару или сумму 10"; page = 1 }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("🧩  КЛАССИКА") }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { mode = 1; score = 0; level = 1; chosen = null; nums = List(25) { Random.nextInt(1, 10) }; note = "Набери 50 очков"; page = 1 }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("🚀  РЕЖИМ УРОВНЕЙ") }
                    Spacer(Modifier.height(24.dp))
                    Text("🏆 РЕКОРД: $best", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink)
                    Text("Открыть профиль", color = purple, modifier = Modifier.clickable { page = 2 }.padding(10.dp))
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("‹", fontSize = 36.sp, color = ink, modifier = Modifier.clickable { page = 0 }.padding(end = 14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (mode == 0) "КЛАССИКА" else "УРОВЕНЬ $level", fontWeight = FontWeight.Black, color = ink, fontSize = 20.sp)
                            Text(note, fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Text("$score", fontSize = 27.sp, fontWeight = FontWeight.Black, color = purple)
                    }
                    Spacer(Modifier.height(14.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(nums) { i, n ->
                            Box(Modifier.aspectRatio(.88f).background(if (chosen == i) purple else colors[(i + n) % colors.size], RoundedCornerShape(15.dp)).clickable {
                                if (chosen == null) { chosen = i; note = "Выбери вторую цифру" }
                                else if (chosen != i) {
                                    val a = nums[chosen!!]
                                    if (a == n || a + n == 10) {
                                        val add = if (a == n) 10 else 15
                                        score += add; best = maxOf(best, score)
                                        val copy = nums.toMutableList(); copy[chosen!!] = Random.nextInt(1, 10); copy[i] = Random.nextInt(1, 10); nums = copy
                                        if (mode == 1 && score >= level * 50) { level++; note = "Уровень пройден! Цель: ${level * 50}" } else note = "+$add очков! Отлично 🎉"
                                        player = player!!.copy(score = score, best = best, level = level)
                                        AccountStore.update(context, player!!)
                                    } else note = "Не совпало — попробуй ещё"
                                    chosen = null
                                }
                            }, contentAlignment = Alignment.Center) { Text("$n", fontSize = 27.sp, fontWeight = FontWeight.Black, color = ink) }
                        }
                    }
                    Button(onClick = { nums = List(25) { Random.nextInt(1, 10) }; chosen = null; note = "Поле перемешано" }, modifier = Modifier.fillMaxWidth()) { Text("Перемешать поле") }
                }
            }
        }
    }
}
