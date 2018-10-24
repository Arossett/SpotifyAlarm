package com.example.riza.customalarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.protocol.types.UserStatus;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "b5144d701ee14ed6a3a60b7e33500b3b";
    private static final String REDIRECT_URI = "com.example.riza.customalarm://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private static final String debug = "DebugCustom";
    private static final String error = "ErrorCustom";

    //use to identify the request
    private static final int AUTH_TOKEN_REQUEST_CODE = 1337;

    private static String mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              AuthenticationRequest request = getAuthenticationRequest();
              AuthenticationClient.openLoginActivity((Activity)view.getContext(),AUTH_TOKEN_REQUEST_CODE, request);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            Log.d(debug, "token is "+mAccessToken);
        }else{
            Log.d(debug, "request code is "+requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private AuthenticationRequest getAuthenticationRequest() {
        return
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                        .setScopes(new String[]{"playlist-read-private"})
                        .setShowDialog(false)
                        .build();
    }

    @Override
    protected void onStart(){

        if(mSpotifyAppRemote == null) {
            // Set the connection parameters
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();

            //Connect to local Spotify app
            SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                    new Connector.ConnectionListener() {

                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            Log.d(debug, "Connected! Yay!");
                            connected();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e(error, throwable.getMessage(), throwable);

                            // Something went wrong when attempting to connect! Handle errors here
                        }
                    });
        }

        super.onStart();
    }


    //Action done when Spotify app is connected
    private void connected(){
        if(mSpotifyAppRemote!=null) {

            //Play playlist
            mSpotifyAppRemote.getPlayerApi().play("spotify:user:spotify:playlist:37i9dQZF1DWVFJtzvDHN4L");

            // Subscribe to PlayerState and display current song
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {

                public void onEvent(PlayerState playerState) {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d(debug, "Currently playing : "+track.name + " by " + track.artist.name);
                    }
                }
            });
        }
    }

    @Override
    protected void onStop(){
        SpotifyAppRemote.CONNECTOR.disconnect(mSpotifyAppRemote);
        super.onStop();
    }

    @Override
    protected void onPause(){
        Log.d(debug, "dafuk");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
