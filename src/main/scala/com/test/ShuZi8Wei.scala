/*
 * Copyright (C) 2019-present, Chenai Nakam(chenai.nakam@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test

import com.test.implicts.{CryptoBytes, CryptoString}
import de.innosystec.unrar.Archive
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.regex.Pattern

import scala.collection.convert.WrapAsScala.mapAsScalaMap

/**
 * @author Chenai Nakam(chenai.nakam@gmail.com)
 * @version 1.0, 08/09/2019
 */
object ShuZi8Wei extends App {
  println()

  //  lazy val path = "/home/weichou/test/8位纯数字.txt"
  //  lazy val BOM = "efbbbf".decodeHex
  //  lazy val FFFE = "fffe".decodeHex
  //  lazy val FEFF = "feff".decodeHex
  //  lazy val A0 = "0a".decodeHex

  //  println(BOM)
  //  println(FFFE)
  //  println(FEFF)
  //  println(A0)

  lazy val HASH_CONTAINS = "1.rar:$RAR3$*1*42153bfd38455446*18f4dd1f*176*171*1*7581534c7c04fb4503d26e9e749ac3a2b0bc964a3b31f78e63e7f8aa8886583d791e2a049b1f0c8c9f94b7260b17afe5a8fafe8e64420b7839289e8803278b039e4a13a7b11a7e4556970e050d9ad975873ea313dcfbbfc94744a873d54e025a0b4a40e31ad559a4e50e2c2ce7672bd6141cd3f1b3b7f42f1bb13c11fee4dd532cd47982cf98477ec9f3032782ff7e5f68f903fd32cf8e3f67a40aa1cee2d8de3f6d0e0963f33f11d039815c482f1175*33:1::8位纯数字.txt"
  lazy val HASH_4_DECODE = "7581534c7c04fb4503d26e9e749ac3a2b0bc964a3b31f78e63e7f8aa8886583d791e2a049b1f0c8c9f94b7260b17afe5a8fafe8e64420b7839289e8803278b039e4a13a7b11a7e4556970e050d9ad975873ea313dcfbbfc94744a873d54e025a0b4a40e31ad559a4e50e2c2ce7672bd6141cd3f1b3b7f42f1bb13c11fee4dd532cd47982cf98477ec9f3032782ff7e5f68f903fd32cf8e3f67a40aa1cee2d8de3f6d0e0963f33f11d039815c482f1175"

  val pwd = String.format("%08d", Integer.valueOf(51678003)) //.getBytes()
  val salt = "42153bfd38455446".decodeHex
  val CRC = Integer.valueOf("18f4dd1f", 16)
  val packSize = 176
  val unpZise = 171


  //    lazy val HASH_CONTAINS = "test2.rar:$RAR3$*1*d1b2b3b933dc7218*321c8870*16*5*1*a9810846f89494554d11d2732d2323cc*33:1::abcd1.txt abcd2.txt"
  //    lazy val HASH_4_DECODE = "a9810846f89494554d11d2732d2323cc"
  //
  //    val pwd = "1234"
  //    val salt = "d1b2b3b933dc7218".decodeHex
  //    val CRC = Integer.valueOf("321c8870", 16)
  //    val packSize = 16
  //    val unpZise = 5

  println {
    HASH_4_DECODE
  }
  val byesDec = HASH_4_DECODE.decodeHex
  println {
    s"byesDec.length: ${byesDec.length}"
  }
  println(s"pwd: $pwd")
  println(s"CRC: $CRC")

  val byteBuffer = ByteBuffer.allocate(byesDec.length)
  byteBuffer.put(byesDec)
  byteBuffer.rewind()

  val out = new ByteArrayOutputStream
  val roa = new ReadOnlyAccessBytesIn(byteBuffer, pwd)
  roa.setSalt(salt)

  val archive = new Archive(roa, packSize)
  archive.extractFile(out, packSize, unpZise, 29, CRC)

  val outBytes = out.toByteArray


  println("outBytes.length: " + outBytes.length)
  //  Raw.incShortLittleEndian()

  println(outBytes.toHex)

  //  println(new String(outBytes))

  println {
    outBytes.map { x => String.format("%08d", Integer.valueOf(x.toBinaryString.takeRight(8))) }.zipWithIndex.mkString(" ")
  }

    for (c <- Charset.availableCharsets().toMap) {
        println {
          c._1 + ": " + new String(outBytes, c._2)
        }
    }

  println(s"existZH: ${existZH(outBytes)}")

  println(new String(outBytes))


  //  val rawpsw = new Array[Byte](2 * pwd.length + 8)
  //  val pwdArr = pwd.getBytes()
  //  for (i <- 0 until pwd.length) {
  //    rawpsw(i * 2) = pwdArr(i)
  //    rawpsw(i * 2 + 1)
  //  }
  //
  //  for (i <- salt.indices) {
  //    rawpsw(i + 2 * pwd.length) = salt(i)
  //  }
  //  println(rawpsw.toHex)
  //  println(rawpsw.map(_.toChar).mkString(""))


  /** 判断是否是中文 */
  def existZH(bytes: Array[Byte]) = pattern.matcher(new String(bytes)).find

  lazy val pattern = Pattern.compile("[\\u4e00-\\u9fa5]")
}
