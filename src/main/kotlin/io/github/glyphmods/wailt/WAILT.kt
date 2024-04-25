package io.github.glyphmods.wailt

import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.common.Mod.EventBusSubscriber
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import java.net.URL

@Mod(WAILT.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object WAILT {
    const val ID = "wailt"
    private lateinit var dispatcher: ToastDispatcher

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.spec)
    }

    @SubscribeEvent
    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            val minecraft = Minecraft.getInstance()
            dispatcher = ToastDispatcher(
                minecraft.toasts, MetadataFetcher(minecraft.gameDirectory, URL(Config.metadataUrl.get()))
            )
            FORGE_BUS.register(dispatcher)
        }
    }
}