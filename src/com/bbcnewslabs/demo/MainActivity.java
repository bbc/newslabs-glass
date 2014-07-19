package com.bbcnewslabs.demo;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;

/**
 * MainActivity
 */
public class MainActivity extends Activity {

    private boolean mResumed;
    private MainService.MainBinder mService;
    private TextToSpeech mSpeech;
    private String apiKey = "yD21N69ilVPsRAQICpFmEF8IWkMPfga0";
    private static final int SPEECH_REQUEST = 0;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    
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
            case R.id.record_video:
            	startRecordingVideo();
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

    private void startRecordingVideo() {
    	System.out.println("In startRecordingVideo...");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }    
    }
    
    private void displaySpeechRecognizer() {
    	mSpeech.speak("What would you like to know more about?", TextToSpeech.QUEUE_FLUSH, null);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);    	
    	
    	try {
	        if (requestCode == SPEECH_REQUEST ) { // && resultCode == RESULT_OK) {
	            List<String> results = intent.getStringArrayListExtra(
	                    RecognizerIntent.EXTRA_RESULTS);
	            String spokenText = results.get(0);
	            
	            // Do something with spokenText.
	
	            // Confirm back to the user what they asked for news about via text-to-speech
	            mSpeech.speak("You asked for news about "+spokenText, TextToSpeech.QUEUE_FLUSH, null);
	            
	            String escapedSpokenText = "Breaking news";
				try {
					escapedSpokenText = java.net.URLEncoder.encode(spokenText, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
	            // Set the URL
	            String url = "http://data.bbc.co.uk/bbcrd-juicer/articles.json?text="+escapedSpokenText+"&product[]=NewsWeb&content_format[]=TextualFormat&apikey="+apiKey;
	            
	            // Get news headlines
	            GetNewsHeadlinesRequestTask rq = new GetNewsHeadlinesRequestTask();
		        rq.setActivity(this);
		        rq.execute(url);
		        
	        } else if (requestCode == REQUEST_VIDEO_CAPTURE) {	        	
	        	String videoUri = intent.getExtras().getString(com.google.android.glass.content.Intents.EXTRA_VIDEO_FILE_PATH);
	            System.out.println("Got Video URI: "+videoUri);
	
	            UploadVideoRequestTask rq = new UploadVideoRequestTask();
		        rq.setActivity(this);
		        rq.execute( videoUri );
	        }
        
        } catch (Exception e) {
        	System.out.println("EXCEPTION: "+e.getMessage() );
        	System.out.println("EXCEPTION AS STRING: "+e.toString() );
        }
    }
}
