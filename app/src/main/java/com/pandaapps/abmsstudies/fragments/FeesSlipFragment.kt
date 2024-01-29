package com.pandaapps.abmsstudies.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.pandaapps.abmsstudies.databinding.FragmentFeesSlipBinding
import im.delight.android.webview.AdvancedWebView
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class FeesSlipFragment : Fragment() {


    private companion object {
        private const val TAG = "FEES_SLIP_FRAGMENTS"
        private const val STORAGE_PERMISSION_CODE = 100
    }



    private lateinit var binding: FragmentFeesSlipBinding

    private lateinit var mContext: Context

   private var displayMsg: String? = ""


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFeesSlipBinding.inflate(LayoutInflater.from(mContext), container, false)

        displayMsg = arguments?.getString("admissionNumber")

        val admissionNumber = displayMsg


        val url = "https://schoolpk.org/493/uploads/feebill/$admissionNumber.png"

        binding.webView.setDesktopMode(true)

        @SuppressLint("ViewConstructor")
        class FeesSlipFragment : AdvancedWebView(mContext), AdvancedWebView.Listener {


            override fun onResume() {
                super.onResume()
                binding.webView.onResume()
            }

            override fun onPause() {
                binding.webView.onPause()
                super.onPause()
            }

            override fun onDestroy() {
                binding.webView.onDestroy()
                super.onDestroy()
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
                super.onActivityResult(requestCode, resultCode, intent)
                binding.webView.onActivityResult(requestCode, resultCode, intent)
            }

            override fun onBackPressed(): Boolean {
                return binding.webView.onBackPressed()
                super.onBackPressed()

            }

            override fun onPageStarted(url: String?, favicon: Bitmap?) {

            }

            override fun onPageFinished(url: String?) {

            }

            override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {

            }

            override fun onDownloadRequested(
                url: String?,
                suggestedFilename: String?,
                mimeType: String?,
                contentLength: Long,
                contentDisposition: String?,
                userAgent: String?
            ) {

            }

            override fun onExternalPageRequest(url: String?) {

            }


        }

        binding.webView.loadUrl(url)

        binding.downloadFessFab.setOnClickListener {

            takeScreenshot()

            if (checkPermission()){
                Log.d(TAG, "onCreateView: Permission already granted, create folder")
                
            }
            else{
                Log.d(TAG, "onCreateView: Permission was not granted")
                requestPermission()
            }


        }




        return binding.root


    }

   private fun takeScreenshot() {
        val now = Date()
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {

            if (Build.VERSION.SDK_INT >Build.VERSION_CODES.P){

                val cw = ContextWrapper(mContext)


                val mPath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/myAppImages/")

                if (!mPath.exists()) {
                    mPath.parentFile.mkdirs()
                }

                val v1 =
                    requireActivity().window.decorView.findViewById<AdvancedWebView>(com.pandaapps.abmsstudies.R.id.webView)
//                    v1.isDrawingCacheEnabled = true
                val canvas = Canvas()
                val bitmap = Bitmap.createBitmap(v1.width * 2, v1.height * 2, Bitmap.Config.ARGB_8888)
                canvas.setBitmap(bitmap)
                canvas.scale(2.0f, 2.0f)
                v1.draw(canvas)
                val imageFile = (mPath)
                val outputStream = FileOutputStream(imageFile)
                val quality = 100
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
                outputStream.flush()
                outputStream.close()

                MotionToast.createColorToast(requireActivity(),"Upload Completed!","$mPath",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION, ResourcesCompat.getFont(mContext,www.sanju.motiontoast.R.font.helvetica_regular))


            }
            else{
                val mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM.toString() + "/"+ now + ".jpg")

                val v1 =
                    requireActivity().window.decorView.findViewById<AdvancedWebView>(com.pandaapps.abmsstudies.R.id.webView)
//                    v1.isDrawingCacheEnabled = true
                val canvas = Canvas()
                val bitmap = Bitmap.createBitmap(v1.width * 2, v1.height * 2, Bitmap.Config.ARGB_8888)
                canvas.setBitmap(bitmap)
                canvas.scale(2.0f, 2.0f)
                v1.draw(canvas)
                val imageFile = (mPath)
                val outputStream = FileOutputStream(imageFile)
                val quality = 100
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
                outputStream.flush()
                outputStream.close()

                MotionToast.createColorToast(requireActivity(),"Upload Completed!","$mPath",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION, ResourcesCompat.getFont(mContext,www.sanju.motiontoast.R.font.helvetica_regular))
            }






        } catch (e: Throwable) {
            e.printStackTrace()
        }



    }


    private fun requestPermission() {

        //Android R (11)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            try {
                Log.d(TAG, "requestPermission: ")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", mContext.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)

            } catch (e: Exception) {
                Log.e(TAG, "requestPermission: ")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {

            //Android below 10

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ),
                STORAGE_PERMISSION_CODE,
            )

        }

    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.R){

            if (Environment.isExternalStorageManager()){

                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted ")


            }

            else{

                Log.d(TAG, "storageActivityResultLauncher: ")
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }

        else{
            //Android is below 11
        }


    }

    private fun checkPermission():Boolean{
        return  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // Android is 11 or above
            Environment.isExternalStorageManager()
        }
        else{
            //Android is below 11(R)
            val write = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(mContext,Manifest.permission.READ_EXTERNAL_STORAGE)

            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isEmpty()){

                val write  = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read  = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){

                    Log.d(TAG, "onRequestPermissionsResult: ")


                }
                else{


                    Log.d(TAG, "onRequestPermissionsResult: ")
                    Toast.makeText(mContext, "External Storage Permission denied...", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}