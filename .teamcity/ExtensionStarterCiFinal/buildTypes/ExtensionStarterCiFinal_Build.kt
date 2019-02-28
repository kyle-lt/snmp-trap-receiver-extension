package ExtensionStarterCiFinal.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object ExtensionStarterCiFinal_Build : BuildType({
    uuid = "75c498f0-14c7-45a6-a538-53ad0f9b87dc"
    name = "Build"

    artifactRules = "target/ExtensionStarterCiMonitor-*.zip"

    vcs {
        root(ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)
    }

    steps {
        maven {
            goals = "clean install -Pno-integration-tests"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }

    triggers {
        vcs {
        }
    }
})
