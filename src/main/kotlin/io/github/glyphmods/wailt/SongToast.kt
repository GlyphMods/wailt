package io.github.glyphmods.wailt

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

private val BACKGROUND_SPRITE = ResourceLocation("minecraft:toast/advancement")

class SongToast(private val artist: Component, private val track: Component) : Toast {
    override fun render(gui: GuiGraphics, component: ToastComponent, timeSinceLastVisible: Long): Toast.Visibility {
        val font = component.minecraft.font
        gui.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height())

        gui.drawString(font, artist, 5, 7, 0xFFA500)
        gui.drawString(font, track, 5, 18, 0xFFFFFF)

        return if (timeSinceLastVisible < Config.toastDuration.get() * 1000 * component.notificationDisplayTimeMultiplier) {
            Toast.Visibility.SHOW
        } else {
            Toast.Visibility.HIDE
        }
    }
}