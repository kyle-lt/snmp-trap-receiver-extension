package ExtensionStarterCiFinal.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object ExtensionStarterCiFinal_HttpsGithubComAdityajagtiani89extensionStarterCiRefsHead : GitVcsRoot({
    uuid = "895f58e6-65ec-4aab-9722-eeeb2b87c04a"
    name = "https://github.com/adityajagtiani89/extension-starter-ci#refs/heads/master"
    url = "https://github.com/adityajagtiani89/extension-starter-ci"
    authMethod = password {
        userName = "adityajagtiani89"
        password = "credentialsJSON:c5402b59-c0f5-484c-805f-402c2e6373a6"
    }
})
