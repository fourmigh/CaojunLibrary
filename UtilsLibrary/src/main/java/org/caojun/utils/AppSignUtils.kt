package org.caojun.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and
import kotlin.experimental.or

object AppSignUtils {

    val SHA1 = "SHA1"
    val MD5 = "MD5"

    /**
     * 返回一个签名的对应类型的字符串
     *
     * @param context
     * @param packageName
     * @param type
     *
     * @return
     */
    fun getSingInfo(context: Context, packageName: String, type: String): String {
        val signs = getSignatures(context, packageName) ?: return ""
        return getSignatureString(signs[0], type)
    }

    /**
     * 返回对应包的签名信息
     *
     * @param context
     * @param packageName
     *
     * @return
     */
    private fun getSignatures(context: Context, packageName: String): Array<Signature>? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            return packageInfo.signatures
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成16进制）
     *
     * @param sig
     * @param type
     *
     * @return
     */
    private fun getSignatureString(sig: Signature, type: String): String {
        val hexBytes = sig.toByteArray()
        var fingerprint = "error!"
        try {
            val digest = MessageDigest.getInstance(type)
            if (digest != null) {
                val digestBytes = digest.digest(hexBytes)
                val sb = StringBuilder()
                for (digestByte in digestBytes) {
                    var hex = Integer.toHexString((digestByte and 0xFF.toByte() or 0x100.toByte()).toInt())
                    if (hex.length < 2) {
                        hex = "0$hex"
                    }
                    val length = hex.length
                    sb.append(hex, length - 2, length)
                }
                fingerprint = sb.toString()
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return fingerprint
    }
}
