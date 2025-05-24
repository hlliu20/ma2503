package com.hlliu.ma2503

import java.nio.charset.Charset

class myTest {
}

private fun fixEncoding(inString: String): String {
    val bytes = inString.encodeToByteArray()
    return String(bytes, Charset.forName("GBK"))
}
private fun fixEncoding2(inString: String): String {
    val bytes = inString.toByteArray(Charset.forName("GBK"))
    return String(bytes, Charset.forName("UTF-8"))
}

// 将字符串转换为十六进制表示
fun stringToHex(input: String, charset: Charset = Charsets.UTF_8): String {
    val bytes = input.toByteArray(charset)
    return bytes.joinToString(separator = "") { byte -> "%02X".format(byte) }
}
fun main() {
    val str1 = "西红柿炒鸡蛋"
    val str2 = "瑗跨孩鏌跨倰楦¤泲"
    println(stringToHex(str1))
    println(stringToHex(str1, Charset.forName("GBK")))

    println(stringToHex(str2))
    println(stringToHex(str2, Charset.forName("GBK")))

    val str3 = fixEncoding(str1)
    println(str3)
    println(stringToHex(str3))
    println(stringToHex(str3, Charset.forName("GBK")))

    val str4 = fixEncoding(str2)
    println(str4)
    println(stringToHex(str4))
    println(stringToHex(str4, Charset.forName("GBK")))

    val str5 = fixEncoding2(str1)
    println(str5)
    println(stringToHex(str5))
    println(stringToHex(str5, Charset.forName("GBK")))

    val str6 = fixEncoding2(str2)
    println(str6)
    println(stringToHex(str6))
    println(stringToHex(str6, Charset.forName("GBK")))
}
//E8A5BFE7BAA2E69FBFE78292E9B8A1E89B8B
//CEF7BAECCAC1B3B4BCA6B5B0
//E79197E8B7A8E5ADA9E98F8CE8B7A8E580B0E6A5A6C2A4E6B3B2
//E8A5BFE7BAA2E69FBFE78292E9B8A1E89B8B
//瑗跨孩鏌跨倰楦¤泲
//E79197E8B7A8E5ADA9E98F8CE8B7A8E580B0E6A5A6C2A4E6B3B2
//E8A5BFE7BAA2E69FBFE78292E9B8A1E89B8B
//鐟楄法瀛╅弻璺ㄥ�版ウ陇娉�
//E9909FE6A584E6B395E7809BE29585E5BCBBE792BAE384A5EFBFBDE78988E382A6E99987E5A889EFBFBD
//E79197E8B7A8E5ADA9E98F8CE8B7A8E53FB0E6A5A6C2A4E6B33F
//������������
//EFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBDEFBFBD
//3F3F3F3F3F3F3F3F3F3F3F3F
//西红柿炒鸡蛋
//E8A5BFE7BAA2E69FBFE78292E9B8A1E89B8B
//CEF7BAECCAC1B3B4BCA6B5B0