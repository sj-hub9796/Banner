package com.mohistmc.banner.gradle

import com.mohistmc.banner.gradle.tasks.BuildSpigotTask
import com.mohistmc.banner.gradle.tasks.DownloadBuildToolsTask
import com.mohistmc.banner.gradle.tasks.ProcessMappingTask
import com.mohistmc.banner.gradle.tasks.RemapSpigotTask
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.bootstrap.LoomGradlePluginBootstrap
import net.fabricmc.loom.configuration.mods.dependency.LocalMavenHelper
import org.gradle.api.Plugin
import org.gradle.api.Project

class BannerGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        LoomGradleExtension.get(project).setGenerateSrgTiny(true)
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

    static def setupSpigot(Project project, File bannerRepo) {
        def bannerExt = project.extensions.getByName('banner') as BannerExtension
        def buildTools = project.rootProject.file("${Project.DEFAULT_BUILD_DIR_NAME}/banner_cache/buildtools")
        def buildToolsFile = new File(buildTools, 'BuildTools.jar')

        def mappingsDir = project.rootProject.file("${Project.DEFAULT_BUILD_DIR_NAME}/banner_cache/tmp_srg")
        def reobfMappings = new File(mappingsDir, 'reobf_bukkit.srg')
        def neoforgeMappings = new File(mappingsDir, 'bukkit_moj.srg')
        def fabricMappings = new File(mappingsDir, 'bukkit_intermediary.srg')
        def fabricInheritance = new File(mappingsDir, 'inheritanceMap_intermediary.txt')

        def spigotDeps = new File(bannerRepo, "com/mohistmc/banner/generated/spigot/${bannerExt.mcVersion}")
        def spigotMapped = new File(spigotDeps, "spigot-${bannerExt.mcVersion}-mapped.jar")
        def spigotDeobf = new File(spigotDeps, "spigot-${bannerExt.mcVersion}-deobf.jar")

        if (reobfMappings.exists() && neoforgeMappings.exists()
                && fabricInheritance.exists() && fabricMappings.exists() && spigotDeobf.exists()) {
            return
        }
        project.logger.lifecycle(":step1 download build tools")
        def downloadSpigot = new DownloadBuildToolsTask()
        downloadSpigot.output = buildToolsFile
        downloadSpigot.run()

        project.logger.lifecycle(":step2 build spigot")
        def buildSpigot = new BuildSpigotTask(project)
        buildSpigot.outputDir = spigotDeps
        buildSpigot.workDir = buildTools
        buildSpigot.mcVersion = bannerExt.mcVersion
        buildSpigot.buildTools = buildToolsFile
        buildSpigot.run()

        new LocalMavenHelper("com.mohistmc.banner.generated", "spigot", bannerExt.mcVersion, null, bannerRepo.toPath()).savePom()

        project.logger.lifecycle(":step3 process mappings")
        def processMapping = new ProcessMappingTask(project)
        processMapping.buildData = new File(buildTools, 'BuildData')
        processMapping.mcVersion = bannerExt.mcVersion
        processMapping.bukkitVersion = bannerExt.bukkitVersion
        processMapping.outDir = mappingsDir
        processMapping.inJar = buildSpigot.outSpigot
        processMapping.run()

        project.logger.lifecycle(":step4 remap spigot jar")
        def remapSpigot = new RemapSpigotTask(project)
        remapSpigot.ssJar = new File(buildTools, 'BuildData/bin/SpecialSource.jar')
        remapSpigot.inJar = buildSpigot.outSpigot
        remapSpigot.inSrg = new File(processMapping.outDir, 'bukkit_srg.srg')
        remapSpigot.inSrgToStable = new File(processMapping.outDir, "srg_to_named.srg")
        remapSpigot.inheritanceMap = new File(processMapping.outDir, 'inheritanceMap.txt')
        remapSpigot.outJar = project.file(spigotMapped)
        remapSpigot.outDeobf = project.file(spigotDeobf)
        remapSpigot.inAt = bannerExt.accessTransformer
        remapSpigot.bukkitVersion = bannerExt.bukkitVersion
        remapSpigot.inExtraSrg = bannerExt.extraMapping
        remapSpigot.run()
    }
}
