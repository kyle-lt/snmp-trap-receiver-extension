package ExtensionStarterCiFinal.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object ExtensionStarterCiFinal_Build : BuildType({
    name = "Build"

    vcs {
        root(ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)
    }

    steps {
        maven {
            goals = "clean test"
            pomLocation = ".teamcity/pom.xml"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            mavenVersion = defaultProvidedVersion()
        }
    }

    triggers {
        vcs {
        }
    }
})
