package dev.notro.ideissuer

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import java.net.HttpURLConnection
import java.net.URL

typealias GitRemoteUrl = String

private const val IDE_ISSUER_TITLE = "IDE-Issuer"
private const val ISSUE_TITLE_MAX_LENGTH = 255

class OpenIssueAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        var title: String? = null
        var attempts = 0

        while (title.isNullOrEmpty() && attempts < 5) {
            title = Messages.showInputDialog(project, "Enter issue title: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon())
            attempts++
        }

        if (title.isNullOrEmpty()) {
            Messages.showMessageDialog(project, "Too many attempts to open an issue. Try again later.", IDE_ISSUER_TITLE, Messages.getErrorIcon())
            return
        }

        if (title.length > ISSUE_TITLE_MAX_LENGTH) {
            Messages.showMessageDialog(project, "Title is too long! ${title.length}", IDE_ISSUER_TITLE, Messages.getErrorIcon())
            return
        }

        Messages.showInfoMessage("Successfully opened issue: $title", IDE_ISSUER_TITLE)
        sendRequestToOpenIssue(project)
    }

    private fun sendRequestToOpenIssue(project: Project) {
        val url = URL("https://www.google.com/") // todo get git remote

        val openConnection = url.openConnection() as HttpURLConnection

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

    private fun getCurrentRemote(project: Project): GitRemoteUrl {
        // todo implement logic using GitRepositoryManager.getInstance(project)
        return ""
    }

    private fun getRemote(): GitRemoteUrl {

        return ""
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

}