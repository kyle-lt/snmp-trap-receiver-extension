package ExtensionStarterCiFinal.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object ExtensionStarterCiFinal_Setup : BuildType({
    uuid = "129f1153-3e0f-4794-a46b-8a1600da7692"
    name = "Setup"

    vcs {
        root(ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)
    }

    steps {
        exec {
            path = "make"
            arguments = "docker-clean"
        }
        exec {
            path = "make"
            arguments = "dockerRun"
        }
        exec {
            path = "make"
            arguments = "sleep"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(ExtensionStarterCiFinal_Build) {
        }
    }
})
