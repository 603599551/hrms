package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jsoft.crm.utils.HanyuPinyinHelper;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class UtilCtrl extends Controller {

    public void pinyin(){
        String content=getPara("content");
        String type=getPara("type");

        if(StringUtils.isEmpty(content)){
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage("content必须有值！");
            renderJson(jhm);
            return;
        }

        String pinyin=null;
        if(type==null || "".equals(type) || "1".equals(type)){
            pinyin=HanyuPinyinHelper.getFirstLettersUp(content);
        }else if("2".equals(type)){
            pinyin=HanyuPinyinHelper.getFirstLettersLo(content);
        }else if("3".equals(type)){
            pinyin=HanyuPinyinHelper.getFirstLetter(content);
        }else if("4".equals(type)){
            pinyin=HanyuPinyinHelper.getPinyinString(content);
        }
        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1);
        jhm.put("pinyin",pinyin);
        renderJson(jhm);
    }
}
