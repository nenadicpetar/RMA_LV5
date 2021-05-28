package com.example.whereami.ui.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.ResultReceiver
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.whereami.BR
import com.example.whereami.R
import com.example.whereami.data.AddressIntentService
import com.example.whereami.data.LocationModel
import com.example.whereami.ui.base.ViewModelFactory
import com.example.whereami.ui.main.viewmodel.MainViewModel
import com.example.whereami.utils.GpsUtils
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ViewDataBinding
    private lateinit var resultReceiver: AddressResultReceiver
    private lateinit var currentPhotoPath: String

    private var isGPSEnabled = false

    private val locationrequest = 100
    private val camerarequest = 101

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        resultReceiver = AddressResultReceiver(null)
        resultReceiver.listener = {
            activity?.runOnUiThread {
                countryText.text = it?.get(0)
                placeText.text = it?.get(1)
                addressText.text = it?.get(2)
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity: FragmentActivity =
            activity ?: throw IllegalArgumentException("Activity is null!")
        val context = context ?: throw IllegalArgumentException("Context is null!")

        viewModel = ViewModelProvider(
            activity,
            ViewModelFactory(activity.application)
        ).get(MainViewModel::class.java)

        binding.setVariable(BR.locationViewModel, viewModel)

        GpsUtils(context).turnGPSOn(object :
        GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                this@MainFragment.isGPSEnabled = isGPSEnable
            }
        })
    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()

        val context = context ?: throw IllegalArgumentException("Context is null!")
        cameraButton.setOnClickListener {
            try {
                if (isCameraPermissionGranted()) {

                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(context.packageManager)?.also {
                            val photoFile: File? = try {
                                createImageFile()
                            } catch (ex: IOException) {
                                ex.printStackTrace()
                                null
                            }

                            photoFile?.also {
                                val photoURI: Uri = FileProvider.getUriForFile(
                                    context,
                                    "com.example.whereami",
                                    it
                                )
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(takePictureIntent, camerarequest)
                            }
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        activity as AppCompatActivity,
                        arrayOf(
                            Manifest.permission.CAMERA
                        ),
                        camerarequest
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       when (requestCode) {
           locationrequest -> {
               invokeLocationAction()
           }
       }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val context = context ?: throw IllegalArgumentException("Context is null!")
        val timestamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        val context = context ?: throw IllegalArgumentException("Context is Null!")
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun invokeLocationAction() {
        when {
            !isGPSEnabled -> return
            isLocationPermissionsGranted() -> startLocationUpdate()
            shouldShowRequestPermissionRationale() -> return

            else -> ActivityCompat.requestPermissions(
                activity as AppCompatActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationrequest
            )
        }
    }

    private fun startLocationUpdate() {
        val activity: FragmentActivity =
            activity ?: throw IllegalArgumentException("Activity is null!")
        viewModel.getLocationData()
            .observe(
                activity
            ) {
                latitudeText.text = it.latitude.toString()
                longitudeText.text = it.longitude.toString()
                startIntentService(it)
            }
    }

    private fun isLocationPermissionsGranted(): Boolean {
        val context = context ?: throw IllegalArgumentException("Context is null!")
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val activity = activity ?: throw IllegalArgumentException("Activity is null!")
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
    }

    private fun startIntentService(location: LocationModel) {
        val intent = Intent(context, AddressIntentService::class.java)
        intent.putExtra("CURRENT_LOCATION_RECEIVER", resultReceiver)
        intent.putExtra("LOCATION_DATA", location)
        context?.startService(intent)
    }

    class AddressResultReceiver(handler: Handler?) : ResultReceiver(handler) {

        var listener: (address: List<String>?) -> Unit = { _ -> }

        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            val serviceResult = resultData.getString("ADDRESS_DATA") ?: ""
            val address = serviceResult.split("|").toList()
            listener.invoke(address)
        }

    }

}