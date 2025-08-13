package com.ilhanaltunbas.divvydrive.data.datasource

import com.ilhanaltunbas.divvydrive.data.auth.TokenManager
import com.ilhanaltunbas.divvydrive.data.entity.KlasorVeDosyaIslemleriDonenSonuc
import com.ilhanaltunbas.divvydrive.data.entity.TicketCevap
import com.ilhanaltunbas.divvydrive.retrofit.FilesDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(var fdao: FilesDao) {
    suspend fun girisYap(username: String, password: String): TicketCevap  {
        return fdao.girisYap(username, password)
    }


    suspend fun klasorListesiGetir(token: String?, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return  fdao.klasorListesiGetir(token, klasorYolu)
    }

    suspend fun dosyaListesiGetir(token: String?, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return  fdao.dosyaListesiGetir(token, klasorYolu)
    }

    suspend fun klasorOlustur (token: String?, klasorAdi: String, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return  fdao.klasorOlustur(token, klasorAdi, klasorYolu)
    }

    suspend fun klasorSil ( token: String?, klasorAdi: String, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.klasorSil(token, klasorAdi, klasorYolu)
    }

    suspend fun klasorGuncelle(token: String?, klasorAdi: String, klasorYolu: String, yeniKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.klasorGuncelle(token, klasorAdi, klasorYolu, yeniKlasorAdi)
    }

    suspend fun klasorTasi(token: String?, klasorAdi: String,klasorYolu: String, hedefKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.klasorTasi(token, klasorAdi, klasorYolu,hedefKlasorAdi)
    }

    suspend fun dosyaGuncelle(token: String?, klasorYolu: String,dosyaAdi: String, yeniDosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.dosyaGuncelle(token, klasorYolu,dosyaAdi, yeniDosyaAdi)
    }

    suspend fun dosyaSil(token: String?, klasorYolu: String,dosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.dosyaSil(token, klasorYolu, dosyaAdi)
    }

    suspend fun dosyaTasi(token: String?, klasorYolu: String,dosyaAdi: String, hedefKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.dosyaTasi(token, klasorYolu,dosyaAdi, hedefKlasorAdi)
    }

    suspend fun dosyaOlustur(token: String?, klasorYolu: String,dosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.dosyaOlustur(token, klasorYolu, dosyaAdi)
    }

    suspend fun dosyaDirektYukle(
        ticketId: String,
        dosyaAdiParam: String,
        klasorYoluParam: String,
        dosyaHashParam: String,
        fileRequestBody: RequestBody // FileRepository'den gelen RequestBody
    ): Response<KlasorVeDosyaIslemleriDonenSonuc> {
        // Doğrudan FilesDao'daki (güncellenmiş) metodu çağır
        return fdao.dosyaDirektYukle(
            ticketId = ticketId,
            dosyaAdi = dosyaAdiParam,
            klasorYolu = klasorYoluParam,
            dosyaHash = dosyaHashParam,
            dosyaIcerigi = fileRequestBody // FilesDao'daki @Body parametresine gider
        )
    }

    suspend fun dosyaMetadataKaydiOlustur(
        ticketId: String,
        parcaSayisi: String,
        dosyaAdi: String,
        herBirParcaninBoyutuByte: String
    ): KlasorVeDosyaIslemleriDonenSonuc { // Doğrudan sonucu dönebilir
        return fdao.dosyaMetadataKaydiOlustur(ticketId, parcaSayisi, dosyaAdi, herBirParcaninBoyutuByte)
    }

    suspend fun dosyaParcasiYukle(
        ticketId: String,
        tempKlasorID: String,
        parcaHash: String,
        parcaNumarasi: String, // Veya Int
        parcaRequestBody: RequestBody
    ): Response<KlasorVeDosyaIslemleriDonenSonuc> {
        return fdao.dosyaParcalariYukkle(ticketId, tempKlasorID, parcaHash, parcaNumarasi, parcaRequestBody)
    }

    suspend fun dosyaYayinla(
        ticketId: String,
        id: String, // tempKlasorID
        dosyaAdi: String,
        klasorYolu: String
    ): KlasorVeDosyaIslemleriDonenSonuc {
        return fdao.dosyaYayinla(ticketId, id, dosyaAdi, klasorYolu)
    }

    suspend fun dosyaIndir(
        ticketId: String?,
        indirilecekYol: String,
        klasorYolu: String,
        dosyaAdi: String
    ): Response<ResponseBody>  {
        return fdao.dosyaIndir(ticketId, indirilecekYol, klasorYolu, dosyaAdi)
    }
}
