package dev.notro.ideissuer

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

internal const val IDE_ISSUER_TITLE = "IDE-Issuer"

private const val ISSUE_TITLE_MAX_LENGTH = 255

class IssueCreationDialog(private val project: Project) {

    internal var title: String? = null

    internal var description: String? = null

    internal var accessToken: String? = null

    fun chatWithUser(): Boolean {
        title = Messages.showInputDialog(project, "Enter issue title: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon())
        var attempts = 0

        if (title == null) // if he clicked cancel
            return false

        var titleLength: Int? = title!!.length

        val invalidTitleLength = titleLength!! > ISSUE_TITLE_MAX_LENGTH
        while (title!!.isEmpty() || invalidTitleLength && attempts < 4) {
            title = Messages.showInputDialog(project, "Enter issue title: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon())
            titleLength = title?.length

            if (invalidTitleLength)
                Messages.showErrorDialog(project, "Title is too long! $titleLength", IDE_ISSUER_TITLE)

            attempts++
        }

        if (title!!.isEmpty() || invalidTitleLength) {
            Messages.showErrorDialog(project, "Too many attempts to open an issue. Try again later.", IDE_ISSUER_TITLE)
            return false
        }

        description = Messages.showInputDialog(project, "Enter description: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon()) ?: ""

        accessToken = Messages.showInputDialog(project, "Enter Access token: ", IDE_ISSUER_TITLE, Messages.getQuestionIcon()) ?: return false
        if (accessToken!!.isEmpty()) {
            Messages.showErrorDialog(project, "Access token Id cannot be empty.", IDE_ISSUER_TITLE)
            return false
        }

        return true
    }
}