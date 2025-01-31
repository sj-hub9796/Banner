package com.mohistmc.banner.mixin.network.protocol.game;

import com.mohistmc.banner.injection.network.protocol.game.InjectionServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundInteractPacket.class)
public class MixinServerboundInteractPacket implements InjectionServerboundInteractPacket {

    @Shadow @Final @Mutable private int entityId;
    @Shadow @Final @Mutable private ServerboundInteractPacket.Action action;

    @Override
    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public boolean isAttack() {
        return this.action.getType() == ServerboundInteractPacket.ActionType.ATTACK;
    }
}
