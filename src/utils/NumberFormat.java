package utils;

import java.text.DecimalFormat;

public class NumberFormat {
    public static String doubleFormatStr(double d){
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(d);
    }
}
