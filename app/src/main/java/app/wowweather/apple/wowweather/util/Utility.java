package app.wowweather.apple.wowweather.util;

import android.text.TextUtils;

import app.wowweather.apple.wowweather.model.City;
import app.wowweather.apple.wowweather.model.County;
import app.wowweather.apple.wowweather.model.Province;
import app.wowweather.apple.wowweather.model.WowWeatherDB;

/**
 * Created by apple on 2015/11/13.
 */
public class Utility {
    //解析和处理服务器返回的省级数据
    public synchronized static boolean handleProvincesResponse(WowWeatherDB wowWeatherDB,
                                                               String response){
        if (!TextUtils.isEmpty(response)){
            String[] allProvinces=response.split(",");
            if (allProvinces!=null&&allProvinces.length>0){
                for (String p:allProvinces){
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    wowWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handlerCityResponse(WowWeatherDB wowWeatherDB,
                                              String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[]allCity=response.split(",");
            if (allCity!=null&&allCity.length>0){
                for (String c:allCity){
                    String[]array=c.split("\\|");
                    City city=new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    wowWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean handlerCountyResponse(WowWeatherDB wowWeatherDB,
                                                String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[]allCounty=response.split(",");
            if (allCounty!=null&&allCounty.length>0){
                for (String c:allCounty){
                    String[]array=c.split("\\|");
                    County county=new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    wowWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
