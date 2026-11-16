package com.example.ciferki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContent { Ciferki() }
    }
}

private val ink = Color(0xFF17213B)
private val purple = Color(0xFF7157E8)
private val tileColors = listOf(
    Color(0xFFFFD166), Color(0xFF72D6C9), Color(0xFFFF8FA3),
    Color(0xFF8EB8FF), Color(0xFFC6A0F6)
)

private data class LevelInfo(
    val targetSum: Int,
    val columns: Int,
    val tileCount: Int,
    val goal: Int,
    val difficulty: String
)

private fun getLevelInfo(level: Int): LevelInfo = when (level) {
    in 1..3 -> LevelInfo(10, 5, 25, 40 + level * 10, "ЛЁГКАЯ")
    in 4..6 -> LevelInfo(9, 6, 30, 70 + level * 10, "СРЕДНЯЯ")
    else -> LevelInfo(8, 7, 35, 100 + level * 10, "СЛОЖНАЯ")
}

@Composable
fun Ciferki() {
    var page by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var unlocked by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf("Найди одинаковые цифры или сумму 10") }
    var completed by remember { mutableStateOf(false) }
    var numbers by remember { mutableStateOf(List(25) { Random.nextInt(1, 10) }) }

    fun beginLevel(number: Int) {
        level = number
        mode = 1
        score = 0
        selected = null
        completed = false
        val info = getLevelInfo(number)
        numbers = List(info.tileCount) { Random.nextInt(1, 10) }
        message = "Набери ${info.goal} очков • пара или сумма ${info.targetSum}"
        page = 1
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = purple)) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF8F6FF), Color(0xFFECE8FF))))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            when (page) {
                0 -> Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔢", fontSize = 62.sp)
                        Text("ЦИФЫРКИ", fontSize = 36.sp, fontWeight = FontWeight.Black, color = ink)
                        Text("Игра на логику и скорость", color = Color.Gray)
                        Spacer(Modifier.height(14.dp))
                        Text("🏆 РЕКОРД: $best", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = ink)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                mode = 0; score = 0; selected = null; completed = false
                                numbers = List(25) { Random.nextInt(1, 10) }
                                message = "Найди одинаковые цифры или сумму 10"
                                page = 1
                            },
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        ) { Text("🧩  КЛАССИКА", fontSize = 18.sp) }
                        Button(
                            onClick = { page = 2 },
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        ) { Text("🗺️  ВЫБОР УРОВНЯ", fontSize = 18.sp) }
                    }
                }
                2 -> Column(Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("‹", fontSize = 34.sp, color = ink,
                            modifier = Modifier.clickable { page = 0 }.padding(end = 14.dp))
                        Column {
                            Text("ВЫБОР УРОВНЯ", fontSize = 25.sp, fontWeight = FontWeight.Black, color = ink)
                            Text("Открыто уровней: $unlocked из 10", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(10) { index ->
                            val number = index + 1
                            val info = getLevelInfo(number)
                            val isOpen = number <= unlocked
                            Column(
                                Modifier.fillMaxWidth()
                                    .background(if (isOpen) Color.White else Color(0xFFE2E0E9), RoundedCornerShape(16.dp))
                                    .clickable(enabled = isOpen) { beginLevel(number) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(if (isOpen) "⭐" else "🔒", fontSize = 23.sp)
                                Text("УРОВЕНЬ $number", fontWeight = FontWeight.Black,
                                    color = if (isOpen) ink else Color.Gray, fontSize = 13.sp)
                                Text(info.difficulty, fontSize = 11.sp, color = purple, fontWeight = FontWeight.Bold)
                                Text("Сумма ${info.targetSum}", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
                else -> {
                    val info = if (mode == 1) getLevelInfo(level)
                        else LevelInfo(10, 5, 25, Int.MAX_VALUE, "КЛАССИКА")
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("‹", fontSize = 34.sp, color = ink,
                                    modifier = Modifier.clickable { page = if (mode == 1) 2 else 0 }.padding(end = 10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (mode == 1) "УРОВЕНЬ $level • ${info.difficulty}" else "КЛАССИКА",
                                        fontSize = 20.sp, fontWeight = FontWeight.Black, color = ink)
                                    Text(message, fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Text("$score", fontSize = 27.sp, fontWeight = FontWeight.Black, color = purple)
                            }
                            Spacer(Modifier.height(8.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(info.columns),
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(numbers) { index, value ->
                                    Box(
                                        Modifier.aspectRatio(1.18f)
                                            .background(
                                                if (selected == index) purple else tileColors[(index + value) % tileColors.size],
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable(enabled = !completed) {
                                                val first = selected
                                                if (first == null) {
                                                    selected = index
                                                    message = "Теперь выбери вторую цифру"
                                                } else if (first != index) {
                                                    val a = numbers[first]
                                                    if (a == value || a + value == info.targetSum) {
                                                        val points = if (a == value) 10 else 15
                                                        score += points
                                                        best = maxOf(best, score)
                                                        val updated = numbers.toMutableList()
                                                        updated[first] = Random.nextInt(1, 10)
                                                        updated[index] = Random.nextInt(1, 10)
                                                        numbers = updated
                                                        if (mode == 1 && score >= info.goal) {
                                                            completed = true
                                                            unlocked = maxOf(unlocked, minOf(10, level + 1))
                                                            message = "УРОВЕНЬ ПРОЙДЕН! Отличная работа!"
                                                        } else {
                                                            message = "+$points очков! Ищи следующую пару"
                                                        }
                                                    } else {
                                                        message = "Не совпало. Ищи пару или сумму ${info.targetSum}"
                                                    }
                                                    selected = null
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$value", fontSize = 24.sp, fontWeight = FontWeight.Black,
                                            color = if (selected == index) Color.White else ink)
                                    }
                                }
                            }
                        }
                        Column(
                            Modifier.width(190.dp).fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (mode == 1) "ЦЕЛЬ: ${info.goal}" else "РЕКОРД: $best",
                                fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ink)
                            if (mode == 1) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (score.toFloat() / info.goal).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("Сложность: ${info.difficulty}", fontSize = 12.sp, color = purple)
                            }
                            Spacer(Modifier.height(16.dp))
                            if (completed) {
                                Button(onClick = { beginLevel(minOf(10, level + 1)) },
                                    modifier = Modifier.fillMaxWidth()) { Text("СЛЕДУЮЩИЙ УРОВЕНЬ") }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { page = 2 }, modifier = Modifier.fillMaxWidth()) {
                                    Text("К УРОВНЯМ")
                                }
                            } else {
                                Button(onClick = {
                                    numbers = List(info.tileCount) { Random.nextInt(1, 10) }
                                    selected = null
                                    message = "Поле обновлено. Продолжай!"
                                }, modifier = Modifier.fillMaxWidth()) { Text("Перемешать") }
                                if (mode == 1) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = { page = 2 }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Выбор уровней")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
