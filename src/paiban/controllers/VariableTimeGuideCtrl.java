package paiban.controllers;

import com.alibaba.fastjson.JSONArray;
import com.common.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

public class VariableTimeGuideCtrl extends BaseCtrl {

    public void add(){
        try {
            JSONArray arr = JSONArray.parseArray(this.getRequestObject());
            System.out.println(arr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.putMessage("设置成功！");
        renderJson(jhm);
    }

    public void getVariableTimeGuide(){
        renderJson("{\"code\":1,\"data\":[[{\"input\":\"1-3\"},{\"input\":\"3\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}]]}");
    }

}
