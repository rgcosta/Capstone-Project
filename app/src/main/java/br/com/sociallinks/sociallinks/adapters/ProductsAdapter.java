package br.com.sociallinks.sociallinks.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.models.Product;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private List<Product> mProducts = new ArrayList<>();

    private ProductsOnClickHandler mClickHandler;

    public interface ProductsOnClickHandler {
        void onClick(Product product);
    }

    public ProductsAdapter(ProductsOnClickHandler onClickHandler){
        this.mClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item_product;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        ProductViewHolder viewHolder = new ProductViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder holder, int position) {

        holder.mProductNameTextView.setText(mProducts.get(position).getName());
        holder.mProductPrice.setText("" + mProducts.get(position).getPrice());
        holder.mProductCommission.setText("" + mProducts.get(position).getCommission());

        Glide.with(holder.mProductImageView.getContext())
                .load(mProducts.get(position).getPhotoUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Bitmap bitmap = ((BitmapDrawable) resource.getCurrent()).getBitmap();
                        Palette palette = Palette.from(bitmap).generate();
                        int defaultColor = 0xFF333333;
                        int color = palette.getMutedColor( defaultColor);
                        holder.itemView.setBackgroundColor(color);
                        return false;
                    }
                })
                .into(holder.mProductImageView);
    }

    @Override
    public int getItemCount() {
        if (mProducts == null)
            return 0;
        else
            return mProducts.size();
    }

    public void setProductsData(List<Product> mProducts) {
        this.mProducts = mProducts;
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mProductImageView;
        TextView mProductNameTextView;
        TextView mProductPrice;
        TextView mProductCommission;

        public ProductViewHolder(View itemView){
            super(itemView);
            this.mProductImageView = itemView.findViewById(R.id.iv_product_image);
            this.mProductNameTextView = itemView.findViewById(R.id.tv_product_title);
            this.mProductPrice = itemView.findViewById(R.id.tv_product_price);
            this.mProductCommission = itemView.findViewById(R.id.tv_product_commission);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Product productClicked = mProducts.get(position);
            mClickHandler.onClick(productClicked);
        }
    }
}
