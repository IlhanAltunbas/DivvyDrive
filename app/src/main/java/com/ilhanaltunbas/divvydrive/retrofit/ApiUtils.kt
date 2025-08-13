package com.ilhanaltunbas.divvydrive.retrofit

class ApiUtils {
    companion object {
        val filesDao: FilesDao by lazy { // ilk erişimde oluşturulur lazy sayesınde
            RetrofitClient.getClient().create(FilesDao::class.java)
        }
    }
}