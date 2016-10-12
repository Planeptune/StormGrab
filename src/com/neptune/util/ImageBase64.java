package com.neptune.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by neptune on 16-9-13.
 * 使用Base64编解码图片的工具集
 */
@Deprecated
public class ImageBase64 {

    //图片编码
    public static String encodingImg(String filePath) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return encodingImg(in);
    }

    //图片编码
    public static String encodingImg(InputStream in) {
        byte[] data = null;
        try {
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    //图片解码
    public static boolean decodingImg(String codex, String savePath) {
        if (codex == null)
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] data = decoder.decodeBuffer(codex);
            int i;
            //调整异常数据
            for (i = 0; i < data.length; i++) {
                if (data[i] < 0)
                    data[i] += 256;
            }
            //生成图片
            /*OutputStream op = new FileOutputStream(savePath);
            op.write(data);
            op.flush();
            op.close();*/
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            BufferedImage img = ImageIO.read(in);
            ImageIO.write(img, "png", new File(savePath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //将编码解码为bufferedimage对象
    public static BufferedImage decoding(String codex) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] data = decoder.decodeBuffer(codex);
            //修正异常
            for (byte d : data) {
                if (d < 0)
                    d += 256;
            }
            InputStream in = new ByteArrayInputStream(data);
            return ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    //将bufferedimage编码
    public static String encoding(BufferedImage img) {
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ImageOutputStream io = ImageIO.createImageOutputStream(bao);
            ImageIO.write(img, "png", io);
            InputStream in = new ByteArrayInputStream(bao.toByteArray());
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();

            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(data);
        } catch (IOException e) {
            return null;
        }
    }
}
