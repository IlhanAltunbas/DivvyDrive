package com.ilhanaltunbas.divvydrive.data.model

import com.ilhanaltunbas.divvydrive.data.entity.Dosya
import com.ilhanaltunbas.divvydrive.data.entity.Klasor

sealed interface ListItem {
    val id: String
    val ad: String

    data class KlasorItem(val klasor: Klasor) : ListItem {
        override val id: String get () = "folder_${klasor.ID}"
        override val ad: String get () = klasor.Adi
    }

    data class DosyaItem(val dosya: Dosya) : ListItem {
        override val id: String get() = "file_${dosya.ID}"
        override val ad: String get() = dosya.Adi
        val boyut: String get() = dosya.Boyut
    }
}