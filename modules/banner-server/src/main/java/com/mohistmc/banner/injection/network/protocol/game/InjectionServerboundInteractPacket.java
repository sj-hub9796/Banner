package com.mohistmc.banner.injection.network.protocol.game;

public interface InjectionServerboundInteractPacket {

    // Paper start - PlayerUseUnknownEntityEvent
    int getEntityId();
    boolean isAttack();
    // Paper end - PlayerUseUnknownEntityEvent
}
