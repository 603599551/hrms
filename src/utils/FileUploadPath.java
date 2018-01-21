package utils;

import java.io.File;
import java.io.FileFilter;


/**
 * @author mym
 * 在web容器启动时需要调用init方法
 * 文件上传路径
 * 目录结构：
 * 		根目录：webroot/upload/
 * 		一级目录：webroot/upload/x/，x取值0-无限
 * 		二级目录：webroot/upload/x/y/，x取值0-无限，y取值0-(MAX_NUMBER-1)
 * 		三级目录：webroot/upload/x/y/z.jpg，x取值0-无限，y取值0-(MAX_NUMBER-1)，y文件夹下最多MAX_NUMBER个文件，z无要求，推荐32位uuid
 */
public class FileUploadPath {
	/**
	 * 根目录
	 */
	public static final String PATH_ROOT="upload";
	public static final int MAX_NUMBER=10240;
	
	
	/**
	 * 一级文件夹
	 */
	private File fold_level_1=null;
	/**
	 * 二级文件夹
	 */
	private File fold_level_2=null;
	/**
	 * 一级文件夹的总数
	 */
	private int count_fold_level_1=0;
	/**
	 * 一级文件夹名字最大的文件夹下，二级文件夹的总数
	 */
	private int count_fold_level_2=0;
	
	/**
	 * 二级文件夹名字最大的文件夹下，图片文件的总数
	 */
	private int count_image=0;
	private static FileUploadPath me;
	private FileUploadPath(){
		
	}
	public static FileUploadPath me(){
		if(me==null){
			me=new FileUploadPath();
		}
		return me;
	}
	/**
	 * 初始化，在serlet容器启动时调用。
	 * 获取webroot/qrcode/下的文件夹，判断出文件夹名字最大的，当该文件夹内的文件夹小于1024时，就继续使用，否则创建下一个文件夹
	 */
	public void init(){
		/*
		 * 获取所有的一级文件夹，如果不存在，那么创建一级文件夹和二级文件夹
		 * 如果存在，就获取一级文件夹中，文件名最大的那个文件夹。
		 * 然后获取该一级文件夹下的二级文件夹，获取二级文件夹中，文件名最大的那个文件夹。
		 */
		File[] fold_level_1_array=getRootPath().listFiles(new FolderFilter());
		if(fold_level_1_array==null || fold_level_1_array.length==0){
			fold_level_1=new File(getRootPath(),"0");
			fold_level_2=new File(fold_level_1,"0");
			fold_level_2.mkdirs();
			count_fold_level_1=1;
			count_fold_level_2=1;
			count_image=0;
		}else{
			count_fold_level_1=fold_level_1_array.length;
			fold_level_1=getMaxName(fold_level_1_array);
			if(fold_level_1==null){//如果一级目录文件夹的名称都是不符合要求的，创建一个是0的文件夹
				fold_level_1=new File(getRootPath(),"0");
				fold_level_2=new File(fold_level_1,"0");
				fold_level_2.mkdirs();
				count_fold_level_1=1;
				count_fold_level_2=1;
				count_image=0;
			}else{
				//获取该一级文件夹下的二级文件夹
				File[] fold_level_2_array=fold_level_1.listFiles(new FolderFilter());
				if(fold_level_2_array==null || fold_level_2_array.length==0){
					fold_level_2=new File(fold_level_1,"0");
					fold_level_2.mkdirs();
					count_fold_level_2=1;
				}else{
					count_fold_level_2=fold_level_2_array.length;
					fold_level_2=getMaxName(fold_level_2_array);
					if(fold_level_2.isDirectory()){
						count_image=fold_level_2.listFiles().length;
					}
				}
			}
		}
	}
	private File getMaxName(File[] array){
		/*
		 * 有时候文件夹名可能人为修改，无法转换为数字型，所以此处将所有文件夹名转换为数字型，放入数组中再找出最大的文件名
		 */
		File maxFile=null;
		int fileName=-1;
		for(int i=0;i<array.length;i++){
			File fold_level_1_file=array[i];
			String name=fold_level_1_file.getName();
			try{
				int nameInt=Integer.parseInt(name);
				if(fileName==-1){
					fileName=nameInt;
					maxFile=fold_level_1_file;
				}else{
					if(fileName<nameInt){
						fileName=nameInt;
						maxFile=fold_level_1_file;
					}
				}
//				folder_level_1_int_array[i]=nameInt;
			}catch(Exception e){
				
			}
		}
		return maxFile;
	}
	/**
	 * 获取根路径
	 * @return
	 */
	public static File getRootPath(){
		File file=PathTools.getWebRootPath(PATH_ROOT);
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}
	/**
	 * 传入图片名称名称，返回保存图片的全路径
	 * @param imageName
	 * @return
	 */
	public synchronized Result get(String imageName){
		/*
		 * 如果图片数量小于最大数，直接拼装路径并返回
		 * 如果图片数量大于等于最大数，如果二级目录小于最大数，创建新的二级目录，拼装路径并返回
		 * 						      如果二级目录大于等于最大数，创建新的一级目录、二级目录，拼装路径并返回
		 */
		count_image++;
		if(count_image<MAX_NUMBER){//如果图片数量小于最大数
//			return new File(fold_level_2,imageName);
		}else{//如果图片数量大于最大数
			count_fold_level_2++;
			if(count_fold_level_2<MAX_NUMBER){//
				count_image=1;
				fold_level_2=new File(fold_level_1,count_fold_level_2+"");
//				return new File(fold_level_2,imageName);
			}else{
				count_fold_level_1++;
				count_fold_level_2=1;
				count_image=1;
				fold_level_1=new File(getRootPath(),count_fold_level_1+"");
				fold_level_2=new File(fold_level_1,count_fold_level_2+"");
//				return new File(fold_level_2,imageName);
			}
		}
		
		File file=new File(fold_level_2,imageName);
		
		StringBuffer sb=new StringBuffer();
		sb.append("/");
		sb.append(getRootPath().getName());
		sb.append("/");
		sb.append(fold_level_1.getName());
		sb.append("/");
		sb.append(fold_level_2.getName());
		sb.append("/");
		sb.append(imageName);
		
		Result r=new Result();
		r.setFile(file);
		r.setUrl(sb.toString());
		return r;
	}
	public synchronized Result get(){
		/*
		 * 如果图片数量小于最大数，直接拼装路径并返回
		 * 如果图片数量大于等于最大数，如果二级目录小于最大数，创建新的二级目录，拼装路径并返回
		 * 						      如果二级目录大于等于最大数，创建新的一级目录、二级目录，拼装路径并返回
		 */
		count_image++;
		if(count_image<MAX_NUMBER){//如果图片数量小于最大数
//			return new File(fold_level_2,imageName);
		}else{//如果图片数量大于最大数
			count_fold_level_2++;
			if(count_fold_level_2<MAX_NUMBER){//
				count_image=1;
				fold_level_2=new File(fold_level_1,count_fold_level_2+"");
//				return new File(fold_level_2,imageName);
			}else{
				count_fold_level_1++;
				count_fold_level_2=1;
				count_image=1;
				fold_level_1=new File(getRootPath(),count_fold_level_1+"");
				fold_level_2=new File(fold_level_1,count_fold_level_2+"");
//				return new File(fold_level_2,imageName);
			}
		}
		
		File file=fold_level_2;
		
		StringBuffer sb=new StringBuffer();
		sb.append("/");
		sb.append(getRootPath().getName());
		sb.append("/");
		sb.append(fold_level_1.getName());
		sb.append("/");
		sb.append(fold_level_2.getName());
		sb.append("/");
		
		Result r=new Result();
		r.setFile(file);
		r.setUrl(sb.toString());
		return r;
	}
	public class Result{
		private File file;
		private String url;
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
	}
	class FolderFilter implements FileFilter {
		
		@Override
		public boolean accept(File pathname) {
			if(pathname.isDirectory()){
				return true;
			}
			return false;
		}
	}
}
