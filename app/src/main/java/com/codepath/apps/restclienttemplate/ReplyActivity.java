package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ReplyActivity extends AppCompatActivity {
    TwitterClient client;
    EditText etTweetEdit;
    TextView tvNumCharacters;
    Context context;
    final int RESULT_OK = 10;
    final int RESULT_NO_TWEET = 18;
    Tweet replyTweet;
    User replyUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        client = TwitterApp.getRestClient(this);
        etTweetEdit = findViewById(R.id.editTweet);
        tvNumCharacters = findViewById(R.id.tvNumCharacters);
        context = this;
        replyTweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        replyUser = replyTweet.user;
        etTweetEdit.setText(String.format("@%s", replyUser.screenName));
        String message = (279 - replyUser.screenName.length()) + " characters remaining";
        tvNumCharacters.setText(message);
        getSupportActionBar().setTitle(String.format("Reply to %s", replyUser.screenName));


        etTweetEdit.addTextChangedListener(new TextWatcher() {
                                               @Override
                                               public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                   String message = (280 - s.length()) + " characters remaining";
                                                   tvNumCharacters.setText(message);
                                               }

                                               @Override
                                               public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                               }

                                               @Override
                                               public void afterTextChanged(Editable s) {

                                               }
                                           }
        );


    }

    public void onSubmit(View v) {
        client.replyTweet(etTweetEdit.getText().toString(), replyTweet.uid, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Tweet tweet = Tweet.fromJSON(response);
                            Intent intent = new Intent(context, TimelineActivity.class);
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                }
        );

    }

    public void goBack(View view) {
        Intent intent = new Intent(context, TimelineActivity.class);
        setResult(RESULT_NO_TWEET, intent);
        finish();
    }

}
