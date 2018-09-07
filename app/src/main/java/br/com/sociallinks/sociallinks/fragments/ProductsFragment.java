package br.com.sociallinks.sociallinks.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class ProductsFragment extends Fragment implements ProductsAdapter.ProductsOnClickHandler {

    private static final String LOG_TAG = ProductsFragment.class.getSimpleName();
    private static final String LIST_PRODUCTS_KEY = "list_products_key";
    public static final String INTENT_PRODUCT_FLAG = "intent_product_flag";

    private ProductsAdapter mProductsAdapter;
    private List<Product> mProducts = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public ProductsFragment() {
        // Required empty public constructor
    }


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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra(INTENT_PRODUCT_FLAG, product);
        startActivity(intent);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
