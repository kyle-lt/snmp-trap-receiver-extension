package ExtensionStarterWithCi2

import ExtensionStarterWithCi2.vcsRoots.*
import ExtensionStarterWithCi2.vcsRoots.ExtensionStarterWithCi2_SshGitSfBitbucketCorpAppdynamicsCom7999bitbucketAeExtens
import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.Project
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings.*
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.versionedSettings

object Project : Project({
    uuid = "2d5369bd-6515-49ab-a913-e37ae314e77a"
    extId = "ExtensionStarterWithCi2"
    parentId = "_Root"
    name = "extension-starter-with-ci-2"

    vcsRoot(ExtensionStarterWithCi2_SshGitSfBitbucketCorpAppdynamicsCom7999bitbucketAeExtens)

    features {
        versionedSettings {
            id = "PROJECT_EXT_3"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = ExtensionStarterWithCi2_SshGitSfBitbucketCorpAppdynamicsCom7999bitbucketAeExtens.extId
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            param("credentialsStorageType", "credentialsJSON")
        }
    }
})
