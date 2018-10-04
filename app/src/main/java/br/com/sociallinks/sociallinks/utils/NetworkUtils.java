package br.com.sociallinks.sociallinks.utils;


import br.com.sociallinks.sociallinks.api.ProductsApiService;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NetworkUtils {

    private final static String BASE_URL =
            "https://placenpepper.com/android/api/";    //Terminar com / para não causar Exception no Retrofit2

    public final static String TOP_PRODUCTS_PATH = "topTrends/products.json";

    public static final String SHARES_PATH = "shares";
    public static final String LINKS_PATH = "links";
    public static final String PRODUCTS_PATH = "products";
    public static final String USERNAME_FIELD = "userName";

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
