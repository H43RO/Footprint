package com.haerokim.project_footprint.Network

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class ImageDownloader : AsyncTask<String, Void, String>() {
    var fileName: String = "footprint"
    val SAVE_FOLDER: String = "/footprint"
    override fun doInBackground(vararg params: String): String {
        val savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER
        val fileDir = File(savePath)
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        val fileUrl = params[0]
        val localPath = "$savePath/$fileName.jpg"

        if (File(savePath + "/" + fileName).exists() == false) {  // 중복 다운로드가 아닐 경우
            try {
                val imgUrl = URL(fileUrl)
                val connection: HttpURLConnection = imgUrl.openConnection() as HttpURLConnection
                val len: Int = connection.contentLength
                val tmpByte = ByteArray(len)
                //입력 스트림을 구한다
                val inputStream: InputStream = connection.inputStream
                val file = File(localPath)
                //파일 저장 스트림 생성
                val fos = FileOutputStream(file)
                var read: Int
                //입력 스트림을 파일로 저장
                while (true) {
                    read = inputStream.read(tmpByte)
                    if (read <= 0) {
                        break
                    }
                    fos.write(tmpByte, 0, read) //file 생성
                }
                inputStream.close()
                fos.close()
                connection.disconnect()

                return localPath
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {  // 중복 다운로드 동작일 경우
            return localPath
        }
        return localPath
    }
}