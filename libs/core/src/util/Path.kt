package util

import domain.AssetReader

/**
 * Strips scheme and host from URL.
 * https://appassets.androidplatform.net/test/index.html -> test/index.html
 */
fun extractLocalPath(url: String): String? {
    val afterScheme = url.substringAfter("://", "")
    if (afterScheme.isEmpty()) return null
    val afterHost = afterScheme.substringAfter('/', "")
    return afterHost.substringBefore('?').substringBefore('#').takeIf { it.isNotEmpty() }
}

suspend fun resolveCompression(
    path: String,
    acceptEncoding: String,
    reader: AssetReader,
): Pair<String, String?>? {
    val br = acceptEncoding.contains("br", ignoreCase = true)
    val gz = acceptEncoding.contains("gzip", ignoreCase = true)
    if (br && reader.exists("$path.br")) return "$path.br" to "br"
    if (gz && reader.exists("$path.gz")) return "$path.gz" to "gzip"
    if (reader.exists(path)) return path to null
    return null
}