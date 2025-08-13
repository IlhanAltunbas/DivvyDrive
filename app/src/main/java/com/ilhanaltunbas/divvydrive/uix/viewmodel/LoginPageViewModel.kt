package com.ilhanaltunbas.divvydrive.uix.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilhanaltunbas.divvydrive.data.auth.TokenManager
import com.ilhanaltunbas.divvydrive.data.entity.TicketCevap
import com.ilhanaltunbas.divvydrive.data.repo.FileRepository
import com.ilhanaltunbas.divvydrive.uix.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginPageViewModel @Inject constructor(
    var frepo: FileRepository,
    var tokenManager: TokenManager)
    : ViewModel() {
    private val _loginstate = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginstate : StateFlow<LoginState> = _loginstate.asStateFlow()


    init {
        checkExistingToken()
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val existingToken = tokenManager.getToken()
            if (existingToken != null && existingToken.isNotBlank()) {
                Log.d("LoginPageViewModel", "Mevcut Ticket Bulundu: $existingToken")

                _loginstate.value = LoginState.Success(TicketCevap(kullaniciAdi = existingToken, ticket = "Mevcut token ile devam", sonuc = true )) // Sahte bir cevapla

            } else {
                Log.d("LoginPageViewModel", "Mevcut Ticket Bulunamadı. Kullanıcı giriş yapmalı.")
            }
        }
    }
    fun girisYap(username: String, password: String) {
        viewModelScope.launch{
            _loginstate.value = LoginState.Loading
            try {
                val cevap: TicketCevap = frepo.girisYap(
                    username, password)

                if (cevap.sonuc) {
                    tokenManager.saveToken(cevap.ticket)
                    Log.d("Token","Token kaydedildi: ${cevap.ticket}")
                    _loginstate.value = LoginState.Success(cevap)
                    Log.d("LoginPageViewModel", "Giriş başarılı. Ticket: ${cevap.ticket}")
                } else {
                    _loginstate.value = LoginState.Error("Kullanıcı adı veya şifre hatalı.")
                }
            } catch (e: IOException) {
                _loginstate.value = LoginState.Error("Ağ bağlantısı hatası.")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    // Şimdilik sadece kodu ve genel bir mesajı gösterelim
                    "Sunucu hatası: Kod ${e.code()}. Detay: $errorBody"
                } else {
                    "Sunucu ile iletişimde bir sorun oluştu: Kod ${e.code()}"
                }
                _loginstate.value = LoginState.Error(errorMessage)
                } catch (e: Exception) {
                _loginstate.value = LoginState.Error("Bir hata oluştu: ${e.message}")

            }

        }
    }
    fun consumeLoginState() {
        _loginstate.value = LoginState.Idle
    }
}

