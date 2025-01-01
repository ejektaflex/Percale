package io.ejekta.percale.fabric

import io.ejekta.percale.Percale
import net.fabricmc.api.ModInitializer


class PercaleModFabric : ModInitializer {

    override fun onInitialize() {
        Percale.LOGGER.info("Percale Loaded!")
    }
}