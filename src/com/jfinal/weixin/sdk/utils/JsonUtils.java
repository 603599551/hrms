package com.jfinal.weixin.sdk.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.jfinal.json.FastJson;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Json转换
 * 
 * JFinal-weixin内部使用
 * 
 * @author L.cm
 * email: 596392912@qq.com
 * site:http://www.dreamlu.net
 * date 2015年5月13日下午4:58:33
 */
public final class JsonUtils {

    private JsonUtils() {}

    /**
     * 将model转为json字符串
     * @param model jfinal model
     * @return JsonString
     */
    public static String toJson(Model<? extends Model<?>> model) {
        return toJson(CPI.getAttrs(model));
    }

    /**
     * 将Collection&lt;Model&gt;转换为json字符串
     * @param models jfinal model
     * @return JsonString
     */
    public static String toJson(Collection<Model<? extends Model<?>>> models) {
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        for (Model<? extends Model<?>> model : models) {
            list.add(CPI.getAttrs(model));
        }
        return toJson(list);
    }

    /**
     * 将 record 转为json字符串
     * @param record jfinal record
     * @return JsonString
     */
    public static String toJson(Record record) {
        return toJson(record.getColumns());
    }

    /**
     * 将List&lt;Record&gt;转换为json字符串
     * @param records jfinal records
     * @return JsonString
     */
    public static String toJson(List<Record> records) {
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        for (Record record : records) {
            list.add(record.getColumns());
        }
        return toJson(list);
    }
    
    private static IJsonFactory jsonFactory = null;
    
    /**
     * 自定义 jsonFactory 用户自定义切换
     * 1. 优先使用用户设定的JsonUtils.setJsonFactory
     * 2. 用户没有手动设定，使用JFinal中设定的
     * 3. JFinal中没有设定，使用JFinal默认的
     * @param jsonFactory json工厂
     */
    public static void setJsonFactory(IJsonFactory jsonFactory) {
        JsonUtils.jsonFactory = jsonFactory;
    }

    /**
     * 将 Object 转为json字符串
     * @param object 对象
     * @return JsonString
     */
    public static String toJson(Object object) {
        if (jsonFactory == null) {
            return Json.getJson().toJson(object);
        }
        return jsonFactory.getJson().toJson(object);
    }

    /**
     * 将 json字符串 转为Object
     * @param jsonString json字符串
     * @param valueType 结果类型
     * @param <T> 泛型标记
     * @return T 结果
     */
    public static <T> T parse(String jsonString, Class<T> valueType) {
        if (jsonFactory == null) {
            // return Json.getJson().parse(jsonString, valueType);
        		
        		Json json = Json.getJson();
        		// JFinalJson 不支持 parse
        		if (json instanceof com.jfinal.json.JFinalJson) {
        			return FastJson.getJson().parse(jsonString, valueType);
        		} else {
        			return json.parse(jsonString, valueType);
        		}
        }
        return jsonFactory.getJson().parse(jsonString, valueType);
    }

}
