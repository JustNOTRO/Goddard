package dev.notro.ideissuer

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val IDE_ISSUER_TITLE = "IDE-Issuer"

private const val ISSUE_TITLE_MAX_LENGTH = 255

private val JSON = "application/json".toMediaType()

class OpenIssueAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        var title: String? = Messages.showInputDialog(project, "Enter issue title: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon())
        var attempts = 0

        if (title == null) // if he clicked cancel
            return

        var titleSize: Int? = title.length

        val invalidLength = titleSize!! > ISSUE_TITLE_MAX_LENGTH
        while (title!!.isEmpty() || invalidLength && attempts < 4) {
            title = Messages.showInputDialog(project, "Enter issue title: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon())
            titleSize = title?.length

            if (invalidLength)
                Messages.showErrorDialog(project, "Title is too long! $titleSize", IDE_ISSUER_TITLE)

            attempts++
        }

        if (title.isEmpty() || invalidLength) {
            Messages.showErrorDialog(project, "Too many attempts to open an issue. Try again later.", IDE_ISSUER_TITLE)
            return
        }

        val description = Messages.showInputDialog(project, "Enter description: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon()) ?: return

        val projectId = Messages.showInputDialog(project, "Enter project Id:  ", IDE_ISSUER_TITLE, Messages.getQuestionIcon()) ?: return
        if (projectId.isEmpty()) {
            Messages.showErrorDialog(project, "Project Id cannot be empty.", IDE_ISSUER_TITLE)
            return
        }

        val accessToken = Messages.showInputDialog(project, "Enter Access token: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon()) ?: return
        if (accessToken.isEmpty()) {
            Messages.showErrorDialog(project, "Access token Id cannot be empty.", IDE_ISSUER_TITLE)
            return
        }

        createIssue(project, title, description, projectId, accessToken)
    }

    private fun createIssue(project: Project, title: String, description: String, projectId: String, accessToken: String) {
        val url = "https://gitlab.com/api/v4/projects/$projectId/issues"
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

        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: Exception) {
            throw RuntimeException("Error occurred while trying to open an issue.", e)
        }

        if (response.isSuccessful)
            Messages.showMessageDialog("Opened issue: $title", IDE_ISSUER_TITLE, Messages.getInformationIcon())
        else
            Messages.showErrorDialog(project, "${response.message} ${response.code}", IDE_ISSUER_TITLE)
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