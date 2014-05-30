package com.bbcnewslabs.demo;

import java.util.List;

import com.google.android.glass.timeline.LiveCard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;

/**
 * MainActivity
 */
public class MainActivity extends Activity {

    private boolean mResumed;
    private MainService.MainBinder mService;
    private LiveCard mLiveCard;
    private static final int SPEECH_REQUEST = 0;
    private static final int RESULT_OK = 1;
    private TextToSpeech mSpeech;
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binderService) {
            if(binderService instanceof MainService.MainBinder) {
                mService = (MainService.MainBinder) binderService;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
        }
    };

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mService == null) {
            bindService(new Intent(this, MainService.class), mConnection, 0);
        }        
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //do nothing
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }
    
    @Override
    public void openOptionsMenu() {
        if (mResumed && mService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.speech_input:
                displaySpeechRecognizer();
                return true;
            case R.id.stop:
                stopService(new Intent(this, MainService.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        unbindService(mConnection);
    }

    private void displaySpeechRecognizer() {
    	mSpeech.speak("What would you like to know more about?", TextToSpeech.QUEUE_FLUSH, null);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	//
        if (requestCode == SPEECH_REQUEST ) { // && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            
            // Do something with spokenText.

            // Confirm back to the user what they asked for news about via text-to-speech
            mSpeech.speak("You asked for news about "+spokenText, TextToSpeech.QUEUE_FLUSH, null);

            // Display back to the user what the asked for news about on screen, as text 
            RemoteViews aRV = new RemoteViews(this.getPackageName(),R.layout.card_text);
            if (mLiveCard == null) {
                mLiveCard = new LiveCard(this, "response");
                aRV.setTextViewText(R.id.main_text, "You asked for news about: "+spokenText);
                mLiveCard.setViews(aRV);
                Intent mIntent = new Intent(this, MainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mLiveCard.setAction(PendingIntent.getActivity(this, 0, mIntent, 0));
                mLiveCard.publish(LiveCard.PublishMode.REVEAL);
            }
            
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
