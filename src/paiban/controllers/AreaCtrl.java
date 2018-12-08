package paiban.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import paiban.service.AreaService;
import utils.DictionaryConstants;
import utils.bean.JsonHashMap;

import java.util.*;

public class AreaCtrl extends BaseCtrl{

    private AreaService service = enhance(AreaService.class);

    public void list(){
        JsonHashMap jhm = new JsonHashMap();
        String storeId = getPara("dept");
        try {
            String select = "select * from h_area where store_id=? order by name";
            List<Record> result = Db.find(select, storeId);
            if(result != null && result.size() > 0){
                for(Record r : result){
                    String kind = r.getStr("kind");
                    String[] kinds = kind.split(",");
                    String post = "";
                    if(kinds != null && kinds.length > 0){
                        for(String s : kinds){
                            post += DictionaryConstants.DICT_STRING_MAP.get(DictionaryConstants.KIND).get(s) + ",";
                        }
                        post = post.substring(0, post.length() - 1);
                    }
                    r.set("kind", post);
                }
            }
            jhm.put("data", result);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("查询失败！");
        }
//        String result = "{\"code\":1,\"data\":[{\"id\":\"asdf\",\"name\":\"m1\",\"kind\":\"摆面，拌菜\"},{\"id\":\"qwer\",\"name\":\"m2\",\"kind\":\"摆面，拌菜\"},{\"id\":\"yuii\",\"name\":\"A1\",\"kind\":\"摆面，拌菜\"}]}";
        renderJson(jhm);
    }

    public void add(){
        JsonHashMap jhm = new JsonHashMap();
        String name = getPara("name");
        String storeId = getPara("dept");
        String[] kinds = getParaValues("kind");
        String kind = "";
        if(kinds != null && kinds.length > 0){
            for(String s : kinds){
                kind += s + ",";
            }
            kind = kind.substring(0, kind.length() - 1);
        }
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String time = DateTool.GetDateTime();
        String max = "select max(sort) maxsort from h_area where store_id=?";
        try{
            Record maxR = Db.findFirst(max, storeId);
            Record area = new Record();
            area.set("id", UUIDTool.getUUID());
            area.set("store_id", storeId);
            area.set("name", name);
            area.set("kind", kind);
            area.set("sort", maxR.getInt("maxsort") == null ? 1: maxR.getInt("maxsort") + 1);
            area.set("creater_id", usu.getUserId());
            area.set("modifier_id", usu.getUserId());
            area.set("create_time", time);
            area.set("modify_time", time);
            boolean flag = Db.save("h_area", area);
            if(flag){
                jhm.putMessage("保存成功！");
            }else{
                jhm.putCode(0);
                jhm.putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("保存失败！");
        }
        renderJson(jhm);
    }

    public void update(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        String name = getPara("name");
        String[] kinds = getParaValues("kind");
        String kind = "";
        if(kinds != null && kinds.length > 0){
            for(String s : kinds){
                kind += s + ",";
            }
            kind = kind.substring(0, kind.length() - 1);
        }
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String time = DateTool.GetDateTime();
        Record area = new Record();
        area.set("id", id);
        area.set("name", name);
        area.set("kind", kind);
        area.set("modifier_id", usu.getUserId());
        area.set("modify_time", time);
        try{
            service.update(area);
            jhm.putMessage("修改成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("修改失败！");
        }
        renderJson(jhm);
    }

    public void delete(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        try{
            service.delete(id);
            jhm.putMessage("删除成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("删除失败！");
        }
        renderJson(jhm);
    }

    public void showById(){
        String id = getPara("id");
//        String result = "{\"code\":1,\"data\":{\"id\":\"asdf\",\"name\":\"m1\",\"kind\":[\"preparation_lb\",\"preparation\",\"fried_noodles\"]}}";
        JsonHashMap jhm = new JsonHashMap();
        try{
            Record record = Db.findById("h_area", id);
            if(record != null){
                String kind = record.getStr("kind");
                String[] kinds = kind.split(",");
                record.set("kind", kinds);
            }
            jhm.put("data", record);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("查询失败！");
        }
        renderJson(jhm);
    }

    public void showAreaStaff(){
//        String date = getPara("start_date");
        String storeId = getPara("dept");
//        String result = "{\"code\":1,\"data\":[{\"id\":\"a123\",\"name\":\"m1\",\"workers\":[{\"id\":\"qwer\",\"name\":\"张三\",\"color\":\"#b7a6d4\"},{\"id\":\"sjkdf\",\"name\":\"王二麻子\",\"color\":\"#316f52\"},{\"id\":\"fghnb\",\"name\":\"李四\",\"color\":\"#fa7a19\"}]},{\"id\":\"b123\",\"name\":\"m2\",\"workers\":[{\"id\":\"qwer\",\"name\":\"张三\",\"color\":\"#b7a6d4\"},{\"id\":\"fghnb\",\"name\":\"李四\",\"color\":\"#fa7a19\"}]},{\"id\":\"c123\",\"name\":\"A1\",\"workers\":[{\"id\":\"qwer\",\"name\":\"张三\",\"color\":\"#b7a6d4\"},{\"id\":\"sjkdf\",\"name\":\"王二麻子\",\"color\":\"#316f52\"}]}]}";
        JsonHashMap jhm = new JsonHashMap();
        try{
//            String select = "select has.area_id id, has.area_name name, hs.name sname, hs.id sid from h_area_staff has, h_staff hs where store_id=? and date=? and has.staff_id=hs.id";
            String select = "select has.area_id id, has.area_name name, hs.name sname, hs.id sid from h_area_staff has, h_staff hs where store_id=? and has.staff_id=hs.id order by has.area_name";
            List<Record> list = Db.find(select, storeId);
            String colorSelect = "select color from h_store_color";
            List<Record> colorList = Db.find(colorSelect);
            String selectArea = "select * from h_area where store_id=?";
            List<Record> areaList = Db.find(selectArea, storeId);
            Map<String, List<Record>> areaStaffListMap = new HashMap<>();
            Map<String, Record> areaStaffMap = new HashMap<>();
            Map<String, Record> staffMap = new HashMap<>();
            if(areaList != null && areaList.size() > 0){
                for(Record r : areaList){
                    areaStaffMap.put(r.getStr("id"), r);
                }
            }
            int index = 0;
            if(list != null && list.size() > 0){
                for(Record r : list){
                    int i = index++ % colorList.size();
                    staffMap.computeIfAbsent(r.getStr("sid"), k -> new Record().set("id", r.get("sid")).set("name", r.get("sname")).set("color", colorList.get(i).get("color")));
                }
                for(Record r : list){
                    List<Record> staffList = areaStaffListMap.computeIfAbsent(r.getStr("id"), k -> new ArrayList<>());
                    staffList.add(staffMap.get(r.getStr("sid")));
                    r.set("workers", staffList);
                    Record areaStaff = areaStaffMap.get(r.getStr("id"));
                    r.set("sort", areaStaff.getInt("sort"));
                    areaStaffMap.put(r.getStr("id"), r);
                }
            }
            List<Record> result = new ArrayList<>();
            if(areaStaffMap != null && areaStaffMap.size() > 0){
                for(String key : areaStaffMap.keySet()){
                    result.add(areaStaffMap.get(key));
                }
            }
            Collections.sort(result, new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    //int o1I = o1 != null ? o1.getInt("sort") : 0;
                    return o1.getInt("sort").compareTo(o2.getInt("sort"));
                }
            });
            jhm.put("data", result);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("查询失败！");
        }
        renderJson(jhm);
    }

    public void edit(){
        String id = getPara("id");
//        String date = getPara("date");
        String[] workers = getParaValues("workers");
        JsonHashMap jhm = new JsonHashMap();
        try{
            if(workers != null && workers.length > 0){
//                service.edit(id, workers, date, new UserSessionUtil(getRequest()));
                service.edit(id, workers, new UserSessionUtil(getRequest()));
            }
            jhm.putMessage("编辑成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("编辑失败！");
        }
        renderJson(jhm);
    }

    public void getStaff(){
        String areaId = getPara("id");
        String storeId = getPara("dept");
//        String date = getPara("date");
//        String result = "{\"code\":1,\"data\":[{\"value\":\"qwer\",\"name\":\"张三\"},{\"value\":\"fghnb\",\"name\":\"李四\"},{\"value\":\"sjkdf\",\"name\":\"王二麻子\"}]}";
        JsonHashMap jhm = new JsonHashMap();
        try{
            Record area = Db.findById("h_area", areaId);
            String staffSelect = "select id value, name from h_staff where dept_id=? and kind like ?";
            List<Record> staffList = Db.find(staffSelect, storeId, "%" + area.get("kind") + "%");
            jhm.put("data", staffList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0);
            jhm.putMessage("查询失败！");
        }
        renderJson(jhm);
    }

}
