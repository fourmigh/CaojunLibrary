package org.caojun.citypicker.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import org.caojun.citypicker.db.DBConfig.COLUMN_C_CODE
import org.caojun.citypicker.db.DBConfig.COLUMN_C_NAME
import org.caojun.citypicker.db.DBConfig.COLUMN_C_PINYIN
import org.caojun.citypicker.db.DBConfig.COLUMN_C_PROVINCE
import org.caojun.citypicker.db.DBConfig.DB_NAME_V1
import org.caojun.citypicker.db.DBConfig.LATEST_DB_NAME
import org.caojun.citypicker.db.DBConfig.TABLE_NAME
import org.caojun.citypicker.model.City
import java.io.*
import java.util.*

class DBManager {

    companion object {
        private const val BUFFER_SIZE = 1024
    }

    private var DB_PATH: String
    private val mContext: Context

    constructor(context: Context) {
        this.mContext = context
        DB_PATH = (File.separator + "data"
                + Environment.getDataDirectory().absolutePath + File.separator
                + context.packageName + File.separator + "databases" + File.separator)
        copyDBFile()
    }

    private fun copyDBFile() {
        val dir = File(DB_PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        //如果旧版数据库存在，则删除
        val dbV1 = File(DB_PATH + DB_NAME_V1)
        if (dbV1.exists()) {
            dbV1.delete()
        }
        //创建新版本数据库
        val dbFile = File(DB_PATH + LATEST_DB_NAME)
        if (!dbFile.exists()) {
            val `is`: InputStream
            val os: OutputStream
            try {
                `is` = mContext.resources.assets.open(LATEST_DB_NAME)
                os = FileOutputStream(dbFile)
                val buffer = ByteArray(BUFFER_SIZE)
//                var length: Int
//                while ((length = `is`.read(buffer, 0, buffer.size)) > 0) {
//                    os.write(buffer, 0, length)
//                }
                do {
                    val length = `is`.read(buffer, 0, buffer.size)
                    if (length > 0) {
                        os.write(buffer, 0, length)
                    }
                } while (length > 0)
                os.flush()
                os.close()
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun getAllCities(): MutableList<City> {
        val db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + LATEST_DB_NAME, null)
        val cursor = db.rawQuery("select * from $TABLE_NAME", null)
        val result = ArrayList<City>()
        var city: City
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_C_NAME))
            val province = cursor.getString(cursor.getColumnIndex(COLUMN_C_PROVINCE))
            val pinyin = cursor.getString(cursor.getColumnIndex(COLUMN_C_PINYIN))
            val code = cursor.getString(cursor.getColumnIndex(COLUMN_C_CODE))
            city = City(name, province, pinyin, code)
            result.add(city)
        }
        cursor.close()
        db.close()
        Collections.sort<City>(result, CityComparator())
        return result
    }

    fun searchCity(keyword: String): MutableList<City> {
        val sql = ("select * from " + TABLE_NAME + " where "
                + COLUMN_C_NAME + " like ? " + "or "
                + COLUMN_C_PINYIN + " like ? ")
        val db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + LATEST_DB_NAME, null)
        val cursor = db.rawQuery(sql, arrayOf("%$keyword%", "$keyword%"))

        val result = ArrayList<City>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_C_NAME))
            val province = cursor.getString(cursor.getColumnIndex(COLUMN_C_PROVINCE))
            val pinyin = cursor.getString(cursor.getColumnIndex(COLUMN_C_PINYIN))
            val code = cursor.getString(cursor.getColumnIndex(COLUMN_C_CODE))
            val city = City(name, province, pinyin, code)
            result.add(city)
        }
        cursor.close()
        db.close()
        val comparator = CityComparator()
        Collections.sort<City>(result, comparator)
        return result
    }

    /**
     * sort by a-z
     */
    private inner class CityComparator : Comparator<City> {
        override fun compare(lhs: City, rhs: City): Int {
            val a = lhs.getPinyin().substring(0, 1)
            val b = rhs.getPinyin().substring(0, 1)
            return a.compareTo(b)
        }
    }
}