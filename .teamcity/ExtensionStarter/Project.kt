package ExtensionStarter

import ExtensionStarter.vcsRoots.*
import ExtensionStarter.vcsRoots.ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "85c0e0f7-336b-42d1-84ad-8abe32b64865"
    id("ExtensionStarter")
    parentId("_Root")
    name = "Extension Starter"

    vcsRoot(ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit)

    params {
        param("env.APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "a4c3657a-5177-48fe-8ec5-c0f383694251")
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
})
