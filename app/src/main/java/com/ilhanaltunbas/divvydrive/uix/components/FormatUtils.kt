package com.ilhanaltunbas.divvydrive.uix.components

import java.text.DecimalFormat

object FormatUtils {
    fun formatSizeToMB(sizeInBytes: Long?): String {
        if (sizeInBytes == null || sizeInBytes < 0) {
            return "-" // Veya "0 MB", "Bilinmiyor" gibi bir ifade
        }
        if (sizeInBytes == 0L) {
            return "0 MB"
        }
        val sizeInKB = sizeInBytes.toDouble() / 1024.0
        val sizeInMB = sizeInKB / 1024.0

        val df =
            DecimalFormat("#.##")
        val formattedMB = df.format(sizeInMB)

        return "$formattedMB MB"
    }


}