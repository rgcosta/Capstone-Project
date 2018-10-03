package br.com.sociallinks.sociallinks.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.data.DataRewinder;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.sociallinks.sociallinks.R;
import br.com.sociallinks.sociallinks.adapters.LinksAdapter;
import br.com.sociallinks.sociallinks.models.Link;
import br.com.sociallinks.sociallinks.utils.NetworkUtils;
import br.com.sociallinks.sociallinks.utils.RecyclerLinkTouchHelper;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static br.com.sociallinks.sociallinks.utils.NetworkUtils.*;


public class LinksFragment extends Fragment implements LinksAdapter.LinksOnClickHandler,
        RecyclerLinkTouchHelper.RecyclerLinkTouchHelperListener, Parcelable {

    private static final String LOG_TAG = LinksFragment.class.getSimpleName();
    private static final String LINKS_KEY = "links_key";

    private DatabaseReference mDbRefLinks;
    private LinksAdapter mLinksAdapter;
    private RecyclerView mLinksRecyclerView;
    private TextView mNoLinksMessageTextView;
    private ValueEventListener mLinksListener;
    private List<Link> mLinks = new ArrayList<>();

    public LinksFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ValidFragment")
    protected LinksFragment(Parcel in) {
        mLinks = in.createTypedArrayList(Link.CREATOR);
    }

    public static final Creator<LinksFragment> CREATOR = new Creator<LinksFragment>() {
        @Override
        public LinksFragment createFromParcel(Parcel in) {
            return new LinksFragment(in);
        }

        @Override
        public LinksFragment[] newArray(int size) {
            return new LinksFragment[size];
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_links, container, false);

        initializeRecyclerView(rootView);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LINKS_KEY)){
                mLinks = savedInstanceState.getParcelableArrayList(LINKS_KEY);
            }
        }


        if (mLinks.isEmpty() || mLinks == null) {
            retrieveLinksFromServer();
        } else {
            Log.e(LOG_TAG, "Reuses existing links for rotated screen!!");
            mLinksAdapter.setLinksData(mLinks);
            retrieveLinksFromServer();
        }

        return rootView;
    }

    private void retrieveLinksFromServer() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;
        }

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

                if (!linksData.isEmpty()) {
                    mNoLinksMessageTextView.setVisibility(View.INVISIBLE);
                    mLinksRecyclerView.setVisibility(View.VISIBLE);
                    mLinksAdapter.setLinksData(linksData);
                    mLinks = linksData;
                }
                else {
                    mLinksRecyclerView.setVisibility(View.INVISIBLE);
                    mNoLinksMessageTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, "Fail to retrieve data: " + databaseError.toException());
            }
        });
    }

    private void initializeRecyclerView(View rootView) {
        mNoLinksMessageTextView = rootView.findViewById(R.id.tv_no_links_message);
        mLinksRecyclerView = rootView.findViewById(R.id.rv_links);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mLinksRecyclerView.setLayoutManager(layoutManager);
        mLinksRecyclerView.setHasFixedSize(true);

        mLinksRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration decorDivider = new DividerItemDecoration(rootView.getContext(), VERTICAL);
        mLinksRecyclerView.addItemDecoration(decorDivider);

        mLinksAdapter = new LinksAdapter(this);
        mLinksRecyclerView.setAdapter(mLinksAdapter);

        ItemTouchHelper.SimpleCallback linkTouchHelperCallback =
                new RecyclerLinkTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(linkTouchHelperCallback).attachToRecyclerView(mLinksRecyclerView);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDbRefLinks.removeEventListener(mLinksListener);
    }

    @Override
    public void onClick(Link link) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("shortLink", link.getLink());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(mLinksRecyclerView, getString(R.string.snackbar_clipboard), LENGTH_LONG)
                .show();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        Log.e(LOG_TAG, "Link " + position + " deleted!");
        //mLinksAdapter.remoteLinkItem(position);
        final Link linkSwiped = mLinksAdapter.getLinkByPosition(position);
        final int productId = linkSwiped.getProductId();

        mDbRefLinks.child(String.valueOf(productId)).removeValue();

        String shortProductName = linkSwiped.getProductName()
                .substring(0, 12)
                .concat(getString(R.string.concat_suffix_to_short_product_name));
        Snackbar.make(mLinksRecyclerView, shortProductName + " " + getString(R.string.product_removed), LENGTH_LONG)
                .setAction(getString(R.string.undo_product_removed), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDbRefLinks.child(String.valueOf(productId)).setValue(linkSwiped);
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mLinks != null)
            outState.putParcelableArrayList(LINKS_KEY, (ArrayList<? extends Parcelable>) mLinks);

        super.onSaveInstanceState(outState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mLinks);
    }

}
