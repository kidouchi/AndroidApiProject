package kidouchi.androidapiproject.api;

import kidouchi.androidapiproject.model.ActiveListings;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by iuy407 on 8/29/15.
 */
public interface Api {

    @GET("/listings/active")
    void activeListings(@Query("includes") String includes,
                        Callback<ActiveListings> callback);
}
