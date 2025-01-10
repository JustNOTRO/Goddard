package dev.notro.ideissuer.git

import com.github.dockerjava.api.exception.UnauthorizedException
import com.intellij.collaboration.auth.ServerAccount
import com.intellij.openapi.project.ProjectManager
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.util.GHCompatibilityUtil
import org.jetbrains.plugins.gitlab.authentication.GitLabSecurityUtil

internal val ServerAccount.accessToken: String
    get() {
        val token = if (this is GithubAccount)
            GHCompatibilityUtil.getOrRequestToken(this, ProjectManager.getInstance().defaultProject)
        else
            buildNewToken()

        if (token == null)
            throw UnauthorizedException("User Unauthorized")

        return token
    }

internal val ServerAccount.namespace: String
    get() {
        val project = ProjectManager.getInstance().defaultProject
        return "${this.name}/${project.name}"
    }

private fun buildNewToken(): String {
    return try {
        val method = GitLabSecurityUtil::class.java.getDeclaredMethod("buildNewTokenUrl")
        method.isAccessible = true
        method.invoke(GitLabSecurityUtil) as String
    } catch (e: ReflectiveOperationException) {
        throw RuntimeException(e)
    }
}
