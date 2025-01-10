package dev.notro.ideissuer.dialogs

import com.intellij.collaboration.auth.ServerAccount
import org.jetbrains.plugins.github.authentication.GHAccountsUtil
import org.jetbrains.plugins.gitlab.authentication.accounts.PersistentGitLabAccountManager
import javax.swing.ComboBoxModel
import javax.swing.event.ListDataListener

class ServerAccountBoxModel : ComboBoxModel<ServerAccount> {

    private val accounts: MutableList<ServerAccount> = mutableListOf()

    private var selectedItem: ServerAccount?

    init {
        this.accounts.addAll(GHAccountsUtil.accounts)
        this.accounts.addAll(PersistentGitLabAccountManager().accountsState.value)
        this.selectedItem = accounts.first()
    }

    override fun getSize(): Int {
        return accounts.size
    }

    override fun getElementAt(index: Int): ServerAccount {
        return accounts[index]
    }

    override fun addListDataListener(l: ListDataListener?) {}

    override fun removeListDataListener(l: ListDataListener?) {}

    override fun setSelectedItem(anItem: Any?) {
        checkNotNull(anItem) { "Selected item cannot be null." }
        this.selectedItem = anItem as ServerAccount
    }

    override fun getSelectedItem(): Any? {
        return selectedItem
    }


}