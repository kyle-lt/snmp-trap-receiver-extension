package ExtensionStarterWithCi.vcsRoots

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot

object ExtensionStarterWithCi_ExtensionStarterWithCi : GitVcsRoot({
    uuid = "cc992ebf-55d2-440c-94e1-bd8bf704316b"
    extId = "ExtensionStarterWithCi_ExtensionStarterWithCi"
    name = "extension-starter-with-ci"
    url = "ssh://git@sf-bitbucket.corp.appdynamics.com:7999/bitbucket/ae/extension-starter-with-ci.git"
    pushUrl = "ssh://git@sf-bitbucket.corp.appdynamics.com:7999/bitbucket/ae/extension-starter-with-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "id_rsa.pem"
    }
})
