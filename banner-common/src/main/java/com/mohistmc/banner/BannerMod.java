package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannerMod {

    public static final String MOD_ID = "banner";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        BannerEventDispatcherRegistry.registerEventDispatchers();
    }

    public static String getVersion() {
        return FabricLoader.getInstance().getModContainer("banner").get().getMetadata().getVersion().getFriendlyString();
    }
}