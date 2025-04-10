package com.mohistmc.banner.mixin.core.advancement;

import com.mohistmc.banner.injection.advancements.InjectionAdvancementHolder;
import net.minecraft.advancements.AdvancementHolder;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AdvancementHolder.class)
public class MixinAdvancementHolder implements InjectionAdvancementHolder {

    @Override
    public Advancement bridge$bukkit() {
        return new CraftAdvancement((AdvancementHolder) (Object) this);
    }

    @Override
    public Advancement toBukkit() {
        return new CraftAdvancement((AdvancementHolder) (Object) this);
    }
}
