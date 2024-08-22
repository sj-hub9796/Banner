package com.mohistmc.banner.mixin.core.world.entity;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface MixinLeashable {

    @Decorate(method = "leashTooFarBehaviour", inject = true, at = @At("HEAD"))
    private void banner$distanceLeash() {
        if (this instanceof Entity entity) {
            Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(entity.getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE));
        }
    }
}