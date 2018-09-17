package br.com.sociallinks.sociallinks;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import br.com.sociallinks.sociallinks.utils.NetworkStateReceiver;

import static br.com.sociallinks.sociallinks.fragments.ProductsFragment.INTENT_PRODUCT_FLAG;

public class NoInternetActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

    private static final String LOG_TAG = NoInternetActivity.class.getSimpleName();

    private NetworkStateReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        this.mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
        this.registerReceiver(mNetworkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void networkAvailable() {
        finish();
    }

    @Override
    public void networkUnavailable() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkStateReceiver.removeListener(this);
        this.unregisterReceiver(mNetworkStateReceiver);
    }
}
