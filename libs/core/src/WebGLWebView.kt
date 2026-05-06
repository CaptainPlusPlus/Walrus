import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewJsBridge
import com.parkwoocheol.composewebview.WebViewSettings
import com.parkwoocheol.composewebview.client.onConsoleMessage
import com.parkwoocheol.composewebview.client.rememberWebChromeClient
import com.parkwoocheol.composewebview.client.rememberWebViewClient
import com.parkwoocheol.composewebview.client.shouldInterceptRequest
import com.parkwoocheol.composewebview.rememberWebViewController
import com.parkwoocheol.composewebview.rememberWebViewState
import domain.AssetReader
import kotlinx.coroutines.runBlocking
import model.assetHost
import model.baseAssetUrl
import model.buildResponse
import model.mimeTypeForPath
import util.extractLocalPath
import util.resolveCompression

/**
 * Composable that serves a WebGL game from composeResources via request interception.
 *
 * Asset serving is handled internally. All communication with the game (emitting events,
 * registering handlers) is the caller's responsibility via [jsBridge]. JavaScript evaluation
 * (e.g. for synthetic key events) is available via [controller].
 *
 * @param startPath Path to the entry point under composeResources/files/, e.g. "yourGame/index.html".
 * @param reader Provides the raw bytes for each intercepted request.
 * @param jsBridge Optional bridge for bidirectional JS communication. Create with
 *   rememberWebViewJsBridge(), configure your handlers in a LaunchedEffect, then pass here.
 * @param controller WebViewController for imperative operations (evaluateJavascript, loadUrl, etc.).
 *   Create with rememberWebViewController() and keep a reference if you need to evaluate JS
 *   from outside the composable (e.g. for synthetic keyboard events).
 */
@Composable
fun WebGLWebView(
    startPath: String,
    reader: AssetReader,
    modifier: Modifier = Modifier,
    jsBridge: WebViewJsBridge? = null,
    controller: WebViewController = rememberWebViewController(),
) {
    val state = rememberWebViewState(url = baseAssetUrl + startPath)

    val settings = WebViewSettings.Default.copy(
        javaScriptEnabled = true,
        domStorageEnabled = true,
        interceptedSchemes = listOf("app"),
    )

    val client = rememberWebViewClient {
        shouldInterceptRequest { _, request ->
            if (request == null) return@shouldInterceptRequest null
            if (!request.url.contains(assetHost)) return@shouldInterceptRequest null

            val path = extractLocalPath(request.url) ?: return@shouldInterceptRequest null
            val accept = request.headers["Accept-Encoding"].orEmpty()

            runBlocking {
                val (actualPath, encoding) = resolveCompression(path, accept, reader)
                    ?: return@runBlocking buildResponse(
                        body = "Not found: $path".encodeToByteArray(),
                        mimeType = "text/plain",
                        contentEncoding = null,
                        statusCode = 404, reasonPhrase = "Not Found",
                    )

                val bytes = reader.read(actualPath)
                    ?: return@runBlocking buildResponse(
                        body = "Read failed".encodeToByteArray(),
                        mimeType = "text/plain",
                        contentEncoding = null,
                        statusCode = 500, reasonPhrase = "Internal Error",
                    )

                buildResponse(
                    body = bytes,
                    mimeType = mimeTypeForPath(path),
                    contentEncoding = encoding,
                )
            }
        }
    }

    val chromeClient = rememberWebChromeClient {
        onConsoleMessage { _, message ->
            println("[WebView native] ${message.message}")
            false
        }
    }

    ComposeWebView(
        state = state,
        controller = controller,
        settings = settings,
        client = client,
        chromeClient = chromeClient,
        jsBridge = jsBridge,
        modifier = modifier,
    )
}
