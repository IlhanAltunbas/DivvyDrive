package com.ilhanaltunbas.divvydrive.uix.state

import com.ilhanaltunbas.divvydrive.data.entity.TicketCevap

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val ticketCevap: TicketCevap) : LoginState()
    data class Error(val message: String) : LoginState()
}