package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
此类的操作都需要过滤是否登录
 */
public class SafeDbAjaxCtrl extends Controller {
    /*
    dbObj表示数据库中的表名
     */
    public void save(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb = (String)paraMap.get("dbObj");
        String id=UUIDTool.getUUID();
        String time= DateTool.GetDateTime();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        try {
            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                Object result=processValue(paraMap,en.getKey(),en.getValue());
                r.set(en.getKey(),result);
            }
            r.remove("dbObj");
            r.set("id",id);
            r.set("creator",creator);
            r.set("creator_name",creator_name);
            r.set("create_time",time);
            r.set("modify_time",time);
            Db.save(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }

    /**
     * 处理value部分
     */
    private Object processValue(Map paraMap,String key,String value){
        Object ret=null;
        String db_tb = (String)paraMap.get("dbObj");
        if("max(1)".equalsIgnoreCase(value)){//如果value是max+1
            Record r=Db.findFirst("select max("+key+") as max from "+db_tb);
            Object max=r.get("max");
            if(max==null) {
                ret = 1;
            }else{
                ret= NumberUtils.parseInt(max,0)+1;
            }
        }else{
            ret=value;
        }
        return ret;
    }
    public void updateById(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb = (String)paraMap.get("dbObj");
        String id=(String)paraMap.get("id");
        String time= DateTool.GetDateTime();
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        try {

            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                Object result=processValue(paraMap,en.getKey(),en.getValue());
                r.set(en.getKey(),result);
            }
            r.remove("dbObj");
//            r.set("create_time",time);
            r.set("modify_time",time);
            Db.update(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    /*
    id:由前台传入id
    dbObj表示数据库中的表名
     */
    public void getById(){
        String id=getPara("id");
        String db_tb = getPara("dbObj");
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        try{
            Record r=Db.findById(db_tb,id);
            if(r==null){
                Map map=new HashMap();
                map.put("code",KEY.CODE.ERROR);
                map.put("msg","无此记录！");
                renderJson(map);
                return;
            }else{
                r.remove("password");
            }
            renderJson(r.getColumns());
        }catch (Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",KEY.CODE.ERROR);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    /*
    id:由前台传入id
    dbObj表示数据库中的表名
     */
    public void deleteById(){
        Map map=new HashMap();

        String id=getPara("id");
        String db_tb = getPara("dbObj");
        if(db_tb==null || "".equals(db_tb)){
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        try{
            boolean b=Db.deleteById(db_tb,id);
            if(b){
                map.put("code",1);
            }else{
                map.put("code",0);
                map.put("msg","删除失败！");
            }
            renderJson(map);
        }catch (Exception e){
            e.printStackTrace();
            map.put("code",0);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    public void modifyPwd(){
        Map map=RequestTool.getParameterMap(getRequest());
        try{
            Map reMap=modifyPwdById(map);
            renderJson(reMap);
        }catch (Exception e){
            e.printStackTrace();
            Map reMap=new HashMap();
            reMap.put("code",-1);
            reMap.put("msg",e.toString());
            renderJson(reMap);
        }
    }
    public void modifyMyPwd(){
//        String db_tb = getPara("dbObj");
//        String oldPwd = getPara("oldPwd");
//        String newPwd = getPara("newPwd");
//        String newPwd2 = getPara("newPwd2");
        Map map=RequestTool.getParameterMap(getRequest());
        Map session=getSessionAttr(KEY.SESSION_ADMIN);
        String id=(String)session.get("id");
        map.put("id",id);
        try{
            Map reMap=modifyPwdById(map);
            renderJson(reMap);
        }catch (Exception e){
            e.printStackTrace();
            Map reMap=new HashMap();
            reMap.put("code",-1);
            reMap.put("msg",e.toString());
            renderJson(reMap);
        }
    }
    private Map modifyPwdById(Map<String,String> map) throws Exception{
            String db_tb = map.get("dbObj");
            String oldPwd = map.get("oldPwd");
            String newPwd = map.get("newPwd");
            String newPwd2 = map.get("newPwd2");
            String id=map.get("id");

            String time= DateTool.GetDateTime();

            Map reMap=new HashMap();
            if(oldPwd==null || "".equals(oldPwd)){
                reMap.put("code",0);
                reMap.put("msg","原密码不能为空！");
                return reMap;
            }
            if(newPwd==null || "".equals(newPwd)){
                reMap.put("code",0);
                reMap.put("msg","新密码不能为空！");
                return reMap;
            }
            if(newPwd2==null || "".equals(newPwd2)){
                reMap.put("code",0);
                reMap.put("msg","确认密码不能为空！");
                return reMap;
            }
            if(!newPwd.equals(newPwd2)){
                reMap.put("code",0);
                reMap.put("msg","新密码与确认密码不相同，请重新输入！");
                return reMap;
            }
            Record reDb=Db.findById(db_tb,id);
            String pwdDb=reDb.get("password");
            if(!pwdDb.equals(oldPwd)){
                reMap.put("code",0);
                reMap.put("msg","原密码不正确，请重新输入原密码！");
                return reMap;
            }


            Record r = new Record();
            r.set("id",id);
            r.set("password",newPwd);
//            r.set("create_time",time);
            r.set("modify_time",time);
            Db.update(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            reMap.put("code",1);
            reMap.put("data",ret);
            return reMap;
    }

}
