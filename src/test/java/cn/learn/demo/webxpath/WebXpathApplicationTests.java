package cn.learn.demo.webxpath;

import cn.learn.demo.webxpath.common.utils.QRCodeUtil;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;



import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import org.w3c.dom.Document;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@RunWith(SpringRunner.class)
@SpringBootTest
public class WebXpathApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Test
    public void TestQrCode() throws Exception {
        String imgPath = "e://microQr.png";
        //解密：  二维码 -> 文字信息
        String imgContent = qrCodeUtil.decoderQRCode(imgPath) ;
        System.out.println(imgContent);

    }

    @Test
    public void TestApkSign() throws Exception {
        String path = "e://qq.apk";
        byte[] bytes = getSignaturesFromApk(path);
        System.out.println("证书MD5:"+hexDigest(bytes));
        File file= new File(path);
        System.out.println("文件的md5: "  +md5(file));
        long apkSize= getFileSize(file);
        String ss2= readableFileSize(apkSize);
        System.out.println("文件大小: "+apkSize+"（字节）/"+ss2);
    }

    @Test
    public void TestAPK(){
        String filePath = "e://qq.apk";
        try(ApkFile apkFile = new ApkFile(new File(filePath))) {
            List<ApkSigner> signers = apkFile.getApkSingers(); // apk v1 signers
            List<ApkV2Signer> v2signers = apkFile.getApkV2Singers(); // apk v2 signers
            System.out.println(1111);
        } catch (IOException | CertificateException e) {
            e.printStackTrace();
        }

        try(ApkFile apkFile = new ApkFile(new File(filePath))) {
            DexClass[] classes = apkFile.getDexClasses();
            for (DexClass dexClass : classes) {
                System.out.println(dexClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ApkFile apkFile = new ApkFile(new File(filePath))) {
            apkFile.setPreferredLocale(Locale.SIMPLIFIED_CHINESE);
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println(1111);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ApkFile apkFile = new ApkFile(new File(filePath))) {
            String manifestXml = apkFile.getManifestXml();
            String xml = apkFile.transBinaryXml("res/menu/main.xml");
            System.out.println(1111);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用Xpath提取网站apk，并解析apk包相关信息
     */
    @Test
    public void TestXpath() {
       // String url = "https://sj.qq.com/myapp/detail.htm?apkName=com.tencent.mobileqq";
        String sampleHtml = "<div><table><td id='1234 foo 5678'>Hello</td>";
       // String sampleXpath = "//a[@class=\"det-down-btn\"]/@data-apkurl";
        //网站下载路径
        String url = "https://c2c.huobi.br.com/zh-cn/client/?type=otc";
        //xapth节点提取规则
        String sampleXpath = "//div[@class=\"app-bottom\"]/div[1]/a/@href";

        try {
            Connection connect = Jsoup.connect(url);
            //设置请求头信息，防止请求跳转不正确页面，用手机浏览器格式解析
            Map<String, String> header = new HashMap<String, String>();
            //header.put("Host", "c2c.huobi.br.com");
            header.put("User-Agent", " Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Mobile Safari/537.36");
            header.put("Accept", "  text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            header.put("Accept-Language", "zh-cn,zh;q=0.5");
            header.put("Accept-Charset", "  GB2312,utf-8;q=0.7,*;q=0.7");
            header.put("Connection", "keep-alive");
            //设置代理IP和端口,注意检查代理IP是否可用  网站 https://www.xicidaili.com/nt/
            sampleHtml = connect.headers(header).proxy("122.5.107.129",9999).timeout(5000).get().body().html();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apkurl = getValueByXpath(sampleXpath, sampleHtml);
        System.out.println(apkurl);
        //保存apk
        downLoadFromUrl(apkurl,"qq.apk","e://");

        ApkFile apkFile = null;
        String apkPath = "e://qq.apk";
        try {
            apkFile = new ApkFile(new File(apkPath));
            ApkMeta apkMeta = apkFile.getApkMeta();
            // System.out.println(apkMeta);
            System.out.println("应用名：" + apkMeta.getLabel());
            System.out.println("包名：" + apkMeta.getPackageName());
            System.out.println("版本：" + apkMeta.getVersionName());
            System.out.println("版本号：" + apkMeta.getVersionCode());
            // 保存所有icon
            for (IconFace iconFace : apkFile.getAllIcons()) {
                saveIcon("e://", iconFace);
            }
            // 保存主icon
             saveIcon("e://icon", apkFile.getIconFile());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // TODO close apkFile
        }
    }

    /**
     * Extract value by xPath from HTML.
     */
    private static String getValueByXpath(String xPath, String html) {
        TagNode tagNode = new HtmlCleaner().clean(html);
        String value = null;
        try {
            Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
            XPath xpath = XPathFactory.newInstance().newXPath();
            value = (String) xpath.evaluate(xPath, doc, XPathConstants.STRING);
        } catch (Exception e) {
            System.out.println("Extract value error. " + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static String downLoadFromUrl(String urlStr, String fileName, String savePath) {
        try {

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            // 防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            // 得到输入流
            InputStream inputStream = conn.getInputStream();
            // 获取自己数组
            byte[] getData = readInputStream(inputStream);

            // 文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir + File.separator + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            // System.out.println("info:"+url+" download success");
            return saveDir + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    /**
     * 保存apk的icon
     *
     * @param savePath 保存的目录
     * @param iconFace
     * @throws IOException
     */
    public static void saveIcon(String savePath, IconFace iconFace)
            throws IOException {
        String iconPath = iconFace.getPath();
        String iconName = iconPath.substring(iconPath.lastIndexOf("/") + 1);
        File file = new File(savePath + iconName);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(iconFace.getData());
        fos.close();
    }

    public static String hexDigest(byte[] bytes) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] md5Bytes = md5.digest(bytes);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 从APK中读取签名
     *
     * @param strFile
     * @return
     * @throws IOException
     */
    private static byte[] getSignaturesFromApk(String strFile) throws IOException {
        File file = new File(strFile);
        JarFile jarFile = new JarFile(file);
        try {
            JarEntry je = jarFile.getJarEntry("AndroidManifest.xml");
            byte[] readBuffer = new byte[8192];
            Certificate[] certs = loadCertificates(jarFile, je, readBuffer);
            if (certs != null) {
                for (Certificate c : certs) {
                    return c.getEncoded();
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 加载签名
     *
     * @param jarFile
     * @param je
     * @param readBuffer
     * @return
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
        }
        return null;
    }

    public static String md5(File file) {
        MessageDigest digest = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];

        try {
            if (!file.isFile()) {
                return "";
            }

            digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);

            while (true) {
                int len;
                if ((len = fis.read(buffer, 0, 1024)) == -1) {
                    fis.close();
                    break;
                }

                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        BigInteger var5 = new BigInteger(1, digest.digest());
        return String.format("%1$032x", new Object[]{var5});
    }

    /**
     * 获取指定文件大小
     * @param file
     * @return
     * @throws Exception
     */
    private long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
        }
        return size;
    }

    //字节转换
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + "/" + units[digitGroups];
    }


}
