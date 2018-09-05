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
            context.externalCacheDir.path
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
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
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
}