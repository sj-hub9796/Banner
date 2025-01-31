package com.mohistmc.banner.injection.network.protocol.game;

public interface InjectionServerboundInteractPacket {

    // Paper start - PlayerUseUnknownEntityEvent
    default int getEntityId() {
        throw new IllegalStateException("Not implemented");
    }
    default boolean isAttack() {
        throw new IllegalStateException("Not implemented");
    }
    // Paper end - PlayerUseUnknownEntityEvent
}
