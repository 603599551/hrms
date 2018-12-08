package utils;

import com.jfinal.plugin.activerecord.Record;

import java.util.HashMap;
import java.util.Map;

public class DictionaryConstants {

    //岗位常量
    public static final String KIND = "post";

    public static final Map<String, Map<String, String>> DICT_STRING_MAP = new HashMap<>();
    public static final Map<String, Map<String, Record>> DICT_RECORD_MAP = new HashMap<>();

}
