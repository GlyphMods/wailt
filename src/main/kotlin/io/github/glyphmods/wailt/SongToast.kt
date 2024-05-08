package io.github.glyphmods.wailt

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

private val BACKGROUND_SPRITE = ResourceLocation("wailt:toast")

class SongToast(private val artist: Component, private val track: Component) : Toast {
    override fun render(gui: GuiGraphics, component: ToastComponent, timeSinceLastVisible: Long): Toast.Visibility {
        val font = component.minecraft.font
        gui.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height())

        gui.drawString(font, track, 42, 5, 0xFFFFFF)
        gui.drawString(font, artist, 42, 18, 0xFFA500)

        return if (timeSinceLastVisible < Config.toastDuration.get() * 1000 * component.notificationDisplayTimeMultiplier) {
            Toast.Visibility.SHOW
        } else {
            Toast.Visibility.HIDE
        }
    }
}