package com.haerokim.project_footprint.Network

import android.os.AsyncTask
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 *  Image를 로컬에 저장하기 위한 AsyncTask Class
 *  - 인스타그램으로 이미지를 공유할 때, 이미지 리소스 URL을 이용하면 오류 발생함
 *  - 따라서 로컬에 이미지를 저장한 뒤 공유 동작을 해야함
 **/

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

        // 중복 다운로드가 아닐 경우 진입
        if (File(savePath + "/" + fileName).exists() == false) {
            try {
                val imgUrl = URL(fileUrl)
                val connection: HttpURLConnection = imgUrl.openConnection() as HttpURLConnection
                val len: Int = connection.contentLength
                val tmpByte = ByteArray(len)

                val inputStream: InputStream = connection.inputStream
                val file = File(localPath)

                val fos = FileOutputStream(file)
                var read: Int
                //입력 스트림을 파일로 저장
                while (true) {
                    read = inputStream.read(tmpByte)
                    if (read <= 0) {
                        break
                    }
                    //File 생성
                    fos.write(tmpByte, 0, read)
                }
                inputStream.close()
                fos.close()
                connection.disconnect()

                return localPath
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {  // 중복 다운로드 동작일 경우 진입
            return localPath
        }
        return localPath
    }
}