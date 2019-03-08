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
