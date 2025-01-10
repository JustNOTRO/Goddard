package dev.notro.ideissuer.dialogs

import com.intellij.collaboration.auth.ServerAccount
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import dev.notro.ideissuer.git.accessToken
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

const val GODDARD_TITLE = "Goddard"

private const val ISSUE_TITLE_MAX_LENGTH = 255

private const val ISSUE_DESCRIPTION_MAX_LENGTH = 10000

class IssueCreationDialog(private val project: Project) {

    lateinit var selectedAccount: ServerAccount

    internal lateinit var title: String

    internal lateinit var description: String

    private val titleField: JTextField = JTextField(20)

    private val descriptionArea = JTextArea(10, 20)

    private val optionsBox: ComboBox<ServerAccount> = ComboBox(ServerAccountBoxModel(), 150)

    fun chatWithUser(): Boolean {
        val panel = setupPanel()

        val option = JOptionPane.showConfirmDialog(
            null,
            panel,
            GODDARD_TITLE,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )

        if (option == JOptionPane.CANCEL_OPTION)
            return false

        println("Stage 1")

        val title = titleField.text
        if (title.isEmpty()) {
            Messages.showErrorDialog(project, "Issue title cannot be empty!", GODDARD_TITLE)
            return false
        }

        println("Stage 2")

        if (title.length > ISSUE_TITLE_MAX_LENGTH) {
            Messages.showErrorDialog(project, "Issue title is too long!", GODDARD_TITLE)
            return false
        }

        println("Stage 3")

        val description = descriptionArea.text
        if (description.length > ISSUE_DESCRIPTION_MAX_LENGTH) {
            Messages.showErrorDialog(project, "Description is too long!", GODDARD_TITLE)
            return false
        }

        println("Stage 4")

        val selectedAccount = optionsBox.selectedItem as ServerAccount?
        if (selectedAccount == null) {
            Messages.showErrorDialog(project, "Do here something in future", GODDARD_TITLE)
            return false
        }

        println("Stage 5")

        val accessToken = selectedAccount.accessToken
        if (accessToken.isEmpty()) {
            Messages.showErrorDialog(project, "Access token Id cannot be empty!", GODDARD_TITLE)
            return false
        }

        println("Stage 6")

        this.title = title
        this.description = description
        this.selectedAccount = selectedAccount

        println("Passed dialog")
        return true
    }

    private fun setupPanel(): JPanel {
        val constraints = GridBagConstraints()
        setGrid(constraints, 0, 0)

        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = JBUI.insets(10, 10, 5, 10)

        val panel = JPanel(GridBagLayout())
        setTitle(panel, constraints)
        setDescription(panel, constraints)

        setOptions(panel, constraints)
//        panel.add(GithubChooseAccountDialog(project,
//            panel,
//            GHAccountsUtil.accounts,
//            "Choose Account",
//            true,
//            true,
//            GODDARD_TITLE).contentPanel
//        )

        return panel
    }

    private fun setTitle(panel: JPanel, constraints: GridBagConstraints) {
        panel.add(JLabel("Enter issue title: "), constraints)

        setGrid(constraints, 1, 0)
        constraints.fill = GridBagConstraints.HORIZONTAL
        setWeight(constraints, 1.0, 0.0)

        panel.add(titleField, constraints)
        setGrid(constraints, 0, 1)
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = JBUI.insets(5, 10)

        titleField.preferredSize = Dimension(200, 35)
    }

    private fun setDescription(panel: JPanel, constraints: GridBagConstraints) {
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true

        panel.add(JLabel("Enter description: "), constraints)

        setGrid(constraints, 1, 1)
        constraints.fill = GridBagConstraints.BOTH
        setWeight(constraints, 1.0, 1.0)

        val descriptionScrollPane = JBScrollPane(descriptionArea)
        panel.add(descriptionScrollPane, constraints)

        setGrid(constraints, 0, 2)
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = JBUI.insets(5, 10)

        descriptionArea.preferredSize = Dimension(250, 500)
    }

    private fun setOptions(panel: JPanel, constraints: GridBagConstraints) {
        setGrid(constraints, 4, 0)
        setWeight(constraints, 2.0, 0.0)
        panel.add(JLabel("Pick instance: "), constraints)

        panel.add(optionsBox, constraints)

//        optionsBox.preferredSize = Dimension(250, 35)
    }

    private fun setGrid(constraints: GridBagConstraints, x: Int, y: Int) {
        constraints.gridx = x
        constraints.gridy = y
    }

    private fun setWeight(constraints: GridBagConstraints, x: Double, y: Double) {
        constraints.weightx = x
        constraints.weighty = y
    }
}