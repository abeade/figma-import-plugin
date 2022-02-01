package com.abeade.plugin.figma.ui.autocomplete

import javax.swing.JTextField
import javax.swing.text.BadLocationException
import javax.swing.SwingUtilities
import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import java.lang.StringBuffer
import java.lang.Runnable
import java.util.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class Autocomplete(
    private val textField: JTextField,
    keywordList: List<String>,
    private val onChanged: (ev: DocumentEvent) -> Unit
) : DocumentListener {

    private enum class Mode {
        INSERT, COMPLETION
    }

    private var mode = Mode.INSERT
    private val keywords = keywordList.sorted()

    override fun changedUpdate(ev: DocumentEvent) {
        onChanged(ev)
    }

    override fun removeUpdate(ev: DocumentEvent) {
        onChanged(ev)
    }

    override fun insertUpdate(ev: DocumentEvent) {
        onChanged(ev)
        if (ev.length != 1 || keywords.isEmpty()) return
        val pos = ev.offset
        var content: String? = null
        try {
            content = textField.getText(0, pos + 1)
        } catch (e: BadLocationException) {
            e.printStackTrace()
        }

        // Find where the word starts
        var w: Int = pos
        while (w >= 0) {
            if (content!![w] == '-') {
                break
            }
            w--
        }

        // Too few chars
        if (pos - w < 2) return

        val prefix = content!!.substring(w + 1)
        val n = Collections.binarySearch(keywords, prefix).let {
            // Move next when found an exact match
            if (it > 0) -(it + 2) else it
        }
        if (n < 0 && -n <= keywords.size) {
            val match = keywords[-n - 1]
            if (match.startsWith(prefix)) {
                // A completion is found
                val completion = match.substring(pos - w)
                // We cannot modify Document from within notification,
                // so we submit a task that does the change later
                SwingUtilities.invokeLater(CompletionTask(completion, pos + 1))
            } else {
                // Match not found
                mode = Mode.INSERT
            }
        } else {
            // Nothing found
            mode = Mode.INSERT
        }
    }

    inner class CommitAction : AbstractAction() {

        override fun actionPerformed(ev: ActionEvent) {
            if (mode == Mode.COMPLETION) {
                val pos = textField.selectionEnd
                textField.caretPosition = pos
                mode = Mode.INSERT
            }
        }
    }

    private inner class CompletionTask(private val completion: String, private val position: Int) : Runnable {

        override fun run() {
            val sb = StringBuffer(textField.text)
            sb.insert(position, completion)
            textField.text = sb.toString()
            textField.caretPosition = position + completion.length
            textField.moveCaretPosition(position)
            mode = Mode.COMPLETION
        }
    }
}