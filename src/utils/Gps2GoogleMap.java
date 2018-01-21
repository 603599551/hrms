package utils;

/**
 * @author Administrator
 * gps转google地图坐标
 */
public class Gps2GoogleMap {

	static double a = 6378245.0;
	static double ee = 0.00669342162296594323;
	/**
	 * 
	 * @param wgLat 纬度 
	 * @param wgLon 经度 
	 * @return double数组，第一个元素是纬度 ，第二个元素经度 
	 */
	public static double[] transform (double wgLat, double wgLon) {

	    if (outOfChina(wgLat, wgLon)) {
	        return new double[]{wgLat, wgLon};
	    }
	    double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
	    double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
	    double radLat = wgLat / 180.0 * Math.PI;
	    double magic = Math.sin(radLat);
	    magic = 1 - ee * magic * magic;
	    double sqrtMagic = Math.sqrt(magic);
	    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
	    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
	    double mgLat = wgLat + dLat;
	    double mgLon = wgLon + dLon;

	    return new double[]{mgLat, mgLon};

	};

	private static boolean outOfChina (double lat, double lon) {

	    if (lon < 72.004 || lon > 137.8347)
	        return true;
	    if (lat < 0.8293 || lat > 55.8271)
	        return true;

	    return false;

	};

	private static double transformLat (double x, double y) {

		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
	    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
	    ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;

	    return ret;

	};

	private static double transformLon (double x,double  y) {

		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
	    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
	    ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
	    ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;

	    return ret;

	}
	public static void main(String[] args){
//		System.out.println(GetDistance(125.310905, 43.878149, 125.310787, 43.884776));
		double wgLat=43.868263;
		double wgLon=125.301033;
		double[] array=transform(wgLat , wgLon );
		System.out.println(array[0]+"---"+array[1]);
	}
}
