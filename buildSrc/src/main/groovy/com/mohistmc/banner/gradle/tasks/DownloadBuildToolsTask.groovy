package com.mohistmc.banner.gradle.tasks

import com.mohistmc.banner.gradle.Utils

class DownloadBuildToolsTask implements Runnable {

    static def URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"

    private File output

    File getOutput() {
        return output
    }

    void setOutput(File output) {
        this.output = output
    }

    @Override
    void run() {
        if (output.exists()) {
            return
        }
        Utils.download(URL, output)
    }
}
