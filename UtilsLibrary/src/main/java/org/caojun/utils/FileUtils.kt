package org.caojun.utils

import android.content.Context
import android.os.Environment
import android.provider.MediaStore.MediaColumns
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentValues
import java.io.File

object FileUtils {

    fun getDiskCachePath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            if (context.externalCacheDir == null) {
                context.cacheDir.path
            } else {
                context.externalCacheDir!!.path
            }
        } else {
            context.cacheDir.path
        }
    }

    fun getUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ",
                arrayOf(filePath), null)
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaColumns._ID))
            val baseUri = Uri.parse("content://media/external/images/media")
            Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } else {
                null
            }
        }
    }

    fun getPath(uri: Uri, contentResolver: ContentResolver): String {
        val filePath: String
        val filePathColumn = arrayOf(MediaColumns.DATA)

        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        //      也可用下面的方法拿到cursor
        //      Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);

        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }

    private val list = ArrayList<String>()

    fun searchFile(extName: String): ArrayList<String> {
        list.clear()

        val rootDir = Environment.getExternalStorageDirectory()
        searchFolder(rootDir.absolutePath, extName)

        return list
    }

    private fun searchFolder(path: String, extName: String) {
        val file = File(path)
        if (!file.exists()) {
            return
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files == null || files.isEmpty()) {
                return
            }
            for (f in files) {
                searchFolder(f.absolutePath, extName)
            }
            return
        }
        val fileName = file.name
        if (fileName.endsWith(".$extName")) {
            list.add(file.absolutePath)
        }
    }
}