package com.adit.camerafragment


import android.content.Context
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.IOException
import android.view.WindowManager
import java.io.FileNotFoundException
import java.io.FileOutputStream
import android.widget.Toast
import android.hardware.Camera.AutoFocusCallback




/**
 * A simple [Fragment] subclass.
 *
 */
class CameraFragment : Fragment(), SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {


    var mCamera: Camera? = null
    var mCameraParam:Camera.Parameters? = null
    var mPreview: SurfaceView? = null
    var filePath: String? = null
    var currentCameraId = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onResume() {
        super.onResume()

        mCameraParam?.jpegQuality = 100
        mCameraParam?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        mCamera?.parameters = mCameraParam
        mCamera?.autoFocus(AutoFocusCallback { success, camera -> run{Log.d("dodol", "dodol")}})
        mPreview = camera_view
        mPreview?.getHolder()?.addCallback(this)
        mPreview?.getHolder()?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)


        mCamera = Camera.open(currentCameraId)

        button_capture.setOnClickListener {
            mCamera?.takePicture(this, null, this)
        }
    }

    override fun onPause() {
        super.onPause()
        mCamera?.stopPreview();
    }

    override fun onDestroy() {
        super.onDestroy()
        mCamera?.release();
        Log.d("CAMERA","Destroy");
    }

    fun onCancelClick(v: View) {

        mCamera?.stopPreview()
        mCamera?.release()
        if (currentCameraId === Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        mCamera = Camera.open(currentCameraId)
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info)
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.getDefaultDisplay().getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }//Natural orientation
        //Landscape left
        //Upside down
        //Landscape right
        val rotate = (info.orientation - degrees + 360) % 360

        //STEP #2: Set the 'rotation' parameter
        val params = mCamera?.getParameters()
        params?.setRotation(rotate)
        try {
            mCamera?.setPreviewDisplay(mPreview?.getHolder())
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        mCamera?.setParameters(params)
        mCamera?.setDisplayOrientation(90)
        mCamera?.startPreview()
    }

    fun onSnapClick(v: View) {
        mCamera?.takePicture(this, null, null, this)
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.getDefaultDisplay().getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }//Natural orientation
        //Landscape left
        //Upside down
        //Landscape right
        val rotate = (info.orientation - degrees + 360) % 360

        //STEP #2: Set the 'rotation' parameter
        val params = mCamera?.getParameters()
        params?.setRotation(rotate)
        mCamera?.setParameters(params)
        mCamera?.setDisplayOrientation(90)
        mCamera?.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.i("PREVIEW","surfaceDestroyed");
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            mCamera?.setPreviewDisplay(mPreview?.getHolder())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onShutter() {
        Toast.makeText(context, "Click!", Toast.LENGTH_SHORT).show()
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        //Here, we chose internal storage
        var fos: FileOutputStream? = null
        try {
            filePath = context?.filesDir.toString() + "test.jpg"
            fos = FileOutputStream(
                    filePath)
            fos!!.write(data)
            fos!!.close()
            //Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.d("Log", "onPictureTaken - wrote bytes: " + data?.size)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("Log", "onPictureTaken - wrote bytes: " + data?.size)
        } finally {
            //val i = getIntent()
            //i.putExtra("Path", filePath)
            //setResult(RESULT_OK, i)
            //finish()
        }
        camera?.startPreview()
    }

}