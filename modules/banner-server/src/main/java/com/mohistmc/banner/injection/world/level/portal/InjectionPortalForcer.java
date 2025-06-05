package com.mohistmc.banner.injection.world.level.portal;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;

public interface InjectionPortalForcer {

    default void pushPortalCreate(Entity entity, int createRadius) {

    }

    default void pushSearchRadius(int searchRadius) {

    }
}
