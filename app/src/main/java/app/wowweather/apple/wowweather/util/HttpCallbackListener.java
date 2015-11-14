package app.wowweather.apple.wowweather.util;

/**
 * Created by apple on 2015/11/13.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
