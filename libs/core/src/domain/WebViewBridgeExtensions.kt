package domain

import com.parkwoocheol.composewebview.WebViewJsBridge
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ConsoleMessage(val level: String, val args: List<String>)

/**
 * Registers a handler for the "console" bridge event, forwarding game-side
 * console.log/info/warn/error calls to the platform log.
 *
 * Call this inside a LaunchedEffect on your bridge before passing it to WebGLWebView.
 * Requires the game to send console output via window.AppBridge.call("console", ...).
 */
fun WebViewJsBridge.logGameConsole() {
    register<String, Unit>("console") { jsonString ->
        runCatching {
            val msg = Json.decodeFromString<ConsoleMessage>(jsonString)
            println("[Game.${msg.level}] ${msg.args.joinToString(" ")}")
        }.onFailure {
            println("[Game.raw] $jsonString")
        }
    }
}
