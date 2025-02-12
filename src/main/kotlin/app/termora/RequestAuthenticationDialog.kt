package app.termora

import app.termora.keymgr.KeyManager
import app.termora.keymgr.OhKeyPair
import com.formdev.flatlaf.extras.components.FlatComboBox
import com.jgoodies.forms.builder.FormBuilder
import com.jgoodies.forms.layout.FormLayout
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Window
import java.awt.event.ItemEvent
import javax.swing.*
import kotlin.math.max

class RequestAuthenticationDialog(owner: Window) : DialogWrapper(owner) {

    private val authenticationTypeComboBox = FlatComboBox<AuthenticationType>()
    private val rememberCheckBox = JCheckBox("Remember")
    private val passwordPanel = JPanel(BorderLayout())
    private val passwordPasswordField = OutlinePasswordField()
    private val publicKeyComboBox = FlatComboBox<OhKeyPair>()
    private val keyManager get() = KeyManager.getInstance()
    private var authentication = Authentication.No

    init {
        isModal = true
        title = "SSH User Authentication"
        controlsVisible = false

        init()

        pack()

        size = Dimension(max(380, size.width), size.height)

        setLocationRelativeTo(null)

        publicKeyComboBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                return super.getListCellRendererComponent(
                    list,
                    if (value is OhKeyPair) value.name else value,
                    index,
                    isSelected,
                    cellHasFocus
                )
            }
        }

        for (keyPair in keyManager.getOhKeyPairs()) {
            publicKeyComboBox.addItem(keyPair)
        }

        authenticationTypeComboBox.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                switchPasswordComponent()
            }
        }

    }

    override fun createCenterPanel(): JComponent {
        authenticationTypeComboBox.addItem(AuthenticationType.Password)
        authenticationTypeComboBox.addItem(AuthenticationType.PublicKey)
        val formMargin = "7dlu"
        val layout = FormLayout(
            "left:pref, $formMargin, default:grow",
            "pref, $formMargin, pref"
        )

        switchPasswordComponent()

        return FormBuilder.create().padding("$formMargin, $formMargin, $formMargin, $formMargin")
            .layout(layout)
            .add("${I18n.getString("termora.new-host.general.authentication")}:").xy(1, 1)
            .add(authenticationTypeComboBox).xy(3, 1)
            .add("${I18n.getString("termora.new-host.general.password")}:").xy(1, 3)
            .add(passwordPanel).xy(3, 3)
            .build()
    }

    private fun switchPasswordComponent() {
        passwordPanel.removeAll()
        if (authenticationTypeComboBox.selectedItem == AuthenticationType.Password) {
            passwordPanel.add(passwordPasswordField, BorderLayout.CENTER)
        } else if (authenticationTypeComboBox.selectedItem == AuthenticationType.PublicKey) {
            passwordPanel.add(publicKeyComboBox, BorderLayout.CENTER)
        }
        passwordPanel.revalidate()
        passwordPanel.repaint()
    }

    override fun createSouthPanel(): JComponent? {
        val box = super.createSouthPanel() ?: return null
        rememberCheckBox.isFocusable = false
        box.add(rememberCheckBox, 0)
        return box
    }

    override fun doCancelAction() {
        authentication = Authentication.No
        super.doCancelAction()
    }

    override fun doOKAction() {
        val type = authenticationTypeComboBox.selectedItem as AuthenticationType
        authentication = authentication.copy(
            type = type,
            password = if (type == AuthenticationType.Password) String(passwordPasswordField.password)
            else (publicKeyComboBox.selectedItem as OhKeyPair).id
        )
        super.doOKAction()
    }

    fun getAuthentication(): Authentication {
        isModal = true
        SwingUtilities.invokeLater { passwordPasswordField.requestFocusInWindow() }
        isVisible = true
        return authentication
    }

    fun isRemembered(): Boolean {
        return rememberCheckBox.isSelected
    }

}