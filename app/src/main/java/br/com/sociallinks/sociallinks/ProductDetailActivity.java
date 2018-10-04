package br.com.sociallinks.sociallinks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import br.com.sociallinks.sociallinks.models.Link;
import br.com.sociallinks.sociallinks.models.Product;
import br.com.sociallinks.sociallinks.utils.NetworkStateReceiver;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static br.com.sociallinks.sociallinks.fragments.ProductsFragment.INTENT_PRODUCT_FLAG;
import static br.com.sociallinks.sociallinks.utils.NetworkUtils.*;

public class ProductDetailActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    private static final String LOG_TAG = ProductDetailActivity.class.getSimpleName();
    private static final String BASE_URL = "https://www.sociallinks.com";
    public static final String QUERY_KEY = "product";
    private static final String DYNAMIC_LINK_DOMAIN = "sociallinks.page.link";

    //iOS test
    private static final String IOS_ADDRESS = "com.sociallinks.ios";
    private static final String APP_STORE_ID = "123456789";

    //Google Analytic test
    private static final String APP_SOURCE = "app";
    private static final String SOCIAL_MEDIUM = "social";
    private static final String EXAMPLE_CAMPAIGN = "example-promo";


    @BindView(R.id.tv_product_price_detailScreen) TextView mProductPrice;
    @BindView(R.id.tv_product_commission_detailScreen) TextView mProductCommission;
    @BindView(R.id.tv_product_description) TextView mProductFullDesc;
    @BindView(R.id.iv_product_photo_detail) ImageView mProductImage;
    @BindView(R.id.meta_bar) LinearLayout mMetaBar;
    @BindView(R.id.toolbar_detailed) Toolbar mToolbar;
    @BindView(R.id.collapsingToolbar_layout) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.fab_share) SpeedDialView mFabShare;
    @BindView(R.id.progressBar) ProgressBar mLoadingIndicator;

    private Product mProduct;
    private Uri mShortDynamicLink;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private NetworkStateReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_PRODUCT_FLAG)) {
            mProduct = intent.getParcelableExtra(INTENT_PRODUCT_FLAG);
            Log.e(LOG_TAG, "INTENT_PRODUCT_FLAG:exists1");
        }

        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
        this.registerReceiver(mNetworkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        this.mUser = FirebaseAuth.getInstance().getCurrentUser();
        this.mDatabase = FirebaseDatabase.getInstance();

        populateScreen();

        addFabSubItem();

        mFabShare.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int fabSelected = actionItem.getId();
                switch (fabSelected){
                    case R.id.fab_link:
                        mLoadingIndicator.setVisibility(View.VISIBLE);

                        Task<ShortDynamicLink> shortDynamicLinkTask = createDynamicLink();

                        shortDynamicLinkTask.addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                mLoadingIndicator.setVisibility(View.INVISIBLE);

                                if (task.isSuccessful()){
                                    mShortDynamicLink = task.getResult().getShortLink();
                                    Log.e(LOG_TAG, mShortDynamicLink.toString());

                                    Link link = new Link(mShortDynamicLink.toString(), mProduct.getId(),
                                            mProduct.getName(), mProduct.getPrice(), mProduct.getPhotoUrl(),
                                            mProduct.getCommission());

                                    DatabaseReference dbRefShares = mDatabase.getReference(SHARES_PATH);
                                    DatabaseReference dbRefProducts = mDatabase.getReference(PRODUCTS_PATH);

                                    dbRefShares.child(mUser.getUid()).child(USERNAME_FIELD)
                                            .setValue(mUser.getDisplayName());
                                    dbRefShares.child(mUser.getUid()).child(LINKS_PATH).child(String.valueOf(mProduct.getId()))
                                            .setValue(link);
                                    dbRefProducts.child(String.valueOf(mProduct.getId()))
                                            .setValue(mProduct);

                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newRawUri("shortLink", mShortDynamicLink);
                                    clipboard.setPrimaryClip(clip);
                                    Snackbar.make(mCollapsingToolbar, getString(R.string.snackbar_clipboard), LENGTH_LONG)
                                            .show();
                                } else {
                                    Log.e(LOG_TAG, "Fail to generate new dynamic link: " +
                                        task.getException());
                                    //Snacbar to verifiy internet connetion
                                }
                            }
                        });
                        return false;

                    case R.id.fab_facebook:
                        Toast.makeText(ProductDetailActivity.this, getString(R.string.fab_facebook_soon), Toast.LENGTH_SHORT).show();
                        return false;
                    case R.id.fab_instagram:
                        Toast.makeText(ProductDetailActivity.this, getString(R.string.fab_instagram_soon), Toast.LENGTH_SHORT).show();
                        return false;
                    case R.id.fab_twitter:
                        Toast.makeText(ProductDetailActivity.this, getString(R.string.fab_twitter_soon), Toast.LENGTH_SHORT).show();
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    private Task<ShortDynamicLink> createDynamicLink() {
        //https://www.sociallinks.com/{$userid}?product={$id}
        Uri fullLinkUri = Uri.withAppendedPath(Uri.parse(BASE_URL), mUser.getUid()).buildUpon()
                .appendQueryParameter(QUERY_KEY, String.valueOf(mProduct.getId())).build();

        Log.e(LOG_TAG, "fullLink: " + fullLinkUri.toString());

        final DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(fullLinkUri)
                .setDynamicLinkDomain(DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .setIosParameters(
                        //for test purposes
                        new DynamicLink.IosParameters.Builder(IOS_ADDRESS)
                                .setAppStoreId(APP_STORE_ID)
                                .setFallbackUrl(Uri.parse(BASE_URL))
                                .setIpadFallbackUrl(Uri.parse(BASE_URL))
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource(APP_SOURCE)
                                .setMedium(SOCIAL_MEDIUM)
                                .setCampaign(EXAMPLE_CAMPAIGN)
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(mProduct.getName())
                                .setDescription(mProduct.getDescription())
                                .setImageUrl(Uri.parse(mProduct.getPhotoUrl()))
                                .build())
                .buildDynamicLink();

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLink.getUri())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT);

        return shortLinkTask;
    }

    private void populateScreen() {
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back_white_24);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mProduct != null) {
            mProductPrice.setText("" + mProduct.getPrice());
            mProductCommission.setText("" + mProduct.getCommission());
            mCollapsingToolbar.setTitle(mProduct.getName());
            mProductFullDesc.setText(mProduct.getDescription());
            Glide.with(this)
                    .load(mProduct.getPhotoUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Bitmap bitmap = ((BitmapDrawable) resource.getCurrent()).getBitmap();
                            changeUIColors(bitmap);
                            return false;
                        }
                    })
                    .into(mProductImage);
        }
    }

    private void addFabSubItem() {
        mFabShare.addActionItem(new SpeedDialActionItem.Builder(
                R.id.fab_link, R.drawable.ic_fab_link_white_24)
                .setFabBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .create()
        );
        mFabShare.addActionItem(new SpeedDialActionItem.Builder(
                R.id.fab_facebook, R.drawable.ic_fab_facebook_white)
                .setFabBackgroundColor(getResources().getColor(R.color.com_facebook_blue))
                .create()
        );
        mFabShare.addActionItem(new SpeedDialActionItem.Builder(
                R.id.fab_instagram, R.drawable.ic_fab_instagram_white)
                .setFabBackgroundColor(getResources().getColor(R.color.instagramLogo))
                .create()
        );
        mFabShare.addActionItem(new SpeedDialActionItem.Builder(
                R.id.fab_twitter, R.drawable.ic_fab_twitter_white)
                .setFabBackgroundColor(getResources().getColor(R.color.twiterLogo))
                .create()
        );
    }

    private void changeUIColors(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                int defaultColor = 0xFF333333;
                int darkMutedColor = palette.getDarkMutedColor(defaultColor);
                mMetaBar.setBackgroundColor(darkMutedColor);
                if (mCollapsingToolbar != null){
                    mCollapsingToolbar.setContentScrimColor(darkMutedColor);
                    mCollapsingToolbar.setStatusBarScrimColor(darkMutedColor);
                }
            }
        });
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
}
