package io.github.glyphmods.wailt

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.github.glyphmods.wailt.Tracks.TrackManifest.ArtistInfo
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
import java.util.*

@EventBusSubscriber
object Tracks : SimplePreparableReloadListener<Map<ResourceLocation, Tracks.Track>>() {
    private lateinit var tracks: Map<ResourceLocation, Track>

    data class Track(val name: Component, val artist: Artist)

    data class Artist(private val name: String, private val info: ArtistInfo) {
        fun nameComponent(fancy: Boolean): Component {
            val default = Component.literal(name)
            return if (fancy) {
                info.fancyName.orElse(default)
            } else {
                default
            }
        }
    }

    data class TrackManifest(val artists: Map<String, ArtistInfo>, val tracks: Map<String, Map<String, Component>>) {
        data class ArtistInfo(val fancyName: Optional<Component>) {
            companion object {
                val CODEC: Codec<ArtistInfo> = RecordCodecBuilder.create {
                    it.group(
                        ComponentSerialization.CODEC.optionalFieldOf("fancy_name").forGetter(ArtistInfo::fancyName)
                    ).apply(it, ::ArtistInfo)
                }

                val DEFAULT = ArtistInfo(Optional.empty())
            }
        }

        companion object {
            val CODEC: Codec<TrackManifest> = RecordCodecBuilder.create {
                it.group(
                    Codec.unboundedMap(
                        Codec.STRING, ArtistInfo.CODEC
                    ).optionalFieldOf(
                        "artists", mapOf()
                    ).forGetter(TrackManifest::artists),
                    Codec.unboundedMap(
                        Codec.STRING,
                        Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC)
                    ).fieldOf("tracks").forGetter(TrackManifest::tracks)
                ).apply(it, ::TrackManifest)
            }
        }
    }

    operator fun get(key: ResourceLocation): Track? = tracks[key]

    override fun prepare(
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ): Map<ResourceLocation, Track> = resourceManager.namespaces.flatMap { namespace ->
        val manifestLocation = ResourceLocation.fromNamespaceAndPath(namespace, "music.json")

        resourceManager.getResourceStack(manifestLocation).flatMap { resource ->
            val manifest = resource.openAsReader().use {
                TrackManifest.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(it)).orThrow
            }

            manifest.tracks.map { (artistName, tracks) ->
                val artist = Artist(
                    artistName,
                    manifest.artists.getOrDefault(artistName, ArtistInfo.DEFAULT)
                )

                tracks.map { (path, name) ->
                    val location = ResourceLocation.fromNamespaceAndPath(namespace, "music/$path")
                    location to Track(name, artist)
                }.toMap()
            }
        }
    }.reduce { a, b -> a + b }.also { tracks ->
        WAILT.LOGGER.info("Loaded ${tracks.size} tracks")
    }

    override fun apply(
        `object`: Map<ResourceLocation, Track>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        tracks = `object`
    }

    @SubscribeEvent
    private fun registerReloadListeners(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(this)
    }
}