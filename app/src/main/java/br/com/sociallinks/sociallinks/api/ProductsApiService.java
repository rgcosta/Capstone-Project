package br.com.sociallinks.sociallinks.api;

import java.util.List;

import br.com.sociallinks.sociallinks.models.Product;
import retrofit2.Call;
import retrofit2.http.GET;


import static br.com.sociallinks.sociallinks.utils.NetworkUtils.TOP_PRODUCTS_PATH;

public interface ProductsApiService {

    @GET(TOP_PRODUCTS_PATH)
    Call<List<Product>> getProducts();

}
