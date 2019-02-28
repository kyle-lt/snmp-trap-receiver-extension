package ExtensionStarterCiFinal.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object ExtensionStarterCiFinal_Stop : BuildType({
    uuid = "adc25bd3-02fb-4561-b335-2d344494f9d2"
    name = "Stop"

    vcs {
        root(ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)
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
        snapshot(ExtensionStarterCiFinal_Setup) {
        }
    }
})
