package app.termora.actions

import app.termora.*

class OpenHostAction : AnAction() {
    companion object {
        /**
         * 打开一个主机
         */
        const val OPEN_HOST = "OpenHostAction"
    }

    override fun actionPerformed(evt: AnActionEvent) {
        if (evt !is OpenHostActionEvent) return
        val terminalTabbedManager = evt.getData(DataProviders.TerminalTabbedManager) ?: return
        val windowScope = evt.getData(DataProviders.WindowScope) ?: return

        // 如果不支持 SFTP 那么不处理这个响应
        if (evt.host.protocol == Protocol.SFTPPty) {
            if (!SFTPPtyTerminalTab.canSupports) {
                return
            }
        }

        val tab = when (evt.host.protocol) {
            Protocol.SSH -> SSHTerminalTab(windowScope, evt.host)
            Protocol.Serial -> SerialTerminalTab(windowScope, evt.host)
            Protocol.SFTPPty -> SFTPPtyTerminalTab(windowScope, evt.host)
            else -> LocalTerminalTab(windowScope, evt.host)
        }

        terminalTabbedManager.addTerminalTab(tab)
        tab.start()
    }
}