package com.mohistmc.banner;


import com.mohistmc.banner.asm.InventoryImplementer;
import com.mohistmc.banner.asm.SwitchTableFixer;
import com.mohistmc.banner.boot.FabricBootstrap;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BannerMixinConfig implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private final InventoryImplementer inventoryImplementer = new InventoryImplementer();
    private final SwitchTableFixer switchTableFixer = new SwitchTableFixer();

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        inventoryImplementer.processClass(targetClass);
        switchTableFixer.processClass(targetClass);
    }

    static {
        // load Banner

        var bootstrap = new FabricBootstrap();
        bootstrap.accept(FabricLauncherBase.getLauncher());
    }
}