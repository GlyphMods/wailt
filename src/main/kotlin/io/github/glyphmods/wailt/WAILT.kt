package io.github.glyphmods.wailt

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.common.Mod.EventBusSubscriber
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.settings.KeyConflictContext
import net.neoforged.neoforge.client.settings.KeyModifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import java.net.URL

@Mod(WAILT.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object WAILT {
    const val ID = "wailt"
    val LOGGER: Logger = LogManager.getLogger(ID)

    private lateinit var dispatcher: ToastDispatcher
    val SHOW_TOAST_KEYBIND by lazy {
        KeyMapping(
            "key.wailt.show-toast",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.misc"
        )
    }

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.spec)
    }

    @SubscribeEvent
    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            val minecraft = Minecraft.getInstance()
            dispatcher = ToastDispatcher(
                minecraft.toasts,
                MetadataFetcher(
                    minecraft.gameDirectory,
                    URL(Config.metadataUrl.get()),
                    Config.forceEmbeddedMetadata.get()
                )
            )
            FORGE_BUS.register(dispatcher)
        }
    }

    @SubscribeEvent
    private fun onRegisterKeyMappings(event: RegisterKeyMappingsEvent) {
        event.register(SHOW_TOAST_KEYBIND)
    }
}