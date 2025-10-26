package io.github.glyphmods.wailt

import net.neoforged.neoforge.common.ModConfigSpec

object Config {
    private val builder = ModConfigSpec.Builder()

    val toastDuration: ModConfigSpec.ConfigValue<Double> = builder.comment("How long to show the toast on screen, in seconds")
        .define("toast.duration", 5.0)

    val disableToastColors: ModConfigSpec.BooleanValue =
        builder.comment("Do not apply formatting to the artist name in the toast")
        .define("toast.disable-color", false)

    val automaticallyShowToast: ModConfigSpec.BooleanValue = builder.comment("Automatically show the toast when the music changes")
        .define("toast.automatically-show", true)


    val spec: ModConfigSpec = builder.build()
}