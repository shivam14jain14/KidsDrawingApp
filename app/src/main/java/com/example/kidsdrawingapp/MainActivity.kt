package com.example.kidsdrawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ib_gallery.setOnClickListener {
            if(isReadStorageAllowed())
            {
//                THEN RUN OUR CODE TO GET THE IMAGE FROM THE GALLERY


//                NOW WE ARE ADDING A PHOTO FROM THE LIBRARY AND WE WILL USE INTENT FOR IT
//                INTENT IS USED TO GO FROM ONE ACTIVITY TO ANOTHER BUT IT CA ALSO BE USED TO FETCH AN IMAGE FROM THE GALLERY
                val pickPhotoIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            }
            else
            {
                requestStoragePermission()
            }
        }
        drawing_view.setSizeForBrush(20.toFloat())
        mImageButtonCurrentPaint=ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        ib_brush.setOnClickListener{showBrushSizeChooseDialog()}






        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }






        ib_save.setOnClickListener{
            if(isReadStorageAllowed())
            {
                //BECAUSE WE HAVE TO SEND A BITMAP AND NOT A CONTAINER

                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute()
            }
            else
            {
                requestStoragePermission()
            }
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode==GALLERY)
            {
                try {
                        if(data!!.data!=null)
                        {
                              iv_background.visibility=View.VISIBLE
                            iv_background.setImageURI(data.data)
                        }
                    else
                        {
                            Toast.makeText(this,"Error in the image type",Toast.LENGTH_LONG).show()
                        }
                }catch(e:Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }






    private fun showBrushSizeChooseDialog()
    {
        val brushDialog= Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size :")
        val smallBtn=brushDialog.ib_small_brush
        smallBtn.setOnClickListener { drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss() }


        val mediumBtn=brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener{drawing_view.setSizeForBrush(20.toFloat())
        brushDialog.dismiss()}
        val largeBtn=brushDialog.ib_large_brush
        largeBtn.setOnClickListener{drawing_view.setSizeForBrush((30.toFloat()))
        brushDialog.dismiss()}

        brushDialog.show()
    }
    fun paintClicked(view: View)
    {
        if(view!=mImageButtonCurrentPaint)
        {
            val imageButton=view as ImageButton
            val colorTag=imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            if(colorTag!="white") {
                imageButton.setImageDrawable(

                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
                )

            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )}
            mImageButtonCurrentPaint=view
        }

    }
    private fun requestStoragePermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()))
        {
                  Toast.makeText(this,"Need permission to add the background",Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
    }

//    FUNCTION FOR ONCE THE USERGIVES US PERMISSION OR NOT


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== STORAGE_PERMISSION_CODE)
        {
            if(grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                isReadStorageAllowed()

            }
            else
            {
                Toast.makeText(this," No Permission Granted",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun isReadStorageAllowed(): Boolean
    {
//        TO CHECK IF WE HAVE GOT THE PERMISSION OR NOT
      val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
//        BECAUSE RETURN MEI BOOL VALUE NAHI HOGI ISILIYE AISE KIYA
    }
    private fun getBitmapFromView(view:View): Bitmap
    {
        val  returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null)
        {
            bgDrawable.draw(canvas)
        }
        else
        {
                      canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

//    private inner class BitmapAsyncTask(val mBitmap:Bitmap):
//
//        AsyncTask<Any, Void, String>(){
//        override fun doInBackground(vararg params: Any?): String {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//
//            var result=""
//            if(mBitmap!=null)
//            {
//                try {
//                          val bytes=ByteArrayOutputStream()
//                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
//                    val f= File(externalCacheDir!!.absoluteFile.toString()+File.separator +"KidsDrawingApp_"+System.currentTimeMillis()/1000+".png")
//                   val fos=FileOutputStream(f)
//                    fos.write(bytes.toByteArray())
//                    fos.close()
//                    result=f.absolutePath
//                } catch(e:Exception)
//                            {
//                                 result=""
//                                e.printStackTrace()
//                            }
//
//
//            }
//            return result
//        }
//
//        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
//            if(!result!!.isEmpty())
//            {
//                Toast.makeText(this@MainActivity,"File saved sucessfully",Toast.LENGTH_SHORT).show()
//            }
//            else
//            {
//                Toast.makeText(this@MainActivity,"Something went wrong",Toast.LENGTH_SHORT).show()
//            }
//        }


//



    @SuppressLint("StaticFieldLeak")
    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
        AsyncTask<Any, Void, String>() {

        /**
         * ProgressDialog is a modal dialog, which prevents the user from interacting with the app.
         *
         * The progress dialog in newer versions is deprecated so we will create a custom progress dialog later on.
         * This is just an idea to use progress dialog.
         */

        @Suppress("DEPRECATION")
        private var mDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {

            var result = ""

            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */

                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "KidDrawingApp_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )
                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fo =
                        FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            cancelProgressDialog()

            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // TODO (Step 1 - Sharing the downloaded Image file)
            // START

            /*MediaScannerConnection provides a way for applications to pass a
            newly created or downloaded media file to the media scanner service.
            The media scanner service will read metadata from the file and add
            the file to the media content provider.
            The MediaScannerConnectionClient provides an interface for the
            media scanner service to return the Uri for a newly scanned file
            to the client of the MediaScannerConnection class.*/

            /*scanFile is used to scan the file when the connection is established with MediaScanner.*/
            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(result), null
            ) { path, uri ->
                // This is used for sharing the image after it has being stored in the storage.
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                ) // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
                shareIntent.type =
                    "image/jpeg" // The MIME type of the data being handled by this intent.
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share"
                    )
                )// Activity Action: Display an activity chooser,
                // allowing the user to pick what they want to before proceeding.
                // This can be used as an alternative to the standard activity picker
                // that is displayed by the system when you try to start an activity with multiple possible matches,
                // with these differences in behavior:
            }
            // END
        }

        /**
         * This function is used to show the progress dialog with the title and message to user.
         */
        private fun showProgressDialog() {
            @Suppress("DEPRECATION")
            mDialog = ProgressDialog.show(
                this@MainActivity,
                "",
                "Saving your image..."
            )
        }

        /**
         * This function is used to dismiss the progress dialog if it is visible to user.
         */
        private fun cancelProgressDialog() {
            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }
    }



    companion object
    {
        private const val STORAGE_PERMISSION_CODE =1
        private const val GALLERY=2

    }
}
