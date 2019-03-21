package ExtensionStarter

import ExtensionStarter.vcsRoots.*
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings
import ExtensionStarter.buildTypes.*
import ExtensionStarter.vcsRoots.*


object Project : Project({
    uuid = "85c0e0f7-336b-42d1-84ad-8abe32b64865"
    id("ExtensionStarter")
    parentId("AE")
    name = "Extension Starter"

    vcsRoot(extensionstarterci)

    buildType(ExtensionStarter_Setup)
//    buildType(ExtensionStarter_Stop)
//    buildType(ExtensionStarter_IntegrationTests)
    buildType(ExtensionStarter_Build)

    params {
        password("env.APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "zxxae5b77700e4d524b40b3477e9e3668df")
        param("env.APPDYNAMICS_CONTROLLER_PORT", "8080")
        param("env.APPDYNAMICS_CONTROLLER_HOST_NAME", "%teamcity.agent.hostname%")
        param("env.APPDYNAMICS_AGENT_ACCOUNT_NAME", "customer1")
        param("env.APPDYNAMICS_CONTROLLER_SSL_ENABLED", "false")
        param("env.GLOBAL_ACCOUNT_NAME", "customer1_70a0b3c7-3f29-4b30-afea-f6e173520cd0")
        param("env.EVENTS_SERVICE_HOST", "ec2-34-221-206-45.us-west-2.compute.amazonaws.com")
        password("env.EVENTS_SERVICE_API_KEY", "zxx9cf37a23c0e67c886aa07f398cf34e391044ba0fc0a044601f0ae7ee4b51ba06ef5650b72c2525ed")
    }

    features {
        versionedSettings {
            id = "PROJECT_EXT_16"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${extensionstarterci.id}"
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN

        }
    }

    buildTypesOrder = arrayListOf(ExtensionStarter_Build,
            ExtensionStarter_Setup)
})
