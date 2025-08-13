package com.ilhanaltunbas.divvydrive.uix.view

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ilhanaltunbas.divvydrive.MainActivity
import com.ilhanaltunbas.divvydrive.R
import com.ilhanaltunbas.divvydrive.data.model.ListItem
import com.ilhanaltunbas.divvydrive.ui.theme.AcikSiyah
import com.ilhanaltunbas.divvydrive.ui.theme.LazyBackground
import com.ilhanaltunbas.divvydrive.ui.theme.LoginBorder
import com.ilhanaltunbas.divvydrive.ui.theme.LoginText
import com.ilhanaltunbas.divvydrive.ui.theme.MaviLogo
import com.ilhanaltunbas.divvydrive.uix.components.BottomSheetOptionItem
import com.ilhanaltunbas.divvydrive.uix.components.ExpandableFloatingActionButton
import com.ilhanaltunbas.divvydrive.uix.components.FileCardItemGrid
import com.ilhanaltunbas.divvydrive.uix.components.FileCardItemList
import com.ilhanaltunbas.divvydrive.uix.state.UploadType
import com.ilhanaltunbas.divvydrive.uix.state.ViewMode
import com.ilhanaltunbas.divvydrive.uix.viewmodel.MainPageViewModel
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController,
             mainPageViewModel: MainPageViewModel = hiltViewModel()
) {

    val klasorAdi by mainPageViewModel.klasorAdi.collectAsState()
    val dosyaAdi by mainPageViewModel.dosyaAdi.collectAsState()
    val anlikDosyaYolu by mainPageViewModel.klasorYolu.collectAsState()

    val itemsState by mainPageViewModel.itemsList.collectAsState()

    var dialogText by remember { mutableStateOf("") }

    val sheetStateSorting = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    var showDialogKlasor by remember { mutableStateOf(false) }
    var showDialogDosya by remember { mutableStateOf(false) }

    var currentViewMode by remember { mutableStateOf(ViewMode.LIST) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF37c7e9), Color(0xFF1e68a9)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    var showRenameDialogKlasor by remember { mutableStateOf(false) }
    var showRenameDialogDosya by remember { mutableStateOf(false) }
    var showMoveDialogKlasor by remember { mutableStateOf(false) }
    var showMoveDialogDosya by remember { mutableStateOf(false) }
    var showDeleteDialogKlasor by remember { mutableStateOf(false) }
    var showCikisYapDialog by remember { mutableStateOf(false) }
    var showDeleteDialogDosya by remember { mutableStateOf(false) }
    var degistirilecekIsim by remember { mutableStateOf("") }
    var degistirilecekYol by remember {mutableStateOf("")}

    val uploadResultState by mainPageViewModel.uploadResult.observeAsState()

    val currentUploadType by mainPageViewModel.currentUploadType.collectAsState()

    val directUploadProgress by mainPageViewModel.directUploadProgress.observeAsState(initial = 0)
    val directUploadResult by mainPageViewModel.directUploadResult.observeAsState()


    val chunkUploadProgress by mainPageViewModel.chunkUploadProgress.observeAsState(initial = 0)
    val uploadStatus by mainPageViewModel.uploadStatus.collectAsState() // Parçalı yükleme durumu
    val uploadResult by mainPageViewModel.uploadResult.observeAsState() // Parçalı yükleme sonucu



    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val isDownloading by mainPageViewModel.isDownloading.collectAsState()
    val downloadProgress by mainPageViewModel.downloadProgress.collectAsState()
    val downloadOutcome by mainPageViewModel.downloadOutcome.collectAsState()
    val kaydedilecekYer by remember { mutableStateOf("") }

    val isUploading by mainPageViewModel.isUploading.collectAsState()


    val messageFromApi by mainPageViewModel.messsageFromApi.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val hedefKlasorApiIcin = if (anlikDosyaYolu.isNullOrEmpty() || anlikDosyaYolu == "/") {
                    "/"
                } else {
                    anlikDosyaYolu
                }

                Log.d("MainPage", "Dosya seçildi: $uri. Hedef API Klasörü: $hedefKlasorApiIcin")

                mainPageViewModel.handleFileSelectionForUpload(uri, hedefKlasorApiIcin)
            } else {
                Log.d("MainPage", "Dosya seçilmedi veya seçici iptal edildi.")

            }
        }
    )

    LaunchedEffect(messageFromApi) { // messageFromApi her değiştiğinde bu blok çalışır
        messageFromApi?.let { currentMessage -> // Eğer mesaj null değilse
            snackbarHostState.showSnackbar(
                message = currentMessage,
                duration = SnackbarDuration.Short // Veya ihtiyacınıza göre Long
            )
            // ÖNEMLİ: Mesajı gösterdikten sonra ViewModel'de temizleyin ki
            // config değişikliği gibi durumlarda aynı mesaj tekrar gösterilmesin.
            mainPageViewModel.clearMessageFromApi()
        }
    }

    LaunchedEffect(downloadOutcome) {
        downloadOutcome?.let { result ->
            val message = result.fold(
                onSuccess = { uri -> "Dosya başarıyla indirildi: ${uri?.lastPathSegment ?: "Bilinmiyor"}" },
                onFailure = { exception -> "İndirme hatası: ${exception.localizedMessage}" }
            )
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            mainPageViewModel.clearDownloadOutcome() // Sonucu işledikten sonra temizle
        }
    }

    LaunchedEffect(directUploadResult) {
        directUploadResult?.let { result ->
            val message = result.fold(
                onSuccess = { "Başarılı (Direkt): ${it.Mesaj ?: "Tamamlandı"}" },
                onFailure = { "Hata (Direkt): ${it.localizedMessage ?: "Bilinmeyen"}" }
            )
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
            mainPageViewModel.clearDirectUploadResult()
        }
    }

    LaunchedEffect(uploadResult) {
        uploadResult?.let { result ->
            val message = result.fold(
                onSuccess = { "Başarılı (Parçalı): ${it.Mesaj ?: "Tamamlandı"}" },
                onFailure = { "Hata (Parçalı): ${it.localizedMessage ?: "Bilinmeyen"}" }
            )
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
            mainPageViewModel.clearUploadResult()
        }
    }

    BackHandler(enabled = anlikDosyaYolu.isNotEmpty()) {
        mainPageViewModel.ustKlasoreGecisYap()
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val titleText = if (anlikDosyaYolu.isEmpty()) {
                        "DivvyDrive"
                    } else {
                        "/$anlikDosyaYolu"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            tint = Color.Unspecified,
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp)),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row {
                                Text(text = "Divvy", fontSize = 18.sp, color = AcikSiyah,fontWeight = FontWeight.Bold)
                                Text(text = "Drive", fontSize = 18.sp, color = MaviLogo,fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = titleText,
                                fontSize = 12.sp,
                                color = LoginText
                            )
                        }
                    }
                },
                actions = {
                    // Sadece çıkış butonu
                    IconButton(onClick = {
                        showCikisYapDialog = true

                    },
                        modifier = Modifier.padding(end = 8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.outline_logout_24),
                            contentDescription = "Çıkış yap",
                            tint = MaviLogo
                        )
                    }
                },

                )
        },
        floatingActionButton = {
            ExpandableFloatingActionButton(
                onFabClicked = { expanded ->
                },
                onOption1Clicked = {
                    if (!isUploading) {
                        filePickerLauncher.launch("*/*")
                    }
                },
                onOption2Clicked = {
                    showDialogDosya = true
                },
                onOption3Clicked = {
                    Log.d("AlertDialogCheck", "onOption3Clicked çağrıldı. showDialog true yapılacak.")
                    showDialogKlasor = true // Bu satırın çalıştığından emin olun
                    Log.d("AlertDialogCheck", "showDialog şimdi: $showDialogKlasor")
                },
                mainFabGradientColors = listOf(Color(0xFF37c7e9), Color(0xFF1e68a9)),
                expandedFabGradientColors = listOf(Color(0xFF37c7e9), Color(0xFF1e68a9)),


            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->



        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()

                .padding(top = 12.dp, bottom = 8.dp)
                .height(2.dp)
                .background(Color.LightGray.copy(alpha = 0.6f))
            ) {

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .then(
                                if (currentViewMode == ViewMode.LIST) {
                                    Modifier.background(
                                        gradientBrush,
                                        RoundedCornerShape(8.dp)
                                    )
                                } else {
                                    Modifier
                                        .border(1.dp, LoginBorder, RoundedCornerShape(8.dp))
                                        .background(Color.White)

                                }
                            )
                            .clickable { currentViewMode = ViewMode.LIST }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FormatListBulleted,
                            contentDescription = "Liste görünümü",
                            tint = if (currentViewMode == ViewMode.LIST) Color.White else MaviLogo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "List",
                            color = if (currentViewMode == ViewMode.LIST) Color.White else MaviLogo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .then(
                                if (currentViewMode == ViewMode.GRID) {
                                    Modifier.background(
                                        gradientBrush,
                                        RoundedCornerShape(8.dp)
                                    )
                                } else {
                                    Modifier
                                        .border(1.dp, LoginBorder, RoundedCornerShape(8.dp))
                                        .background(Color.White)

                                }
                            )
                            .clickable { currentViewMode = ViewMode.GRID }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = "Izgara Görünümü",
                            tint = if (currentViewMode == ViewMode.GRID) Color.White else MaviLogo,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Grid",
                            color = if (currentViewMode == ViewMode.GRID) Color.White else MaviLogo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))


                }

                Box {
                    Row(
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    sheetStateSorting.show()
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = "Sırala",
                            tint = MaviLogo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sırala", color = MaviLogo, fontSize = 14.sp)
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Sıralama Seçeneklerini Aç",
                            tint = MaviLogo,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()

                .padding(top = 8.dp)
                .height(2.dp)
                .background(Color.LightGray.copy(alpha = 0.6f))
            ) {

            }

            val currentItems = itemsState
            if (currentItems == null) {
                // Yükleniyor durumu
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (currentItems.isEmpty()) {
                // Boş liste durumu
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bu Klasör Boş.")
                }
            } else {

                if (isUploading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    when (currentUploadType) {
                        UploadType.CHUNKED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                uploadStatus?.let { status ->
                                    Text(
                                        status,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { chunkUploadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "$chunkUploadProgress%",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        UploadType.DIRECT -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    "Dosya direkt yükleniyor...",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { directUploadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "$directUploadProgress%",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        UploadType.NONE -> {
                            CircularProgressIndicator()
                            Text("Yükleniyor...", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isDownloading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, // İçeriği ortalar
                        modifier = Modifier
                            .fillMaxWidth() // Genişliği doldurur
                            .padding(vertical = 8.dp, horizontal = 16.dp) // Kenarlardan boşluk
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f }, // Progress 0.0f ile 1.0f arasında olmalı
                            modifier = Modifier.fillMaxWidth() // Progress bar'ın genişliğini ayarlar
                        )
                        Text(
                            text = "İndiriliyor: $downloadProgress%",
                            fontSize = 12.sp, // Yazı tipi boyutu
                            modifier = Modifier.padding(top = 4.dp) // Progress bar ile arasında boşluk
                        )
                    }
                }


                when (currentViewMode) {
                    ViewMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LazyBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(
                                items = currentItems,
                                key = { listItem -> listItem.id }
                            ) { listItem ->
                                when (listItem){
                                    is ListItem.KlasorItem -> {
                                        FileCardItemList(listItem.klasor.Adi,
                                            "",
                                            navController,
                                            onFolderClicked = { folderName ->
                                                mainPageViewModel.klasoreGecisYap(folderName)
                                            },
                                            onFileClicked = {
                                                println("Hata: Klasör için onFileClicked çağrıldı: $it")
                                            },
                                            onDeleteClicked = {silinecekKlasorAdi ->
                                                mainPageViewModel.updateKlasorAdi(silinecekKlasorAdi)
                                                showDeleteDialogKlasor = true
                                            },
                                            onRenameClicked = { eskiAdi ->
                                                mainPageViewModel.updateKlasorAdi(eskiAdi)
                                                degistirilecekIsim = eskiAdi
                                                showRenameDialogKlasor = true
                                                //mainPageViewModel.klasorAdiGuncelle(eskiAdi, yeniAdi)
                                            },
                                            onMoveClicked = { mevcutKlasorAdi ->
                                                mainPageViewModel.updateKlasorAdi(mevcutKlasorAdi)
                                                degistirilecekYol = anlikDosyaYolu
                                                showMoveDialogKlasor = true
                                            },
                                            onDownloadClicked= { indirilecekKlasorAdi ->
                                                //Burada indirme yapilacak
                                            }
                                        )
                                    }
                                    is ListItem.DosyaItem -> {
                                        FileCardItemList(
                                            listItem.dosya.Adi,
                                            listItem.dosya.Boyut,
                                            navController,
                                            onFolderClicked = {
                                                println("Hata: Dosya için onFolderClicked çağrıldı: $it")
                                            },
                                            onFileClicked = { fileName ->
                                                println("Dosyaya tıklandı: $fileName. Ayrı sayfa açılacak.")
                                                // Detail sayfasi gelirse eklenecek
                                            },
                                            onDeleteClicked = { silinecekDosyaAdi ->
                                                mainPageViewModel.updateDosyaAdi(silinecekDosyaAdi)
                                                showDeleteDialogDosya = true
                                            } ,
                                            onRenameClicked = { eskiAdi ->
                                                mainPageViewModel.updateDosyaAdi(eskiAdi)
                                                degistirilecekIsim = eskiAdi
                                                showRenameDialogDosya = true
                                                //mainPageViewModel.dosyaAdiGuncelle(eskiAdi, yeniAdi)
                                            },
                                            onMoveClicked = { mevcutDosyaAdi ->
                                                mainPageViewModel.updateKlasorAdi(mevcutDosyaAdi)
                                                degistirilecekYol = anlikDosyaYolu
                                                showMoveDialogDosya = true
                                            },
                                            onDownloadClicked = { indirilecekKlasorAdi ->
                                                if (!isDownloading) {
                                                    // 1. İndirilecek dosyanın tam bilgilerini al
                                                    val dosyaDetaylari = listItem.dosya
                                                    val serverFolderPath = mainPageViewModel.klasorYolu
                                                    val fileNameToSave = dosyaDetaylari.Adi // Zaten dosyaAdiParametreOlarakGelen ile aynı olmalı

                                                    Log.d("MainPage", "İndirme isteği: $fileNameToSave, API Yolu: , Sunucu Klasörü: $serverFolderPath")

                                                    mainPageViewModel.updateDosyaAdi(fileNameToSave)


                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        mainPageViewModel.indirVeKaydetDosyayi(
                                                            context = context,
                                                            apiIndirilecekYol = kaydedilecekYer,
                                                            apiKlasorYolu = serverFolderPath,
                                                            dosyaAdi = fileNameToSave
                                                        )
                                                    }
                                                } else {
                                                    // Eğer zaten bir indirme devam ediyorsa, kullanıcıya bilgi ver
                                                    scope.launch { // Coroutine scope (Composable'ın başında tanımlanmış olmalı)
                                                        snackbarHostState.showSnackbar(
                                                            message = "Devam eden bir indirme işlemi var. Lütfen bekleyin.",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                    Log.d("MainPage", "Yeni indirme isteği reddedildi, mevcut indirme devam ediyor.")
                                                }
                                            },

                                        )
                                    }
                                }
                            }
                        }
                    }

                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LazyBackground),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(
                                items = currentItems,
                                key = { listItem -> listItem.id }
                            ) { listItem ->
                                when (listItem){
                                    is ListItem.KlasorItem -> {
                                        FileCardItemGrid(listItem.klasor.Adi,
                                            "",
                                            navController,
                                            onFolderClicked = { folderName ->
                                                mainPageViewModel.klasoreGecisYap(folderName)
                                            },
                                            onFileClicked = { /* Bu durum klasörler için olmamalı, ama API tutarlı değilse diye? */
                                                println("Hata: Klasör için onFileClicked çağrıldı: $it")
                                            },
                                            onDeleteClicked = {silinecekKlasorAdi ->
                                                mainPageViewModel.updateKlasorAdi(silinecekKlasorAdi)
                                                showDeleteDialogKlasor = true
                                            },
                                            onRenameClicked = { eskiAdi ->
                                                mainPageViewModel.updateKlasorAdi(eskiAdi)
                                                degistirilecekIsim = eskiAdi
                                                showRenameDialogKlasor = true
                                                //mainPageViewModel.klasorAdiGuncelle(eskiAdi, yeniAdi)
                                            },
                                            onMoveClicked = { mevcutKlasorAdi ->
                                                mainPageViewModel.updateKlasorAdi(mevcutKlasorAdi)
                                                degistirilecekYol = anlikDosyaYolu
                                                showMoveDialogKlasor = true
                                            },
                                            onDownloadClicked= { indirilecekKlasorAdi ->
                                                //Burada indirme yapilacak
                                            }
                                        )
                                    }
                                    is ListItem.DosyaItem -> {
                                        FileCardItemGrid(
                                            listItem.dosya.Adi,
                                            listItem.dosya.Boyut,
                                            navController,
                                            onFolderClicked = { /* Bu durum dosyalar için olmamalı */
                                                println("Hata: Dosya için onFolderClicked çağrıldı: $it")
                                            },
                                            onFileClicked = { fileName ->
                                                println("Dosyaya tıklandı: $fileName. Ayrı sayfa açılacak.")
                                                // Gelecekte: navController.navigate("fileDetail/$fileName")
                                            },
                                            onDeleteClicked = { silinecekDosyaAdi ->
                                                mainPageViewModel.updateDosyaAdi(silinecekDosyaAdi)
                                                showDeleteDialogDosya = true
                                            },
                                            onRenameClicked = { eskiAdi ->
                                                mainPageViewModel.updateDosyaAdi(eskiAdi)
                                                degistirilecekIsim = eskiAdi
                                                showRenameDialogDosya = true
                                                //mainPageViewModel.dosyaAdiGuncelle(eskiAdi, yeniAdi)
                                            },
                                            onMoveClicked = { mevcutDosyaAdi ->
                                                mainPageViewModel.updateKlasorAdi(mevcutDosyaAdi)
                                                degistirilecekYol = anlikDosyaYolu
                                                showMoveDialogDosya = true
                                            },
                                            onDownloadClicked = { indirilecekKlasorAdi ->
                                                if (!isDownloading) { // Eğer zaten bir indirme devam etmiyorsa
                                                    // 1. İndirilecek dosyanın tam bilgilerini al
                                                    val dosyaDetaylari = listItem.dosya
                                                    val serverFolderPath = mainPageViewModel.klasorYolu
                                                    val fileNameToSave = dosyaDetaylari.Adi // Zaten dosyaAdiParametreOlarakGelen ile aynı olmalı

                                                    Log.d("MainPage", "İndirme isteği: $fileNameToSave, API Yolu: , Sunucu Klasörü: $serverFolderPath")

                                                    // Opsiyonel: Snackbar'da göstermek için indirilen dosyanın adını ViewModel'e set et
                                                    mainPageViewModel.updateDosyaAdi(fileNameToSave)

                                                    // ViewModel üzerinden indirme işlemini başlat
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        mainPageViewModel.indirVeKaydetDosyayi(
                                                            context = context, // Composable'ın başından alınan context
                                                            apiIndirilecekYol = kaydedilecekYer,
                                                            apiKlasorYolu = serverFolderPath,
                                                            dosyaAdi = fileNameToSave
                                                        )
                                                    }
                                                } else {
                                                    // Eğer zaten bir indirme devam ediyorsa, kullanıcıya bilgi ver
                                                    scope.launch { // Coroutine scope (Composable'ın başında tanımlanmış olmalı)
                                                        snackbarHostState.showSnackbar(
                                                            message = "Devam eden bir indirme işlemi var. Lütfen bekleyin.",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                    Log.d("MainPage", "Yeni indirme isteği reddedildi, mevcut indirme devam ediyor.")
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(showDialogKlasor){
            AlertDialog(
                onDismissRequest = {
                    showDialogKlasor = false
                },
                title = { Text("Klasör Oluştur", fontWeight = FontWeight.Bold, color = MaviLogo) },
                text = {
                    OutlinedTextField(
                        value = dialogText,
                        onValueChange = {  dialogText = it
                        },
                        label = { Text("Klasör Adı") },
                        singleLine = true,
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )
                        // modifier = Modifier.fillMaxWidth() // Genellikle iyi bir fikirdir
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogText.isNotBlank()) {
                                mainPageViewModel.updateKlasorAdi(dialogText)
                                mainPageViewModel.klasorOlustur()
                                showDialogKlasor = false
                                dialogText = ""
                            } else {
                                Log.d("Dialog", "Klasör adı boş.")
                            }
                        },
                        // Butonun enabled durumunu da textFieldValue'ya bağlayabilirsiniz:
                        enabled = dialogText.isNotBlank()

                    ) {
                        Text("Oluştur")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialogKlasor = false
                        // Opsiyonel: İptalde de TextField'ı temizle
                        // textFieldValue = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
        if(showDialogDosya){
            AlertDialog(
                onDismissRequest = {
                    showDialogDosya = false
                },
                title = { Text("Dosya Oluştur", fontWeight = FontWeight.Bold, color = MaviLogo) },
                text = {
                    OutlinedTextField(
                        value = dialogText,
                        onValueChange = {  dialogText = it
                        },
                        label = { Text("Dosya Adı") },
                        singleLine = true,
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogText.isNotBlank()) { // 3. Kontrolü yerel state üzerinden yap
                                mainPageViewModel.updateDosyaAdi(dialogText) // 4. ViewModel'i oluşturmadan hemen önce güncelle
                                mainPageViewModel.dosyaOlustur()
                                showDialogDosya = false
                                dialogText = "" // Başarılı oluşturma sonrası temizle
                            } else {
                                // Kullanıcıya uyarı göster (Snackbar vb.)
                                // VEYA şimdilik sadece diyaloğu açık bırak
                                Log.d("Dialog", "Dosya adı boş.")
                                // showDialog = false // Boşsa kapatma, kullanıcı düzeltsin
                            }
                        },
                        // Butonun enabled durumunu da textFieldValue'ya bağlayabilirsiniz:
                        enabled = dialogText.isNotBlank()
                    ) {
                        Text("Oluştur")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialogDosya = false
                        // Opsiyonel: İptalde de TextField'ı temizle
                        // textFieldValue = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
        if (showRenameDialogKlasor && degistirilecekIsim.isNotBlank()) {
            AlertDialog(
                onDismissRequest = {
                    showRenameDialogKlasor = false
                    degistirilecekIsim = "" // Diyalog kapatıldığında hedef dosyayı temizle
                },
                title = { Text("Klasoru Yeniden Adlandır") },
                text = {
                    OutlinedTextField(
                        value = degistirilecekIsim,
                        onValueChange = { degistirilecekIsim = it },
                        label = { Text("Yeni Klasor Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (degistirilecekIsim.isNotBlank()) {
                                // 2. ViewModel'e hem eski adı hem yeni adı ilet
                                mainPageViewModel.klasorGuncelle(klasorAdi!!, degistirilecekIsim)
                                showRenameDialogKlasor = false
                                mainPageViewModel.updateKlasorAdi("")
                                degistirilecekIsim = ""
                            } else {
                                // Kullanıcıya uyarı göster (opsiyonel)
                            }
                        },
                        enabled = degistirilecekIsim.isNotBlank() // Boş değilse aktif
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRenameDialogKlasor = false
                        mainPageViewModel.updateKlasorAdi("")
                        degistirilecekIsim = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
        if (showRenameDialogDosya && degistirilecekIsim.isNotBlank()) {
            AlertDialog(
                onDismissRequest = {
                    showRenameDialogDosya = false
                    degistirilecekIsim = "" // Diyalog kapatıldığında hedef dosyayı temizle
                },
                title = { Text("Dosyayı Yeniden Adlandır") },
                text = {
                    OutlinedTextField(
                        value = degistirilecekIsim,
                        onValueChange = { degistirilecekIsim = it },
                        label = { Text("Yeni Dosya Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )

                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (degistirilecekIsim.isNotBlank()) {
                                // 2. ViewModel'e hem eski adı hem yeni adı ilet
                                mainPageViewModel.dosyaGuncelle(dosyaAdi!!,degistirilecekIsim)
                                showRenameDialogDosya = false
                                mainPageViewModel.updateDosyaAdi("")
                                degistirilecekIsim = ""
                            } else {
                                // Kullanıcıya uyarı göster (opsiyonel)
                            }
                        },
                        enabled = degistirilecekIsim.isNotBlank() // Boş değilse aktif
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRenameDialogDosya = false
                        mainPageViewModel.updateKlasorAdi("")
                        degistirilecekIsim = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
        if (showMoveDialogKlasor) {
            AlertDialog(
                onDismissRequest = {
                    showMoveDialogKlasor = false
                    degistirilecekYol = "" // Diyalog kapatıldığında hedef dosyayı temizle
                },
                title = { Text("Klasörü Taşı") },
                text = {
                    OutlinedTextField(
                        value = degistirilecekYol,
                        onValueChange = { degistirilecekYol = it },
                        label = { Text("Yeni Klasor Yolu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (degistirilecekYol.isNotBlank()) {
                                // 2. ViewModel'e hem eski adı hem yeni adı ilet
                                mainPageViewModel.klasorTasi(klasorAdi!!,degistirilecekYol)
                                showMoveDialogKlasor = false
                                degistirilecekYol = ""
                            } else {
                                // Kullanıcıya uyarı göster (opsiyonel)
                            }
                        },
                        enabled = degistirilecekYol.isNotBlank() // Boş değilse aktif
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showMoveDialogKlasor = false
                        degistirilecekYol = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
        if (showMoveDialogDosya) {
            AlertDialog(
                onDismissRequest = {
                    showMoveDialogDosya = false
                    degistirilecekYol = "" // Diyalog kapatıldığında hedef dosyayı temizle
                },
                title = { Text("Dosyayı Taşı") },
                text = {
                    OutlinedTextField(
                        value = degistirilecekYol,
                        onValueChange = { degistirilecekYol = it },
                        label = { Text("Yeni Dosya Yolu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            focusedBorderColor = MaviLogo,
                            unfocusedBorderColor = LoginText,
                            focusedLabelColor = LoginText,
                            unfocusedLabelColor = MaviLogo,
                            cursorColor = LoginText,
                            unfocusedPlaceholderColor = LoginText,
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (degistirilecekYol.isNotBlank()) {
                                // 2. ViewModel'e hem eski adı hem yeni adı ilet
                                mainPageViewModel.dosyaTasi(klasorAdi!!,degistirilecekYol)
                                showMoveDialogDosya = false
                                degistirilecekYol = ""
                            } else {
                                // Kullanıcıya uyarı göster (opsiyonel)
                            }
                        },
                        enabled = degistirilecekYol.isNotBlank() // Boş değilse aktif
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showMoveDialogDosya = false
                        degistirilecekYol = ""
                    }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
    if (showDeleteDialogKlasor) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialogKlasor = false
                mainPageViewModel.updateKlasorAdi("")
            },
            title = { Text("Klasörü Sil") },
            text = {
                Text("Bu klasörü silmek istediğinize emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (klasorAdi.isNotBlank()) {
                            // 2. ViewModel'e hem eski adı hem yeni adı ilet
                            mainPageViewModel.klasorSil(klasorAdi)
                            showDeleteDialogKlasor = false
                            mainPageViewModel.updateKlasorAdi("")
                        } else {
                            // Kullanıcıya uyarı göster (opsiyonel)
                        }
                    },
                    enabled = klasorAdi.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, // Use containerColor for background
                        contentColor = Color.White // Optionally, set content color for better contrast
                    )// Boş değilse aktif
                ) {
                    Text(text = "Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialogKlasor = false
                    mainPageViewModel.updateKlasorAdi("")
                }) {
                    Text("İptal")
                }
            }
        )
    }
    if (showDeleteDialogDosya) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialogDosya = false
                mainPageViewModel.updateDosyaAdi("")
            },
            title = { Text("Dosyayı Sil") },
            text = {
                Text("Bu dosyayı silmek istediğinize emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dosyaAdi.isNotBlank()) {
                            mainPageViewModel.dosyaSil(dosyaAdi)
                            showDeleteDialogDosya = false
                            mainPageViewModel.updateDosyaAdi("")
                        } else {
                            // Kullanıcıya uyarı göster (opsiyonel)
                        }
                    },
                    enabled = dosyaAdi.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, // Use containerColor for background
                        contentColor = Color.White // Optionally, set content color for better contrast
                    )// Boş değilse aktif
                ) {
                    Text(text = "Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialogDosya = false
                    mainPageViewModel.updateDosyaAdi("")
                }) {
                    Text("İptal")
                }
            }
        )
    }

    if(showCikisYapDialog) {
        AlertDialog(
            onDismissRequest = {
                showCikisYapDialog = false
            },
            title = { Text("Çıkış Yap") },
            text = {
                Text("Çıkış yapmak istediğinize emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mainPageViewModel.cikisYap()
                        navController.navigate("login") {
                            popUpTo("mainPage") {
                                inclusive = true
                            }
                        }
                        showCikisYapDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Evet")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCikisYapDialog = false
                }) {
                    Text("Iptal")
                }
            }
        )
    }



    if (sheetStateSorting.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetStateSorting.hide()}
            },
            sheetState = sheetStateSorting
        ) {
            Column (
                modifier = Modifier
                    .padding(vertical = 16.dp)
            ){
                BottomSheetOptionItem(
                    icon = Icons.Filled.Edit,
                    text = "Ada Göre (A-Z)",
                    onClick = {
                        println(" Ada Göre (A-Z)")
                        scope.launch { sheetStateSorting.hide()
                        }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.Delete,
                    text = "Ada Göre (Z-A)",
                    onClick = {
                        println(" silinecek.")
                        scope.launch { sheetStateSorting.hide() }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.Share,
                    text = "Tarihe Göre (Yeni)",
                    onClick = {
                        println(" paylaşılacak.")
                        scope.launch { sheetStateSorting.hide() }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.Share,
                    text = "Tarihe Göre (Eski)",
                    onClick = {
                        println(" paylaşılacak.")
                        scope.launch { sheetStateSorting.hide() }
                    }
                )
            }

        }
    }


}









fun getIconForFileExtension(fileName: String?): Int {
    val extension = fileName?.substringAfterLast('.', "")?.lowercase() ?: ""


    return when (extension) {
        "pdf" -> R.drawable.pdf_icon
        "xls", "xlsx" -> R.drawable.xls_icon
        "doc", "docx" -> R.drawable.docx_icon
        "txt" -> R.drawable.txt_icon
        "jpg", "jpeg", "png", "gif" -> R.drawable.image_icon
        "mp4", "avi", "mov" -> R.drawable.video_icon
        "mp3", "wav" -> R.drawable.audio_icon
        "" -> R.drawable.folder_icon

        else -> R.drawable.default_file_icon

    }
}