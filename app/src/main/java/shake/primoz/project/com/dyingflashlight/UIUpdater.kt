package shake.primoz.project.com.dyingflashlight

import android.os.Handler
import android.os.Looper
import android.util.Log
import primoz.project.com.musicopter.main.MainPresenter


/**
 * Created by primo on 9. 03. 2018.
 */

class UIUpdater// Run the passed runnable
// Re-run it after the update interval
/**
 * Creates an UIUpdater object, that can be used to
 * perform UIUpdates on a specified time interval.
 *
 * @param uiUpdater A runnable containing the update routine.
 */(uiUpdater: Runnable) {
    // Create a Handler that uses the Main Looper to run in
    private val mHandler = Handler(Looper.getMainLooper())

    private var mStatusChecker: Runnable? = null
    private var UPDATE_INTERVAL = 5000L

    /**
     * Starts the periodical update routine (mStatusChecker
     * adds the callback to the handler).
     */
    @Synchronized
    fun start() {
        mStatusChecker!!.run()
    }

    /**
     * Stops the periodical update routine from running,
     * by removing the callback.
     */
    @Synchronized
    fun stop() {
        mHandler.removeCallbacks(mStatusChecker)

    }

    init {
        mStatusChecker = object : Runnable {
            override fun run() {
                if (MainPresenter.stutterTimesBeforeDeath > 0) {
                    // Run the passed runnable
                    uiUpdater.run()
                    // Re-run it after the update interval
                    mHandler.postDelayed(this, UPDATE_INTERVAL)
                    Log.d("Handler", MainPresenter.stutterTimesBeforeDeath.toString())
                }
            }
        }
    }
}