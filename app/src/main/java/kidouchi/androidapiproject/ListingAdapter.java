package kidouchi.androidapiproject;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.PlusShare;
import com.squareup.picasso.Picasso;

import kidouchi.androidapiproject.api.EtsyApi;
import kidouchi.androidapiproject.google.GoogleServicesHelper;
import kidouchi.androidapiproject.model.ActiveListings;
import kidouchi.androidapiproject.model.Listing;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by iuy407 on 8/29/15.
 */
public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingHolder>
        implements Callback<ActiveListings>, GoogleServicesHelper.GoogleServicesListener {

    public static final int REQUEST_CODE_PLUS_ONE = 10;
    public static final int REQUEST_CODE_SHARE = 11;

    private MainActivity mActivity;
    private LayoutInflater mInflater;
    private ActiveListings mActiveListings;

    private boolean isGooglePlayServiceAvailable;

    public ListingAdapter(MainActivity activity) {
        this.mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
        this.isGooglePlayServiceAvailable = false;
    }

    @Override
    public ListingHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ListingHolder(mInflater.inflate(R.layout.layout_listing, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ListingHolder listingHolder, int i) {
        final Listing listing = mActiveListings.results[i];
        listingHolder.mTitleView.setText(listing.title);
        listingHolder.mPriceView.setText(listing.price);
        listingHolder.mShopNameView.setText(listing.Shop.shop_name);

        listingHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openListing = new Intent(Intent.ACTION_VIEW);
                openListing.setData(Uri.parse(listing.url));
                mActivity.startActivity(openListing);
            }
        });

        if (isGooglePlayServiceAvailable) {
            listingHolder.mPlusOneButton.setVisibility(View.VISIBLE);
            listingHolder.mPlusOneButton.initialize(listing.url, REQUEST_CODE_PLUS_ONE);
            listingHolder.mPlusOneButton.setAnnotation(PlusOneButton.ANNOTATION_NONE);

            listingHolder.mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new PlusShare.Builder(mActivity)
                            .setType("text/plain")
                            .setText("Checkout this item on Etsy " + listing.title)
                            .setContentUrl(Uri.parse(listing.url))
                            .getIntent();
                    mActivity.startActivityForResult(intent, REQUEST_CODE_SHARE);
                }
            });

        } else {
            listingHolder.mPlusOneButton.setVisibility(View.GONE);

            listingHolder.mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Checkout this item on Etsy " + listing.title + " " + listing.url);
                    intent.setType("text/plain");

                    mActivity.startActivityForResult(Intent.createChooser(intent, "Share"), REQUEST_CODE_SHARE);
                }
            });
        }

        Picasso.with(listingHolder.mImageView.getContext())
                .load(listing.Images[0].url_570xN)
                .into(listingHolder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (mActiveListings == null) {
            return 0;
        }

        if (mActiveListings.results == null) {
            return 0;
        }

        return mActiveListings.results.length;
    }

    @Override
    public void success(ActiveListings activeListings, Response response) {
        this.mActiveListings = activeListings;
        notifyDataSetChanged();
        this.mActivity.showList();
    }

    @Override
    public void failure(RetrofitError error) {
//        Log.d("ERROR", error.getUrl());
//        Log.d("ERROR", error.getResponse() + "");
//        Log.d("ERROR", error.getMessage());
        this.mActivity.showError();
    }

    public ActiveListings getActiveListings() {
        return mActiveListings;
    }

    @Override
    public void onConnected() {

        if (getItemCount() == 0) {
            EtsyApi.getActiveListings(this);
        }

        isGooglePlayServiceAvailable = true;
        notifyDataSetChanged();
    }

    @Override
    public void onDisconnected() {

        if (getItemCount() == 0) {
            EtsyApi.getActiveListings(this);
        }

        isGooglePlayServiceAvailable = false;
        notifyDataSetChanged();
    }

    public class ListingHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mTitleView;
        public TextView mShopNameView;
        public TextView mPriceView;
        public PlusOneButton mPlusOneButton;
        public ImageButton mShareButton;

        public ListingHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.listing_image);
            mTitleView = (TextView) itemView.findViewById(R.id.listing_title);
            mShopNameView = (TextView) itemView.findViewById(R.id.listing_shop_name);
            mPriceView = (TextView) itemView.findViewById(R.id.listing_price);
            mPlusOneButton = (PlusOneButton) itemView.findViewById(R.id.listing_plus_one_btn);
            mShareButton = (ImageButton) itemView.findViewById(R.id.listing_share_btn);
        }
    }
}
