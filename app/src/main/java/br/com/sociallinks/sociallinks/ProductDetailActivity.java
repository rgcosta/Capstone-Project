package br.com.sociallinks.sociallinks;

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

import java.util.concurrent.Executor;

import br.com.sociallinks.sociallinks.fragments.ProductsFragment;
import br.com.sociallinks.sociallinks.models.Product;
import butterknife.BindView;
import butterknife.ButterKnife;

import static br.com.sociallinks.sociallinks.fragments.ProductsFragment.INTENT_PRODUCT_FLAG;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProductDetailActivity.class.getSimpleName();

    @BindView(R.id.tv_product_price_detailScreen) TextView mProductPrice;
    @BindView(R.id.tv_product_commission_detailScreen) TextView mProductCommission;
    @BindView(R.id.tv_product_description) TextView mProductFullDesc;
    @BindView(R.id.iv_product_photo_detail) ImageView mProductImage;
    @BindView(R.id.meta_bar) LinearLayout mMetaBar;
    @BindView(R.id.toolbar_detailed) Toolbar mToolbar;
    @BindView(R.id.collapsingToolbar_layout) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.fab_share) FloatingActionButton mFabShare;

    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_PRODUCT_FLAG)) {
            mProduct = intent.getParcelableExtra(INTENT_PRODUCT_FLAG);
        }

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
        mFabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.placenpepper.com/"))
                .setDynamicLinkDomain("sociallinks.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder("com.example.ios")
                                .setAppStoreId("123456789")
                                .setMinimumVersion("1.0.1")
                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .setItunesConnectAnalyticsParameters(
                        new DynamicLink.ItunesConnectAnalyticsParameters.Builder()
                                .setProviderToken("123456")
                                .setCampaignToken("example-promo")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("Example of a Dynamic Link")
                                .setDescription("This link works whether the app is installed or not!")
                                .build())
                .buildDynamicLink();  // Or buildShortDynamicLink()

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dynamicLink.getUri())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.e(LOG_TAG, shortLink.toString());
                        } else {
                            // Error
                            // ...
                        }
                    }
                });
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
