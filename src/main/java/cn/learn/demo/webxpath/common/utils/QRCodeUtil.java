package cn.learn.demo.webxpath.common.utils;

import com.swetake.util.Qrcode;
import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class QRCodeUtil {
    /**
     *加密：  文字信息 ->二维码.png
     * @param content 文字信息
     * @param imgPath 二维码路径
     * @param imgType 二维码类型：png
     * @param size    二维码尺寸
     * @throws Exception
     */
    public void encoderQRCode(String content,String imgPath,String imgType,int size)  throws Exception{
        //BufferedImage ：内存中的一张图片
        BufferedImage bufImg =   qRcodeCommon(content,imgType,size);

        File file = new File(imgPath);// "src/二维码.png" --> 二维码.png

        //生成图片
        ImageIO.write(bufImg, imgType, file);
    }

    /**
     *产生一个二维码的BufferedImage
     * @param content 二维码隐藏信息
     * @param imgType 图片格式
     * @param size    图片大小
     * @return
     */
    private BufferedImage qRcodeCommon(String content, String imgType, int size) throws Exception {
        BufferedImage bufImg =null;
        //Qrcode对象：字符串->boolean[][]
        Qrcode qrCodeHandler = new Qrcode();
        //设置二维码的排错率：7% L<M<Q<H30%  ：排错率越高,可存储的信息越少；但是对二维码清晰对要求越小
        qrCodeHandler.setQrcodeErrorCorrect('M');
        //可存放的信息类型：N：数字、  A：数字+A-Z  B：所有
        qrCodeHandler.setQrcodeEncodeMode('B');
        //尺寸：取值范围：1-40
        qrCodeHandler.setQrcodeVersion(size);

        //"Hello world" -> byte[]"Hello world" -->boolean[][]
        byte[] contentBytes = content.getBytes("UTF-8");
        boolean[][] codeOut = qrCodeHandler.calQrcode(contentBytes);

        int imgSize =  67 + 12*(size -1) ;

        //BufferedImage：内存中的图片
        bufImg = new BufferedImage(imgSize,imgSize,BufferedImage.TYPE_INT_RGB );//red green blue

        //创建一个画板
        Graphics2D gs = bufImg.createGraphics();
        gs.setBackground(Color.WHITE);//将画板的背景色设置为白色
        gs.clearRect( 0,0, imgSize,imgSize); //初始化
        gs.setColor(Color.BLACK);//设置 画板上 图像的颜色（二维码的颜色）

        int pixoff = 2 ;

        for(int j=0;j<codeOut.length;j++) {
            for(int i=0;i<codeOut.length;i++) {
                if(codeOut[j][i]) {
                    gs.fillRect(j*3+pixoff , i*3+pixoff, 3, 3);
                }
            }
        }
        //增加LOGO
        //将硬盘中的src/logo.png  加载为一个Image对象
        Image logo =  ImageIO.read(new File("/src/logo.png")  ) ;
        int maxHeight = bufImg.getHeight();
        int maxWdith = bufImg.getWidth();

        //在已生成的二维码上 画logo
        gs.drawImage(logo,imgSize/5*2,imgSize/5*2, maxWdith/5,maxHeight/5 ,null) ;

        gs.dispose(); //释放空间
        bufImg.flush(); //清理
        return bufImg ;
    }

    //解密：  二维码(图片路径) -> 文字信息
    public String decoderQRCode(String imgPath) throws Exception{

        //BufferedImage内存中的图片  ：硬盘中的imgPath图片 ->内存BufferedImage
        BufferedImage bufImg =  ImageIO.read( new File(imgPath) ) ;
        //解密
        QRCodeDecoder decoder = new QRCodeDecoder() ;

        TwoDimensionCodeImage tdcImage = new TwoDimensionCodeImage(bufImg);
        byte[] bs = decoder.decode(tdcImage) ;    //bufImg
        //byte[] -->String
        String content    = new String(bs,"UTF-8");
        return content;
    }

    class TwoDimensionCodeImage implements QRCodeImage {
        BufferedImage bufImg;  //将图片加载到内存中
        public TwoDimensionCodeImage(BufferedImage bufImg) {
            this.bufImg = bufImg;
        }
        public int getWidth() {
            return bufImg.getWidth();
        }

        public int getHeight() {
            return bufImg.getHeight();
        }

        public int getPixel(int x, int y) {
            return bufImg.getRGB(x,y);
        }
    }

    public static void main(String[] args)  throws Exception{
        //生成二维码
        /*
         * 生成图片的路径        src/二维码.png
         * 文字信息、网址信息 ：  "helloworld"
         */
        String imgPath = "e:\\二维码2.png";
        String content =  "http://baidu.com";  //扫描二维码后，网页跳转

        //生成二维码
        /*
         * 加密：  文字信息 ->二维码
         * 解密：  二维码 -> 文字信息
         */
        QRCodeUtil qrUtil = new QRCodeUtil();
        //加密：  文字信息 ->二维码
        qrUtil.encoderQRCode(content, imgPath, "png", 17);

//           TwoDimensionCode handler = new TwoDimensionCode();
//           handler.encoderQRCode(content, imgPath, "png", 7);


//        //解密：  二维码 -> 文字信息
        String imgContent = qrUtil.decoderQRCode(imgPath) ;
        System.out.println(imgContent);


    }
}
