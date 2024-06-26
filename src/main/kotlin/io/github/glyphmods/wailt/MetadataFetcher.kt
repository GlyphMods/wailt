package io.github.glyphmods.wailt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.net.URL

const val FORMAT_VERSION = 1

interface MetadataFile {
    val version: Int
}

class MetadataFetcher(gameDirectory: File, val baseUrl: URL, val forceEmbedded: Boolean) {
    val cacheDirectory = gameDirectory.resolve("wailt").also {
        if (!it.exists()) {
            check(it.mkdir()) { "Failed to create cache directory! $it" }
        }
        check(it.isDirectory) { "Cache directory $it is not a directory!" }
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T : MetadataFile> fetchFile(fileName: String): T = if (forceEmbedded) {
        WAILT.LOGGER.warn("Using embedded copy of $fileName, as requested")
        runCatching {
            Json.decodeFromStream<T>(this::class.java.getResourceAsStream("/$fileName")!!)
        }.getOrElse {
            throw RuntimeException("Could not load metadata file $fileName", it)
        }
    } else {
        runCatching {
            URL(baseUrl, "tracks.json").openStream().reader().readText()
        }.onSuccess { data ->
            WAILT.LOGGER.debug("Caching downloaded file $fileName")
            try {
                cacheDirectory.resolve(fileName).writeText(data)
            } catch (e: Exception) {
                WAILT.LOGGER.warn("Failed to cache downloaded file $fileName:", e)
            }
        }.mapCatching {
            Json.decodeFromString<T>(it).also { data ->
                check(data.version == FORMAT_VERSION) { "File $fileName has an unsupported version ${data.version}! (expected $FORMAT_VERSION)" }
            }
        }.getOrElse { downloadError ->
            WAILT.LOGGER.warn("Failed to download or parse $fileName, loading cached file")
            WAILT.LOGGER.debug("Download error:", downloadError)
            cacheDirectory.resolve(fileName).runCatching {
                Json.decodeFromString<T>(readText())
            }.getOrElse { readError ->
                WAILT.LOGGER.warn("Unable to read $fileName from cache, using embedded copy")
                WAILT.LOGGER.debug("Cache read error:", readError)
                runCatching {
                    Json.decodeFromStream<T>(this::class.java.getResourceAsStream("/$fileName")!!)
                }.getOrElse {
                    throw RuntimeException("Could not load metadata file $fileName", it).apply {
                        addSuppressed(readError)
                        addSuppressed(downloadError)
                    }
                }
            }
        }
    }.also { check(it.version == FORMAT_VERSION) }
}