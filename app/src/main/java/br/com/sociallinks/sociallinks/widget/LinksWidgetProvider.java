package br.com.sociallinks.sociallinks.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import br.com.sociallinks.sociallinks.ProductsActivity;
import br.com.sociallinks.sociallinks.R;

/**
 * Implementation of App Widget functionality.
 */
public class LinksWidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = LinksWidgetProvider.class.getSimpleName();

     static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.links_widget_provider);
        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.list_view_widget, intent);

        Intent intentClicHandle = new Intent(context, ProductsActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intentClicHandle, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.iv_product_mini_linkScreen_widget, pendingIntent);

        views.setEmptyView(R.id.list_view_widget, R.id.empty_view);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        LinksWidgetService.startActionRetrieveLinks(context);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        int[] appWidgetIds){
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.e(LOG_TAG, "onDeleted");
        for (int appWidgetId : appWidgetIds) {
            LinksWidgetService.removeLinksListener();
        }

        super.onDeleted(context, appWidgetIds);
    }
}

