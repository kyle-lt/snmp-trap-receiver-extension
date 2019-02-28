package ExtensionStarterCiFinal

import ExtensionStarterCiFinal.buildTypes.*
import ExtensionStarterCiFinal.vcsRoots.*
import ExtensionStarterCiFinal.vcsRoots.ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    id("ExtensionStarterCiFinal")
    parentId("_Root")
    name = "Extension Starter Ci Final"

    vcsRoot(ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead)

    buildType(ExtensionStarterCiFinal_Setup)
    buildType(ExtensionStarterCiFinal_Stop)
    buildType(ExtensionStarterCiFinal_IntegrationTests)
    buildType(ExtensionStarterCiFinal_Build)

    features {
        versionedSettings {
            id = "PROJECT_EXT_1"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead.id}"
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }
    buildTypesOrder = arrayListOf(ExtensionStarterCiFinal_Build, ExtensionStarterCiFinal_Setup, ExtensionStarterCiFinal_IntegrationTests, ExtensionStarterCiFinal_Stop)
})
