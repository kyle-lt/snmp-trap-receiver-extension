package ExtensionStarterWithCi

import ExtensionStarterWithCi.vcsRoots.*
import ExtensionStarterWithCi.vcsRoots.ExtensionStarterWithCi_ExtensionStarterWithCi
import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.Project
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings.*
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.versionedSettings

object Project : Project({
    uuid = "985e4f31-40a0-4e98-961f-6051ef6677be"
    extId = "ExtensionStarterWithCi"
    parentId = "_Root"
    name = "extension-starter-with-ci"

    vcsRoot(ExtensionStarterWithCi_ExtensionStarterWithCi)

    features {
        versionedSettings {
            id = "PROJECT_EXT_2"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = ExtensionStarterWithCi_ExtensionStarterWithCi.extId
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            param("credentialsStorageType", "credentialsJSON")
        }
    }
})
