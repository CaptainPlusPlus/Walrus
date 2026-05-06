package model

import com.parkwoocheol.composewebview.PlatformWebResourceResponse
import com.parkwoocheol.composewebview.createPlatformWebResourceResponse

fun buildResponse(
    body: ByteArray,
    mimeType: String,
    contentEncoding: String?,
    statusCode: Int = 200,
    reasonPhrase: String = "OK",
): PlatformWebResourceResponse {
    val headers = buildMap {
        put("Content-Type", mimeType)
        put("Content-Length", body.size.toString())
        contentEncoding?.let { put("Content-Encoding", it) }
    }
    return createPlatformWebResourceResponse(
        mimeType = mimeType,
        encoding = "UTF-8",
        data = body,
        statusCode = statusCode,
        reasonPhrase = reasonPhrase,
        responseHeaders = headers,
    )
}