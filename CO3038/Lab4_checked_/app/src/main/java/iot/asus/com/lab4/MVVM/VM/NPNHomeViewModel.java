package iot.asus.com.lab4.MVVM.VM;

import iot.asus.com.lab4.MVVM.View.NPNHomeView;
import iot.asus.com.lab4.Network.ApiResponseListener;

/**
 * Created by Le Trong Nhan on 19/06/2018.
 */

public class NPNHomeViewModel extends BaseViewModel<NPNHomeView> {
    public void updateToServer(String url)
    {
        requestGETWithURL(url, new ApiResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                view.onSuccessUpdateServer(response);
            }

            @Override
            public void onError(String error) {
                view.onErrorUpdateServer(error);
            }
        });
    }
}
