package com.mysticraccoon.dashdotinputview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mysticraccoon.dashdotinputview.ui.theme.DarkGreen
import com.mysticraccoon.dashdotinputview.ui.theme.DashDotInputViewTheme
import com.mysticraccoon.dashdotinputview.ui.theme.GlassBlack
import com.mysticraccoon.dashdotinputview.ui.theme.LightColor
import com.mysticraccoon.dashdotinputview.ui.theme.LightColorAlpha
import com.mysticraccoon.dashdotinputview.ui.theme.LightRed
import com.mysticraccoon.dashdotinputview.ui.theme.Silver
import com.mysticraccoon.dashdotinputview.ui.theme.TableBrown
import com.mysticraccoon.dashdotinputview.ui.theme.WallBrown
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach


/**
 * There are rules to help people distinguish dots from dashes in Morse code.
The length of a dot is 1 time unit.
A dash is 3 time units.
The space between symbols (dots and dashes) of the same letter is 1 time unit.
The space between letters is 3 time units.
The space between words is 7 time units.
 *
 * */

const val DEFAULT_TIMER_TIME = 500L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashDotInputViewTheme {
                MorseScreen()
            }
        }
    }
}

@Preview
@Composable
fun MorseScreenPreview() {
    DashDotInputViewTheme {
        MorseScreen()
    }
}

@Composable
fun MorseScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGreen)

    ) {

        var morseText by remember {
            mutableStateOf("")
        }

        var alphaText by remember {
            mutableStateOf("")
        }

        Image(
            painter = painterResource(id = R.drawable.concrete_wall),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alpha = .2f
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(LightColor, LightColorAlpha, Color.Transparent),
                        center = Offset(getScreenWidth(), -20f),
                        radius = 700f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(modifier = Modifier.weight(.20f))
            MorseBook(modifier = Modifier.weight(.4f), morseText, alphaText)
            MorseBox(modifier = Modifier.weight(.35f), morseTextChanged = {
                morseText = it
            }, alphaTextChanged = {
                alphaText = it
            })
        }
    }
}

@Composable
fun MorseBook(modifier: Modifier = Modifier, morseText: String, alphaText: String) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.open_blank_book),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            Text(
                text = morseText, modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(.5f)
            )
            Text(
                text = alphaText, modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f)
            )
        }
    }

}

@Composable
fun MorseBox(
    modifier: Modifier = Modifier,
    morseTextChanged: (String) -> Unit,
    alphaTextChanged: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(WallBrown)
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TableBrown)

    ) {

        var metronomeInterval by remember {
            mutableStateOf(DEFAULT_TIMER_TIME)
        }

        val dashInterval by remember {
            derivedStateOf { metronomeInterval * 3L }
        }

        val newWordInterval by remember {
            derivedStateOf { metronomeInterval * 7L }
        }

        val metronomeIntervalText by remember {
            derivedStateOf { metronomeInterval.toString()}
        }

        var isMetronomeStopped by remember {
            mutableStateOf(true)
        }
        var tic by remember {
            mutableStateOf(true)
        }

        var currentElapsedTime by remember {
            mutableStateOf(0L)
        }

        var lastElapsedTime by remember {
            mutableStateOf(0L)
        }

        var duration by remember {
            mutableStateOf(0L)
        }

        var isMorseTimerRunning by remember {
            mutableStateOf(false)
        }

        var pauseDuration by remember {
            mutableStateOf(0L)
        }

        val interactionSource = remember { MutableInteractionSource() }

        val morseText = remember { MutableStateFlow("") }
        LaunchedEffect(morseTextChanged) {
            morseText
                .onEach { println(it) }
                .collect {
                    morseTextChanged(it)
                    alphaTextChanged(translateMorseToAlpha(it))
                }
        }

        LaunchedEffect(key1 = metronomeInterval, key2 = isMetronomeStopped) {
            while (!isMetronomeStopped) {
                tic = !tic
                delay(metronomeInterval)
            }
        }

        LaunchedEffect(key1 = interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (!isMetronomeStopped) {
                    when (interaction) {
                        is PressInteraction.Press -> {
                            lastElapsedTime = System.currentTimeMillis()
                            pauseDuration = lastElapsedTime - currentElapsedTime
                            isMorseTimerRunning = true
                        }

                        is PressInteraction.Release -> {
                            currentElapsedTime = System.currentTimeMillis()
                            duration = currentElapsedTime - lastElapsedTime
                            isMorseTimerRunning = false
                        }

                        is PressInteraction.Cancel -> {
                            currentElapsedTime = System.currentTimeMillis()
                            duration = currentElapsedTime - lastElapsedTime
                            isMorseTimerRunning = false
                        }
                    }
                }
            }

        }

        LaunchedEffect(key1 = duration) {
            Log.d("260423", "Duration: $duration")
            if (!isMetronomeStopped) {
                if (duration >= dashInterval) {
                    morseText.value += '-'
                } else {
                    morseText.value += '.'
                }
            }
        }

        LaunchedEffect(key1 = pauseDuration) {
            Log.d("260423", "Pause: $duration")
            if (!isMetronomeStopped) {
                if (pauseDuration >= newWordInterval) {
                    //new word
                    morseText.value += '|'
                } else if (pauseDuration >= dashInterval) {
                    //new letter
                    morseText.value += '/'
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(all = 16.dp)) {
                OldMetalCircularButton(
                    modifier = Modifier.size(50.dp),
                    onClickButton = {
                        if(metronomeInterval > 100){
                            metronomeInterval -= 100
                        }
                    },
                    buttonVector = Icons.Filled.Remove,
                    contentDescription = "Decrease"
                )
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .widthIn(150.dp)
                        .border(width = 4.dp, color = Silver, shape = CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        Text(text = metronomeIntervalText, fontSize = 32.sp, color = Color.Red)
                        Text(text = "ms", color = Color.Red)
                    }
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .widthIn(150.dp)
                            .clip(CircleShape)
                            .background(GlassBlack)
                    )
                }
                OldMetalCircularButton(
                    modifier = Modifier.size(50.dp),
                    onClickButton = {
                        if(metronomeInterval < 1000){
                            metronomeInterval += 100
                        }
                    },
                    buttonVector = Icons.Filled.Add,
                    contentDescription = "Increase"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OldMetalCircularButton(
                    modifier = Modifier.size(50.dp),
                    onClickButton = {
                        isMetronomeStopped = false
                    },
                    buttonVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start"
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.botao_vermelho),
                        contentDescription = "metronome light",
                        modifier = Modifier.size(50.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (tic) Color.Transparent else LightRed)
                    )
                }
                OldMetalCircularButton(
                    modifier = Modifier.size(50.dp),
                    onClickButton = {
                        isMetronomeStopped = true
                    },
                    buttonVector = Icons.Filled.Stop,
                    contentDescription = "Stop"
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp)
                .border(width = 4.dp, color = Silver, shape = RoundedCornerShape(8.dp))
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassBlack)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ) { /* update some business state here */ }
//                .pointerInput(Unit) {
//                    awaitEachGesture {
//                        while (true) {
//                            withTimeoutOrNull(newWordInterval) {
//                                awaitFirstDown()
//                            } ?: run {
//                                //space
//                                morseText += '%'
//                            }
//                            val upEvent = waitForUpOrCancellation() ?: continue
//                            val duration = upEvent.uptimeMillis - upEvent.previousUptimeMillis
//                            Log.d("260423", "Duration: $duration")
//                            if (duration >= dashInterval) {
//                                morseText += '-'
//                            } else {
//                                morseText += '.'
//                            }
//                        }
//                    }
//                },
        ) {

        }
    }
}

@Composable
fun getScreenWidth(): Float {
    return LocalConfiguration.current.screenWidthDp.toFloat()
}

@Composable
fun OldMetalCircularButton(
    modifier: Modifier = Modifier,
    onClickButton: () -> Unit,
    buttonVector: ImageVector,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClickButton),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(id = R.drawable.old_metal_button), contentDescription = contentDescription)
        Image(imageVector = buttonVector, contentDescription = "Stop")
    }
}

fun translateMorseToAlpha(morseText: String): String {
    var text = ""
    val words = morseText.split("|")
    words.forEach {word ->
        val letters = word.split("/")
        text += letters.map { morseToAlphaMap[it] ?: "" }.joinToString("")
        text += " "
    }
    return text.trim()
}

val morseToAlphaMap = mapOf(
    ".-" to 'A',
    "-..." to 'B',
    "-.-." to 'C',
    "-.." to 'D',
    "." to 'E',
    "..-." to 'F',
    "--." to 'G',
    "...." to 'H',
    ".." to 'I',
    ".---" to 'J',
    "-.-" to 'K',
    ".-.." to 'L',
    "--" to 'M',
    "-." to 'N',
    "---" to 'O',
    ".--." to 'P',
    "--.-" to 'Q',
    ".-." to 'R',
    "..." to 'S',
    "-" to 'T',
    "..-" to 'U',
    "...-" to 'V',
    ".--" to 'W',
    "-..-" to 'X',
    "-.--" to 'Y',
    "--.." to 'Z',
    ".----" to '1',
    "..---" to '2',
    "...--" to '3',
    "....-" to '4',
    "....." to '5',
    "-...." to '6',
    "--..." to '7',
    "---.." to '8',
    "----." to '9',
    "-----" to '0',
)