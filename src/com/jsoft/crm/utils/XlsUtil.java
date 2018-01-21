package com.jsoft.crm.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;

import easy.util.DateTool;
import easy.util.UUIDTool;

public class XlsUtil {

	private static String[] columnArr = {"name", "gender", "phone", "sfzh", "status_name", "source_name", "qq", "email", "school_name", "speciality_name", "class", "room_num", "school_year"};
	private static String[] columnName = {"姓名", "性别", "电话", "身份证号", "状态", "来源", "QQ", "电邮", "学校", "专业", "班级", "寝室号", "入学年份"};
	private static boolean[] validateColumnFlag = {true, false, true, false, true, false, false, false, true, false, false, false, true};

	public static Map<String, Object> parseXls(String dir, String name,  UserBean userBean) {
		String str = dir + name;
		File file = new File(str);
		return parseXls(file, userBean, dir, name);
	}

	public static Map<String, Object> parseXls(File xls, UserBean userBean, String dir, String name) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(xls);
			return parseXls(fis, userBean, dir, name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(File.separator);
	}

	public static Map<String, Object> parseXls(FileInputStream fis, UserBean userBean, String dir, String name) {
		Map<String, Object> result = new HashMap<>();
		Map<String, String> statusMap = _query("dictionary", "7");
		Map<String, String> sourceMap = _query("dictionary", "12");
		Map<String, String> schoolMap = _query("dictionary", "24");
		Map<String, String> specialityMap = _query("dictionary", "25");
		List<Record> studentList = new ArrayList<>();
		List<Record> studentOwnerList = new ArrayList<>();
		List<Record> studentRepetitionList = new ArrayList<>();
		List<CellMsg> cellMsgList = new ArrayList<>();

		Record student;
		try {
			Workbook wb = new HSSFWorkbook(fis);
			Sheet sheet = wb.getSheetAt(0);
			Row row = sheet.getRow(0);
			int rowNum = sheet.getLastRowNum();
			int colNum = row.getPhysicalNumberOfCells();
			Set<String> phoneSet = new HashSet<>();
			for (int i = 2; i <= rowNum; i++) {
				boolean[] isBlankFlagArr = new boolean[colNum];
				student = new Record();
				CellMsg cellMsg = new CellMsg(i);
				boolean isVilidateSuccess = true;
				row = sheet.getRow(i);
				for(int j = 0; j < colNum; j++){
					Object obj = getCellFormatValue(row.getCell(j));
					if((obj != null && obj.toString().length() > 0 && !"null".equalsIgnoreCase(obj.toString()))){
						student.set(columnArr[j], obj);
					}else{
						isBlankFlagArr[j] = true;
						if(validateColumnFlag[j]){
							cellMsg.set(j);
							isVilidateSuccess = false;
						}else{
							student.set(columnArr[j], obj);
						}
					}
				}

				boolean continueFlag = true;

				for(boolean bl : isBlankFlagArr){
					continueFlag = continueFlag && bl;
				}
				if(continueFlag){
					continue;
				}

				String gender = student.get("gender");
				if("男".equals(gender)){
					student.set("gender", "1");
				}else if("女".equals(gender)){
					student.set("gender", "0");
				}else{
					student.set("gender", null);
				}
				String uuid= UUIDTool.getUUID();
				String pinyin = HanyuPinyinHelper.getFirstLettersLo(student.get("name") + "");

				String status = statusMap.get(student.get("status_name") + "");
				String source = sourceMap.get(student.get("source_name") + "");
				String school = schoolMap.get(student.get("school_name") + "");
				String speciality = specialityMap.get(student.get("speciality_name") + "");

				if(status == null || status.length() <= 0){
					String status_name = student.getStr("status_name");
					if(status_name != null && status_name.length() > 0){
						cellMsg.set(4);
						isVilidateSuccess = false;
					}
				}
				if(source == null || source.length() <= 0){
					String source_name = student.getStr("source_name");
					if(source_name != null && source_name.length() > 0){
						cellMsg.set(5);
						isVilidateSuccess = false;
					}
				}
				if(school == null || school.length() <= 0){
					String school_name = student.getStr("school_name");
					if(school_name != null && school_name.length() > 0){
						cellMsg.set(8);
						isVilidateSuccess = false;
					}
				}
				if(speciality == null || speciality.length() <= 0){
					String speciality_name = student.getStr("speciality_name");
					if(speciality_name != null && speciality_name.length() > 0){
						cellMsg.set(9);
						isVilidateSuccess = false;
					}
				}

				String time= DateTool.GetDateTime();
				student.set("id", uuid);
				student.set("pinyin", pinyin);
				student.set("status", status);
				student.set("source", source);
				student.set("school", school);
				student.set("speciality", speciality);
				student.set("deleted", 1);
				student.set("create_time", time);
				student.set("modify_time", time);

				String creator_id = userBean.getId();
				String creator_name = userBean.getName();
				Record studentOwner = new Record();
				studentOwner.set("id", UUIDTool.getUUID());
				studentOwner.set("student_id", uuid);
				studentOwner.set("staff_id", creator_id);
				studentOwner.set("follow", 0);
				studentOwner.set("creator", creator_id);
				studentOwner.set("creator_name", creator_name);
				studentOwner.set("create_time", time);

				if(!isVilidateSuccess){
					cellMsgList.add(cellMsg);
				}else{
					List<Record> rList = Db.find("select * from student where phone=? and name=?", student.get("phone"), student.get("name"));
					int phoneSetLength = phoneSet.size();
					phoneSet.add(student.getStr("phone"));
					if(rList != null && rList.size() > 0){
						student.set("errorMsg", "第" + (i + 1) + "行记录重复！");
						studentRepetitionList.add(student);
					}else{
						if(phoneSetLength != phoneSet.size()){
							studentList.add(student);
							studentOwnerList.add(studentOwner);
						}else{
							student.set("errorMsg", "第" + (i + 1) + "行记录重复！");
							studentRepetitionList.add(student);
						}
					}
				}
			}
			String errorFilePath = createMsgXls(cellMsgList, dir, name);
			result.put("studentList", studentList);
			result.put("studentOwnerList", studentOwnerList);
			result.put("studentRepetitionList", studentRepetitionList);
			result.put("cellMsgList", cellMsgList);
			result.put("errorFilePath", errorFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static Map<String, String> _query(String dbObj, String parent_id) {
		Map<String, String> result = new HashMap<>();
		StringBuilder where = new StringBuilder("select * from " + dbObj + " where 1=1 ");
		List<String> paraList = new ArrayList<>();
		if (StringUtils.isNotEmpty(parent_id)) {
			where.append(" and parent_id=?");
			paraList.add(parent_id);
		}
		where.append("order by sort");
		List<Record> list = Db.find(where.toString(), paraList.toArray());
		if(list != null && list.size() > 0){
			for(Record r : list){
				result.put(r.getStr("name"), r.getStr("id"));
			}
		}
		return result;
	}

	public static String createMsgXls(List<CellMsg> cellMsgList, String path, String name) throws IOException{
		File xls = new File(path + name);
		String pathNew = path + "error.xls";
		File xlsNew = new File(pathNew);

		FileInputStream fisXls = new FileInputStream(xls);
		FileInputStream fisNew = new FileInputStream(xls);
		FileOutputStream fos = new FileOutputStream(xlsNew);

		HSSFWorkbook wbXls = new HSSFWorkbook(fisXls);
		HSSFWorkbook wbNew = new HSSFWorkbook(fisNew);

		clearWorkbook(wbNew);

		HSSFSheet sheetXls = wbXls.getSheetAt(0);
		HSSFSheet sheetNew = wbNew.getSheetAt(0);

		HSSFCellStyle style = wbNew.createCellStyle();
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(IndexedColors.RED.getIndex());

		int newRowNum = 2;
		for(int i = 0; i < cellMsgList.size(); i++){
			CellMsg cmsg = cellMsgList.get(i);
			HSSFRow xlsRow = sheetXls.getRow(cmsg.row);
			HSSFRow newRow = sheetNew.createRow(newRowNum);

			POIUtils.copyRow(wbNew, xlsRow, newRow, true);
			newRowNum++;
			Set<Integer> columnSet = cmsg.columnSet;
			for(int columnIndex : columnSet){
				Cell cell = newRow.getCell(columnIndex);
				if(cell != null){
					cell.setCellStyle(style);
				}else{
					cell = newRow.createCell(columnIndex);
					cell.setCellStyle(style);
				}
			}
		}

		wbNew.write(fos);
		fos.close();
		fisXls.close();
		fisNew.close();
		return pathNew;
	}

	private static void clearWorkbook(HSSFWorkbook wb){
		Sheet sheet = wb.getSheetAt(0);
		int rowNum = sheet.getLastRowNum();
		for(int r = rowNum; r > 1; r--){
			Row row = sheet.getRow(r);
			sheet.removeRow(row);
		}
	}

	public static void export(List<Record> studentList, OutputStream out) {
		// 声明一个工作薄
		HSSFWorkbook wb = new HSSFWorkbook();
		// 声明一个单子并命名
		HSSFSheet sheet = wb.createSheet("学生表");
		// 给单子名称一个长度
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式

//		CellStyle style = wb.createCellStyle();
//		style.setFillForegroundColor(IndexedColors.RED.getIndex());
//		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		HSSFCellStyle style = wb.createCellStyle();
		style.setFillBackgroundColor(HSSFColor.RED.index);

		// 创建第一行（也可以称为表头）
		HSSFRow row = sheet.createRow(0);
		// 样式字体居中
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 给表头第一行一次创建单元格
		HSSFCell cell = null;
		String[] titleNameArr = {"姓名", "性别", "电话", "身份证号", "学员状态", "来源", "QQ", "电邮", "学校", "专业", "班级号", "寝室号", "入学年份"};
		for(int i = 0; i < titleNameArr.length; i++){
			cell = row.createCell(i);
			cell.setCellValue(titleNameArr[i]);
			cell.setCellStyle(style);
		}

		// 向单元格里填充数据
		for (short i = 0; i < studentList.size(); i++) {
			Record student = studentList.get(i);
			row = sheet.createRow(i + 1);
			row.createCell(0).setCellValue(student.get("name") + "");
			row.createCell(1).setCellValue(student.get("gender") + "");
			row.createCell(2).setCellValue(student.get("phone") + "");
			row.createCell(3).setCellValue(student.get("sfzh") + "");
			row.createCell(4).setCellValue(student.get("status_name") + "");
			row.createCell(5).setCellValue(student.get("source_name") + "");
			row.createCell(6).setCellValue(student.get("qq") + "");
			row.createCell(7).setCellValue(student.get("email") + "");
			row.createCell(8).setCellValue(student.get("school_name") + "");
			row.createCell(9).setCellValue(student.get("speciality_name") + "");
			row.createCell(10).setCellValue(student.get("class") + "");
			row.createCell(11).setCellValue(student.get("room_num") + "");
			row.createCell(12).setCellValue(student.get("school_year") + "");
		}

		try {
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * 根据Cell类型设置数据
	 *
	 * @param cell
	 * @return
	 * @author zengwendong
	 */
	private static Object getCellFormatValue(Cell cell) {
		Object cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC
				case Cell.CELL_TYPE_FORMULA: {
					// 判断当前的cell是否为Date
					if (DateUtil.isCellDateFormatted(cell)) {
						// 如果是Date类型则，转化为Data格式
						// data格式是带时分秒的：2013-7-10 0:00:00
						// cellvalue = cell.getDateCellValue().toLocaleString();
						// data格式是不带带时分秒的：2013-7-10
						Date date = cell.getDateCellValue();
						cellvalue = date;
					} else {// 如果是纯数字

						// 取得当前Cell的数值
						cellvalue = String.valueOf(cell.getNumericCellValue());
					}
					break;
				}
				case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
					// 取得当前的Cell字符串
					cellvalue = cell.getRichStringCellValue().getString();
					break;
				default:// 默认的Cell值
					cellvalue = "";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;
	}

	static class CellMsg{
		int row;
		Set<Integer> columnSet = new HashSet<>();
		public CellMsg(int row){
			this.row = row;
		}
		public void set(int columnIndex){
			columnSet.add(columnIndex);
		}
	}

}
