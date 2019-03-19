package ExtensionStarter.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object extensionstarterci : GitVcsRoot({
    uuid = "feaa1794-49b2-43c7-83a0-0b822eee1bd9"
    name = "Extension Starter"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/extension-starter-with-ci.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    agentCleanPolicy = AgentCleanPolicy.ON_BRANCH_CHANGE
    branchSpec = """
        +:refs/heads/(master)
        +:refs/(pull-requests/*)/from
    """.trimIndent()
})



