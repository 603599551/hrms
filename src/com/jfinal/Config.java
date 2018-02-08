package com.jfinal;

import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.ss.controllers.HomeCtrl;
import com.ss.controllers.LoginCtrl;
import com.ss.controllers.MenuCtrl;
import com.ss.controllers.UserCtrl;
import com.ss.goods.controllers.*;
import com.ss.organization.controllers.DeptCtrl;
import com.ss.organization.controllers.JobCtrl;
import com.ss.organization.controllers.StaffCtrl;
import com.ss.organization.controllers.StoreCtrl;
import com.ss.stock.controllers.DailySummaryCtrl;

import java.io.File;

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
		routes.add("/mgr/menu",MenuCtrl.class);
		routes.add("/mgr/dept",DeptCtrl.class);
		routes.add("/mgr/staff",StaffCtrl.class);
		routes.add("/mgr/job",JobCtrl.class);
		routes.add("/mgr/store",StoreCtrl.class);
		routes.add("/mgr/goodsUnit",GoodsUnitCtrl.class);
		routes.add("/mgr/goodsType",GoodsTypeCtrl.class);
		routes.add("/mgr/materialType",MaterialTypeCtrl.class);

		routes.add("/mgr/goodsMaterial",GoodsMaterialCtrl.class);
		routes.add("/mgr/goods",GoodsCtrl.class);
		routes.add("/mgr/goodsInitForm", GoodsInitFormCtrl.class);
		routes.add("/mgr/material", MaterialCtrl.class);
		routes.add("/mgr/bomMgr", BomMgrCtrl.class);
		routes.add("/login", LoginCtrl.class);
		routes.add("/mgr/user", UserCtrl.class);

		routes.add("/mgr/dailySummary", DailySummaryCtrl.class);
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
		// 事务控制器
//		interceptors.add(new TxByActionKeyRegex("(.*save.*|.*update.*|.*del.*)",true));
	}

	@Override
	public void configHandler(Handlers handlers) {

	}
}
