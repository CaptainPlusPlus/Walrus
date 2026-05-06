package domain

interface AssetReader {
    suspend fun exists(path: String): Boolean
    suspend fun read(path: String): ByteArray?
}