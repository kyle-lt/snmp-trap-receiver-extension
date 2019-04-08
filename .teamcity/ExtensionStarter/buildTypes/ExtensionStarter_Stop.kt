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

object ExtensionStarter_Stop : BuildType({
    uuid = "bdc25bd3-02fb-4563-b335-2d344494f9d2"
    name = "Stop"

    vcs {
        root(extensionstarterci)
    }

    requirements {
        matches("env.AGENT_OS", "Windows")
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
        dependency(ExtensionStarter_Setup) {
            snapshot{

            }
        }
        dependency(ExtensionStarter_IntegrationTests) {
            snapshot{

            }
        }
    }
})
