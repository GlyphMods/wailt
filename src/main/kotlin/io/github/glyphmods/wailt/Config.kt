package io.github.glyphmods.wailt

import net.neoforged.neoforge.common.ModConfigSpec

object Config {
    private val builder = ModConfigSpec.Builder()

    val toastDuration = builder.comment("How long to show the toast on screen, in seconds")
        .define("toast.duration", 5.0)
    val metadataUrl =
        builder.comment("The URL to download song metadata from. This should generally be left at the default.")
            .define(
                "meta-url",
                "https://raw.githubusercontent.com/GlyphMods/wailt/1.20.2-neoforge/src/main/resources/songs/"
            )

    val spec = builder.build()
}