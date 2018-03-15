package shake.primoz.project.com.dyingflashlight.main

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import primoz.project.com.musicopter.main.MainContract
import primoz.project.com.musicopter.main.MainPresenter
import shake.primoz.project.com.dyingflashlight.R
import shake.primoz.project.com.dyingflashlight.utils.ShakeDetector
import shake.primoz.project.com.dyingflashlight.utils.ShakeDetector.OnShakeListener
import android.support.v4.view.ViewCompat.animate
import android.view.animation.DecelerateInterpolator
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.view.ViewGroup
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import hotchemi.android.rate.AppRate
import hotchemi.android.rate.OnClickButtonListener
import  shake.primoz.project.com.dyingflashlight.BuildConfig
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.ad_app_install.*
import java.util.*


const val AD_UNIT_ID = "API_KEY"

class MainActivity : AppCompatActivity(), MainContract.View {

    private lateinit var mPresenter: MainContract.Presenter
    // The following are used for the shake detection
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        // val kit = Crashlytics.Builder().core(core).build()
        Fabric.with(this, Crashlytics())

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, AD_UNIT_ID)
        loadAd()

        //Rate this app
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(2) // default 10
                .setRemindInterval(1) // default 1
                .setShowLaterButton(true) // default true
                .setMessage("If you enjoy this app, can you please rate it on the Google Play Store. It would mean a lot to me.")
                .monitor()

        showRateThisApp()

        mPresenter = MainPresenter(this, this)
        mPresenter.start()
    }

    override fun showRateThisApp() {
        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this)
       // AppRate.with(this).showRateDialog(this)
    }


    override fun onResume() {
        super.onResume()
        val handler = Handler()
        handler.postDelayed({
            //Little delay because of cold boot
            if (!mPresenter.isflashON() && mPresenter.checkIfHasFlashLight())
                mPresenter.switchFlash()
        }, 2000)

        if (mPresenter.checkIfHasFlashLight()) mSensorManager?.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)

    }

    override fun onPause() {
        super.onPause()
        if (mPresenter.checkIfHasFlashLight()) mSensorManager?.unregisterListener(mShakeDetector)
    }

    override fun initViews() {
        if (mPresenter.checkIfHasFlashLight()) {
            shakeInit()
            initToggleOnOffListener()
        } else {
            showNoFlashlightFound()
        }
    }

    private fun showNoFlashlightFound() {
        textViewShake.text = getString(R.string.no_flashlight_found)
    }

    private fun shakeInit() {
        // ShakeDetector initialization
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : OnShakeListener {

            override fun onShake(count: Int) {
                mPresenter.phoneIsShaked(count)
            }
        })
    }

    private fun initToggleOnOffListener() {
        tvPowerSwitch.setOnClickListener({
            mPresenter.switchFlash()
        })
    }

    private fun loadAd() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    override fun showON() {
        tvPowerSwitch.setTextColor(ContextCompat.getColor(this, R.color.orange))
    }

    override fun showOFF() {
        tvPowerSwitch.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun showDying() {
        mPresenter.loadDying()
    }

    override fun setPresenter(presenter: MainContract.Presenter) {
        mPresenter = presenter
    }

}
