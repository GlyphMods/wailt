package io.github.glyphmods.wailt

import kotlinx.serialization.Serializable
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.client.resources.language.LanguageManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.sounds.SoundSource
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent

@Serializable
data class SongTranslations(
    override val version: Int,
    val artists: Map<String, String>,
    val tracks: Map<String, Map<String, String>>
) : MetadataFile

@Serializable
data class ArtistMappings(override val version: Int, val artists: Map<String, Map<String, List<String>>>) : MetadataFile

data class Track(val title: Component, val artist: Component)

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
class ToastDispatcher(
    private val toastComponent: ToastComponent,
    private val languageManager: LanguageManager,
    private val metadataFetcher: MetadataFetcher
) : ResourceManagerReloadListener {
    init {
        WAILT.LOGGER.info("Downloading artist index")
    }

    val artistMappings = metadataFetcher.fetchFile<ArtistMappings>("artists.json").artists
    var tracks = refreshSongs()
    private val missingSongs = mutableSetOf<ResourceLocation>()

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        tracks = refreshSongs()
    }

    private fun refreshSongs(): Map<String, Map<String, Track>> {
        val langCode = languageManager.selected
        WAILT.LOGGER.info("Downloading song translations for language $langCode")
        val translations = metadataFetcher.fetchFile<SongTranslations>("lang/$langCode.json")
        return artistMappings.mapValues { (namespace, artists) ->
            artists.flatMap { (artist, tracks) ->
                if (artist !in translations.artists) {
                    WAILT.LOGGER.warn("No $langCode translation defined for artist $artist")
                }
                tracks.map { track ->
                    track to Track(
                        Component.literal(
                            translations.artists[artist] ?: artist
                        ), // warning about missing translations happened earlier
                        Component.literal(translations.tracks[namespace]?.get(track) ?: track.also {
                            WAILT.LOGGER.warn("No $langCode translation defined for track $namespace:$track by artist $artist")
                        })
                    )
                }
            }.toMap()
        }
    }

    @SubscribeEvent
    fun onPlaySoundEvent(event: PlayStreamingSourceEvent) {
        val sound = event.sound
        if (sound.source == SoundSource.MUSIC) {
            val location = sound.sound.location
            val track = tracks[location.namespace]?.get(location.path.removePrefix("music/"))
            if (track != null) {
                toastComponent.addToast(SongToast(track.title, track.artist))
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