package com.echdr.android.echdrapp.data.service;

import androidx.appcompat.app.AppCompatActivity;

import com.echdr.android.echdrapp.data.Sdk;
import com.echdr.android.echdrapp.ui.login.LoginActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LogOutService {

    public static Disposable logOut(AppCompatActivity activity) {
        return Sdk.d2().userModule().logOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ActivityStarter.startActivity(activity, LoginActivity.getLoginActivityIntent(activity.getApplicationContext()), true),
                        Throwable::printStackTrace);
    }
}
