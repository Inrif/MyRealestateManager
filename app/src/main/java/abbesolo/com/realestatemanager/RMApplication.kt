package abbesolo.com.realestatemanager

import androidx.multidex.MultiDexApplication
import abbesolo.com.realestatemanager.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Created by HOUNSA Romuald on 21/07/21.
 */
class RMApplication:MultiDexApplication (){
    // METHODS -------------------------------------------------------------------------------------

    // -- MultiDexApplication --

    override fun onCreate() {
        super.onCreate()

        // Timber: Logger
        Timber.plant(Timber.DebugTree())

        // Koin: Dependency injection framework
        startKoin {
            androidLogger()
            androidContext(this@RMApplication)
            modules(appModule)
        }
    }
}