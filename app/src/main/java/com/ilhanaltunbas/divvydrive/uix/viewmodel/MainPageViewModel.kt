package com.ilhanaltunbas.divvydrive.uix.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilhanaltunbas.divvydrive.data.auth.TokenManager
import com.ilhanaltunbas.divvydrive.data.entity.Dosya
import com.ilhanaltunbas.divvydrive.data.entity.Klasor
import com.ilhanaltunbas.divvydrive.data.entity.KlasorVeDosyaIslemleriDonenSonuc
import com.ilhanaltunbas.divvydrive.data.model.ListItem
import com.ilhanaltunbas.divvydrive.data.network.ProgressRequestBody
import com.ilhanaltunbas.divvydrive.data.repo.FileRepository
import com.ilhanaltunbas.divvydrive.uix.state.SortOption
import com.ilhanaltunbas.divvydrive.uix.state.UploadType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.collections.sliceArray
import kotlin.math.ceil
import kotlin.math.min



@HiltViewModel
class MainPageViewModel @Inject constructor(
    private val frepo: FileRepository,
    private val tokenManager: TokenManager, private val application: Application)
    : ViewModel() {


    private val _klasorAdi = MutableStateFlow("")
    val klasorAdi: StateFlow<String> = _klasorAdi.asStateFlow()
    private val _yeniDosyaAdi = MutableStateFlow("") // Yeni dosya adı için
    val yeniDosyaAdi: StateFlow<String> = _yeniDosyaAdi.asStateFlow()

    private val _dosyaAdi = MutableStateFlow("")
    val dosyaAdi: StateFlow<String> = _dosyaAdi.asStateFlow()
    private val _klasorYolu = MutableStateFlow("")
    val klasorYolu: StateFlow<String> = _klasorYolu.asStateFlow()
    private val _itemsList = MutableStateFlow<List<ListItem>?>(null)
    val itemsList: StateFlow<List<ListItem>?> = _itemsList.asStateFlow()

    private val _currentSortOption = MutableStateFlow(SortOption.NONE) // Varsayılan sıralama
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()


    private val _klasorListesi = MutableStateFlow<List<Klasor>?>(null)
    val klasorListesi: StateFlow<List<Klasor>?> = _klasorListesi.asStateFlow()

    private val _dosyaListesi = MutableStateFlow<List<Dosya>?>(null)
    val dosyaListesi: StateFlow<List<Dosya>?> = _dosyaListesi.asStateFlow()
    private val _secilenDosyaAdi = MutableLiveData<String?>()
    val secilenDosyaAdi: LiveData<String?> = _secilenDosyaAdi


    // Yükleme durumu için StateFlow
    // --- Genel Yükleme State'leri ---
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _currentUploadType = MutableStateFlow(UploadType.NONE)
    val currentUploadType: StateFlow<UploadType> = _currentUploadType.asStateFlow()

    // --- Direkt Yükleme İçin State'ler ---
    private val _directUploadProgress = MutableLiveData<Int>()
    val directUploadProgress: LiveData<Int> = _directUploadProgress
    private val _directUploadResult = MutableLiveData<Result<KlasorVeDosyaIslemleriDonenSonuc>?>()
    val directUploadResult: LiveData<Result<KlasorVeDosyaIslemleriDonenSonuc>?> = _directUploadResult


    // --- Parçalı Yükleme İçin State'ler ---
    private val _chunkUploadProgress = MutableLiveData<Int>()
    val chunkUploadProgress: LiveData<Int> = _chunkUploadProgress

    private val _uploadStatus = MutableStateFlow<String?>(null)
    val uploadStatus: StateFlow<String?> = _uploadStatus.asStateFlow()

    private val _uploadResult = MutableLiveData<Result<KlasorVeDosyaIslemleriDonenSonuc>?>()
    val uploadResult: LiveData<Result<KlasorVeDosyaIslemleriDonenSonuc>?> = _uploadResult


    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private val _downloadOutcome = MutableStateFlow<Result<Uri>?>(null) // Uri? veya Unit? de olabilir
    val downloadOutcome: StateFlow<Result<Uri>?> = _downloadOutcome

    val messsageFromApi = MutableStateFlow<String?>(null)

    private val contentResolver: ContentResolver = application.contentResolver

    init {
        viewModelScope.launch {
            _klasorYolu.collect { path ->
                Log.d("MainPageViewModel", "Klasör yolu değiştirildi: $path")
                icerikListesiGetir()
            }
        }
        icerikListesiGetir()
    }



    fun klasoreGecisYap(klasorIsmi: String) {
        val current = _klasorYolu.value
        val newPath = if (current.isEmpty()) {
            klasorIsmi
        } else {
            "${current.trimEnd('/')}/$klasorIsmi"
        }
        _klasorYolu.value = newPath

    }

    fun ustKlasoreGecisYap() {
        val current = _klasorYolu.value
        if (current.isNotEmpty()) {
            val lastSlashIndex = current.lastIndexOf('/')
            if (lastSlashIndex >= 0) {
                _klasorYolu.value = current.substring(0, lastSlashIndex)
            } else {
                _klasorYolu.value = ""
            }
        }

    }

    fun updateKlasorAdi(newName: String) {
        _klasorAdi.value = newName
    }
    fun updateDosyaAdi(newName: String) {
        _dosyaAdi.value = newName
    }

    fun clearUploadResult() {
        _uploadResult.postValue(null)
    }
    fun clearDirectUploadResult() {
        _directUploadResult.postValue(null)
    }
    fun clearMessageFromApi() {
        messsageFromApi.value = null
    }


    fun cikisYap() {
        viewModelScope.launch {
            tokenManager.clearToken()
            Log.d("MainPageViewModel", "Çıkış yapıldı. Token Silindi")
            messsageFromApi.value = "Çıkış yapıldı"
        }

    }


    fun klasorOlustur() {
        viewModelScope.launch {
            val sonuc = frepo.klasorOlustur(tokenManager.getToken(), klasorAdi.value, klasorYolu.value)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }

    fun dosyaOlustur() {
        viewModelScope.launch {
            val sonuc = frepo.dosyaOlustur(tokenManager.getToken(), klasorYolu.value, dosyaAdi.value)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }
    fun klasorSil(silinecekKlasorAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.klasorSil(tokenManager.getToken(), silinecekKlasorAdi, klasorYolu.value)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }


    fun klasorGuncelle(klasorAdi: String, yeniKlasorAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.klasorGuncelle(tokenManager.getToken(), klasorAdi,klasorYolu.value, yeniKlasorAdi)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }

    }
    fun dosyaGuncelle( dosyaAdi : String,yeniDosyaAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.dosyaGuncelle(tokenManager.getToken(), klasorYolu.value,dosyaAdi, yeniDosyaAdi)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }

    fun dosyaSil(silinecekDosyaAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.dosyaSil(tokenManager.getToken(), klasorYolu.value,silinecekDosyaAdi)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }
    fun klasorTasi(klasorAdi: String, hedefKlasorAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.klasorTasi(tokenManager.getToken(),klasorAdi,  klasorYolu.value,hedefKlasorAdi)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }

    fun dosyaTasi(dosyaAdi: String, hedefKlasorAdi: String) {
        viewModelScope.launch {
            val sonuc = frepo.dosyaTasi(tokenManager.getToken(), klasorYolu.value,dosyaAdi, hedefKlasorAdi)
            icerikListesiGetir()
            messsageFromApi.value = sonuc.Mesaj
        }
    }


    fun icerikListesiGetir() {
        viewModelScope.launch {
            _itemsList.value = null
            try {
                Log.d("MainPageViewModel_Icerik", "Icerik getirme fonksiyonu cagirildi")


                val klasorNesnesiDeferred = async { frepo.klasorListesiGetir(
                    tokenManager.getToken(), klasorYolu.value
                ) }
                val dosyaNesnesiDeferred = async { frepo.dosyaListesiGetir(
                    tokenManager.getToken(), klasorYolu.value
                ) }



                val klasorNesnesi = klasorNesnesiDeferred.await()
                val dosyaNesnesi = dosyaNesnesiDeferred.await()

                val combinedList = mutableListOf<ListItem>()
                var klasorlerBasarili = false
                var dosyalarBasarili = false

                if (klasorNesnesi.Sonuc && klasorNesnesi.SonucKlasorListe != null) {
                    klasorNesnesi.SonucKlasorListe.forEach { klasor ->
                        combinedList.add(ListItem.KlasorItem(klasor))
                    }
                    _klasorListesi.value = klasorNesnesi.SonucKlasorListe
                    klasorlerBasarili = true
                } else {
                    _klasorListesi.value = emptyList()
                    Log.e("MainPageViewModel", "Klasor listesi getirilemedi: ${klasorNesnesi.Mesaj}")
                }

                if (dosyaNesnesi.Sonuc && dosyaNesnesi.SonucDosyaListe != null) {
                    dosyaNesnesi.SonucDosyaListe.forEach { dosya ->
                        combinedList.add(ListItem.DosyaItem(dosya))
                    }
                    _dosyaListesi.value = dosyaNesnesi.SonucDosyaListe
                    dosyalarBasarili = true
                } else {
                    _dosyaListesi.value = emptyList() // Geçici (opsiyonel)
                    Log.e("MainPageViewModel", "Dosya listesi getirilemedi: ${dosyaNesnesi.Mesaj}")
                }

                if (klasorlerBasarili || dosyalarBasarili || combinedList.isNotEmpty()) {
                    _itemsList.value = combinedList
                } else {
                    _itemsList.value = emptyList()
                    Log.e("MainPageViewModel", "Hem klasör hem de dosya listesi getirilemedi veya boş.")
                }

            } catch (e: Exception) {
                _klasorListesi.value = emptyList()
                _dosyaListesi.value = emptyList()
                _itemsList.value = emptyList()
                Log.e("MainPageViewModel", "Hata oluştu: ${e.message}")
            }
        }
    }

    fun handleFileSelectionForUpload(uri: Uri, hedefKlasorYoluOnServer: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileSize = contentResolver.getFileSize(uri)
            val oneMB = 1 * 1024 * 1024L

            if (fileSize <= 0) {
                Log.e("ViewModel", "Geçersiz dosya boyutu: $fileSize URI: $uri")
                _uploadResult.postValue(Result.failure(IOException("Geçersiz dosya boyutu.")))
                return@launch
            }

            Log.d("ViewModel", "Dosya seçildi: $uri, Boyut: $fileSize bytes, Hedef: $hedefKlasorYoluOnServer")

            if (fileSize < oneMB) {
                Log.i("ViewModel", "1MB'den küçük dosya, direkt yükleme başlatılıyor.")
                startDirectUpload(uri, hedefKlasorYoluOnServer)
            } else {
                Log.i("ViewModel", "1MB veya daha büyük dosya, parçalı yükleme başlatılıyor.")
                startChunkedUpload(uri, hedefKlasorYoluOnServer)
            }
        }
    }


    fun startDirectUpload(uri: Uri, hedefYol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            _currentUploadType.value = UploadType.DIRECT
            _directUploadProgress.postValue(0)
            _directUploadResult.postValue(null)

            Log.d("ViewModelDirect", "Direkt yükleme başlatıldı: $uri, Hedef: $hedefYol")
            try {
                val fileName = contentResolver.getFileName(uri) ?: "unknown_file_${System.currentTimeMillis()}"
                val fileBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Dosya okunamadı (direkt yükleme).")

                if (fileBytes.isEmpty()) {
                    throw IOException("Seçilen dosya boş (direkt yükleme).")
                }
                Log.d("ViewModelDirect", "Dosya Adı: $fileName, Byte Boyutu: ${fileBytes.size}")


                val progressRequestBody = ProgressRequestBody(
                    fileBytes = fileBytes,
                    listener = object : ProgressRequestBody.UploadCallbacks {
                        override fun onProgressUpdate(percentage: Int) { _directUploadProgress.postValue(percentage) }
                        override fun onError(e: Exception) { Log.e("DirectUpload", "Hata (callback): ${e.message}", e) }
                        override fun onFinish() { Log.i("DirectUpload", "Bitti (callback)") }
                    }
                )

                val result = frepo.dosyaDirektYukle(
                    targetFileNameOnServer = fileName,
                    fileRequestBody = progressRequestBody,
                    currentPath = hedefYol,
                    dosyaHash = calculateMD5FromBytes(fileBytes)
                )
                _directUploadResult.postValue(result)

                if(result.isSuccess) {
                    Log.i("ViewModelDirect", "Direkt yükleme API yanıtı başarılı: ${result.getOrNull()?.Mesaj}")
                    icerikListesiGetir()
                } else {
                    Log.e("ViewModelDirect", "Direkt yükleme API yanıtı başarısız: ${result.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                Log.e("ViewModelDirect", "Direkt yükleme sırasında genel hata: ${e.message}", e)
                _directUploadResult.postValue(Result.failure(e))
            } finally {
                _isUploading.value = false
                _currentUploadType.value = UploadType.NONE

            }
        }
    }



    fun clearUploadStatus() {
        _uploadStatus.value = null
    }




    private fun getFileBytesFromUri(uri: Uri, contentResolver: ContentResolver): ByteArray? {
        Log.d("ViewModelUtils", "getFileBytesFromUri çağrıldı URI: $uri")
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                Log.d("ViewModelUtils", "InputStream açıldı, readBytes çağrılacak.")
                val bytes = inputStream.readBytes()
                Log.d("ViewModelUtils", "readBytes bitti. Okunan byte sayısı: ${bytes.size}")
                bytes
            } ?: run {
                Log.e("ViewModelUtils", "contentResolver.openInputStream(uri) null döndü!")
                null
            }
        } catch (e: IOException) {
            Log.e("ViewModelUtils", "getFileBytesFromUri içinde URI'dan byte okuma hatası: $uri", e)
            null
        }
    }

    private fun ContentResolver.getFileSize(uri: Uri): Long {
        val cursor = this.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && !it.isNull(sizeIndex)) { // Ekstra null kontrolü
                    return it.getLong(sizeIndex)
                }
            }
        }
        Log.w("ViewModelUtils", "Dosya boyutu alınamadı URI: $uri")
        return 0L
    }


    private fun calculateMD5FromBytes(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(bytes)
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) { // NoSuchAlgorithmException
            Log.e("ViewModelUtils", "MD5 hesaplanırken hata", e)
            "" // Veya bir hata durumu yönetin
        }
    }
    private fun ContentResolver.getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }


    fun startChunkedUpload(uri: Uri, hedefSunucuKlasorYolu: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            _currentUploadType.value = UploadType.CHUNKED
            _chunkUploadProgress.postValue(0)
            _uploadStatus.value = "Parçalı yükleme hazırlanıyor..."
            _uploadResult.postValue(null)

            var tempKlasorIdFromServer: String? = null

            try {
                val dosyaAdi = contentResolver.getFileName(uri) ?: "unknown_${System.currentTimeMillis()}"
                val dosyaBoyutu = contentResolver.getFileSize(uri)

                if (dosyaBoyutu <= 0) {
                    _uploadStatus.value = "Hata: Dosya boyutu geçersiz veya okunamadı."
                    throw IOException("Dosya boyutu geçersiz veya okunamadı. URI: $uri")
                }

                Log.d("ChunkUploadVM", "Parçalı Yükleme Başladı - Dosya: $dosyaAdi, Boyut: $dosyaBoyutu, Hedef Klasör: $hedefSunucuKlasorYolu")

                val herBirParcaninBoyutuByte = 1 * 1024 * 1024L
                val parcaSayisi = ceil(dosyaBoyutu.toDouble() / herBirParcaninBoyutuByte.toDouble()).toInt()

                _uploadStatus.value = "Meta veri oluşturuluyor..."
                Log.d("ChunkUploadVM", "Meta veri oluşturuluyor. Parça Sayısı: $parcaSayisi")

                // Meta Veri Kaydı Oluştur
                val metadataResult = frepo.dosyaMetadataKaydiOlustur(
                    parcaSayisi = parcaSayisi.toString(),
                    dosyaAdi = dosyaAdi,
                    herBirParcaninBoyutuByte = herBirParcaninBoyutuByte.toString()
                )

                metadataResult.fold(
                    onSuccess = { donenSonuc ->
                        if (!donenSonuc.Sonuc || donenSonuc.Mesaj.isNullOrBlank()) {
                            _uploadStatus.value = "Hata: Meta veri kaydı başarısız veya ID alınamadı."
                            throw IOException("Meta veri kaydı başarısız veya tempKlasorID alınamadı: ${donenSonuc.Mesaj}")
                        }
                        tempKlasorIdFromServer = donenSonuc.Mesaj // Sunucudan dönen ID
                        Log.i("ChunkUploadVM", "Meta veri başarılı. Geçici ID: $tempKlasorIdFromServer")
                    },
                    onFailure = {
                        _uploadStatus.value = "Hata: Meta veri API çağrısı başarısız oldu."
                        throw it
                    }
                )


                val dosyaBytesTamami = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Dosya içeriği okunamadı. URI: $uri")



                // ADIM 2: Parçaları Yükle
                _uploadStatus.value = "Parçalar yükleniyor (0/$parcaSayisi)..."
                Log.d("ChunkUploadVM", "Parça yükleme başlıyor. Geçici ID: $tempKlasorIdFromServer")
                var yuklenenByteSayisi: Long = 0

                for (i in 0 until parcaSayisi) {
                    val anlikParcaNumarasi = i // 0-indexed
                    val offset = i * herBirParcaninBoyutuByte
                    val parcaBoyutu = min(herBirParcaninBoyutuByte, dosyaBoyutu - offset)

                    if (parcaBoyutu <= 0) { // Okunacak byte kalmadıysa veya hata varsa
                        Log.w("ChunkUploadVM", "Parça $anlikParcaNumarasi için hesaplanan boyut <= 0. Atlanıyor veya döngü sonlandırılıyor.")
                        if (dosyaBoyutu - offset <= 0 && i > 0) break // Son parça işlendi, çık
                        continue
                    }

                    val endIndex = (offset + parcaBoyutu).toInt()
                    if (endIndex > dosyaBytesTamami.size || offset.toInt() >= endIndex) {
                        Log.e("ChunkUploadVM", "Parça $anlikParcaNumarasi için geçersiz indeks. Offset: $offset, ParcaBoyutu: $parcaBoyutu, EndIndex: $endIndex, DosyaBoyutu: ${dosyaBytesTamami.size}")
                        throw IOException("Parça $anlikParcaNumarasi için geçersiz indeks hesaplandı.")
                    }

                    val parcaBytes = dosyaBytesTamami.sliceArray(offset.toInt() until endIndex)

                    if (parcaBytes.isEmpty()) {
                        Log.w("ChunkUploadVM", "Parça $anlikParcaNumarasi boş. Atlanıyor.")
                        continue
                    }

                    val parcaHash = calculateMD5FromBytes(parcaBytes)
                    _uploadStatus.value = "Parça ${anlikParcaNumarasi + 1}/$parcaSayisi yükleniyor..."

                    // Her parça için progress takibi
                    val parcaProgressCallback = object : ProgressRequestBody.UploadCallbacks {
                        override fun onProgressUpdate(percentage: Int) {
                            val buParcadanYuklenen = (parcaBytes.size.toDouble() * percentage.toDouble() / 100.0).toLong()
                            val genelYuklenenToplam = yuklenenByteSayisi + buParcadanYuklenen
                            val genelProgressYuzde = ((genelYuklenenToplam.toDouble() / dosyaBoyutu.toDouble()) * 100.0).toInt()
                            _chunkUploadProgress.postValue(min(100, genelProgressYuzde))
                        }
                        override fun onError(e: Exception) {
                            Log.e("ChunkUploadVM_ParcaCallback", "Parça ${anlikParcaNumarasi + 1} yüklenirken hata (callback): ${e.message}", e)
                        }
                        override fun onFinish() {

                        }
                    }

                    val progressParcaRequestBody = ProgressRequestBody(
                        fileBytes = parcaBytes,
                        listener = parcaProgressCallback
                    )

                    Log.d("ChunkUploadVM", "Parça ${anlikParcaNumarasi + 1} yükleniyor. Boyut: ${parcaBytes.size}, Hash: $parcaHash")
                    val parcaYuklemeResult = frepo.dosyaParcasiYukle(
                        tempKlasorIdFromServer!!,
                        parcaHash,
                        anlikParcaNumarasi.toString(),
                        progressParcaRequestBody
                    )

                    parcaYuklemeResult.fold(
                        onSuccess = { parcaSonuc ->
                            if (!parcaSonuc.Sonuc) {
                                _uploadStatus.value = "Hata: Parça ${anlikParcaNumarasi + 1} yüklenemedi."
                                throw IOException("Parça ${anlikParcaNumarasi + 1} yüklenemedi: ${parcaSonuc.Mesaj}")
                            }
                            Log.i("ChunkUploadVM", "Parça ${anlikParcaNumarasi + 1} başarıyla yüklendi.")
                            yuklenenByteSayisi += parcaBytes.size // Sadece bu parça bittiğinde ana sayaca ekle
                            // Parça bittikten sonra genel progress'i tekrar güncelle
                            val anlikGenelProgress = ((yuklenenByteSayisi.toDouble() / dosyaBoyutu.toDouble()) * 100.0).toInt()
                            _chunkUploadProgress.postValue(min(100, anlikGenelProgress))
                        },
                        onFailure = {
                            _uploadStatus.value = "Hata: Parça ${anlikParcaNumarasi + 1} API çağrısı başarısız oldu."
                            throw it
                        }
                    )
                }

                Log.i("ChunkUploadVM", "Tüm parçalar başarıyla yüklendi. Toplam yüklenen: $yuklenenByteSayisi")
                _chunkUploadProgress.postValue(100) // Tüm parçalar bittiğinde %100 yap

                // Dosyayı Yayınla
                _uploadStatus.value = "Dosya yayınlanıyor..."
                Log.d("ChunkUploadVM", "Dosya yayınlanıyor. Geçici ID: $tempKlasorIdFromServer, Adı: $dosyaAdi, Hedef Klasör: $hedefSunucuKlasorYolu")
                val yayinlaResult = frepo.dosyaYayinla(
                    tempKlasorIdFromServer!!,
                    dosyaAdi,
                    hedefSunucuKlasorYolu,
                )

                yayinlaResult.fold(
                    onSuccess = { donenSonuc ->
                        if (!donenSonuc.Sonuc) {
                            _uploadStatus.value = "Hata: Dosya yayınlanamadı."
                            throw IOException("Dosya yayınlanamadı: ${donenSonuc.Mesaj}")
                        }
                        _uploadStatus.value = "Dosya başarıyla yüklendi ve yayınlandı!"
                        Log.i("ChunkUploadVM", "Dosya başarıyla yayınlandı: ${donenSonuc.Mesaj}")
                        _uploadResult.postValue(Result.success(donenSonuc))
                        icerikListesiGetir()
                    },
                    onFailure = {
                        _uploadStatus.value = "Hata: Dosya yayınlama API çağrısı başarısız oldu."
                        throw it
                    }
                )

            } catch (e: IOException) { // Bilinen I/O hataları
                Log.e("ChunkUploadVM", "Parçalı yükleme sırasında IO Hatası: ${e.message}", e)
                if (_uploadStatus.value?.startsWith("Hata:") == false) {
                    _uploadStatus.value = "Yükleme hatası: ${e.localizedMessage}"
                }
                _uploadResult.postValue(Result.failure(e))
            } catch (e: Exception) { // Diğer beklenmedik hatalar
                Log.e("ChunkUploadVM", "Parçalı yükleme sırasında genel hata: ${e.message}", e)
                if (_uploadStatus.value?.startsWith("Hata:") == false) {
                    _uploadStatus.value = "Yükleme sırasında beklenmedik bir hata oluştu."
                }
                _uploadResult.postValue(Result.failure(e))
            } finally {
                _isUploading.value = false
                _currentUploadType.value = UploadType.NONE
                Log.i("ChunkUploadVM", "Parçalı yükleme işlemi bitti (finally). isUploading: ${_isUploading.value}")

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun indirVeKaydetDosyayi(
        context: Context,
        apiIndirilecekYol: String,
        apiKlasorYolu: StateFlow<String>,
        dosyaAdi: String
    ) {
        if (dosyaAdi.isBlank()) {
            Log.e("MainPageViewModel", "Dosya adı boş.")
            _downloadOutcome.value = Result.failure(IllegalArgumentException("Dosya adı belirtilmedi."))
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0
            _downloadOutcome.value = null

            try {
                Log.d("MainPageViewModel", "frepo.dosyaIndir çağrılıyor. API Yolu: $apiIndirilecekYol, Klasör: $apiKlasorYolu, Ad: $dosyaAdi")
                val response: Response<ResponseBody> = frepo.dosyaIndir(
                    indirilecekYol = apiIndirilecekYol,
                    klasorYolu = apiKlasorYolu.value,
                    dosyaAdi = dosyaAdi
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    messsageFromApi.value = response.message()
                    if (responseBody != null) {
                        Log.d("MainPageViewModel", "Response başarılı, dosya kaydediliyor.")
                        val savedUri = saveFileToDownloads(
                            context,
                            responseBody,
                            dosyaAdi // Kaydedilecek dosya adı
                        ) { progressPercentage ->
                            _downloadProgress.value = progressPercentage
                        }

                        if (savedUri != null) {
                            Log.i("MainPageViewModel", "Dosya başarıyla indirildi ve kaydedildi: $savedUri")
                            _downloadOutcome.value = Result.success(savedUri)
                        } else {
                            Log.e("MainPageViewModel", "Dosya kaydedilemedi.")
                            _downloadOutcome.value = Result.failure(IOException("Dosya kaydedilemedi."))
                        }
                    } else {
                        Log.e("MainPageViewModel", "İndirme response body null. Code: ${response.code()}")
                        _downloadOutcome.value = Result.failure(IOException("İndirme yanıtı boş (body null)."))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Bilinmeyen sunucu hatası"
                    Log.e("MainPageViewModel", "Dosya indirme API hatası: ${response.code()} - $errorBody")
                    _downloadOutcome.value = Result.failure(IOException("Dosya indirilemedi: ${response.code()} - $errorBody"))
                }

            } catch (e: Exception) {
                Log.e("MainPageViewModel", "Dosya indirme sırasında genel hata: ${e.message}", e)
                _downloadOutcome.value = Result.failure(e)
            } finally {
                _isDownloading.value = false
                // Hata durumunda progress'i sıfırlayabilir veya UI'da bir hata göstergesi ayarlayabilirsiniz
                if (_downloadOutcome.value?.isFailure == true && _downloadProgress.value < 100) {
                    _downloadProgress.value = 0 // Veya -1 gibi bir hata değeri
                } else if (_downloadOutcome.value?.isSuccess == true && _downloadProgress.value < 100 && _downloadProgress.value > 0){
                    // Eğer başarılı oldu ama progress %100 değilse (contentLength 0 veya -1 ise olabilir)
                    _downloadProgress.value = 100 // Başarılıysa %100 yap
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveFileToDownloads(
        context: Context,
        responseBody: ResponseBody,
        fileName: String,
        onProgressUpdate: (percentage: Int) -> Unit
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            var outputStream: OutputStream? = null
            var inputStream: InputStream? = null
            var uri: Uri? = null

            try {
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                    inputStream = responseBody.byteStream()
                    val totalBytes = responseBody.contentLength()

                    if (outputStream != null) {
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE) // 4 * 1024
                        var bytesCopied: Long = 0
                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            bytes = inputStream.read(buffer)

                            if (totalBytes > 0) {
                                val progress = ((bytesCopied.toDouble() / totalBytes.toDouble()) * 100).toInt()
                                onProgressUpdate(progress)
                            } else {onProgressUpdate(0) // Ya da UI'da belirsiz progress göster
                            }
                        }
                        // Dosya sonuna gelindiğinde, eğer totalBytes bilinmiyorduysa %100 yap
                        if (totalBytes <= 0 && bytesCopied > 0) {
                            onProgressUpdate(100)
                        } else if (totalBytes > 0 && bytesCopied == totalBytes) {
                            onProgressUpdate(100)
                        }
                        Log.d("MainPageViewModel", "Dosya MediaStore'a kopyalandı. Kopyalanan byte: $bytesCopied, Toplam: $totalBytes")
                    } else {
                        Log.e("MainPageViewModel", "MediaStore output stream null.")
                        uri?.let { resolver.delete(it, null, null) }
                        uri = null
                    }
                } else {
                    Log.e("MainPageViewModel", "MediaStore'a yeni dosya girişi oluşturulamadı.")
                }
            } catch (e: Exception) {
                Log.e("MainPageViewModel", "Dosya MediaStore'a kaydedilirken hata: ${e.message}", e)
                uri?.let {
                    try { resolver.delete(it, null, null) } catch (deleteEx: Exception) { /* ignore */ }
                }
                uri = null
                onProgressUpdate(0) // Hata durumunda progress'i sıfırla
            } finally {
                try {
                    outputStream?.flush()
                    outputStream?.close()
                    inputStream?.close()
                } catch (ioe: IOException) {
                    Log.w("MainPageViewModel", "Stream kapatılırken hata: ${ioe.message}")
                }
            }
            uri
        }
    }

    fun clearDownloadOutcome() {

        _downloadOutcome.value = null
    }

    }