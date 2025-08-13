package com.ilhanaltunbas.divvydrive.data.repo

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import com.ilhanaltunbas.divvydrive.data.auth.TokenManager
import com.ilhanaltunbas.divvydrive.data.datasource.RemoteDataSource
import com.ilhanaltunbas.divvydrive.data.entity.KlasorVeDosyaIslemleriDonenSonuc
import com.ilhanaltunbas.divvydrive.data.entity.TicketCevap
import com.ilhanaltunbas.divvydrive.data.network.ProgressRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class FileRepository @Inject constructor(var rds: RemoteDataSource, var tokenManager: TokenManager) {
    suspend fun girisYap(username: String, password: String) : TicketCevap {
        return rds.girisYap(username, password)
    }

    suspend fun klasorListesiGetir(token: String?, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
          return  rds.klasorListesiGetir(token, klasorYolu)
    }
    suspend fun dosyaListesiGetir(token: String?, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return  rds.dosyaListesiGetir(token, klasorYolu)
    }

    suspend fun klasorOlustur(token: String?, klasorAdi: String, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return  rds.klasorOlustur(token, klasorAdi, klasorYolu)
    }

    suspend fun klasorSil(token: String?, klasorAdi: String, klasorYolu: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.klasorSil(token, klasorAdi, klasorYolu)
    }

    suspend fun klasorGuncelle(token: String?, klasorAdi: String, klasorYolu: String, yeniKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.klasorGuncelle(token, klasorAdi, klasorYolu, yeniKlasorAdi)
    }

    suspend fun klasorTasi(token: String?, klasorAdi: String, klasorYolu: String,hedefKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.klasorTasi(token, klasorAdi,klasorYolu, hedefKlasorAdi)
    }

    suspend fun dosyaGuncelle(token: String?, klasorYolu: String,dosyaAdi: String, yeniDosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.dosyaGuncelle(token, klasorYolu,dosyaAdi, yeniDosyaAdi)
    }

    suspend fun dosyaSil(token: String?, klasorYolu: String,dosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.dosyaSil(token, klasorYolu, dosyaAdi)
    }
    suspend fun dosyaTasi(token: String?, klasorYolu: String,dosyaAdi: String, hedefKlasorAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.dosyaTasi(token, klasorYolu,dosyaAdi, hedefKlasorAdi)

    }
    suspend fun dosyaOlustur(token: String?, klasorYolu: String,dosyaAdi: String) : KlasorVeDosyaIslemleriDonenSonuc {
        return rds.dosyaOlustur(token, klasorYolu, dosyaAdi)
    }

    suspend fun dosyaDirektYukle(
        targetFileNameOnServer: String,
        fileRequestBody: RequestBody,
        currentPath: String,
        dosyaHash: String
    ): Result<KlasorVeDosyaIslemleriDonenSonuc> {

        val ticketId = tokenManager.getToken()
        if (ticketId.isNullOrBlank()) {
            Log.e("FileRepository", "Token bulunamadı, dosya yükleme iptal ediliyor.")
            return Result.failure(IOException("Token bulunamadı."))
        }
        Log.d("FileRepository", "Token alındı, RemoteDataSource çağrılacak.")

        return try {
            val response = rds.dosyaDirektYukle(
                ticketId = ticketId,
                dosyaAdiParam = targetFileNameOnServer,
                klasorYoluParam = currentPath,
                dosyaHashParam = dosyaHash,
                fileRequestBody = fileRequestBody // Doğrudan RequestBody (ProgressRequestBody)
            )

            // Yanıt işleme mantığınız (bu kısmı kendi yapınıza göre uyarlayın)
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.Sonuc) { // Sunucudan gelen Sonuc alanı true ise başarılı
                    Log.i("FileRepository", "Dosya yükleme API yanıtı başarılı: ${responseBody.Mesaj}")
                    Result.success(responseBody)
                } else {
                    Log.e("FileRepository", "Dosya yükleme sunucu hatası (Sonuc false): ${responseBody.Mesaj}")
                    Result.failure(IOException("Sunucu hatası (Sonuc false): ${responseBody.Mesaj}"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                Log.e("FileRepository", "Dosya yükleme API hatası: ${response.code()} ${response.message()} - Hata Gövdesi: $errorBody")
                Result.failure(IOException("API hatası: ${response.code()} ${response.message()} - Detay: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Dosya yükleme sırasında genel hata: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getTicket(): String? = tokenManager.getToken()

    suspend fun dosyaMetadataKaydiOlustur(
        parcaSayisi: String,
        dosyaAdi: String,
        herBirParcaninBoyutuByte: String
    ): Result<KlasorVeDosyaIslemleriDonenSonuc> {
        val ticket = getTicket() ?: return Result.failure(IOException("Token bulunamadı"))
        return try {
            val response = rds.dosyaMetadataKaydiOlustur(ticket, parcaSayisi, dosyaAdi, herBirParcaninBoyutuByte)
            // Yanıt işleme (isSuccessful, body null değil, body.Sonuc true)
            if (response.Sonuc) { // KlasorVeDosyaIslemleriDonenSonuc doğrudan dönüyor
                Result.success(response)
            } else {
                Result.failure(IOException("Meta veri kaydı oluşturulamadı: ${response.Mesaj}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun dosyaParcasiYukle(
        tempKlasorID: String,
        parcaHash: String,
        parcaNumarasi: String, // Veya Int
        parcaRequestBody: RequestBody // ProgressRequestBody buraya gelecek
    ): Result<KlasorVeDosyaIslemleriDonenSonuc> {
        val ticket = getTicket() ?: return Result.failure(IOException("Token bulunamadı"))
        return try {
            val response = rds.dosyaParcasiYukle(ticket, tempKlasorID, parcaHash, parcaNumarasi, parcaRequestBody)
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.Sonuc) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(IOException("Parça yüklenemedi: ${response.body()!!.Mesaj}"))
                }
            } else {
                Result.failure(IOException("Parça yükleme API hatası: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun dosyaYayinla(
        tempKlasorID: String, // 'ID' parametresi için
        dosyaAdi: String,
        klasorYolu: String
    ): Result<KlasorVeDosyaIslemleriDonenSonuc> {
        val ticket = getTicket() ?: return Result.failure(IOException("Token bulunamadı"))
        return try {
            val response = rds.dosyaYayinla(ticket, tempKlasorID, dosyaAdi, klasorYolu)
            if (response.Sonuc) {
                Result.success(response)
            } else {
                Result.failure(IOException("Dosya yayınlanamadı: ${response.Mesaj}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dosyaIndir(
        indirilecekYol: String,
        klasorYolu: String,
        dosyaAdi: String
    ): Response<ResponseBody> {
        val ticketId = getTicket()
        return rds.dosyaIndir(ticketId, indirilecekYol, klasorYolu, dosyaAdi)
    }

}
