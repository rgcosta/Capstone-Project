package br.com.sociallinks.sociallinks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.concurrent.Executor;

import br.com.sociallinks.sociallinks.fragments.ProductsFragment;
import br.com.sociallinks.sociallinks.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;

import static br.com.sociallinks.sociallinks.fragments.ProductsFragment.INTENT_PRODUCT_FLAG;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProductDetailActivity.class.getSimpleName();
    private static final String BASE_URL = "https://www.sociallinks.com";
    private static final String PATH_URL = "product";
    private static final String QUERY_KEY = "id";
    private static final String DYNAMIC_LINK_DOMAIN = "sociallinks.page.link";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_PRODUCT_FLAG)) {
            mProduct = intent.getParcelableExtra(INTENT_PRODUCT_FLAG);
        }

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

                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newRawUri("shortLink", mShortDynamicLink);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(ProductDetailActivity.this, "Link copied to your clipboard.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(LOG_TAG, "Fail to generate new dynamic link: " +
                                        task.getException());
                                }
                            }
                        });
                        return false;
                    case R.id.fab_facebook:
                        Toast.makeText(ProductDetailActivity.this, "Facebook", Toast.LENGTH_SHORT).show();
                        return false;
                    case R.id.fab_instagram:
                        Toast.makeText(ProductDetailActivity.this, "Instagram", Toast.LENGTH_SHORT).show();
                        return false;
                    case R.id.fab_twitter:
                        Toast.makeText(ProductDetailActivity.this, "Twitter", Toast.LENGTH_SHORT).show();
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    private Task<ShortDynamicLink> createDynamicLink() {
        Uri fullLinkUri = Uri.withAppendedPath(Uri.parse(BASE_URL), PATH_URL).buildUpon()
                .appendQueryParameter(QUERY_KEY, String.valueOf(mProduct.getId())).build();

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(fullLinkUri)
                .setDynamicLinkDomain(DYNAMIC_LINK_DOMAIN)
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .setIosParameters(
                        //for test purposes
                        new DynamicLink.IosParameters.Builder("com.sociallinks.ios")
                                .setAppStoreId("123456789")
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("app")
                                .setMedium("social")
                                .setCampaign("example-promo")
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
}
