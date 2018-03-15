package primoz.project.com.musicopter.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.widget.Toast
import android.hardware.camera2.CameraManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import shake.primoz.project.com.dyingflashlight.AndroidVersion
import shake.primoz.project.com.dyingflashlight.UIUpdater
import shake.primoz.project.com.dyingflashlight.main.MainActivity
import android.hardware.Camera.CameraInfo


/**
 * Created by Primož on 09/02/2018.
 */
class MainPresenter(view: MainContract.View, private val context: Context) : MainContract.Presenter {
    val TAG = "MainPresenter"
    var mView: MainContract.View = view
    private var flashIsShowing = false
    private var firstTime = true
    private var disabledFlash = false
    private var disabledFlashPressed = 3
    var exitRequested = false

    private var camera: Camera? = null //For Pre marshmallow

    companion object {
        var stutterTimesBeforeDeath = 5
    }

    var stutterHandler = UIUpdater(Runnable {
        run {
            stutter()
        }
    })

    override fun isflashON(): Boolean {
        return flashIsShowing
    }

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        mView.initViews()
    }


    private fun turnOnFlash(camManager: CameraManager) {
        val cameraId = camManager.cameraIdList[0] // Usually front camera is at 0 position.
        if (AndroidVersion.isMarshmallow()) {
            camManager.setTorchMode(cameraId, true)
        } else {
            camera = Camera.open(cameraId.toInt())
            val params = camera?.parameters
            params?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera?.parameters = params
            camera?.startPreview()
        }
        flashIsShowing = true

        Log.d(TAG, "Flash is ON")
    }


    private fun turnOffFlash(camManager: CameraManager) {
        val cameraId = camManager.cameraIdList[0] // Usually front camera is at 0 position.
        if (AndroidVersion.isMarshmallow()) {
            camManager.setTorchMode(cameraId, false)
        } else {
            camera?.stopPreview()
            camera?.release()

            camera = null
        }
        flashIsShowing = false

        Log.d(TAG, "Flash is OFF")
    }


    override fun switchFlash() {
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = camManager.cameraIdList[0] // Usually front camera is at 0 position.

        if (firstTime) {
            loadFirstTimeOn()
            mView.showON()
            firstTime = false
        } else {
            if (!flashIsShowing) {
                Log.d(TAG, "User pressed ON")
                if (disabledFlash) {
                    turnOnALittleTillDeath()
                } else {
                    turnON()
                }
            } else {
                Log.d(TAG, "User pressed OFF")
                turnOFF()
            }
        }
    }


    override fun turnON() {
        Log.d(TAG, "turnON")
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        turnOnFlash(camManager)
        mView.showON()
        exitRequested = false  //Exit requested, so in threads it will no exit
        startStutteringIfNeeded()
    }

    override fun turnOFF() {
        Log.d(TAG, "turnOFF")
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        turnOffFlash(camManager)
        mView.showOFF()
        exitRequested = true  //Exit requested, so in threads it will no exit
        stutterHandler.stop() //Stop listening
    }

    private fun turnOnALittleTillDeath() {
        if (disabledFlashPressed > 0) {
            disabledFlashPressed--
            loadWontTurnON()
        }
        Log.d(TAG, "Waiting user to smack the phone")
    }

    private fun startStutteringIfNeeded() {
        if (stutterTimesBeforeDeath > 0) {
            stutterHandler.start() // Start repeating
        }
    }

    override fun switchFlashWithoutChangingButton() {
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        flashIsShowing = if (!flashIsShowing) {
            turnOnFlash(camManager)
            true
        } else {
            turnOffFlash(camManager)
            false
        }
    }

    override fun checkIfHasFlashLight(): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(context, "No flashlight on this phone", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    override fun phoneIsShaked(count: Int) {
        Log.d(TAG, "Shaked$count")

        if (!disabledFlash && flashIsShowing) {
            loadFirstTimeOn()
        }

        if (count > 1 && disabledFlash) {
            resetFlashlight()
        }

    }

    private fun resetFlashlight() {
        disabledFlash = false
        flashIsShowing = false
        disabledFlashPressed = 3
        stutterTimesBeforeDeath = 5
        exitRequested = false
        loadFirstTimeOn()
        mView.showON()
    }

    private fun getNewRandomDelay(leftLimitSmall: Long, rightLimitSmall: Long) =
            leftLimitSmall + (Math.random() * (rightLimitSmall - leftLimitSmall)).toLong()


    override fun loadFirstTimeOn() {
        val t = object : Thread() {  //More testing required
            override fun run() {
                try {
                    val leftLimitSmall = 5L
                    val rightLimitSmall = 50L

                    val leftLimitLong = 100L
                    val rightLimitLong = 200L


                    val leftLimitReallyLong = 500L
                    val rightLimitReallyLong = 1500L

                    val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)

                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitLong, rightLimitLong)
                    turnOnInThread(camManager, leftLimitLong, rightLimitLong)


                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitReallyLong, rightLimitReallyLong)

                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)

                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)


                } catch (e: InterruptedException) {
                    Log.d("LoadFirstTimeON", "Thread interrupted $exitRequested")
                    if (exitRequested) {
                        turnOFF()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Log.d("LoadFirstTimeON", "Finally $exitRequested")
                    if (!exitRequested) {
                        startStutteringIfNeeded()
                    }
                    exitRequested = false
                }
            }

            private fun turnOnInThread(camManager: CameraManager, leftLimitSmall: Long, rightLimitSmall: Long) {
                if (exitRequested) {
                    Log.d(TAG, "Load First Time ON Interrupt requested")
                    interrupt()
                    return
                }
                turnOnFlash(camManager)
                sleep(getNewRandomDelay(leftLimitSmall, rightLimitSmall))
            }

            private fun turnOffInThread(camManager: CameraManager, leftLimitSmall: Long, rightLimitSmall: Long) {
                if (exitRequested) {
                    Log.d(TAG, "Load First Time ON Interrupt requested")
                    interrupt()
                    return
                }
                turnOffFlash(camManager)
                sleep(getNewRandomDelay(leftLimitSmall, rightLimitSmall))
            }
        }
        t.start()
    }

    private fun stutter() {
        val t = object : Thread() {  //More testing required
            override fun run() {
                try {
                    val leftLimitSmall = 5L
                    val rightLimitSmall = 20L

                    val leftLimitMedium = 30L
                    val rightLimitMedium = 50L

                    val leftLimitReallyLong = 1000L
                    val rightLimitReallyLong = 2000L

                    val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                    turnOnInThread(camManager, leftLimitReallyLong, rightLimitReallyLong)

                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitMedium, rightLimitMedium)
                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitSmall, rightLimitMedium)
                    turnOnInThread(camManager, leftLimitSmall, rightLimitMedium)
                    //2 s

                    stutterTimesBeforeDeath--
                    if (stutterTimesBeforeDeath <= 0) {
                        loadDying()
                        disabledFlash = true
                    }

                } catch (e: InterruptedException) {
                    Log.d("$TAG stutter", "Thread interrupted $exitRequested")
                    if (exitRequested) {
                        turnOFF()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Log.d("$TAG stutter", "Finally $exitRequested")
                    exitRequested = false
                }

            }

            private fun turnOffInThread(camManager: CameraManager, leftLimit: Long, rightLimit: Long) {
                if (exitRequested) {
                    Log.d(TAG, "stutter Interrupt requested")
                    interrupt()
                    return
                }
                turnOffFlash(camManager)
                sleep(getNewRandomDelay(leftLimit, rightLimit))
            }

            private fun turnOnInThread(camManager: CameraManager, leftLimit: Long, rightLimit: Long) {
                if (exitRequested) {
                    Log.d(TAG, "stutter Interrupt requested")
                    interrupt()
                    return
                }
                turnOnFlash(camManager)
                sleep(getNewRandomDelay(leftLimit, rightLimit))
            }
        }
        t.start()
    }


    override fun loadDying() {
        val t = object : Thread() {  //More testing required
            override fun run() {
                try {
                    val leftLimitSmall = 5L
                    val rightLimitSmall = 50L

                    val leftLimitLong = 100L
                    val rightLimitLong = 200L


                    val leftLimitReallyLong = 1000L
                    val rightLimitReallyLong = 2000L

                    val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitLong, rightLimitLong)

                    turnOffInThread(camManager, leftLimitSmall, rightLimitSmall)
                    turnOnInThread(camManager, leftLimitReallyLong, rightLimitReallyLong)
                } catch (e: InterruptedException) {
                    Log.d("$TAG dying", "Thread interrupted $exitRequested")
                    turnOFF()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    turnOFF()
                    Log.d("$TAG dying", "Finally $exitRequested")
                }
            }

            private fun turnOffInThread(camManager: CameraManager, leftLimit: Long, rightLimit: Long) {
                if (exitRequested) {
                    Log.d(TAG, "dying Interrupt requested")
                    interrupt()
                    return
                }
                turnOffFlash(camManager)
                sleep(getNewRandomDelay(leftLimit, rightLimit))
            }

            private fun turnOnInThread(camManager: CameraManager, leftLimit: Long, rightLimit: Long) {
                if (exitRequested) {
                    Log.d(TAG, "dying Interrupt requested")
                    interrupt()
                    return
                }
                turnOnFlash(camManager)
                sleep(getNewRandomDelay(leftLimit, rightLimit))
            }
        }
        t.start()
    }

    fun loadWontTurnON() {
        val t = object : Thread() {  //More testing required
            override fun run() {
                try {
                    val leftLimitSmall = 5L
                    val rightLimitSmall = 50L

                    val leftLimitMedium = 50L
                    val rightLimitMedium = 200L

                    val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                    turnOnFlash(camManager)
                    sleepIfNeeded(leftLimitSmall, rightLimitSmall)
                    turnOffFlash(camManager)
                    sleepIfNeeded(leftLimitMedium, rightLimitMedium)
                    turnOnFlash(camManager)
                    sleepIfNeeded(leftLimitSmall, rightLimitSmall)

                } catch (e: InterruptedException) {
                    Log.d("$TAG wont ON", "Thread interrupted $exitRequested")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    //2 s
                    turnOFF()
                    exitRequested = false
                }

            }

            private fun sleepIfNeeded(leftLimit: Long, rightLimit: Long) {
                if (exitRequested) {
                    interrupt()
                } else {
                    sleep(getNewRandomDelay(leftLimit, rightLimit))
                }
            }
        }
        t.start()
    }

}