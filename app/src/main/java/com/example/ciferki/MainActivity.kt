package com.example.ciferki
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{Ciferki()}}}
private val ink=Color(0xFF17213B);private val purple=Color(0xFF7157E8)
@Composable fun Ciferki(){
 var page by remember{mutableIntStateOf(0)};var mode by remember{mutableIntStateOf(0)}
 var score by remember{mutableIntStateOf(0)};var best by remember{mutableIntStateOf(0)};var level by remember{mutableIntStateOf(1)}
 var chosen by remember{mutableStateOf<Int?>(null)};var note by remember{mutableStateOf("Найди одинаковые цифры или сумму 10")}
 var nums by remember{mutableStateOf(List(25){Random.nextInt(1,10)})}
 val colors=listOf(Color(0xFFFFD166),Color(0xFF72D6C9),Color(0xFFFF8FA3),Color(0xFF8EB8FF),Color(0xFFC6A0F6))
 MaterialTheme(colorScheme=lightColorScheme(primary=purple)){
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF8F6FF),Color(0xFFECE8FF)))).padding(18.dp)){
 if(page==0) Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
 Text("🔢",fontSize=64.sp);Text("ЦИФЫРКИ",fontSize=36.sp,fontWeight=FontWeight.Black,color=ink);Text("Игра на логику и скорость",color=Color.Gray);Spacer(Modifier.height(28.dp))
 Button(onClick={mode=0;score=0;level=1;chosen=null;nums=List(25){Random.nextInt(1,10)};note="Найди пару или сумму 10";page=1},modifier=Modifier.fillMaxWidth().height(58.dp)){Text("🧩  КЛАССИКА")}
 Spacer(Modifier.height(12.dp))
 Button(onClick={mode=1;score=0;level=1;chosen=null;nums=List(25){Random.nextInt(1,10)};note="Набери 50 очков";page=1},modifier=Modifier.fillMaxWidth().height(58.dp)){Text("🚀  РЕЖИМ УРОВНЕЙ")}
 Spacer(Modifier.height(24.dp));Text("🏆 РЕКОРД: $best",fontSize=22.sp,fontWeight=FontWeight.Bold,color=ink)
 }else Column(Modifier.fillMaxSize()){
 Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()){
 Text("‹",fontSize=36.sp,color=ink,modifier=Modifier.clickable{page=0}.padding(end=14.dp))
 Column(Modifier.weight(1f)){Text(if(mode==0)"КЛАССИКА" else "УРОВЕНЬ $level",fontWeight=FontWeight.Black,color=ink,fontSize=20.sp);Text(note,fontSize=12.sp,color=Color.DarkGray)}
 Text("$score",fontSize=27.sp,fontWeight=FontWeight.Black,color=purple)
 }
 Spacer(Modifier.height(14.dp))
 LazyVerticalGrid(columns=GridCells.Fixed(5),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
 itemsIndexed(nums){i,n->Box(Modifier.aspectRatio(.88f).background(if(chosen==i)purple else colors[(i+n)%colors.size],RoundedCornerShape(15.dp)).clickable{
 if(chosen==null){chosen=i;note="Выбери вторую цифру"}else if(chosen!=i){
 val a=nums[chosen!!]
 if(a==n||a+n==10){val add=if(a==n)10 else 15;score+=add;best=maxOf(best,score);val copy=nums.toMutableList();copy[chosen!!]=Random.nextInt(1,10);copy[i]=Random.nextInt(1,10);nums=copy
 if(mode==1&&score>=level*50){level++;note="Уровень пройден! Цель: "+(level*50)}else note="+$add очков! Отлично 🎉"
 }else note="Не совпало — попробуй ещё";chosen=null
 }
 },contentAlignment=Alignment.Center){Text("$n",fontSize=27.sp,fontWeight=FontWeight.Black,color=ink)}}
 }
 Button(onClick={nums=List(25){Random.nextInt(1,10)};chosen=null;note="Поле перемешано"},modifier=Modifier.fillMaxWidth()){Text("Перемешать поле")}
 }
 }
 }
}
