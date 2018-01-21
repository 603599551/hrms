package utils.qrcode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import com.swetake.util.Qrcode;

public class QRCodeBuilder2 {
	public static final char ERROR_CORRECT_7 = 'L';
	public static final char ERROR_CORRECT_15 = 'M';
	public static final char ERROR_CORRECT_25 = 'Q';
	public static final char ERROR_CORRECT_30 = 'H';

	private boolean[][] s = null;
	/**
	 * @param text 文本
	 * @param errorCorrect 容错率
	 * @throws UnsupportedEncodingException
	 */
	public void newQrcode(String text,char errorCorrect) throws UnsupportedEncodingException{
		byte[] textByteArray = text.getBytes("utf-8");
		
		Qrcode qrcode = new Qrcode();
		// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
		qrcode.setQrcodeErrorCorrect(errorCorrect);
		qrcode.setQrcodeEncodeMode('B');
		

		for (int qrcodeVersion = 1; qrcodeVersion <= 40; qrcodeVersion++) {
			/*
			 * 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大
			 * 这个版本号代表你生成的二维码的像素的大小。版本1是21*21的，版本号每增加1，边长增加4。 也就是说版本7的大小是45 *
			 * 45的。版本号最大值是40（177x177）。另外，版本7的编码的字节数如果超过了119，那么将无法编码
			 */
			qrcode.setQrcodeVersion(qrcodeVersion);
			try {
				//如果信息量超过当前二维码版本所能存储的信息，会报错，重复调用，直到不报错为止
				s = qrcode.calQrcode(textByteArray);//会打印overflow，原因不明
				System.out.println("qrcodeVersion=" + qrcodeVersion);
				break;
			} catch (Exception e) {
				continue;
			}
		}
	}
	public void saveImage(int imageWidth,File imageFile) throws IOException{
		saveImage(imageWidth,Color.BLACK, Color.WHITE, imageFile);
	}
	public void saveImage(int imageWidth,Color color,Color background,File imageFile) throws IOException{
		int blockSize = imageWidth / s.length;// 计算每个块的大小
		/*
		 * 作用1：设置偏移量，不设置可能导致解析出错。
		 * 作用2：让二维码居中显示。当上面的blockSize不是整除时，会多出空白，经过下面计算让二维码居中显示
		 */
		int pixoff = (imageWidth - s.length * blockSize) / 2;

		BufferedImage bi = new BufferedImage(imageWidth, imageWidth,
				BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = bi.createGraphics();
		g.setBackground(background);
		g.clearRect(0, 0, imageWidth, imageWidth);
		g.setColor(color);
		
		for (int i = 0; i < s.length; i++) {
			for (int j = 0; j < s.length; j++) {
				if (s[j][i]) {
					g.fillRect(j * blockSize + pixoff, i * blockSize
							+ pixoff, blockSize, blockSize);
				}
			}
		}
		g.dispose();
		bi.flush();
		if (!imageFile.exists())
			imageFile.createNewFile();
		ImageIO.write(bi, "jpg", imageFile);
	}
	public static void buildImage(String text, int imageWidth,File imageFile)throws Exception {
		buildImage(text, ERROR_CORRECT_15, imageWidth, imageFile);
	}
	public static void buildImage(String text, char errorCorrect, int imageWidth,File imageFile)
			throws Exception {
		try {
			QRCodeBuilder2 qrb=new QRCodeBuilder2();
			qrb.newQrcode(text, errorCorrect);
			qrb.saveImage(imageWidth, imageFile);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		String string = "http://www.wx25.net/html/0/0/ccac454b9ce641849e62c3bf5aa52d5f.html";
		QRCodeBuilder2.buildImage(string, 200,new File("f:\\\\a.jpg"));
		long end = System.currentTimeMillis();
		long last = end - start;
		System.out.println("time consume:" + last);
	}
}
