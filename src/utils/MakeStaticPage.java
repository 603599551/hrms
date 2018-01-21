package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MakeStaticPage {
	//String saveRoute = new File(PathTools.getWebRootPath(),"/html/"+aid+".html").getAbsolutePath(); 获取路径
	public static void makeHtml(String page, String filePath, String chartset) {//page 路径 url，filePath 保存路径，charset 字符集
		HttpURLConnection huc = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			huc = (HttpURLConnection) new URL(page).openConnection();
			huc.connect();
			InputStream stream = huc.getInputStream();
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath), chartset));
			br = new BufferedReader(new InputStreamReader(stream, chartset));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().length() > 0) {
					bw.write(line);
					bw.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw.close();
				huc.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}   

}
