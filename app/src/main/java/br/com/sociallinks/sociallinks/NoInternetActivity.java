package br.com.sociallinks.sociallinks;

import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.sociallinks.sociallinks.utils.NetworkStateReceiver;

public class NoInternetActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

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
