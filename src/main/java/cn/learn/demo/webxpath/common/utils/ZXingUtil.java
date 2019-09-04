package cn.learn.demo.webxpath.common.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jp.sourceforge.qrcode.util.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 解析QRcode二维码图片
 */
public class ZXingUtil {
    public static void encodeImg(String imgPath,String format,String content,int width,int height,String logo) throws Exception {
        Hashtable<EncodeHintType,Object > hints = new Hashtable<EncodeHintType,Object>() ;
        //排错率  L<M<Q<H
        hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) ;
        //编码
        hints.put( EncodeHintType.CHARACTER_SET, "utf-8") ;
        //外边距：margin
        hints.put( EncodeHintType.MARGIN, 1) ;
        /*
         * content : 需要加密的 文字
         * BarcodeFormat.QR_CODE:要解析的类型（二维码）
         * hints：加密涉及的一些参数：编码、排错率
         */
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,width,height, hints);
        //内存中的一张图片：此时需要的图片 是二维码-> 需要一个boolean[][] ->BitMatrix
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int x=0;x<width;x++) {
            for(int y=0;y<height;y++) {
                image.setRGB(x, y,     (bitMatrix.get(x,y)? Color.BLACK:Color.WHITE)  );
            }
        }
        //画logo
        image = DrawImageUtil.logoMatrix(image, logo) ;
        //string->file
        File file = new File(imgPath);
        //生成图片
        ImageIO.write(image, format, file);
    }

    //解密：二维码->文字
    public static void decodeImg(File file) throws Exception {
        if(!file.exists()) return ;
        //file->内存中的一张图片
        BufferedImage imge = ImageIO.read(file)  ;

        MultiFormatReader formatReader = new MultiFormatReader() ;
        LuminanceSource source = new BufferedImageLuminanceSource(imge);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        //图片 ->result
        Map map = new HashMap();
        map.put(EncodeHintType.CHARACTER_SET, "utf-8") ;
        Result result = formatReader.decode(binaryBitmap  ,map ) ;
        System.out.println("解析结果："+ result.toString());
    }



    public static void main(String[] args) throws Exception {
        String imgPath = "src/二维码12.png";
        String content = "helloworld你好";
        String logo = "src/logo.png";
        //加密：文字信息->二维码
        ZXingUtil.encodeImg(imgPath, "png",content, 430, 430,logo);
        //解密：二维码  ->文字信息
        ZXingUtil.decodeImg(new File(imgPath));
    }


}