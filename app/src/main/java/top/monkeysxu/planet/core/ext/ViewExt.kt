package top.monkeysxu.planet.core.ext

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.loadSystemBar(tops: Int = -1) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v: View, insets: WindowInsetsCompat ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        val cutoutTop = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).top
        v.setPadding(
            0,
            if (tops == -1) maxOf(statusTop, cutoutTop) else tops,
            0,
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) ime.bottom else systemBars.bottom
        )
        insets
    }
}