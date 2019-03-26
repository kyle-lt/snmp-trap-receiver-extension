package ExtensionStarter.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import ExtensionStarter.buildTypes.*;
import ExtensionStarter.vcsRoots.*;
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven

object ExtensionStarter_Setup : BuildType({
    uuid = "329f1153-3f0f-4714-a44b-8a1790da7692"
    name = "Setup"

    vcs {
        root(extensionstarterci)
    }

    requirements {
        equals("teamcity.agent.hostname", "ip-10-143-10-80")
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
        dependency(ExtensionStarter_Build) {

            snapshot{

            }

            artifacts {
                artifactRules = """
                target/Extension-Starter-Ci-*.zip => target
            """.trimIndent()
            }
        }
    }
})
