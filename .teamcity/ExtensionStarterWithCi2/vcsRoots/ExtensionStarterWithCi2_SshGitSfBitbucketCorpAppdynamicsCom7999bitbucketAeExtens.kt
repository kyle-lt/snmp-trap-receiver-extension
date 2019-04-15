package ExtensionStarterWithCi2.vcsRoots

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot

object ExtensionStarterWithCi2_SshGitSfBitbucketCorpAppdynamicsCom7999bitbucketAeExtens : GitVcsRoot({
    uuid = "546da94d-5bd7-44a4-8545-47a87122a897"
    extId = "ExtensionStarterWithCi2_SshGitSfBitbucketCorpAppdynamicsCom7999bitbucketAeExtens"
    name = "ssh://git@sf-bitbucket.corp.appdynamics.com:7999/bitbucket/ae/extension-starter-with-ci.git"
    url = "ssh://git@sf-bitbucket.corp.appdynamics.com:7999/bitbucket/ae/extension-starter-with-ci.git"
    pushUrl = "ssh://git@sf-bitbucket.corp.appdynamics.com:7999/bitbucket/ae/extension-starter-with-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "id_rsa.pem"
    }
})
