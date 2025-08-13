# DivvyDrive
A mobile application developed in **Kotlin** using **MVVM architecture**, **Jetpack Compose**, and **Material 3**, designed to interact with remote API servers under authorization and ticket-based communication.  
The system connects to **16 different API endpoints** to provide full file and folder management on a cloud server.

---

## 📱 Application Overview
- **Number of Pages**: 2 (Login Screen & Home Screen)
- **Main Purpose**: Communicate with remote APIs under authorization using a ticket system.
- **Architecture**: MVVM with Model, Repository, DataSource, and DAO (Database Access Object) structure.
- **Network Layer**: Retrofit + OkHttp, managed by a common `AuthInterceptor`.
- **UI Layer**: Jetpack Compose + Material 3
- **State Management**: StateFlow & LiveData
- **Asynchronous Operations**: Kotlin Coroutines
- **Dependency Injection**: Hilt

---

## 🔌 API Endpoints
The application integrates with the following API endpoints:

- **Ticket Management**: `TicketAl`
- **Folder Operations**:
  - `KlasorListesiGetir`
  - `KlasorOlustur`
  - `KlasorSil`
  - `KlasorGuncelle`
  - `KlasorTasi`
- **File Operations**:
  - `DosyaListesiGetir`
  - `DosyaOlustur`
  - `DosyaSil`
  - `DosyaGuncelle`
  - `DosyaTasi`
  - `DosyaMetaDataKaydiOlustur`
  - `DosyaParcalariYukle`
  - `DosyaYayinla`
  - `DosyaDirektYukle`
  - `DosyaIndir`

---

## 📂 File Upload Process
- **Files < 1 MB** → Uploaded directly using `DosyaDirektYukle`.
- **Files > 1 MB** → 
  1. Metadata record created with `DosyaMetaDataKaydiOlustur` (returns `tempKlasorId`).
  2. File split into parts and sent to `DosyaParcalariYukle` along with hash values.
  3. Once all parts are uploaded, `DosyaYayinla` is called.

---

## 🖥 UI Features
- **View Modes**: Grid view & List view.
- **Folder/File Actions**: Managed via a 3-dot menu in LazyLayout items.
- **Folder Path & Name**: Controlled via modular and manageable flows for LazyLayout rendering.
- **API Messages**: Displayed to the user via Snackbars.
- **Create Folder/File**: Input taken from an AlertDialog triggered by a FAB button.
- **File Download**: ProgressBar UI shown during download.

---

## 🛠 Technical Details
- **Navigation**: Between Login & Home screens, with logout option.
- **UI State Management**: ViewModels manage UI state and handle user interactions using StateFlow and LiveData.
- **LazyLayout**: Efficient rendering of file/folder lists.
- **Snackbar Messages**: For API response feedback.
- **Dialogs**: For folder/file creation.
- **Hilt**: Used for dependency injection.

---

## 🎯 Purpose
This project was developed for **educational purposes** to demonstrate:
- MVVM architectural pattern.
- Jetpack Compose UI design.
- StateFlow & LiveData integration.
- API communication via Retrofit & OkHttp.
- File management on a remote server.

---
