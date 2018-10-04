package br.com.sociallinks.sociallinks;

import android.content.Intent;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.sociallinks.sociallinks.fragments.LinksFragment;
import br.com.sociallinks.sociallinks.fragments.ProductsFragment;
import br.com.sociallinks.sociallinks.utils.NetworkStateReceiver;

public class ProductsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private static final String NAV_PRODUCTS_KEY = "nav_products_key";
    private static final String NAV_LINKS_KEY = "nav_links_key";

    private ProductsFragment mProductsFragment;
    private LinksFragment mLinksFragment;
    private NetworkStateReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        if (savedInstanceState != null){
            if (savedInstanceState.containsKey(NAV_PRODUCTS_KEY)){
                mProductsFragment = savedInstanceState.getParcelable(NAV_PRODUCTS_KEY);
            }
            if (savedInstanceState.containsKey(NAV_LINKS_KEY)) {
                mLinksFragment = savedInstanceState.getParcelable(NAV_LINKS_KEY);
            }
        }

        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
        this.registerReceiver(mNetworkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //First NavigationView item selection
        if (mProductsFragment == null) {
            mProductsFragment = new ProductsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mProductsFragment)
                    .commit();
        }

        setUserProfileMenu(navigationView);
    }

    private void setUserProfileMenu(NavigationView navigationView) {
        ImageView userImageView = navigationView.getHeaderView(0).findViewById(R.id.iv_user_pic);
        TextView userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.tv_user_name);
        TextView userEmailTextView = navigationView.getHeaderView(0).findViewById(R.id.tv_user_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            userNameTextView.setText(user.getDisplayName());
            userEmailTextView.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(new RequestOptions().circleCrop())
                        .into(userImageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.image_default_profile)
                        .into(userImageView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (id) {
            case R.id.nav_products :
                if (mProductsFragment == null) {
                    mProductsFragment = new ProductsFragment();
                }

                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, mProductsFragment)
                        .commit();
                break;
            case R.id.nav_links:
                if (mLinksFragment == null){
                    mLinksFragment = new LinksFragment();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, mLinksFragment)
                        .commit();
                break;
            case R.id.nav_favorites:
                Toast.makeText(this, getString(R.string.future_feature_message), Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, getString(R.string.future_feature_message), Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.nav_account:
                Toast.makeText(this, getString(R.string.future_feature_message), Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.nav_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                break;
            default:
                if (mProductsFragment == null) {
                    mProductsFragment = new ProductsFragment();
                }

                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, mProductsFragment)
                        .commit();
                break;

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {
        Intent intent = new Intent(this, NoInternetActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
        this.registerReceiver(mNetworkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNetworkStateReceiver.removeListener(this);
        this.unregisterReceiver(mNetworkStateReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mProductsFragment != null) {
            outState.putParcelable(NAV_PRODUCTS_KEY, mProductsFragment);
        }
        if (mLinksFragment != null) {
            outState.putParcelable(NAV_LINKS_KEY, mLinksFragment);
        }
        super.onSaveInstanceState(outState);
    }
}
