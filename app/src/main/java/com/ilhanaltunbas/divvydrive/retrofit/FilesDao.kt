package com.ilhanaltunbas.divvydrive.retrofit

import com.ilhanaltunbas.divvydrive.data.entity.Klasor
import com.ilhanaltunbas.divvydrive.data.entity.KlasorVeDosyaIslemleriDonenSonuc
import com.ilhanaltunbas.divvydrive.data.entity.TicketCevap
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface FilesDao {

    // BASE URL : https://test.divvydrive.com/Test/Staj/
    @POST("TicketAl")
    @FormUrlEncoded
    suspend fun girisYap(
        @Field ("KullaniciAdi") kullaniciAdi : String,
        @Field ("Sifre") sifre : String
    ): TicketCevap

    @GET("KlasorListesiGetir")
    suspend fun klasorListesiGetir(
        @Query("ticketID") ticketId: String?,
        @Query("klasorYolu") klasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @GET("DosyaListesiGetir")
    suspend fun dosyaListesiGetir(
        @Query("ticketID") ticketId: String?,
        @Query("klasorYolu") klasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @POST("KlasorOlustur")
    @FormUrlEncoded
    suspend fun klasorOlustur (
        @Field("ticketID") ticketId: String?,
        @Field("klasorAdi") klasorAdi: String,
        @Field("klasorYolu") klasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @POST("DosyaOlustur")
    @FormUrlEncoded
    suspend fun dosyaOlustur (
        @Field("ticketID") ticketId: String?,
        @Field("klasorYolu") klasorYolu: String,
        @Field("dosyaAdi") dosyaAdi: String
    ) : KlasorVeDosyaIslemleriDonenSonuc



    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "KlasorSil", hasBody = true)
    suspend fun klasorSil (
        @Field("ticketID") ticketId: String?,
        @Field("klasorAdi") klasorAdi: String,
        @Field("klasorYolu") klasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "DosyaSil", hasBody = true)
    suspend fun dosyaSil (
        @Field("ticketID") ticketId: String?,
        @Field("klasorYolu") klasorYolu: String,
        @Field("dosyaAdi") dosyaAdi: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @FormUrlEncoded
    @PUT("KlasorGuncelle")
    suspend fun klasorGuncelle(
        @Field("ticketID") ticketId: String?,
        @Field("klasorAdi") klasorAdi: String,
        @Field("klasorYolu") klasorYolu: String,
        @Field("yeniKlasorAdi") yeniKlasorAdi: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @FormUrlEncoded
    @PUT("DosyaGuncelle")
    suspend fun dosyaGuncelle(
        @Field("ticketID") ticketId: String?,
        @Field("klasorYolu") klasorYolu: String,
        @Field("dosyaAdi") dosyaAdi: String,
        @Field("yeniDosyaAdi") yeniDosyaAdi: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @FormUrlEncoded
    @PUT("KlasorTasi")
    suspend fun klasorTasi(
        @Field("ticketID") ticketId: String?,
        @Field("klasorAdi") klasorAdi: String,
        @Field("klasorYolu") klasorYolu: String,
        @Field("yeniKlasorYolu") yeniKlasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @FormUrlEncoded
    @PUT("DosyaTasi")
    suspend fun dosyaTasi(
        @Field("ticketID") ticketId: String?,
        @Field("klasorYolu") klasorYolu: String,
        @Field("dosyaAdi") dosyaAdi: String,
        @Field("yeniDosyaYolu") yeniDosyaYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc



    @POST("DosyaDirektYukle")
    suspend fun dosyaDirektYukle(
        @Query("ticketID") ticketId: String,
        @Query("dosyaAdi") dosyaAdi: String,
        @Query("klasorYolu") klasorYolu: String,
        @Query("dosyaHash") dosyaHash: String,
        @Body dosyaIcerigi: RequestBody
    ): retrofit2.Response<KlasorVeDosyaIslemleriDonenSonuc>


    @POST("DosyaMetadataKaydiOlustur")
    @FormUrlEncoded
    suspend fun dosyaMetadataKaydiOlustur (
        @Field("ticketID") ticketId: String?,
        @Field("parcaSayisi") parcaSayisi: String,
        @Field("dosyaAdi") dosyaAdi: String,
        @Field("herBirParcaninBoyutuByte") herBirParcaninBoyutuByte: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @POST("DosyaParcalariYukle")
    suspend fun dosyaParcalariYukkle(
        @Query("ticketID") ticketId: String,
        @Query("tempKlasorID") tempKlasorID: String,
        @Query("parcahash") parcahash: String,
        @Query("parcaNumarasi") parcaNumarasi: String,
        @Body dosyaIcerigi: RequestBody
    ): retrofit2.Response<KlasorVeDosyaIslemleriDonenSonuc>

    @POST("DosyaYayinla")
    @FormUrlEncoded
    suspend fun dosyaYayinla (
        @Field("ticketID") ticketId: String?,
        @Field("ID") id: String,
        @Field("dosyaAdi") dosyaAdi: String,
        @Field("klasorYolu") klasorYolu: String
    ) : KlasorVeDosyaIslemleriDonenSonuc

    @POST("DosyaIndir")
    @FormUrlEncoded
    suspend fun dosyaIndir (
        @Field("ticketID") ticketId: String?,
        @Field("indirilecekYol") indirilecekYol: String,
        @Field("klasorYolu") klasorYolu: String,
        @Field("dosyaAdi") dosyaAdi: String
    ) : retrofit2.Response<ResponseBody>

}