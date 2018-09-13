package br.com.sociallinks.sociallinks;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import br.com.sociallinks.sociallinks.models.Product;
import br.com.sociallinks.sociallinks.utils.FirebasePersistance;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static br.com.sociallinks.sociallinks.ProductDetailActivity.QUERY_KEY;

public class ProductSharedActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProductSharedActivity.class.getSimpleName();
    private static final String PRODUCT_KEY = "product_key";

    @BindView(R.id.tv_product_price_shopScreen) TextView mProductPrice;
    @BindView(R.id.tv_product_commission_shopScreen) TextView mProductCommission;
    @BindView(R.id.tv_product_description_shopScreen) TextView mProductFullDesc;
    @BindView(R.id.iv_product_photo_shopScreen) ImageView mProductImage;
    @BindView(R.id.meta_bar_shopScreen) LinearLayout mMetaBar;
    @BindView(R.id.toolbar_detailed_shopScreen) Toolbar mToolbar;
    @BindView(R.id.collapsingToolbar_layout_shopScreen) CollapsingToolbarLayout mCollapsingToolbar;

    private FirebaseDatabase mDatabase;
    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_shared);

        ButterKnife.bind(this);
        this.mDatabase = FirebaseDatabase.getInstance();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PRODUCT_KEY))
                mProduct = savedInstanceState.getParcelable(PRODUCT_KEY);
        }

        if (mProduct == null) {
            retrieveProductFlow();
        } else {
            Log.e(LOG_TAG, "Reusing mProduct");
            populateScreen();
        }

    }

    private void retrieveProductFlow() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if (pendingDynamicLinkData != null) {

                            Uri deepLink = pendingDynamicLinkData.getLink();
                            Log.e(LOG_TAG, "Link captured: " + deepLink.toString());

                            DatabaseReference dbRefProductId = mDatabase.getReference("products/"
                                    + deepLink.getQueryParameter(QUERY_KEY));

                            dbRefProductId.addValueEventListener(getProductListener());

                        } else {
                            Snackbar.make(findViewById(R.id.movie_detail_container_shopScreen),
                                    getString(R.string.snackbar_retrieve_link_error), LENGTH_LONG)
                                    .show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                });
    }

    @NonNull
    private ValueEventListener getProductListener() {
        return new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mProduct = dataSnapshot.getValue(Product.class);
                    if (mProduct != null) {
                        populateScreen();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(LOG_TAG, "Fail to retrieve product: " + databaseError.toException());
                }
            };
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
    protected void onSaveInstanceState(Bundle outState) {
        if (mProduct != null)
            outState.putParcelable(PRODUCT_KEY, mProduct);

        super.onSaveInstanceState(outState);
    }
}
