package io.github.glyphmods.wailt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent

@Serializable
data class Tracks(
    override val version: Int,
    val tracks: Map<String, Map<String, Map<String, String>>>,
    val artists: Map<String, Artist>
) : MetadataFile

@Serializable
data class Artist(val component: JsonElement)

data class Track(val title: Component, val artist: Component)

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
class ToastDispatcher(private val toastComponent: ToastComponent, metadataFetcher: MetadataFetcher) {
    init {
        WAILT.LOGGER.info("Downloading artist index")
    }

    private val tracks = metadataFetcher.fetchFile<Tracks>("tracks.json").let { metadata ->
        metadata.tracks.mapValues { (_, artists) ->
            artists.flatMap { (artist, tracks) ->
                tracks.map { (id, name) ->
                    val artistComponent = metadata.artists[artist]?.let {
                        Component.Serializer.fromJson(it.component.toString())!!
                    } ?: Component.literal(artist).withColor(0xFFA500)
                    id to Track(Component.literal(name), artistComponent)
                }
            }.toMap()
        }
    }.also {
        WAILT.LOGGER.info("Loaded information for ${it.values.sumOf { it.size }} songs in ${it.size} namespaces")
    }
    private val missingSongs = mutableSetOf<ResourceLocation>()

    @SubscribeEvent
    fun onPlaySoundEvent(event: PlayStreamingSourceEvent) {
        val sound = event.sound
        if (sound.source == SoundSource.MUSIC) {
            val location = sound.sound.location
            val track = tracks[location.namespace]?.get(location.path.removePrefix("music/"))
            if (track != null) {
                toastComponent.addToast(SongToast(track.artist, track.title))
            } else {
                toastComponent.addToast(
                    SongToast(
                        Component.translatable("gui.wailt.toast.unknown"),
                        Component.literal(location.path)
                    )
                )
                if (missingSongs.add(location)) { // Only warn once for each missing track
                    WAILT.LOGGER.warn("No metadata is defined for music track $location")
                }
            }
        }
    }
}