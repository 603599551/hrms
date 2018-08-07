package utils;

import com.alibaba.fastjson.JSON;
import com.jfinal.json.Json;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import easy.util.DateTool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContentTransformationUtil {

    /**
     * @author wangze
     *   PC端排班的数据格式转化成APP需要的数据格式
     * @param pcStr
     * {
     * "color":"#72b05a",
     * "id":"612458c3f01e47b5810b6bca90b4a91d",
     * "name":"黄琦凯",
     * "work":[
     * {"kind":"waiter","pos":[6,0],"salary":12.00},
     * {"kind":"waiter","pos":[7,0],"salary":12.00},
     * {"kind":"waiter","pos":[8,0],"salary":12.00},
     * {"kind":"waiter","pos":[9,0],"salary":12.00},
     * {"kind":"waiter","pos":[10,0],"salary":12.00},
     * {"kind":"waiter","pos":[11,0],"salary":12.00},
     * {"kind":"waiter","pos":[12,0],"salary":12.00},
     * {"kind":"waiter","pos":[13,0],"salary":12.00},
     * {"kind":"waiter","pos":[14,0],"salary":12.00},
     * {"kind":"waiter","pos":[15,0],"salary":12.00},
     * {"kind":"waiter","pos":[16,0],"salary":12.00},
     * {"kind":"waiter","pos":[17,0],"salary":12.00},
     * {"kind":"waiter","pos":[18,0],"salary":12.00},
     * {"kind":"waiter","pos":[19,0],"salary":12.00},
     * {"kind":"waiter","pos":[20,0],"salary":12.00},
     * {"kind":"waiter","pos":[21,0],"salary":12.00},
     * {"kind":"waiter","pos":[22,0],"salary":12.00},
     * {"kind":"waiter","pos":[23,0],"salary":12.00},
     * {"kind":"waiter","pos":[24,0],"salary":12.00},
     * {"kind":"waiter","pos":[25,0],"salary":12.00}]}
     * @return [{"start":"09:00:00","end":"14:00:00"}]
     */
    public static String PcToAppPaiban(String pcStr){
        return PcToAppPaibanTimePeriod(PcPaibanToPcXianShi(pcStr));
    }
    /**
     *
     * @param pcStr PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     * @return  格式key为"start"和"end"的json数组转成的字符串，装的是一段时间的开始时间和结束时间
     */
    public static String PcToAppPaibanTimePeriod(String pcStr){
        if(pcStr == null || pcStr.trim().length() <= 0){
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        JSONArray jsonArray = new JSONArray();
        String pcContent = "";
        try {
            net.sf.json.JSONObject jsonSave = new net.sf.json.JSONObject();
            JSONObject jsonObject = JSONObject.fromObject(pcStr);
            Date initTime = simpleDateFormat.parse("07:30:00");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime= "";                         //连续时间段中开始的时间
            String endTime = "";                        //连续时间段中结束的时间
            Date transDate = new Date();               //将毫秒数转化为日期类，然后再将日期类转化为小时格式的时间
            int count = 0;
            Map paraMap = new HashMap();
            for(int i = 0; i < 66; i++){
                if(count == 0){
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + i * standardTime);
                        startTime = simpleDateFormat.format(transDate);
                        paraMap.put("start", startTime);
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);
                        paraMap.put("end", endTime);
                        count++;
                    }
                } else {
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);    //记录下一段连续时间段暂时的结束时间
                        paraMap.replace("end", endTime);
                    } else {
                        count = 0;
                        jsonSave.putAll(paraMap);
                        jsonArray.add(jsonSave);
                        jsonSave = new net.sf.json.JSONObject();
                        paraMap=new HashMap();
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        pcContent = jsonArray.toString();
        return pcContent;
    }
    /**
     * PC端，排班表（h_staff_paiban）的json格式转化为闲时表（h_staff_idle_time）的格式
     * @param pcStr
     * @return
     */
    public static String PcPaibanToPcXianShi(String pcStr){
        String result = "";
        if(pcStr == null || pcStr.trim().length() <= 0){
            return "";
        }
        JSONObject pcJson = JSONObject.fromObject(pcStr);
        JSONArray workArr = pcJson.getJSONArray("work");
        if(workArr != null && workArr.size() > 0){
            Map<String, Integer> map = new HashMap<>();
            for(int i = 0; i < 66; i++){
                map.put(i + "", 0);
            }
            for(int i = 0; i < workArr.size(); i++){
                JSONObject obj = workArr.getJSONObject(i);
                JSONArray posArr = obj.getJSONArray("pos");
                map.put(posArr.getString(0) + "", 1);
            }
            result = JSONObject.fromObject(map).toString();
        }
        return result;
    }
    /**
     *
     * @param appStr app端需要的格式key为"start"和"end"的json数组转成的字符串
     * @return   PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     */
    public static String AppToPcXianShi(String appStr){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonSave = new JSONObject();
        String appContent = "";

        try {
            jsonArray =JSONArray.fromObject(appStr);
            Date initTime = simpleDateFormat.parse("07:30:00");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime= "";                         //连续时间段中开始的时间
            Long startMilliSecond;
            Date transDate = new Date();

            int []key = new int[66];
            for(int i : key){
                key[i] = 0;
            }

            for(int i = 0; i < key.length; i++){
                jsonSave.put(String.valueOf(i),"0");
            }

            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                startTime = jsonObject.getString("start");
                transDate = simpleDateFormat.parse(startTime);
                startMilliSecond = transDate.getTime();
                jsonSave.put(String.valueOf((startMilliSecond-initMilliSecond)/standardTime),"1");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        appContent = jsonSave.toString();
        return appContent;
    }


    /**
     *
     * @param pcStr PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     * @return  app端需要的格式key为"start"和"end"的json数组转成的字符串
     */
    public static String PcToAppXianShi(String pcStr){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        JSONArray jsonArray = new JSONArray();
        String pcContent = "";

        try {
            JSONObject jsonSave = new JSONObject();
            JSONObject jsonObject = JSONObject.fromObject(pcStr);
            Date initTime = simpleDateFormat.parse("07:30:00");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime= "";                         //连续时间段中开始的时间
            String endTime = "";                        //连续时间段中结束的时间
            Date transDate = new Date();               //将毫秒数转化为日期类，然后再将日期类转化为小时格式的时间
            Map paraMap = new HashMap();

            for(int i = 0; i < 66; i++){
                if(jsonObject.getString(String.valueOf(i)).equals("1")){
                    transDate.setTime(initMilliSecond + i * standardTime);
                    startTime = simpleDateFormat.format(transDate);
                    paraMap.put("start", startTime);
                    transDate.setTime(initMilliSecond + (i+1) * standardTime);
                    endTime = simpleDateFormat.format(transDate);
                    paraMap.put("end", endTime);
                    jsonSave.putAll(paraMap);
                    jsonArray.add(jsonSave);
                    jsonSave = new net.sf.json.JSONObject();
                    paraMap=new HashMap();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        pcContent = jsonArray.toString();
        return pcContent;
    }

    /**
     *
     * @param pcStr PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     * @return  格式key为"start"和"end"的json数组转成的字符串，装的是一段时间的开始时间和结束时间
     */
    public static String PcToAppXianShiTimePeriod(String pcStr){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        JSONArray jsonArray = new JSONArray();
        String pcContent = "";
        try {
            net.sf.json.JSONObject jsonSave = new net.sf.json.JSONObject();
            JSONObject jsonObject = JSONObject.fromObject(pcStr);
            Date initTime = simpleDateFormat.parse("07:30:00");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime= "";                         //连续时间段中开始的时间
            String endTime = "";                        //连续时间段中结束的时间
            Date transDate = new Date();               //将毫秒数转化为日期类，然后再将日期类转化为小时格式的时间
            int count = 0;
            Map paraMap = new HashMap();
            for(int i = 0; i < 66; i++){
                if(count == 0){
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + i * standardTime);
                        startTime = simpleDateFormat.format(transDate);
                        paraMap.put("start", startTime);
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);
                        paraMap.put("end", endTime);
                        count++;
                    }
                } else {
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);    //记录下一段连续时间段暂时的结束时间
                        paraMap.replace("end", endTime);
                    } else {
                        count = 0;
                        jsonSave.putAll(paraMap);
                        jsonArray.add(jsonSave);
                        jsonSave = new net.sf.json.JSONObject();
                        paraMap=new HashMap();
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        pcContent = jsonArray.toString();
        return pcContent;
    }


    /**
     *
     * @param jsonTime  PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     * @return  用List装好的一个或多个连续时间段时间段 etc: size = 2    0 = 07:30:00-08:30:00     1 = 09:30:00-10:30:00
     */
    public static List<String> JsonTimeToStringTimeXianShi(String jsonTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        List<String> timeList = new ArrayList();
        String stringTime = "";                                        //记录返回的字符串时间格式
        try {
            JSONObject jsonObject = JSONObject.fromObject(jsonTime);
            Date initTime = simpleDateFormat.parse("07:30");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime= "";                         //连续时间段中开始的时间
            String endTime = "";                        //连续时间段中结束的时间
            Date transDate = new Date();               //将毫秒数转化为日期类，然后再将日期类转化为小时格式的时间
            int count = 0;                             //判断是否连续条件

            for(int i = 0; i < 66; i++){
                if(count == 0){
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + i * standardTime);
                        startTime = simpleDateFormat.format(transDate);
                        stringTime = startTime + "-";
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);
                        count++;
                    }
                } else {
                    if(jsonObject.getString(String.valueOf(i)).equals("1")){
                        transDate.setTime(initMilliSecond + (i+1) * standardTime);
                        endTime = simpleDateFormat.format(transDate);    //记录下一段连续时间段暂时的结束时间
                    } else {
                        stringTime += endTime;
                        count = 0;
                        timeList.add(stringTime);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeList;
    }


    /**
     *
     * @param stringTime 用逗号分割的连续时间段 etc: 07:30:00-08:30:00 , 09:30:00-10:30:00
     * @return  PC端需要的格式,key为0-65,value为 0/1 的jsonObject转成的字符串
     */
    public static String StringTimeToJsonTimeXianShi(String stringTime){
        String jsonTime = "";
        DateTool dateTool = new DateTool();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String nowDate = dateTool.GetDateTime();
        try {
            Date initTime = simpleDateFormat.parse("07:30"); //最早上班时间
            Long initMilliSecond = initTime.getTime();             //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;               //每15分钟的毫秒数
            Map paramsMap = new HashMap();                        //存放15分钟一组的Map
            int[] key = new int[66];                              //Map的key
            for (int i = 0; i < key.length; i++) {
                paramsMap.put(String.valueOf(i), "0");
            }
            String[] time = stringTime.split(",");           //将逗号分割的时间段分为一个个时间段
            for (int i = 0; i < time.length; i++) {               //遍历每一个时间段
                String[] timePeriod = time[i].split("-");
                Date startTime = simpleDateFormat.parse(timePeriod[0]);
                Long startMilliSecond = startTime.getTime();           //一个时间段的开始时间毫秒数
                Date endTime = simpleDateFormat.parse(timePeriod[1]);
                Long endMilliSecond = endTime.getTime();               //一个时间段结束时间的毫秒数
                int startDur = (int) ((startMilliSecond - initMilliSecond) / standardTime);            //与最早上班时间相隔多少个15分钟
                int dur = (int) ((endMilliSecond - startMilliSecond) / standardTime);                  //一个时间段包含多少个15分钟
                for (int j = 0; j < key.length; j++) {
                    if (j >= startDur && j < startDur + dur) {             //在当前循环的时间段时间内，则将对应的key改为1
                        paramsMap.put(String.valueOf(j), "1");
                    }
                    if (j >= startDur + dur) {                            //当key超出当前循环时间段时间，则跳出循环
                        break;
                    }
                }
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(paramsMap);
            jsonTime = jsonObject.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return jsonTime;
    }

}
