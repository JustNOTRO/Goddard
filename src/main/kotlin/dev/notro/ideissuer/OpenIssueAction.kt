package dev.notro.ideissuer

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

private val JSON = "application/json".toMediaType()

class OpenIssueAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val dialog = IssueCreationDialog(project)
        if (dialog.chatWithUser()) // if the dialog passed successfully
            createIssue(project, dialog.title!!, dialog.description!!, dialog.accessToken!!)
    }

    private fun createIssue(project: Project, title: String, description: String, accessToken: String) {
        val pathWithNameSpace = URLEncoder.encode(getPathWithNameSpace(project, accessToken), "UTF-8")
        val url = "https:/gitlab.com/api/v4/projects/$pathWithNameSpace/issues"

        val json = """
        {
            "title": "$title",
            "description": "$description"
        }
        """.trimIndent()


        val body: RequestBody = json.toRequestBody(JSON)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .header("PRIVATE-TOKEN", accessToken)
            .build()

        val okHttpClient = OkHttpClient()
        val successful = AtomicBoolean(false)

        CompletableFuture.runAsync {
            val response = try {
                okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                throw RuntimeException("Error occurred while trying to open an issue.", e)
            }

            successful.set(true)
            response.close()
        }.thenAccept {
            SwingUtilities.invokeLater {
                if (successful.get())
                    Messages.showMessageDialog(project, "Opened issue: $title", IDE_ISSUER_TITLE, Messages.getInformationIcon())
                else
                    Messages.showErrorDialog(project, "Error could not open issue: $title", IDE_ISSUER_TITLE)
            }
        }
    }

    private fun getPathWithNameSpace(project: Project, accessToken: String): String {
        val url = "https://gitlab.com/api/v4/projects?owned=true"

        val request: Request = Request.Builder()
            .url(url)
            .get()
            .header("PRIVATE-TOKEN", accessToken)
            .build()

        val okHttpClient = OkHttpClient()
        var pathWithNameSpace = ""

        val future = CompletableFuture<String>()

        future.completeAsync {
            val response = executeRequest(okHttpClient, request, future)

            val projects = JSONArray(response!!.body!!.string())
            response.close()


            for (gitProject in projects) {
                gitProject as JSONObject
                if (gitProject.getString("path").equals(project.name, ignoreCase = true))
                    pathWithNameSpace = gitProject.getString("path_with_namespace")
            }

            pathWithNameSpace
        }

        return future.get()
    }

    private fun executeRequest(okHttpClient: OkHttpClient, request: Request, future: CompletableFuture<String>): Response? {
        return try {
            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            future.completeExceptionally(e)
            return null
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = false

        val editor = event.getData(CommonDataKeys.EDITOR) ?: return

        if (canOpenIssue(editor))
            event.presentation.isEnabledAndVisible = true
    }

    private fun getTextFromLine(editor: Editor): String {
        val document = editor.document

        val lineNumber = document.getLineNumber(editor.caretModel.offset)
        return document.getText(TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber)))
    }

    private fun canOpenIssue(editor: Editor): Boolean {
        val line = getTextFromLine(editor)
        return line.contains("todo", ignoreCase = true)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

}