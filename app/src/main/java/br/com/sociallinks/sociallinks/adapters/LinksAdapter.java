package br.com.sociallinks.sociallinks.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.models.Link;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.LinkViewHolder> {

    private List<Link> mLinks = new ArrayList<>();

    private LinksOnClickHandler mClickHandler;

    public interface LinksOnClickHandler {
        void onClick(Link link);
    }

    public LinksAdapter(LinksOnClickHandler linksOnClickHandler){
        this.mClickHandler = linksOnClickHandler;
    }


    @NonNull
    @Override
    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item_link;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        LinkViewHolder viewHolder = new LinkViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {

        int cashMade = (int) (mLinks.get(position).getProductPrice()*mLinks.get(position).getBuyCounts()
                *(mLinks.get(position).getCommission()/100f));

        holder.mProductName.setText(mLinks.get(position).getProductName());
        holder.mLinkAdress.setText(mLinks.get(position).getLink());
        holder.mBuyCounts.setText(String.valueOf(mLinks.get(position).getBuyCounts()));
        holder.mCashMade.setText(String.valueOf(cashMade));

        Glide.with(holder.mProductMiniPhoto.getContext())
                .load(mLinks.get(position).getProductPhotoUrl())
                .apply(new RequestOptions().circleCrop())
                .into(holder.mProductMiniPhoto);
    }

    @Override
    public int getItemCount() {
        if (mLinks == null)
            return 0;
        else
            return mLinks.size();
    }

    public void setLinksData(List<Link> mLinks) {
        this.mLinks = mLinks;
        notifyDataSetChanged();
    }

    public Link getLinkByPosition(int position) {
        return mLinks.get(position);
    }

    public class LinkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_product_mini_linkScreen) ImageView mProductMiniPhoto;
        @BindView(R.id.tv_product_title_linkScreen) TextView mProductName;
        @BindView(R.id.tv_product_link) TextView mLinkAdress;
        @BindView(R.id.tv_buyCounts) TextView mBuyCounts;
        @BindView(R.id.tv_cash) TextView mCashMade;
        public @BindView(R.id.view_background) RelativeLayout viewBackground;
        public @BindView(R.id.view_foreground) ConstraintLayout viewForeground;


        public LinkViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int linkPosition = getAdapterPosition();
            Link linkClicked = mLinks.get(linkPosition);
            mClickHandler.onClick(linkClicked);
        }
    }
}
