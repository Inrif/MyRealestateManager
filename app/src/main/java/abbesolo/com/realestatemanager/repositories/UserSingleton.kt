package abbesolo.com.realestatemanager.repositories

import abbesolo.com.realestatemanager.models.RMUser

/**
 * Created by ROmuald Hounsa on 15/08/21.
 */
object UserSingleton {

    init {
    }


    var mUser: RMUser? = null


    fun getUser(): RMUser? {
        return mUser
    }
}
