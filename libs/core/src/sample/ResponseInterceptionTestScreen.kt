package sample

import WebGLWebView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkwoocheol.composewebview.rememberWebViewController
import com.parkwoocheol.composewebview.rememberWebViewJsBridge
import domain.ComposeResourceAssetReader
import domain.logGameConsole

private enum class InputMode { Linear, Button }

private data class KeyDef(val label: String, val key: String, val value: Float)

private val KEY_UP    = KeyDef("↑",     "ArrowUp",    0.75f)
private val KEY_DOWN  = KeyDef("↓",     "ArrowDown",  0.25f)
private val KEY_LEFT  = KeyDef("←",     "ArrowLeft",  0.4f)
private val KEY_RIGHT = KeyDef("→",     "ArrowRight", 0.6f)
private val KEY_SPACE = KeyDef("Space", " ",           1.0f)

private fun keyCode(key: String) = if (key == " ") "Space" else key

private fun keyEventJs(type: String, key: String) =
    "window.dispatchEvent(new KeyboardEvent('$type',{key:'$key',code:'${keyCode(key)}',bubbles:true,cancelable:true}));"

@Composable
fun ResponseInterceptionTestScreen() {
    val reader = remember { ComposeResourceAssetReader() }
    var inputValue by remember { mutableStateOf(0f) }
    var inputMode by remember { mutableStateOf(InputMode.Linear) }
    val bridge = rememberWebViewJsBridge()
    val controller = rememberWebViewController()

    LaunchedEffect(bridge) {
        bridge.logGameConsole()
    }

    LaunchedEffect(inputValue) {
        bridge.emit("sensor_data", inputValue)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WebGLWebView(
            startPath = "test/index.html",
            reader = reader,
            jsBridge = bridge,
            controller = controller,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        InputPanel(
            mode = inputMode,
            onModeChange = { inputMode = it; inputValue = 0f },
            onValue = { inputValue = it },
            onKeyDown = { key -> controller.evaluateJavascript(keyEventJs("keydown", key), null) },
            onKeyUp   = { key -> controller.evaluateJavascript(keyEventJs("keyup",   key), null) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InputPanel(
    mode: InputMode,
    onModeChange: (InputMode) -> Unit,
    onValue: (Float) -> Unit,
    onKeyDown: (String) -> Unit,
    onKeyUp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.background(Color(0xFF1E1E1E))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(InputMode.Linear to "Linear", InputMode.Button to "Button").forEach { (m, label) ->
                Button(
                    onClick = { onModeChange(m) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (m == mode) MaterialTheme.colorScheme.primary else Color(0xFF3A3A3A),
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                ) {
                    Text(text = label, fontSize = 13.sp)
                }
            }
        }
        when (mode) {
            InputMode.Linear -> LinearInputControl(
                onValue = onValue,
                modifier = Modifier.fillMaxWidth().height(112.dp),
            )
            InputMode.Button -> ButtonInputControl(
                onValue = onValue,
                onKeyDown = onKeyDown,
                onKeyUp = onKeyUp,
                modifier = Modifier.fillMaxWidth().height(112.dp),
            )
        }
    }
}

@Composable
private fun LinearInputControl(onValue: (Float) -> Unit, modifier: Modifier = Modifier) {
    var thumbFraction by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = thumbFraction.toString(),
            color = Color(0xFF888888),
            fontSize = 12.sp,
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { thumbFraction = 0f; onValue(0f) },
                        onDragCancel = { thumbFraction = 0f; onValue(0f) },
                    ) { change, _ ->
                        thumbFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValue(thumbFraction)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            val thumbDp = 28.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF555555)),
            )
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - thumbDp) * thumbFraction)
                    .size(thumbDp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun ButtonInputControl(
    onValue: (Float) -> Unit,
    onKeyDown: (String) -> Unit,
    onKeyUp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameButton(KEY_UP,    onValue, onKeyDown, onKeyUp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameButton(KEY_LEFT,  onValue, onKeyDown, onKeyUp)
            GameButton(KEY_SPACE, onValue, onKeyDown, onKeyUp)
            GameButton(KEY_RIGHT, onValue, onKeyDown, onKeyUp)
        }
        GameButton(KEY_DOWN,  onValue, onKeyDown, onKeyUp)
    }
}

@Composable
private fun GameButton(
    def: KeyDef,
    onValue: (Float) -> Unit,
    onKeyDown: (String) -> Unit,
    onKeyUp: (String) -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (pressed) MaterialTheme.colorScheme.primary else Color(0xFF3A3A3A))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onValue(def.value)
                        onKeyDown(def.key)
                        tryAwaitRelease()
                        pressed = false
                        onValue(0f)
                        onKeyUp(def.key)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = def.label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
