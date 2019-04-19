package AEConfig.vcsRoots

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot

object AEConfig_Starter : GitVcsRoot({
    uuid = "b4830a0e-df16-47e3-9d8b-463c4a457b8a"
    extId = "AEConfig_Starter"
    name = "Starter"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/extension-starter-with-ci.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/extension-starter-with-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "id_rsa.pem"
    }
})
