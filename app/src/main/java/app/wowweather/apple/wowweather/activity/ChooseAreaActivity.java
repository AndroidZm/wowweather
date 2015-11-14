package app.wowweather.apple.wowweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.wowweather.apple.wowweather.R;
import app.wowweather.apple.wowweather.model.City;
import app.wowweather.apple.wowweather.model.County;
import app.wowweather.apple.wowweather.model.Province;
import app.wowweather.apple.wowweather.model.WowWeatherDB;
import app.wowweather.apple.wowweather.util.HttpCallbackListener;
import app.wowweather.apple.wowweather.util.HttpUtil;
import app.wowweather.apple.wowweather.util.Utility;

/**
 * Created by apple on 2015/11/13.
 */
public class ChooseAreaActivity extends Activity {
    private TextView titleText;
    private ListView listView;
    private int currentLevel;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;
    private WowWeatherDB wowWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selecedtCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        wowWeatherDB = WowWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(index);
                    queryCities();
                } else {
                    selecedtCity = cityList.get(index);
                    queryCounty();
                }
            }
        });
        queryProvince(); //加载省级数据
    }

    //查询全国所有的省，优先从数据库中查询，如果没有查询到再去服务器查询
    private void queryProvince() {
        provinceList = wowWeatherDB.loadProvince();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromService(null, "province");
        }
    }

    private void queryFromService(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(wowWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handlerCityResponse(wowWeatherDB, response,
                            selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handlerCountyResponse(wowWeatherDB, response
                            , selecedtCity.getId());
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,
                                "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        {

        }
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

    private void queryCounty() {
        countyList = wowWeatherDB.loadCounty(selecedtCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selecedtCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromService(selecedtCity.getCityCode(), "county");
        }
    }

    private void queryCities() {
        cityList = wowWeatherDB.loadCity(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromService(selectedProvince.getProvinceCode(), "city");
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvince();
        }else {
            finish();
        }
    }
}
