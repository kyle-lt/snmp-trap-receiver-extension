package ExtensionStarter

import ExtensionStarter.vcsRoots.*
import ExtensionStarter.vcsRoots.ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings
import ExtensionStarter.buildTypes.*
import ExtensionStarter.vcsRoots.*

object Project : Project({
    uuid = "85c0e0f7-336b-42d1-84ad-8abe32b64865"
    id("ExtensionStarter")
    parentId("_Root")
    name = "Extension Starter"

    vcsRoot(ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit)

    buildType(ExtensionStarter_Setup)
    buildType(ExtensionStarter_Stop)
    buildType(ExtensionStarter_IntegrationTests)
    buildType(ExtensionStarter_Build)

    params {
        password("env.APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "credentialsJSON:65747919-6823-4341-8a84-db15e792b3d7")
        param("env.APPDYNAMICS_CONTROLLER_PORT", "8090")
        param("env.APPDYNAMICS_CONTROLLER_HOST_NAME", "3.122.230.13")
        param("env.APPDYNAMICS_AGENT_ACCOUNT_NAME", "customer1")
        param("env.APPDYNAMICS_CONTROLLER_SSL_ENABLED", "false")
        password("env.EVENTS_SERVICE_API_KEY", "credentialsJSON:e652455f-881c-4d2f-9062-c2e25d28e7eb")
    }

    features {
        versionedSettings {
            id = "PROJECT_EXT_25"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit.id}"
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(ExtensionStarter_Build,
            ExtensionStarter_Setup,
            ExtensionStarter_IntegrationTests,
            ExtensionStarter_Stop)
})
