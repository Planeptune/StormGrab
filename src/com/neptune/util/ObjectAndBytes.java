package com.neptune.util;

import java.io.*;

/**
 * Created by neptune on 16-10-11.
 * object与byte[]相互转换的工具
 */
public class ObjectAndBytes {
    public static byte[] objectToBytes(Object value) {
        try {
            ByteArrayOutputStream bop = new ByteArrayOutputStream();
            ObjectOutputStream op = new ObjectOutputStream(bop);
            op.writeObject(value);

            byte[] result = bop.toByteArray();
            bop.close();
            op.close();
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public static Object bytesToObject(byte[] value) {
        try {
            ByteArrayInputStream bip = new ByteArrayInputStream(value);
            ObjectInputStream ip = new ObjectInputStream(bip);

            Object result = ip.readObject();
            bip.close();
            ip.close();
            return result;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
