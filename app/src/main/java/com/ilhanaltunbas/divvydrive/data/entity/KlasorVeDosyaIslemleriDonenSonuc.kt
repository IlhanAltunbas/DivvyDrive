package com.ilhanaltunbas.divvydrive.data.entity

class KlasorVeDosyaIslemleriDonenSonuc(
    val Mesaj: String,
    val Sonuc: Boolean,
    val SonucKlasorListe: List<Klasor>?,
    val SonucDosyaListe: List<Dosya>?
) {
}