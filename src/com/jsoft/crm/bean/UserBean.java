package com.jsoft.crm.bean;

import java.util.HashMap;

/**
 * @author mym
 * jstl只能从map对象中取值
 */
public class UserBean extends HashMap<String,Object>{
	private String id;
	private String name;
	private String email;
	private String realName;
	private String createTime;
	private String modifyTime;
	private int loginNum;
	private int status;
	private int level;
	private String qqOpenid;
	private String deptId;
	private String deptName;
	private String jobId;
	private String jobName;
	public String getQqOpenid() {
		return qqOpenid;
	}
	public void setQqOpenid(String qqOpenid) {
		this.qqOpenid = qqOpenid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		put("id", id);
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		put("email", email);
		this.email = email;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		put("status", status);
		this.status = status;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		put("level", level);
		this.level = level;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		put("realName", realName);
		this.realName = realName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		put("createTime", createTime);
		this.createTime = createTime;
	}
	public String getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(String modifyTime) {
		put("modifyTime", modifyTime);
		this.modifyTime = modifyTime;
	}
	public int getLoginNum() {
		return loginNum;
	}
	public void setLoginNum(int loginNum) {
		put("loginNum", loginNum);
		this.loginNum = loginNum;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		put("name", name);
		this.name = name;
	}
	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
}
