package br.com.sociallinks.sociallinks.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import br.com.sociallinks.sociallinks.api.ProductsApiService;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NetworkUtils {

    private final static String BASE_URL =
            "https://placenpepper.com/android/api/";    //Terminar com / para não causar Exception no Retrofit2

    public final static String TOP_PRODUCTS_PATH = "topTrends/products.json";

    private final Retrofit retrofit;

    public NetworkUtils() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public ProductsApiService getProductsApiService(){
        return this.retrofit.create(ProductsApiService.class);
    }
}
