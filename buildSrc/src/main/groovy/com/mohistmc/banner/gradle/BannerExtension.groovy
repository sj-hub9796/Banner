package com.mohistmc.banner.gradle

import org.gradle.api.Project

class BannerExtension {

    private final Project project
    private String mcVersion
    private String bukkitVersion
    private File accessTransformer
    private File extraMapping
    private final MappingsConfiguration mappingsConfiguration = new MappingsConfiguration()


    BannerExtension(Project project) {
        this.project = project
    }


    String getMcVersion() {
        return mcVersion
    }

    void setMcVersion(String mcVersion) {
        this.mcVersion = mcVersion
    }

    String getBukkitVersion() {
        return bukkitVersion
    }

    void setBukkitVersion(String bukkitVersion) {
        this.bukkitVersion = bukkitVersion
    }

    File getAccessTransformer() {
        return accessTransformer
    }

    void setAccessTransformer(File accessTransformer) {
        this.accessTransformer = accessTransformer
    }

    File getExtraMapping() {
        return extraMapping
    }

    void setExtraMapping(File extraMapping) {
        this.extraMapping = extraMapping
    }

    MappingsConfiguration getMappingsConfiguration() {
        return mappingsConfiguration
    }

    static class MappingsConfiguration {

        private File bukkitToNeoForge

        File getBukkitToNeoForge() {
            return bukkitToNeoForge
        }

        void setBukkitToNeoForge(File bukkitToNeoForge) {
            this.bukkitToNeoForge = bukkitToNeoForge
        }

        private File bukkitToFabric

        File getBukkitToFabric() {
            return bukkitToFabric
        }

        void setBukkitToFabric(File bukkitToFabric) {
            this.bukkitToFabric = bukkitToFabric
        }

        private File bukkitToFabricInheritance

        File getBukkitToFabricInheritance() {
            return bukkitToFabricInheritance
        }

        void setBukkitToFabricInheritance(File bukkitToFabricInheritance) {
            this.bukkitToFabricInheritance = bukkitToFabricInheritance
        }

        private File reobfBukkitPackage

        File getReobfBukkitPackage() {
            return reobfBukkitPackage
        }

        void setReobfBukkitPackage(File reobfBukkitPackage) {
            this.reobfBukkitPackage = reobfBukkitPackage
        }
    }
}
