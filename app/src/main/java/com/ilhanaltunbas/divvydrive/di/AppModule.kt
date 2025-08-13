package com.ilhanaltunbas.divvydrive.di


import android.content.Context
import com.ilhanaltunbas.divvydrive.data.auth.TokenManager
import com.ilhanaltunbas.divvydrive.data.datasource.RemoteDataSource
import com.ilhanaltunbas.divvydrive.data.network.AuthInterceptor
import com.ilhanaltunbas.divvydrive.data.repo.FileRepository
import com.ilhanaltunbas.divvydrive.retrofit.ApiUtils
import com.ilhanaltunbas.divvydrive.retrofit.FilesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideRemoteDataSource(fdao: FilesDao): RemoteDataSource {
        return RemoteDataSource(fdao)
    }
    @Provides
    @Singleton
    fun provideFileRepository(rds: RemoteDataSource, tokenManager: TokenManager): FileRepository {
        return FileRepository(rds, tokenManager)
    }
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
    @Provides
    @Singleton
    fun provideFilesDao(): FilesDao {
        return ApiUtils.filesDao
    }




}