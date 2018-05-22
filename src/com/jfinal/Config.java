package com.jfinal;

import com.common.controllers.DictionaryCtrl;
import com.common.controllers.SelectDataBuilderCtrl;
import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.tx.TxByMethodRegex;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.logistics.order.controllers.OutWarehouseOrderCtrl;
import com.logistics.order.controllers.StoreScrapCtrl;
import com.logistics.order.controllers.WarehouseStockMaterialTreeCtrl;
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
import com.store.order.controllers.*;
import com.store.print.PrintCtrl;
import com.warehouse.controllers.WarehouseManagerCtrl;
import com.warehouse.controllers.WarehouseMovementCtrl;
import com.warehouse.controllers.WarehouseStockCtrl;

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
		routes.add("/mgr/goodsAndGoodsTypeTree", GoodsAndGoodsTypeTreeCtrl.class);
		routes.add("/mgr/materialAndMaterialTypeTreeCtrl", MaterialAndMaterialTypeTreeCtrl.class);
		routes.add("/mgr/storeOrderCtrl", StoreOrderCtrl.class);

		routes.add("/mgr/dailySummary", DailySummaryCtrl.class);

		routes.add("/mgr/storeOrderManager", StoreOrderManagerCtrl.class);
		routes.add("/mgr/dict", DictionaryCtrl.class);
		routes.add("/mgr/logistics/storeOrder", com.logistics.order.controllers.StoreOrderCtrl.class);
		/*
		物流出库订单
		 */
		routes.add("/mgr/logistics/outWarehouseOrder", OutWarehouseOrderCtrl.class);
		routes.add("/mgr/common/store", com.common.controllers.StoreCtrl.class);
		routes.add("/mgr/warehouse/warehouseManager", WarehouseManagerCtrl.class);
		routes.add("/mgr/common/selectDataBuilder", SelectDataBuilderCtrl.class);
		/*
		门店订单
		 */
		//门店退货单
		routes.add("/mgr/store/returnGoods", ReturnGoodsCtrl.class);
		//门店废弃单
		routes.add("/mgr/store/storeScrapManager", StoreScrapManagerCtrl.class);
		//物流处理废弃单
		routes.add("/mgr/store/storeScrap", StoreScrapCtrl.class);
		//门店接收订单
		routes.add("/mgr/store/storeOrderReceiver", StoreOrderReceiverCtrl.class);

		//打印Ctrl
		routes.add("/mgr/print/print", PrintCtrl.class);
		routes.add("/mgr/warehouse/warehouseStockMaterialTree", WarehouseStockMaterialTreeCtrl.class);
		//门店盘点
		routes.add("/mgr/storeStock", StoreStockCtrl.class);
		//仓库库存
		routes.add("/mgr/warehouse/warehouseStock", WarehouseStockCtrl.class);
		routes.add("/mgr/warehouse/warehouseMovement", WarehouseMovementCtrl.class);

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
}
