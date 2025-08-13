package com.ilhanaltunbas.divvydrive.uix.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ilhanaltunbas.divvydrive.R
import com.ilhanaltunbas.divvydrive.ui.theme.LoginText
import com.ilhanaltunbas.divvydrive.uix.view.getIconForFileExtension
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileCardItemList(fileName: String,
                     fileSize: String,
                     navController: NavController,
                     onFolderClicked: (folderName: String) -> Unit,
                     onFileClicked: (fileName: String) -> Unit,
                     onDeleteClicked: (silinecekDosyaAdi: String) -> Unit,
                     onRenameClicked: (eskiDosyaAdi: String,) -> Unit,
                     onMoveClicked: (dosyaAdi: String,) -> Unit,
                     onDownloadClicked: (indirilecekDosyaAdi: String) -> Unit) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Genellikle ya tam açık ya kapalı istenir
    )
    val scope = rememberCoroutineScope()




    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable {
                // fileName null veya boş olmamalı, kontrol eklenebilir
                val ext = fileName.substringAfterLast('.', "").lowercase()
                if (ext.isEmpty()) { // Uzantı yoksa klasör varsayalım
                    onFolderClicked(fileName)
                } else { // Uzantı varsa dosya
                    onFileClicked(fileName)
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.4.dp, Color.LightGray.copy(alpha = 0.6f))

    ) {
        Row(
            modifier = Modifier
                .padding(start = 22.dp, top = 14.dp, bottom = 14.dp, end = 22.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(
                    getIconForFileExtension(fileName)
                ),
                contentDescription = "Dosya İkonu",
                modifier = Modifier
                    .background(Color.Gray.copy(0.2f), shape = RoundedCornerShape(12.dp))
                    .padding(all = 12.dp)
                    .size(28.dp),
                tint = Color.Unspecified,
            )

            Column (verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .padding(start = 18.dp)) {
                Text(
                    text = fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(text = FormatUtils.formatSizeToMB(fileSize.toLongOrNull()),
                    color = LoginText,
                    fontSize = 12.sp)

            }

            Spacer(Modifier.weight(1f))

            Box{
                Icon(painterResource(R.drawable.options_icon),
                    contentDescription = "Seçenekler",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            scope.launch {
                                sheetState.show()
                            }
                        }
                )

            }

        }


    }
    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide()}
            },
            sheetState = sheetState
        ) {
            Column (
                modifier = Modifier
                    .padding(vertical = 16.dp)
            ){
                BottomSheetOptionItem(
                    icon = Icons.Filled.Edit,
                    text = "Yeniden Adlandır",
                    onClick = {
                        onRenameClicked(fileName)
                        scope.launch { sheetState.hide()
                        }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.Delete,
                    text = "Sil",
                    onClick = {
                        onDeleteClicked(fileName)
                        //mesaj ekleyebiliriz daha sonrasindsa
                        scope.launch { sheetState.hide() }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.MoveDown,
                    text = "Taşı",
                    onClick = {
                        onMoveClicked(fileName)
                        scope.launch { sheetState.hide() }
                    }
                )
                BottomSheetOptionItem(
                    icon = Icons.Filled.Download,
                    text = "İndir",
                    onClick = {
                        onDownloadClicked(fileName)
                        scope.launch { sheetState.hide() }
                    }
                )
            }

        }
    }

}