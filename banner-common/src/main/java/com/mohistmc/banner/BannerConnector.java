package com.mohistmc.banner;

import io.izzel.arclight.mixin.MixinTools;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class BannerConnector implements IMixinConnector {

    @Override
    public void connect() {
        MixinTools.setup();
        Mixins.addConfiguration("mixins.banner.core.json");
    }
}
