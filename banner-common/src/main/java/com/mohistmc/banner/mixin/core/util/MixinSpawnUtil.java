package com.mohistmc.banner.mixin.core.util;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnUtil.class)
public class MixinSpawnUtil {

    @Inject(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private static <T extends Mob> void banner$pushSpawnReason(EntityType<T> entityType, EntitySpawnReason entitySpawnReason, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k, SpawnUtil.Strategy strategy, boolean bl, CallbackInfoReturnable<Optional<T>> cir) {
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    @Inject(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V"))
    private static <T extends Mob> void banner$addRemovalReason(EntityType<T> entityType, EntitySpawnReason entitySpawnReason, ServerLevel serverLevel, BlockPos blockPos, int i, int j, int k, SpawnUtil.Strategy strategy, boolean bl, CallbackInfoReturnable<Optional<T>> cir, @Local Mob mob) {
        mob.pushRemoveCause(null);
    }
}
