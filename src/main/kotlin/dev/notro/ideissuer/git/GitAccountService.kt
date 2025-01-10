package dev.notro.ideissuer.git

import ai.grazie.utils.mpp.URLEncoder
import ai.grazie.utils.mpp.encodeURL
import com.intellij.collaboration.auth.ServerAccount
import com.intellij.openapi.project.Project
import dev.notro.ideissuer.dialogs.IssueCreationDialog
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.gitlab.authentication.accounts.GitLabAccount
import org.json.JSONArray
import org.json.JSONObject

private val JSON = "application/json".toMediaType()

object GitAccountService {

    private val projects: MutableMap<String, String> = mutableMapOf()

    fun createIssue(project: Project, dialog: IssueCreationDialog): Boolean {
        val selectedAccount = dialog.selectedAccount
        val url = "${generateUrl(selectedAccount)}/${getProjectId(project, selectedAccount)}/issues"

        val json = """
        {
            "title": "${dialog.title}",
            "description": "${dialog.description}"
        }
        """.trimIndent()

        val httpRequestData = HttpRequestData(selectedAccount, url, "POST", selectedAccount.accessToken, json.toRequestBody(JSON))
        val response = executeRequest(httpRequestData)
        return response.isSuccessful
    }

    private fun executeRequest(requestData: HttpRequestData): Response {
        val request = if (requestData.type == "POST")
            buildPostRequest(requestData)
        else
            buildGetRequest(requestData)

        val okHttpClient = OkHttpClient()

        println(request.header("Authorization"))

        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            throw RuntimeException("Error occurred while trying to create an issue.", e)
        }

        return response
    }

    private fun buildPostRequest(requestData: HttpRequestData): Request {
        return Request.Builder()
            .url(requestData.url)
            .post(requestData.body!!)
            .handleHeader(requestData.selectedAccount)
            .build()
    }

    private fun buildGetRequest(requestData: HttpRequestData): Request {
        return Request.Builder()
            .url(requestData.url)
            .get()
            .handleHeader(requestData.selectedAccount)
            .build()
    }

    private fun generateUrl(selectedAccount: ServerAccount): String {
        val accountName = selectedAccount.name
        return if (selectedAccount is GithubAccount)
            "https://api.github.com/search/repositories?q=user:$accountName"
         else
            "https://gitlab.com/api/v4/users/${accountName}/projects"
    }

    private fun getProjectId(project: Project, selectedAccount: ServerAccount): String {
        val projectKey = "${selectedAccount.name}/${project.name}"
        projects[projectKey]?.let { // if we already have it in cache
            return it
        }

        val url = URLEncoder.encodeURL(generateUrl(selectedAccount))

        // when we use GET we don't need to update anything, so body null
        val data = HttpRequestData(selectedAccount, url, "GET", selectedAccount.accessToken, null)

        val response = executeRequest(data)

        println(response.body!!.string())
        val gitProjects = JSONArray(response.body!!.string())

        for (gitProject in gitProjects) {
            gitProject as JSONObject

            val pathWithNamespace = getPathWithNamespace(selectedAccount, gitProject)
            println(pathWithNamespace)
//            println(projectPathWithNamespace)
            if (projectKey.equals(pathWithNamespace, ignoreCase = true)) {
                projects[projectKey] = gitProject.getInt("id").toString()
                return projects[projectKey]!!
            }
        }

        throw RuntimeException("Could not find project with that name.")
    }

    private fun Request.Builder.handleHeader(selectedAccount: ServerAccount): Request.Builder {
        return apply {
            if (selectedAccount is GithubAccount)
                header("Authorization", "token ${selectedAccount.accessToken}")
            else if (selectedAccount is GitLabAccount)
                header("PRIVATE-TOKEN", selectedAccount.accessToken)
        }
    }

    private fun getPathWithNamespace(selectedAccount: ServerAccount, gitProject: JSONObject): String {
        return if (selectedAccount is GithubAccount)
            gitProject.getString("full_name")
        else
            gitProject.getString("path_with_namespace")
    }
}

data class HttpRequestData(val selectedAccount: ServerAccount, val url: String, val type: String, val accessToken: String, val body: RequestBody?)