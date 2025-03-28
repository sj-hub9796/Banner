package com.mohistmc.banner.gradle

import net.fabricmc.loom.bootstrap.LoomGradlePluginBootstrap
import org.gradle.api.Project

class BannerGradlePluginFabric extends BannerGradlePlugin {

    @Override
    void apply(Project project) {
        project.plugins.apply(LoomGradlePluginBootstrap)
        def bannerExt = project.extensions.create('banner', BannerExtension, project)
        def bannerRepo = project.rootProject.file("${Project.DEFAULT_BUILD_DIR_NAME}/banner_repo")
        project.repositories.maven {
            name = "Banner Spigot Repo"
            url = bannerRepo
        }

        def mappingsDir = project.rootProject.file("${Project.DEFAULT_BUILD_DIR_NAME}/banner_cache/tmp_srg")
        def reobfMappings = new File(mappingsDir, 'reobf_bukkit.srg')
        def neoforgeMappings = new File(mappingsDir, 'bukkit_moj.srg')
        def fabricMappings = new File(mappingsDir, 'bukkit_intermediary.srg')
        def fabricInheritance = new File(mappingsDir, 'inheritanceMap_intermediary.txt')
        bannerExt.mappingsConfiguration.reobfBukkitPackage = reobfMappings
        bannerExt.mappingsConfiguration.bukkitToNeoForge = neoforgeMappings
        bannerExt.mappingsConfiguration.bukkitToFabric = fabricMappings
        bannerExt.mappingsConfiguration.bukkitToFabricInheritance = fabricInheritance

        project.afterEvaluate {
            setupSpigot(project, bannerRepo)
        }
    }
}
