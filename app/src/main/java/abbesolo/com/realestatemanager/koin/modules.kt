package com.mancel.yann.realestatemanager.koin

import abbesolo.com.realestatemanager.database.Database
import abbesolo.com.realestatemanager.repositories.*
import abbesolo.com.realestatemanager.viewModel.RMViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


//https://blog.ippon.fr/2019/09/03/android-di-de-dagger-vers-koin/

val appModule = module {

    // Database
    single { Database.getDatabase(get()) }

    // DAO
    single { get<Database>().userDAO() }
    single { get<Database>().rmDAO() }
    single { get<Database>().photoDAO() }
    single { get<Database>().poiDAO() }
    single { get<Database>().rmAndPoiRefCrossDAO() }

    // Repository
    single<PlaceRepository> { MapApiRepository() }
    single<RMUserRepository> { RMUserRepositoryImpl(get()) }
    single<RMRepository> { RMRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }
    single<PoiRepository> { PoiRepositoryImpl(get()) }
    single<RMAndPoiRefCrossRespository> { RMAndPoiRefCrossRespositoryImpl(get()) }

    // ViewModel
    viewModel { RMViewModel(get(), get(), get(), get(), get(), get()) }
}