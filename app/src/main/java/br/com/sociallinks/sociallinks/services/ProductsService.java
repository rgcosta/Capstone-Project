package br.com.sociallinks.sociallinks.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.models.Product;
import br.com.sociallinks.sociallinks.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsService extends IntentService {

    private static final String LOG_TAG = ProductsService.class.getSimpleName();
    private static final String ACTION_RETRIEVE_PRODUCTS =
            "br.com.sociallinks.sociallinks.action.retrieve_products";
    public static final String BROADCAST_ACTION_PRODUCTS_RETRIEVED =
            "br.com.sociallinks.sociallinks.action.products_retrieved";
    public static final String INTENT_EXTRA_PRODUCTS_FLAG = "intent_extra_products_flag";

    public ProductsService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_PRODUCTS.equals(action)) {
                retrieveProducts();
            }
        }
    }

    private void retrieveProducts() {
        Call<List<Product>> call = new NetworkUtils().getProductsApiService().getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                Log.e(LOG_TAG, "Products size: " + response.body().size());

                if (response.isSuccessful()) {
                    Intent intentBroadcast = new Intent();
                    intentBroadcast.setAction(BROADCAST_ACTION_PRODUCTS_RETRIEVED);
                    intentBroadcast.putParcelableArrayListExtra(
                            INTENT_EXTRA_PRODUCTS_FLAG,
                            (ArrayList<? extends Parcelable>) response.body()
                    );
                    sendBroadcast(intentBroadcast);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    public static void startActionRetrieveProducts(Context context){
        Intent intent = new Intent(context, ProductsService.class);
        intent.setAction(ACTION_RETRIEVE_PRODUCTS);
        context.startService(intent);
    }
}
