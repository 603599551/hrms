package com.jfinal;

import com.common.controllers.DictionaryCtrl;
import com.common.controllers.JobCtrl;
import com.hr.controllers.HomeCtrl;
import com.hr.controllers.LoginCtrl;
import com.hr.controllers.MenuCtrl;
import com.hr.controllers.UserCtrl;
import com.hr.hr.controllers.HrCtrl;
import com.hr.hr.controllers.PerformanceCtrl;
import com.hr.mobile.Idletime.controllers.IdletimeCtrl;
import com.hr.mobile.addresslist.AddressListCtrl;
import com.hr.mobile.leave.controllers.LeaveCtrl;
import com.hr.question.controllers.ExamCtrl;
import com.hr.question.controllers.QuestionCtrl;
import com.hr.staff.controllers.StaffCtrl;
import com.hr.staff.controllers.StaffNotOnJobCtrl;
import com.hr.store.controllers.*;
import com.hr.train.controllers.ArticleCtrl;
import com.hr.train.controllers.TypeCtrl;
import com.hr.workTime.controllers.WorkTimeCtrl;
import com.hr.workTime.controllers.WorkTimeDetailCtrl;
import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.tx.TxByMethodRegex;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import easy.util.FileUploadPath;
import paiban.controllers.SchedulingCtrl;
import paiban.controllers.StaffIdleTimeCtrl;
import paiban.controllers.StoreForecastTurnoverCtrl;
import paiban.controllers.VariableTimeGuideCtrl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config extends JFinalConfig {

	public static boolean devMode=false;
	/**
	 *
	 */
	public static File web_inf_path=null;
	@Override
	public void configConstant(Constants constants) {
		String path=Thread.currentThread().getContextClassLoader().getResource("/").getPath();
		web_inf_path=new File(path).getParentFile();

		loadPropertyFile("config.txt");
		devMode=getPropertyToBoolean("devMode", false);
		constants.setDevMode(devMode);
		constants.setEncoding("utf-8");
		constants.setViewType(ViewType.JSP);
//		arg0.setError404View("/white.jsp");
//		arg0.setError500View("/500.jsp");
	}

	@Override
	public void configRoute(Routes routes) {
		routes.add("/",HomeCtrl.class);
		routes.add("/login", LoginCtrl.class);
		routes.add("/mgr/menu",MenuCtrl.class);
		routes.add("/mgr/user", UserCtrl.class);
		routes.add("/mgr/job", JobCtrl.class);

		routes.add("/mgr/dict", DictionaryCtrl.class);

		routes.add("/mgr/store", StoreCtrl.class);
		routes.add("/mgr/hr", HrCtrl.class);
		routes.add("/mgr/staff", StaffCtrl.class);
		routes.add("/mgr/staffNotOnJob", StaffNotOnJobCtrl.class);
		routes.add("/mgr/storeMgr", StoreMgrCtrl.class);
		routes.add("/mgr/moveOut", MoveOutCtrl.class);
		routes.add("/mgr/moveIn", MoveInCtrl.class);
		routes.add("/mgr/apply", ApplyCtrl.class);
		routes.add("/mgr/performance", PerformanceCtrl.class);

		//培训
		routes.add("/mgr/train/type", TypeCtrl.class);
		routes.add("/mgr/train/article", ArticleCtrl.class);

		routes.add("/mgr/question", QuestionCtrl.class);
		routes.add("/mgr/exam", ExamCtrl.class);

		routes.add("/mgr/NoticeCtrl", com.hr.notice.controllers.NoticeCtrl.class);

		//测试排班
		routes.add("/mgr/storeForecastTurnoverCtrl", StoreForecastTurnoverCtrl.class);
		routes.add("/mgr/schedulingCtrl", SchedulingCtrl.class);
		routes.add("/mgr/variableTimeGuideCtrl", VariableTimeGuideCtrl.class);
		routes.add("/mgr/staffIdleTimeCtrl", StaffIdleTimeCtrl.class);
		//工资统计
		routes.add("/mgr/workTimeCtrl", WorkTimeCtrl.class);
		//工资详情
		routes.add("/mgr/workTimeDetailCtrl", WorkTimeDetailCtrl.class);

		//通讯录
		routes.add("/mgr/mobile/addresslist", AddressListCtrl.class);

		//员工端消息回显
		routes.add("/mgr/mobile/notice", com.hr.mobile.notice.controllers.NoticeCtrl.class);
        //app端
        routes.add("/mgr/mobile/leave", LeaveCtrl.class);

        //测试mobile
        routes.add("/mgr/mobile/scheduling",com.hr.mobile.scheduling.controllers.SchedulingCtrl.class);
        routes.add("/mobile/login", com.hr.mobile.LoginCtrl.class);
        routes.add("/mgr/mobile/Idletime",IdletimeCtrl.class);
        //经理端离职审核
        routes.add("/mgr/mobile/resign", com.hr.mobile.resign.controllers.ResignCtrl.class);
        //员工离职申请
        routes.add("/mgr/mobile/setting", com.hr.mobile.setting.controllers.SettingCtrl.class);
		//店长端排班情况配置
		routes.add("/mgr/mobile/storeMgr/scheduling", com.hr.mobile.storeMgr.controllers.SchedulingCtrl.class);
        routes.add("mgr/mobile/sign",com.hr.mobile.sign.controllers.SignCtrl.class);
		//员工端工资
		routes.add("/mgr/mobile/salary", com.hr.mobile.salary.controllers.SalaryCtrl.class);
		//经理端处理签到信息
		routes.add("/mgr/mobile/storeMgr/sign",com.hr.mobile.storeMgr.controllers.SignCtrl.class);
		//经理端考核
		routes.add("/mgr/mobile/examine", com.hr.mobile.examine.ExamineCtrl.class);
		//员工端培训
		routes.add("mgr/mobile/train", com.hr.mobile.train.controllers.TrainCtrl.class);

	}

	@Override
	public void configEngine(Engine engine) {

	}

	@Override
	public void configPlugin(Plugins plugins) {
		String databaseURL=getProperty("jdbcUrl");
		String databaseUser=getProperty("username");
		String databasePassword=getProperty("password").trim();

		Integer initialPoolSize = getPropertyToInt("initialPoolSize");
		Integer minIdle = getPropertyToInt("minIdle");
		Integer maxActivee = getPropertyToInt("maxActivee");

		DruidPlugin druidPlugin = new DruidPlugin(databaseURL,databaseUser,databasePassword);
		druidPlugin.set(initialPoolSize,minIdle,maxActivee);
		druidPlugin.setFilters("stat,wall");
		plugins.add(druidPlugin);

		//实体映射
		ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
//        activeRecordPlugin.addMapping("news","id", News.class);
		plugins.add(activeRecordPlugin);

		// ehcache缓存插件
		plugins.add(new EhCachePlugin());
	}

	@Override
	public void configInterceptor(Interceptors interceptors) {
		// 给service增加事务控制，过滤方法名为save*，update*，delete*
		interceptors.addGlobalServiceInterceptor(new TxByMethodRegex("(save.*|update.*|delete.*)"));
	}

	@Override
	public void configHandler(Handlers handlers) {

	}
	@Override
	public void afterJFinalStart() {
		// TODO Auto-generated method stub
		super.afterJFinalStart();
		FileUploadPath.me().init();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sDate = sdf.format(new Date());
		System.out.println("当前时间："+sDate);

	}
}
