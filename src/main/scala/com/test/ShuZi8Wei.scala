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

  println {
    outBytes.map { x => String.format("%08d", Integer.valueOf(x.toBinaryString.takeRight(8))) }.zipWithIndex.mkString(" ")
  }

  for (c <- Charset.availableCharsets().toMap) {
    println {
      c._1 + ": " + new String(outBytes, c._2)
    }
  }

  println(s"existZH: ${existZH(outBytes)}")

  // “疫苗之王”。etherscan 上居然用 “view as utf-8” 看不了了。
  // https://etherscan.io/tx/0x4e18a55ba9f295774723e621a1e7599a2cb1be84170b9d3b9f0cb02f8d799005
  println {
    new String(("e4b880e7af87e2809ce796abe88b97e4b98be78e8be2809de588b7e4ba86e5b18fefbc9a200a31e3808120e69bbee7bb8fe59ba0e4b8bae4b889e9b9bfe" +
      "5a5b6e7b289e8a2abe5a484e79086e79a84e5ad99e592b8e6b3bde58d87e4bbbbe4b8bae59bbde5aeb6e9a39fe59381e88dafe59381e79b91e79da3e7aea1e79086e5b1" +
      "80e589afe5b180e995bfefbc8ce88dafe59381e5ae89e585a8e680bbe79b91efbc8ce680bbe7aea1e796abe88b97e38082e784b6e8808cefbc8ce4b889e8819ae6b0b0e" +
      "883bae6a188e78886e69699e4babae8928be58dabe99481e98187e8a2ade8baabe4baa1e38082200a32e3808120e5bd93e5b9b4efbc8ce69b9de58589e5b1b1e8a5bfe7" +
      "96abe88b97e4ba8be4bbb6e5908eefbc8ce8aeb0e88085e78e8be5858be58ba4e69c80e7bb88e7a6bbe5bc80e8aeb0e88085e8bf99e4b8aae8818ce4b89aefbc8ce4b8b" +
      "be7bc96e58c85e69c88e998b3e8b083e7a6bbe3808a3231e4b896e7baaae7bb8fe6b58ee68aa5e98193e3808be38082e7adbee58f91e78e8be5858be58ba4e79a84e8bf" +
      "99e7af87e7a8bfe5ad90e697b6efbc8ce58c85e69c88e998b3e5b7b2e98187e8a781e4ba86e58fafe883bde79a84e7bb93e69e9ce38082e4bb96e8afb4efbc8ce4b88de" +
      "7adbeefbc8ce5afb9e4b88de8b5b7e5ada9e5ad90e38082200a33e3808120e7be8ee59bbde5bcbae7949fe797b1e5ad90e7b289e887b4e7998ce588a4e5bcbae7949fe8" +
      "b594e581bf3232e5908de58f97e5aeb3e88085200a3437e4babfe7be8ee98791efbc9be68891e4bbace58187e796abe88b97e68993e585a53235e4b887e5908de584bfe" +
      "7aba5e4bd93e58685efbc8ce6b2a1e694b6313836e694afefbc8ce7bd9ae6acbe333030e4b887e380820ac2a0e6ada4e588bbe68891e4bbace883bde5819ae79a84e58f" +
      "afe883bde4b88de5a49aefbc8ce7949ae887b3e6b2a1e595a5e794a8efbc8ce4bd86e698afe5b88ce69c9be4bda0e883bde5a49fe585b3e6b3a8e8bf99e4bbb6e4ba8be" +
      "fbc8ce58f91e58f91e69c8be58f8be59c88efbc8ce8a2abe5928ce8b090e4ba86efbc8ce5868de58f91e58f91e8af95e8af95efbc8ce4b88de8a681e7ad89e588b0e4ba" +
      "8be68385e588b0e4ba86e4bda0e5a4b4e4b88aefbc8ce6b2a1e4babae7ab99e587bae69da5e4b8bae4bda0e8afb4e8af9de4ba86e38082").decodeHex, "utf-8")
  }


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
