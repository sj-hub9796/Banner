package com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem;

import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;

public interface InjectionStructureTemplate {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        throw new IllegalStateException("Not implemented");
    }
}
