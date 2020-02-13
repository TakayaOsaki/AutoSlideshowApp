package jp.techacademy.takaya.oosaki.autoslideshowapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import java.lang.Exception
import android.widget.Toast
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val MAX_ARRAY = 10000

    private var mTimer: Timer? = null

    private var mHandler = Handler()
    var PicArray: Array<Uri?> = arrayOfNulls(MAX_ARRAY)
    var MaxIndex: Int = 0
    var CurrentIndex: Int = 0
    var FirstFlg: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{
            //to check whether the version is after Android 6.0 or not
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //check the status of the permission
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // permitted
                    getContentsInfo()
                } else {
                    // show the dialog for the permission since not permitted
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                }
            } else {
                getContentsInfo()
            }

            CurrentIndex = 0
            FirstFlg = true
            play_button.setText("再生")
            play_button.setOnClickListener {
                if(play_button.text == "再生"){
                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                if(FirstFlg == true){
                                    CurrentIndex = 0
                                    FirstFlg = false
                                }else{
                                    CurrentIndex++
                                }
                                if(CurrentIndex >= MaxIndex){
                                    CurrentIndex = 0
                                }
                                mHandler.post {
                                    imageView.setImageURI(PicArray[CurrentIndex])
                                }
                            }
                        }, 2000, 2000)
                        play_button.setText("停止")
                        forward_button.isEnabled = false
                        backward_button.isEnabled = false
                    }

                }
                else
                {
                    if (mTimer != null){
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    play_button.setText("再生")
                    forward_button.isEnabled = true
                    backward_button.isEnabled = true
                }

            }

            forward_button.setOnClickListener {
                if(FirstFlg == true){
                    CurrentIndex = 0
                    FirstFlg = false
                }else{
                    CurrentIndex++
                }
                if(CurrentIndex >= MaxIndex){
                    CurrentIndex = 0
                }
                imageView.setImageURI(PicArray[CurrentIndex])
            }

            backward_button.setOnClickListener {
                if(FirstFlg == true){
                    CurrentIndex = 0
                    FirstFlg = false
                }else{
                    CurrentIndex--
                }
                if(CurrentIndex < 0){
                    CurrentIndex = MaxIndex -1
                }
                imageView.setImageURI(PicArray[CurrentIndex])
            }

        }
        catch (e: Exception){
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
                else{
                    finish()
                }
        }
    }

    private fun getContentsInfo() {
        try{
            // get the information for pictures
            var i = 0
            val resolver = contentResolver
            val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // type of data
                null,
                null,
                null,
                null
            )
            i = 0
            MaxIndex = MAX_ARRAY + 1
            for (j in 0 until MAX_ARRAY){
                PicArray[j] = null
            }
            if (cursor!!.moveToFirst()) {
                do {
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    PicArray[i] = imageUri
                    i++
                    if(i == MAX_ARRAY){
                        Toast.makeText(applicationContext, "画像が最大数に達しました。\r\nこれ以上の画像は表示できません。", Toast.LENGTH_LONG).show()
                        break
                    }
                    //Log.d("ANDROID", "URI : " + PicArray[i])
                } while (cursor.moveToNext())
                MaxIndex = i
            }
            cursor.close()
            if(MaxIndex == MAX_ARRAY + 1){
                Toast.makeText(applicationContext, "画像が1つも見つかりませんでした。", Toast.LENGTH_LONG).show()
                ///TODO: disable buttons
            }
        }
        catch (e: Exception){
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }
    }
}
