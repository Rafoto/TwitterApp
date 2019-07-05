package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {
    TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;
    Context context;
    final int COMPOSE_TWEET_CODE = 20;
    final int REPLY_TWEET_CODE = 90;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);
        getSupportActionBar().setTitle("Twitter");
        miActionProgressItem = findViewById(R.id.miActionProgress);
        context = this;
        populateTimeline();
        // find the RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);
        // init the arraylist
        tweets = new ArrayList<>();
        // construct the adapter from this datasource
        tweetAdapter = new TweetAdapter(tweets);
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                swipeContainer.setRefreshing(false);
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(tweetAdapter);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#243447")));
        populateTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v = (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
//        client.getHomeTimeline(new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // Remember to CLEAR OUT old items before appending in the new ones
//                tweetAdapter.clear();
//                // ...the data has come back, add new items to your adapter...
//                populateTimeline();
//                Log.d("Tweet", "Tweet Refreshed");
//                // Now we call setRefreshing(false) to signal refresh has finished
//                swipeContainer.setRefreshing(false);            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//            }
//
//        });
        showProgressBar();
        tweetAdapter.clear();
        populateTimeline();
        hideProgressBar();
    }

    private void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d("TwitterClient","Timeline Populated");
//                int position = tweetAdapter.getAdapterPosition(); // gets item position
//                if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
//                    Tweet tweet = tweets.get(position);
//                    Intent intent = new Intent(context, BookDetailActivity.class);
//                    intent.putExtra("book", Parcels.wrap(book));
//                    context.startActivity(intent);
//
//                }
                response.toString();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                Log.d("TwitterClient", response.toString());
                // iterate through the JSON array
                // for each entry, deserialize the JSON object
                for (int i = 0; i < response.length(); i++) {

                    // convert each object to a Tweet model
                    // add that Tweet model
                    // notify the adapter
                    Tweet tweet = null;
                    try {
                        tweet = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // COMPOSE_TWEET_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == COMPOSE_TWEET_CODE) {
            // Extract name value from result extras
            Log.d("Activity Result", "Message: " + data.getSerializableExtra("tweet"));
            Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
        if (resultCode == RESULT_OK && requestCode == REPLY_TWEET_CODE) {
            // Extract name value from result extras
            Log.d("Activity Result", "Reply: " + data.getSerializableExtra("tweet"));
            Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
    }

    public void composeButtonClick(View view) {
        int id = view.getId();
        Intent intent = new Intent(this, ComposeActivity.class);
        this.startActivityForResult(intent, COMPOSE_TWEET_CODE);
    }

    public void detailView(View view) {
        if (tweetAdapter.viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
            Tweet tweet = tweets.get(tweetAdapter.viewHolder.getAdapterPosition());
            Intent intent = new Intent(context, ReplyActivity.class);
            intent.putExtra("tweet", Parcels.wrap(tweet));
            startActivity(intent);
        }
    }
//    public void onTweetClicked(View view) {
//
//
//        if (tweetAdapter.viewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
//            Tweet tweet = tweets.get(tweetAdapter.viewHolder.getAdapterPosition());
//            // We can access the data within the views
//            Intent intent = new Intent(context, ReplyActivity.class);
//            intent.putExtra("tweet", Parcels.wrap(tweet));
//            intent.putExtra("user", Parcels.wrap(tweet.user));
//            Log.d("user", tweet.user.name);
//            this.startActivityForResult(intent, REPLY_TWEET_CODE);
//        }
//    }


}

