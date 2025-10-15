package io.github.glyphmods.wailt

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI

const val FORMAT_VERSION = 1

interface MetadataFile {
    val version: Int
}

class MetadataFetcher(gameDirectory: File, val baseURL: URI, val forceEmbedded: Boolean) {
    private val gson = Gson()
    val cacheDirectory = gameDirectory.resolve("wailt").also {
        if (!it.exists()) {
            check(it.mkdir()) { "Failed to create cache directory! $it" }
        }
        check(it.isDirectory) { "Cache directory $it is not a directory!" }
    }

    private fun <T: MetadataFile> loadFromResource(fileName: String, type: Class<T>) =
        this::class.java.getResourceAsStream("/$fileName")!!.reader().runCatching {
            gson.fromJson(this, type)
        }

    suspend fun <T : MetadataFile> fetchFile(fileName: String, type: Class<T>): T =
        if (forceEmbedded) {
            WAILT.LOGGER.warn("Using embedded copy of $fileName, as requested")
            loadFromResource(fileName, type).getOrElse { throw RuntimeException("Could not load metadata file $fileName", it) }
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    baseURL.resolve(fileName).toURL().openStream().reader().use {
                        it.readText()
                    }
                }
            }.onSuccess { data ->
                WAILT.LOGGER.debug("Caching downloaded file $fileName")
                cacheDirectory.resolve(fileName).runCatching {
                    writeText(data)
                }.onFailure {
                    WAILT.LOGGER.warn("Failed to cache downloaded file $fileName:", it)
                }
            }.mapCatching { data ->
                gson.fromJson(data, type)!!.also {
                    check(it.version == FORMAT_VERSION) { "File $fileName has an unsupported version ${it.version}! (expected $FORMAT_VERSION)" }
                }
            }.getOrElse { downloadError ->
                WAILT.LOGGER.warn("Failed to download or parse $fileName, loading cached file")
                WAILT.LOGGER.debug("Download error:", downloadError)
                cacheDirectory.resolve(fileName).runCatching {
                    gson.fromJson(readText(), type)
                }.getOrElse { readError ->
                    WAILT.LOGGER.warn("Unable to read $fileName from cache, using embedded copy")
                    WAILT.LOGGER.debug("Cache read error:", readError)
                    loadFromResource<T>(fileName, type).getOrElse {
                        throw RuntimeException("Could not load metadata file $fileName", it).apply {
                            addSuppressed(readError)
                            addSuppressed(downloadError)
                        }
                    }
                }
            }
        }.also { check(it.version == FORMAT_VERSION) }

    suspend inline fun <reified T : MetadataFile> fetchFile(fileName: String) = fetchFile(fileName, T::class.java)
}