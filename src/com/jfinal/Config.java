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
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.TxByMethodRegex;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.jfinal.weixin.demo.WeixinApiController;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import easy.util.FileUploadPath;
import org.apache.commons.lang.StringUtils;
import paiban.controllers.*;
import utils.DictionaryConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class Config extends JFinalConfig {

	/*微信配置*/
	// 本地开发模式
	private boolean isLocalDev = false;
	public static String DOMAIN="";
	//商户相关资料
	public static String appid = "";
	public static String partner = "";
	public static String paternerKey = "";

	/**
	 * 如果生产环境配置文件存在，则优先加载该配置，否则加载开发环境配置文件
	 * @param pro 生产环境配置文件
	 * @param dev 开发环境配置文件
	 */
	public void loadProp(String pro, String dev) {
		try {
			PropKit.use(pro);
		}
		catch (Exception e) {
			PropKit.use(dev);
			isLocalDev = true;
		}
	}


	public static boolean devMode=false;
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

		/*微信配置*/
		loadProp("a_little_config_pro.txt", "a_little_config.txt");
		// ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
		ApiConfigKit.setDevMode(constants.getDevMode());
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
		routes.add("/mgr/question/type", com.hr.question.controllers.TypeCtrl.class);
		routes.add("/mgr/notice", com.hr.notice.controllers.NoticeCtrl.class);

		//测试排班
		routes.add("/mgr/storeForecastTurnoverCtrl", StoreForecastTurnoverCtrl.class);
		routes.add("/mgr/schedulingCtrl", SchedulingCtrl.class);
		routes.add("/mgr/variableTimeGuideCtrl", VariableTimeGuideCtrl.class);
		routes.add("/mgr/staffIdleTimeCtrl", StaffIdleTimeCtrl.class);
		routes.add("/mgr/areaCtrl", AreaCtrl.class);
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

		//微信小程序
		routes.add("/wx/staff", com.hr.wxapplet.staff.controller.StaffCtrl.class);
		routes.add("/wx/manager", com.hr.wxapplet.manager.controller.ManageCtrl.class);
		routes.add("/wx/common", com.hr.wxapplet.common.CommonCtrl.class);
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
		loadDictionary();
	}

	/**
	 * 初始化微信
	 */
	public void wxInit(){

		DOMAIN=PropKit.get("domain");
		appid=PropKit.get("appId");
		partner=PropKit.get("mch_id");
		paternerKey=PropKit.get("paternerKey");

		if(StringUtils.isBlank(DOMAIN)){
			System.out.println("请在配置文件中设置domain！");
		}
		if(StringUtils.isBlank(appid)){
			System.out.println("请在配置文件中设置appid！");
		}
		if(StringUtils.isBlank(partner)){
			System.out.println("请在配置文件中设置partner！");
		}
		if(StringUtils.isBlank(paternerKey)){
			System.out.println("请在配置文件中设置paternerKey！");
		}

		// 1.5 之后支持redis存储access_token、js_ticket，需要先启动RedisPlugin
//        ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache());
		// 1.6新增的2种初始化
//        ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache(Redis.use("weixin")));
//        ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache("weixin"));

		ApiConfig ac = new ApiConfig();
		// 配置微信 API 相关参数
		ac.setToken(PropKit.get("token"));
		ac.setAppId(PropKit.get("appId"));
		ac.setAppSecret(PropKit.get("appSecret"));



		/**
		 *  是否对消息进行加密，对应于微信平台的消息加解密方式：
		 *  1：true进行加密且必须配置 encodingAesKey
		 *  2：false采用明文模式，同时也支持混合模式
		 */
		ac.setEncryptMessage(PropKit.getBoolean("encryptMessage", false));
		ac.setEncodingAesKey(PropKit.get("encodingAesKey", "setting it in config file"));

		/**
		 * 多个公众号时，重复调用ApiConfigKit.putApiConfig(ac)依次添加即可，第一个添加的是默认。
		 */
		ApiConfigKit.putApiConfig(ac);

		/**
		 * 1.9 新增LocalTestTokenCache用于本地和线上同时使用一套appId时避免本地将线上AccessToken冲掉
		 *
		 * 设计初衷：https://www.oschina.net/question/2702126_2237352
		 *
		 * 注意：
		 * 1. 上线时应保证此处isLocalDev为false，或者注释掉该不分代码！
		 *
		 * 2. 为了安全起见，此处可以自己添加密钥之类的参数，例如：
		 * http://localhost/weixin/api/getToken?secret=xxxx
		 * 然后在WeixinApiController#getToken()方法中判断secret
		 *
		 * @see WeixinApiController#getToken()
		 */
//        if (isLocalDev) {
//            String onLineTokenUrl = "http://localhost/weixin/api/getToken";
//            ApiConfigKit.setAccessTokenCache(new LocalTestTokenCache(onLineTokenUrl));
//        }
        /*
        微信小程序
         */
//        WxaConfig wc = new WxaConfig();
//        wc.setAppId("wx4f53594f9a6b3dcb");
//        wc.setAppSecret("eec6482ba3804df05bd10895bace0579");
//        WxaConfigKit.setWxaConfig(wc);

//        Timer timer=new Timer();
//        TimerTask tt=new TimerTask() {
//            @Override
//            public void run() {
//                AccessTokenApi.
//            }
//        };
	}

	private void loadDictionary(){
		List<Record> dictList = Db.find("select * from h_dictionary order by sort");
		Map<String, List<Record>> dictMap = new HashMap<>();
		Map<String, String> dictIdValueMap = new HashMap<>();
		if(dictList != null && dictList.size() > 0){
			for(Record r : dictList){
				List<Record> list = dictMap.computeIfAbsent(r.getStr("parent_id"), k -> new ArrayList<>());
				list.add(r);
				dictIdValueMap.put(r.getStr("id"), r.getStr("value"));
			}
		}
		if(dictMap != null && dictMap.size() > 0){
		    for(String key : dictMap.keySet()){
		        if("0".equals(key)){
		            continue;
                }
		        List<Record> list = dictMap.get(key);
		        String dict_key = dictIdValueMap.get(key);
		        Map<String, String> stringMap = DictionaryConstants.DICT_STRING_MAP.computeIfAbsent(dict_key, k -> new HashMap<>());
		        Map<String, Record> recordMap = DictionaryConstants.DICT_RECORD_MAP.computeIfAbsent(dict_key, k -> new HashMap<>());
		        if(list != null && list.size() > 0){
		            for(Record r : list){
		                stringMap.put(r.getStr("value"), r.getStr("name"));
                        recordMap.put(r.getStr("value"), r);
                    }
                }
            }
        }
	}
}
