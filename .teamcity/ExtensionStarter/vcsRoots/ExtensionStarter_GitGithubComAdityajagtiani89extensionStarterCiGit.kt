package ExtensionStarter.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object ExtensionStarter_GitGithubComAdityajagtiani89extensionStarterCiGit : GitVcsRoot({
    uuid = "feaa1794-49b2-43c7-83a0-0b822eee1bd9"
    name = "git@github.com:adityajagtiani89/extension-starter-ci.git"
    url = "git@github.com:adityajagtiani89/extension-starter-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "Teamcity"
        passphrase = "credentialsJSON:3778f913-a638-4404-9c00-0adc64ef8646"
    }
})
