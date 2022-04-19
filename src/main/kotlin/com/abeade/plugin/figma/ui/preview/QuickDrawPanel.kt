package com.abeade.plugin.figma.ui.preview

import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel

class QuickDrawPanel : JPanel() {

    private var image: BufferedImage? = null
    private var computedSize = Dimension()

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(image, 0, 0, this)
    }

    override fun getPreferredSize(): Dimension = computedSize

    fun setImage(bi: BufferedImage?) {
        image = bi
        setComponentSize()
        repaint()
    }

    private fun setComponentSize() {
        image?.let {
            computedSize.width = it.width
            computedSize.height = it.height
            revalidate()
        }
    }
}