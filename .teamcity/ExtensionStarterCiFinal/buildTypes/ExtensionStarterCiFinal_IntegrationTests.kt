package ExtensionStarterCiFinal.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object ExtensionStarterCiFinal_IntegrationTests : BuildType({
    uuid = "7BE210AD-581F-42A3-9938-FD5482212236"
    name = "Integration Tests"

    vcs {
        root(ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)
    }

    steps {
        maven {

            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }


    }

    dependencies {
        dependency(ExtensionStarterCiFinal_Setup) {
            snapshot {

            }
        }
    }

    triggers {
        vcs {
        }
    }
})
