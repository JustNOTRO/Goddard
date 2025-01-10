package dev.notro.ideissuer.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import dev.notro.ideissuer.dialogs.IssueCreationDialog
import dev.notro.ideissuer.git.GitAccountService

//private val JSON = "application/json".toMediaType()

class OpenIssueAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val dialog = IssueCreationDialog(project)
        if (dialog.chatWithUser())
            GitAccountService.createIssue(project, dialog)
    }

//    private fun createIssue(project: Project, dialog: IssueCreationDialog) {
//        val accessToken = dialog.accessToken
//        val pathWithNameSpace = URLEncoder.encode(getProjectId(project, accessToken), "UTF-8")
//        val url = "https:/gitlab.com/api/v4/projects/$pathWithNameSpace/issues"
//
//        val json = """
//        {
//            "title": "${dialog.title}",
//            "description": "${dialog.description}"
//        }
//        """.trimIndent()
//
//        val body: RequestBody = json.toRequestBody(JSON)
//        val request: Request = Request.Builder()
//            .url(url)
//            .post(body)
//            .header("PRIVATE-TOKEN", accessToken)
//            .build()
//
//        HttpRequestTask(project, dialog, request).run {
//            val response = executeRequest(request)
//
//            ApplicationManager.getApplication().invokeLater {
//                if (response.isSuccessful)
//                    Messages.showMessageDialog(project, "Opened issue: $dialog.title", GODDARD_TITLE, Messages.getInformationIcon())
//                else
//                    Messages.showErrorDialog(project, "Error could not open issue: $dialog.title", GODDARD_TITLE)
//            }
//
//            response.close()
//        }
//    }

//    private fun getProjectId(project: Project, accessToken: String): String {
//        val url = "https://gitlab.com/api/v4/projects?owned=true"
//        val request: Request = Request.Builder()
//            .url(url)
//            .get()
//            .header("PRIVATE-TOKEN", accessToken)
//            .build()
//
//        val okHttpClient = OkHttpClient()
//        val response = executeRequest(okHttpClient, request)
//
//        val projects = JSONArray(response.body!!.string())
//
//        for (gitProject in projects) {
//            gitProject as JSONObject
//            if (gitProject.getString("path").equals(project.name, ignoreCase = true))
//                return gitProject.getString("id")
//        }
//
//
//        throw RuntimeException("Failed to retrieve project id")
//    }

//    private fun executeRequest(okHttpClient: OkHttpClient, request: Request): Response {
//        return try {
//            okHttpClient.newCall(request).execute()
//        } catch (e: Exception) {
//            throw RuntimeException("Error occurred while trying to retrieve path with namespace.", e)
//        }
//    }

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