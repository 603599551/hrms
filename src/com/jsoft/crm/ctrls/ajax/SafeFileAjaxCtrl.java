package com.jsoft.crm.ctrls.ajax;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;

import com.jfinal.KEY;
import com.jfinal.upload.UploadFile;
import easy.util.FileUploadPath;
import easy.util.UUIDTool;

/**
 * 上传文件
 */
public class SafeFileAjaxCtrl extends Controller {
	public void upload(){
		try {
			String shareLogoURL = "";
			FileUploadPath.Result fupResult = FileUploadPath.me().get();
			String saveDir = fupResult.getFile().getAbsolutePath();
			UploadFile uf = getFile("file", saveDir);
			String oldName = uf.getFile().getName();
			if (uf != null) {
				String newName = UUIDTool.getUUID() + oldName.substring(oldName.lastIndexOf("."));
				File shareLogFile = new File(uf.getFile().getParentFile(), newName);
				uf.getFile().renameTo(shareLogFile);
				System.out.println("上传文件路径："+shareLogFile.getAbsolutePath());
				shareLogoURL = fupResult.getUrl() + newName;
			}
			Map reMap=new HashMap();
			reMap.put("code",1);
			reMap.put("file_path",shareLogoURL);
			reMap.put("original_name",oldName);

			renderJson(reMap);
		}catch (Exception e){
			e.printStackTrace();
			Map reMap=new HashMap();
			reMap.put("code",-1);
			reMap.put("msg",e.toString());

			renderJson(reMap);
		}
	}

	
}
