package br.com.sociallinks.sociallinks.widget;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.models.Link;

import static br.com.sociallinks.sociallinks.utils.NetworkUtils.*;

public class LinksWidgetService extends IntentService {

    private static final String LOG_TAG = LinksWidgetService.class.getSimpleName();
    public static final String ACTION_RETRIEVE_LINKS = "br.com.sociallinks.sociallinks.widget.action.retrieve_links";
    private static final String NOTIFICATION_CHANNEL_ID = "notification_channel_id";

    private static DatabaseReference mDbRefLinks;
    private static ValueEventListener mLinksListener;
    private static List<Link> mLinks = new ArrayList<>();

    public LinksWidgetService() {
        super(LOG_TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        Notification notification =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_link_24)
                        .setContentTitle(getString(R.string.app_name) + " " + getString(R.string.channel_name))
                        .setContentText(getString(R.string.widget_description))
                        .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.widget_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RETRIEVE_LINKS.equals(action)) {
                handleActionRetrieveLinks();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    private void handleActionRetrieveLinks() {
        Log.e(LOG_TAG, "handleActionRetrieveLinks");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDbRefLinks = database.getReference(SHARES_PATH).child(user.getUid())
                .child(LINKS_PATH);
        mLinksListener = mDbRefLinks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(LOG_TAG, "Items: " + dataSnapshot.getChildrenCount());

                List<Link> linksData = new ArrayList<>();
                for (DataSnapshot linkSnapshot : dataSnapshot.getChildren()) {
                    Link link = linkSnapshot.getValue(Link.class);
                    linksData.add(link);
                }

                Log.e(LOG_TAG, "TOTAL: " + linksData.size());

                mLinks = linksData;
                Context ct = getApplicationContext();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ct);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(ct, LinksWidgetProvider.class));

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_widget);
                LinksWidgetProvider.updateAppWidgets(ct, appWidgetManager, appWidgetIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, "Fail to retrieve data: " + databaseError.toException());
            }
        });
    }

    public static List<Link> getLinks() {
        if (mLinks.isEmpty())
            return null;
        else
            return mLinks;
    }

    public static void startActionRetrieveLinks(Context context) {
        Intent intent = new Intent(context, LinksWidgetService.class);
        intent.setAction(ACTION_RETRIEVE_LINKS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void removeLinksListener() {
        if (mDbRefLinks != null) {
            mDbRefLinks.removeEventListener(mLinksListener);
            Log.e(LOG_TAG, "removeLinksListener");
        }
    }
}
