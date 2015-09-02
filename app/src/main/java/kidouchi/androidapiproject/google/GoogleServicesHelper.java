package kidouchi.androidapiproject.google;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

/**
 * Created by iuy407 on 8/30/15.
 */
public class GoogleServicesHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface GoogleServicesListener {
        void onConnected();

        void onDisconnected();
    }

    public static final int REQUEST_CODE_RESOLUTION = -100;
    public static final int REQUEST_CODE_AVAILABILITY = -101;

    private Activity mActivity;
    private GoogleServicesListener mListener;
    private GoogleApiClient mApiClient;

    private static final String mGoogleServerClientId = "964400360548-el2leqdgam0j04085r01uma215e3apo7.apps.googleusercontent.com";

    public GoogleServicesHelper(Activity activity, GoogleServicesListener listener) {
        this.mListener = listener;
        this.mActivity = activity;

        this.mApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API,
                        Plus.PlusOptions.builder()
                                .setServerClientId(mGoogleServerClientId)
                                .build())
                .build();
    }

    public void connect() {
        if (isGooglePlayServicesAvailable()) {
            mApiClient.connect();
        } else {
            mListener.onDisconnected();
        }
    }

    public void disconnect() {
        if (isGooglePlayServicesAvailable()) {
            mApiClient.disconnect();
        } else {
            mListener.onDisconnected();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int availability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        switch (availability) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
                GooglePlayServicesUtil.getErrorDialog(
                        availability, mActivity, REQUEST_CODE_AVAILABILITY).show();
                return false;
            default:
                return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mListener.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mListener.onDisconnected();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                connect();
            }
        } else {
            mListener.onDisconnected();
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RESOLUTION || requestCode == REQUEST_CODE_AVAILABILITY) {
            if (resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                mListener.onDisconnected();
            }
        }
    }
}
