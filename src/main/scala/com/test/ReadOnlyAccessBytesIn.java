/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 23.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */
package com.test;

import de.innosystec.unrar.io.IReadOnlyAccess;
import gnu.crypto.cipher.IBlockCipher;
import gnu.crypto.cipher.Rijndael;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class ReadOnlyAccessBytesIn implements IReadOnlyAccess {
    private ByteBuffer in;
    private byte[] salt;
    private Queue<Byte> data = new LinkedList<Byte>();
    private Rijndael rin;
    private byte[] AESKey = new byte[16];
    private byte[] AESInit = new byte[16];
    private String password;

    public ReadOnlyAccessBytesIn(ByteBuffer in, String password) {
        this.in = in;
        this.password = password;
    }

    public int readFully(byte[] buffer, int count) throws IOException {
        assert (count > 0) : count;

        // read salt
        if (this.salt != null) {
            // System.out.println("salt is not null");

            int cs = data.size();
            int sizeToRead = count - cs;

            if (sizeToRead > 0) {
                int alignedSize = sizeToRead + ((~sizeToRead + 1) & 0xf);
                for (int i = 0; i < alignedSize / 16; i++) {
                    //long ax = System.currentTimeMillis();
                    byte[] tr = new byte[16];
                    this.readFully(tr, 0, 16);

                    /**
                     * decrypt & add to data list.
                     */
                    byte[] out = new byte[16];
                    this.rin.decryptBlock(tr, 0, out, 0);
                    for (int j = 0; j < out.length; j++) {
                        this.data.add((byte) (out[j] ^ AESInit[j % 16])); //32:114, 33:101
                    }

                    for (int j = 0; j < AESInit.length; j++) {
                        AESInit[j] = tr[j];
                    }
                    //System.out.println(System.currentTimeMillis()-ax);
                }
                //System.out.println(out);
            }

            for (int i = 0; i < count; i++) {
                buffer[i] = data.poll();
            }

        } else {
            this.readFully(buffer, 0, count);
        }

        return count;
    }

    @Override
    public void close() throws IOException {
    }

    public long getPosition() throws IOException {
        return in.position();
    }

    public void setPosition(long pos) throws IOException {
        in.position((int) pos);
    }

    @Override
    public int read() throws IOException {
        return in.get();
    }

    @Override
    public int read(byte[] buffer, int off, int count) throws IOException {
        in.get(buffer, off, count);
        return count;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;

        if (salt != null) {
            // caculate aes key and aes iv and then init rinj
            //String password = "1234";
            this.rin = new Rijndael();
            initAES(this.rin, salt, AESInit, AESKey);
        }
    }

    public void initAES(Rijndael rin, byte[] salt, byte[] AESInit, byte[] AESKey) {
        if (salt.length == 0) salt = this.salt;
        int rawLength = 2 * password.length();
        byte[] rawpsw = new byte[rawLength + 8];
        byte[] pwd = password.getBytes();
        for (int i = 0; i < password.length(); i++) {
            rawpsw[i * 2] = pwd[i];
            rawpsw[i * 2 + 1] = 0;
        }
        for (int i = 0; i < salt.length; i++) {
            rawpsw[i + rawLength] = salt[i];
        }

        // rawLength += 8;

        // SHA-1
        try {
            MessageDigest sha = MessageDigest.getInstance("sha-1");

            final int HashRounds = 0x40000;
            final int xh = HashRounds / 16;
            //List<Byte> content = new ArrayList<Byte>();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            //byte[] input = null;
            byte[] digest = null;
//			byte[] pswNum = new byte[3];
            //long ax = System.currentTimeMillis();
            for (int i = 0; i < HashRounds; i++) {
                bout.write(rawpsw);
                //bb.put(rawpsw);
                //content.addAll(Arrays.asList(rawpsw));
//				pswNum[0] = (byte) i;
//				pswNum[1] = (byte) (i >> 8);
//				pswNum[2] = (byte) (i >> 16);
                //bb.put(pswNum);
                bout.write(new byte[]{(byte) i, (byte) (i >> 8), (byte) (i >> 16)});
                //content.addAll(Arrays.asList(pswNum));
                if (i % xh == 0) {
//					//System.out.println("i="+i);
//					input = new byte[content.size()];
//					for(int j=0;j<input.length;j++){
//						input[j] = content.get(j);
//					}
                    //input = bout.toByteArray();
                    //long ax = System.currentTimeMillis();
                    byte[] input = bout.toByteArray();
                    //long bx = System.currentTimeMillis();

                    sha.update(input);
                    //long cx = System.currentTimeMillis();
                    //System.out.println(cx-bx);
                    digest = sha.digest();
                    //AESInit[i / xh] = digest[digest.length-1];
                    AESInit[i / xh] = digest[19];
                }
            }
            //System.out.println(System.currentTimeMillis()-ax);
//			input = new byte[content.size()];
//			for(int j=0;j<input.length;j++){
//				input[j] = content.get(j);
//			}
            //input = bout.toByteArray();
            //sha.reset();
            sha.update(bout.toByteArray());
            digest = sha.digest();
            //System.out.println(System.currentTimeMillis()-ax);
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    AESKey[i * 4 + j] = (byte) (((digest[i * 4] * 0x1000000) & 0xff000000 | ((digest[i * 4 + 1] * 0x10000) & 0xff0000) | ((digest[i * 4 + 2] * 0x100) & 0xff00) | digest[i * 4 + 3] & 0xff) >> (j * 8));
            //AESKey[i * 4 + j] = (byte) (((digest[i*4]<<24)&0xff000000+(digest[i*4+1]<<16)&0xff0000+(digest[i*4+2]<<8)&0xff00+digest[i*4+3]&0xff) >> (j * 8));
            //System.out.println(System.currentTimeMillis()-ax);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put(IBlockCipher.KEY_MATERIAL, AESKey);
            map.put(IBlockCipher.CIPHER_BLOCK_SIZE, new Integer(16));
            rin.init(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        } while (n < len);
    }

    public void resetData() {
        this.data.clear();
    }

    @Override
    public int paddedSize() {
        return this.data.size();
    }
}
