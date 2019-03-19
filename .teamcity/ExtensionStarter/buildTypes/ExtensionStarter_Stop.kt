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
        equals("teamcity.agent.hostname", "ip-10-143-10-127")
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
