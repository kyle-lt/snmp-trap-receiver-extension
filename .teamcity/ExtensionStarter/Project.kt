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
    buildType(ExtensionStarter_Stop)
    buildType(ExtensionStarter_IntegrationTests)
    buildType(ExtensionStarter_Build)

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
            ExtensionStarter_Setup,
            ExtensionStarter_IntegrationTests,
            ExtensionStarter_Stop)
})
