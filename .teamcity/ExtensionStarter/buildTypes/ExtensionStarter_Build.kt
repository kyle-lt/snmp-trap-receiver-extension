package ExtensionStarter.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import ExtensionStarter.vcsRoots.*;
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven

object ExtensionStarter_Build : BuildType({
    uuid = "75c498f0-14c7-45a6-a538-53ad0f9b87dc"
    name = "Build"

    vcs {
        root(extensionstarterci)
    }

    requirements {
        equals("teamcity.agent.hostname", "ip-10-143-10-127")
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

    artifactRules = """
       +:target/Extension-Starter-Ci-*.zip => target
    """.trimIndent()
})
