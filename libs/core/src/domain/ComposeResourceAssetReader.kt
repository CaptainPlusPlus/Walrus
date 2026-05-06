package domain

import core.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class ComposeResourceAssetReader : AssetReader {
    override suspend fun read(path: String): ByteArray? =
        runCatching { Res.readBytes("files/$path") }.getOrNull()

    override suspend fun exists(path: String): Boolean =
        read(path) != null
}