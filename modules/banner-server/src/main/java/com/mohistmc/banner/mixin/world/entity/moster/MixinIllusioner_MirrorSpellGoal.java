package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.monster.Illusioner;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Illusioner$IllusionerMirrorSpellGoal")
public class MixinIllusioner_MirrorSpellGoal {

    @SuppressWarnings("target")
    @Shadow(aliases = {"field_7300"}, remap = false)
    private Illusioner outerThis;

    @Inject(method = "performSpellCasting", at = @At("HEAD"))
    private void banner$reason(CallbackInfo ci) {
        outerThis.pushEffectCause(EntityPotionEffectEvent.Cause.ILLUSION);
    }
}
