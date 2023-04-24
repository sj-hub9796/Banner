package com.mohistmc.banner.injection.world.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;

public interface InjectionEntity {

    default CraftEntity getBukkitEntity() {
        return null;
    }

    default int getDefaultMaxAirSupply() {
        return 0;
    }

    default float getBukkitYaw() {
        return 0;
    }

    default boolean isChunkLoaded() {
        return false;
    }

    default void postTick() {
    }

    default void setSecondsOnFire(int i, boolean callEvent) {
    }

    default SoundEvent getSwimSound0() {
        return null;
    }

    default SoundEvent getSwimSplashSound0() {
        return null;
    }

    default SoundEvent getSwimHighSpeedSplashSound0() {
        return null;
    }

    default boolean canCollideWithBukkit(Entity entity) {
        return false;
    }

    default org.spigotmc.ActivationRange.ActivationType bridge$activationType() {
        return null;
    }

    default void inactiveTick() {

    }

    default long bridge$activatedTick() {
        return 0;
    }

    default void banner$setActivatedTick(long activatedTick) {

    }

    default boolean bridge$defaultActivationState() {
        return false;
    }

    default void banner$setDefaultActivationState(boolean state) {

    }

    default boolean bridge$generation() {
        return false;
    }

    default void banner$setGeneration(boolean gen) {

    }
}
