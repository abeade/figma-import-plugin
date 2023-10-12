package com.abeade.plugin.figma.utils

import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import javax.swing.JComponent

internal fun JBPopup.showAbove(component: JComponent) {
    val northWest = RelativePoint(component, Point())

    addListener(object : JBPopupListener {
        override fun beforeShown(event: LightweightWindowEvent) {
            val popup = event.asPopup()
            val location = Point(popup.locationOnScreen).apply { y = northWest.screenPoint.y - popup.size.height }

            popup.setLocation(location)
        }
    })
    show(northWest)
}
