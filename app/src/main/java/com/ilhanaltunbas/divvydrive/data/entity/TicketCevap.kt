package com.ilhanaltunbas.divvydrive.data.entity

import com.google.gson.annotations.SerializedName

data class TicketCevap(
    @SerializedName("KullaniciAdi")
    val kullaniciAdi: String?,
    @SerializedName("ID")
    val ticket: String,
    @SerializedName("Sonuc")
    val sonuc: Boolean
)