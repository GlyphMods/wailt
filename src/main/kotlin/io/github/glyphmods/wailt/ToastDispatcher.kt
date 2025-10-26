package io.github.glyphmods.wailt

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS


class ToastDispatcher(private val toastComponent: ToastComponent) {
    init {
        FORGE_BUS.addListener<PlayStreamingSourceEvent> { event ->
            val sound = event.sound
            if (sound.source == SoundSource.MUSIC && Config.automaticallyShowToast.get()) {
                dispatchToast(sound.sound.location)
            }
        }

        FORGE_BUS.addListener<ClientTickEvent.Pre> {
            val minecraft = Minecraft.getInstance()
            while (WAILT.SHOW_TOAST_KEYBIND.consumeClick()) {
                minecraft.musicManager.currentMusic?.let {
                    dispatchToast(it.sound.location)
                } ?: toastComponent.addToast(InfoToast(Component.translatable("gui.wailt.toast.no-song")))
            }
        }
    }

    private val missingSongs = mutableSetOf<ResourceLocation>()

    private fun dispatchToast(location: ResourceLocation) {
        val track = Tracks[location]
        val fancy = Config.disableToastColors.isFalse
        if (track != null) {
            toastComponent.addToast(SongToast(track.artist.nameComponent(fancy), track.name))
        } else {
            toastComponent.addToast(
                SongToast(
                    Component.translatable("gui.wailt.toast.unknown"), Component.literal(location.path)
                )
            )
            if (missingSongs.add(location)) { // Only warn once for each missing track
                WAILT.LOGGER.warn("No metadata is defined for music track $location")
            }
        }
    }
}