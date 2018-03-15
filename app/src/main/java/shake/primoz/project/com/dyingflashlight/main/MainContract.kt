package primoz.project.com.musicopter.main

import android.content.Context
import android.hardware.camera2.CameraManager
import primoz.project.com.musicopter.BasePresenter
import primoz.project.com.musicopter.BaseView

/**
 * Created by Primož on 09/02/2018.
 */
interface MainContract {

    interface View : BaseView<MainContract.Presenter> {
        fun initViews()
        fun showON()
        fun showOFF()
        fun showDying()
        fun showRateThisApp()
    }

    interface Presenter : BasePresenter {
        fun loadDying()
        fun turnON()
        fun turnOFF()
        fun checkIfHasFlashLight():Boolean
        fun switchFlash()
        fun switchFlashWithoutChangingButton()
        fun loadFirstTimeOn()
        fun phoneIsShaked(count: Int)
        fun isflashON(): Boolean
    }
}