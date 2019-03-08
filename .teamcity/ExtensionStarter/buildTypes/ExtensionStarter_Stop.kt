package ExtensionStarter.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import ExtensionStarter.buildTypes.*;
import ExtensionStarter.vcsRoots.*;

object ExtensionStarter_Stop : BuildType({
    uuid = "75c498f0-14c7-45a6-a538-53ad0f9b87dc"
    name = "Build"

    artifactRules = "target/ExtensionStarterCiMonitor-*.zip"

    vcs {
        root(ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit)
    }

    steps {
        exec {
            path = "make"
            arguments = "dockerStop"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(ExtensionStarter_Setup) {
        }
    }
})
