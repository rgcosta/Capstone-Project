package br.com.sociallinks.sociallinks.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.ProductDetailActivity;
import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.adapters.ProductsAdapter;
import br.com.sociallinks.sociallinks.models.Product;
import br.com.sociallinks.sociallinks.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment implements ProductsAdapter.ProductsOnClickHandler, Parcelable{

    private static final String LOG_TAG = ProductsFragment.class.getSimpleName();
    private static final String LIST_PRODUCTS_KEY = "list_products_key";
    public static final String INTENT_PRODUCT_FLAG = "intent_product_flag";

    private ProductsAdapter mProductsAdapter;
    private List<Product> mProducts = new ArrayList<>();

    public ProductsFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ValidFragment")
    protected ProductsFragment(Parcel in) {
        mProducts = in.createTypedArrayList(Product.CREATOR);
    }

    public static final Creator<ProductsFragment> CREATOR = new Creator<ProductsFragment>() {
        @Override
        public ProductsFragment createFromParcel(Parcel in) {
            return new ProductsFragment(in);
        }

        @Override
        public ProductsFragment[] newArray(int size) {
            return new ProductsFragment[size];
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        initializeRecyclerView(view);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIST_PRODUCTS_KEY)) {
                mProducts = savedInstanceState.getParcelableArrayList(LIST_PRODUCTS_KEY);
            }
        }

        if (mProducts.isEmpty() || mProducts == null) {
            callToProducts();
        } else {
            Log.e(LOG_TAG, "Reuses existing products. Saving data!!");
            mProductsAdapter.setProductsData(mProducts);
        }

        return view;
    }

    private void initializeRecyclerView(View view) {
        RecyclerView productsRecyclerView = view.findViewById(R.id.recycler_view);

        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(getResources().getInteger(R.integer.grid_span_count),
                        StaggeredGridLayoutManager.VERTICAL);
        productsRecyclerView.setLayoutManager(layoutManager);
        productsRecyclerView.setHasFixedSize(true);

        mProductsAdapter = new ProductsAdapter(this);
        productsRecyclerView.setAdapter(mProductsAdapter);
    }

    private void callToProducts() {
        Call<List<Product>> call = new NetworkUtils().getProductsApiService().getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                Log.e(LOG_TAG, "Products size: " + response.body().size());

                if (response.isSuccessful()) {
                    mProducts = response.body();
                    mProductsAdapter.setProductsData(mProducts);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (mProducts != null)
            savedInstanceState.putParcelableArrayList(LIST_PRODUCTS_KEY, (ArrayList<? extends Parcelable>) mProducts);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View itemView, Product product) {

        final View productImageView = itemView.findViewById(R.id.iv_product_image);

        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra(INTENT_PRODUCT_FLAG, product);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            getActivity(),
                            productImageView,
                            getString(R.string.product_shared_element_transition)
                    );
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mProducts);
    }
}
