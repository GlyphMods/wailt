package io.github.glyphmods.wailt

import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT
import java.net.URI

@Mod(WAILT.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object WAILT {
    const val ID = "wailt"
    private lateinit var dispatcher: ToastDispatcher

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOADING_CONTEXT.activeContainer.registerConfig(ModConfig.Type.CLIENT, Config.spec)
    }

    @SubscribeEvent
    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            val minecraft = Minecraft.getInstance()
            dispatcher = ToastDispatcher(
                minecraft.toasts,
                MetadataFetcher(
                    minecraft.gameDirectory,
                    URI.create(Config.metadataUrl.get()).toURL(),
                    Config.forceEmbeddedMetadata.get()
                )
            )
            FORGE_BUS.register(dispatcher)
        }
    }
}