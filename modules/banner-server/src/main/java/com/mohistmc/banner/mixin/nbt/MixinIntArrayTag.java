package com.mohistmc.banner.mixin.nbt;

import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInput;
import java.io.IOException;

@Mixin(targets = "net.minecraft.nbt.IntArrayTag$1")
public class MixinIntArrayTag {

    @Inject(method = "readAccounted",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtAccounter;accountBytes(JJ)V"))
    private static void banner$addCheck(DataInput dataInput, NbtAccounter nbtAccounter, CallbackInfoReturnable<byte[]> cir) throws IOException {
        com.google.common.base.Preconditions.checkArgument(dataInput.readInt() < 1 << 24); // Spigot
    }
}
