package com.mohistmc.banner.mixin.world.level.block;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChorusFlowerBlock.class)
public abstract class MixinChorusFlowerBlock extends Block {


    // @formatter:off
    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final private ChorusPlantBlock plant;

    public MixinChorusFlowerBlock(Properties properties) {
        super(properties);
    }

    @Shadow private static boolean allNeighborsEmpty(LevelReader worldIn, BlockPos pos, @Nullable Direction excludingSide) { return false; }
    @Shadow protected abstract void placeGrownFlower(Level worldIn, BlockPos pos, int age);
    @Shadow protected abstract void placeDeadFlower(Level worldIn, BlockPos pos);
    // @formatter:on

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0))
    private boolean banner$callBlockSpreadEvent0(ServerLevel level, BlockPos pos, BlockState state, int flag, @Local(ordinal = 1) BlockPos blockPos, @Local(ordinal = 0) int i) {
        if (CraftEventFactory.handleBlockSpreadEvent(level, pos, blockPos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, i), 2)) {
            level.setBlock(pos, this.plant.getStateForPlacement(level, pos), 2);
            this.placeGrownFlower(level, blockPos, i);
            return true;
        }
        return false;
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeGrownFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)V", ordinal = 0))
    private void banner$cancelPlaceGrownFlower(ChorusFlowerBlock instance, Level level, BlockPos pos, int age) {
        // do nothing
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeGrownFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)V", ordinal = 1))
    private void banner$callBlockSpreadEvent1(ChorusFlowerBlock instance, Level level, BlockPos pos, int age, @Local(ordinal = 2) BlockPos blockPos2, @Local(ordinal = 0) int i) {
        if (CraftEventFactory.handleBlockSpreadEvent(level, pos, blockPos2, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, i + 1), 2)) {
            this.placeGrownFlower(level, blockPos2, i + 1);
        }
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeDeadFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", ordinal = 0))
    private void banner$callBlockGrowEvent0(ChorusFlowerBlock instance, Level level, BlockPos pos) {
        if (CraftEventFactory.handleBlockGrowEvent(level, pos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5), 2)) {
            this.placeDeadFlower(level, pos);
        }
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeDeadFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", ordinal = 1))
    private void banner$callBlockGrowEvent1(ChorusFlowerBlock instance, Level level, BlockPos pos) {
        if (CraftEventFactory.handleBlockGrowEvent(level, pos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5), 2)) {
            this.placeDeadFlower(level, pos);
        }
    }

    @Inject(method = "onProjectileHit", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private void banner$hitByProjectile(Level p_51654_, BlockState p_51655_, BlockHitResult result, Projectile projectile, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(projectile, result.getBlockPos(), Blocks.AIR.defaultBlockState())) {
            ci.cancel();
        }
    }
}
