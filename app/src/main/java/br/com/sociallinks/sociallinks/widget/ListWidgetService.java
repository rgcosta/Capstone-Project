package br.com.sociallinks.sociallinks.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.models.Link;
import br.com.sociallinks.sociallinks.services.LinksWidgetService;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private List<Link> mLinks = new ArrayList<>();

    public ListRemoteViewsFactory(Context context){
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
        this.mLinks = LinksWidgetService.getLinks();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mLinks == null)
            return 0;
        else
            return mLinks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_view_item);
        Link link = mLinks.get(position);
        int cashMade = (int) (link.getProductPrice()*link.getBuyCounts()
                *(link.getCommission()/100f));

        views.setTextViewText(R.id.tv_buyCounts_widget, "" + link.getBuyCounts());
        views.setTextViewText(R.id.tv_cash_widget, "" + cashMade);

        int width  = (int) mContext.getResources().getDimension(R.dimen.widget_product_image_size);
        int height = (int) mContext.getResources().getDimension(R.dimen.widget_product_image_size);

        RequestBuilder<Bitmap> builder =
                Glide.with(mContext)
                .asBitmap()
                .load(link.getProductPhotoUrl())
                .apply(new RequestOptions().circleCrop());

        FutureTarget<Bitmap> futureTarget = builder.submit(width, height);
        try {
            views.setImageViewBitmap(R.id.iv_product_mini_linkScreen_widget, futureTarget.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
