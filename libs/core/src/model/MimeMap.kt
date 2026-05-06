package model

val DefaultMimeMap: Map<String, String> = mapOf(
    "html" to "text/html", "htm" to "text/html",
    "js" to "application/javascript", "mjs" to "application/javascript",
    "json" to "application/json",
    "wasm" to "application/wasm",
    "css" to "text/css",
    "txt" to "text/plain",
    "png" to "image/png", "jpg" to "image/jpeg", "jpeg" to "image/jpeg",
    "gif" to "image/gif", "svg" to "image/svg+xml", "ico" to "image/x-icon",
    "woff" to "font/woff", "woff2" to "font/woff2", "ttf" to "font/ttf",
    "data" to "application/octet-stream",       // Unity asset bundles
    "unityweb" to "application/octet-stream",   // Unity asset bundles
    "mem" to "application/octet-stream",        // Emscripten memory image
    "pck" to "application/octet-stream",        // Godot pack file
)

fun mimeTypeForPath(path: String): String {
    val ext = path.substringAfterLast('.', "").lowercase()
    return DefaultMimeMap[ext] ?: "application/octet-stream"
}