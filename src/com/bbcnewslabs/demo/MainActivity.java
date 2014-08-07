package com.bbcnewslabs.demo;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.google.android.glass.timeline.LiveCard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
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
    private String apiKey = "yD21N69ilVPsRAQICpFmEF8IWkMPfga0"; //@fixme should not be hard coded or in the repo!
    public TextToSpeech mSpeech;
    public static final int REQUEST_SEARCH_NEWS = 0;
    public static final int REQUEST_VIDEO_CAPTURE = 1;
    public static final int SAVE_VIDEO_RESPONSE = 2;
    
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
        	case R.id.news_headlines:
        		displayLatestHeadlines();
        		return true;
            case R.id.search_news:
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
    	mSpeech.speak("What would you like to more about?", TextToSpeech.QUEUE_FLUSH, null);
    	try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, REQUEST_SEARCH_NEWS);
    }

    private void displayLatestHeadlines() {
    	mSpeech.speak("Here are the latest headlines", TextToSpeech.QUEUE_FLUSH, null);
		// Get a recent article (may not actually be important breaking news!)
		String url = "http://data.bbc.co.uk/bbcrd-juicer/articles.json?product[]=NewsWeb&content_format[]=TextualFormat&apikey="+apiKey;
        SearchNewsTask rq = new SearchNewsTask();
        rq.setActivity(this);
        rq.execute(url);		        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);    	
    	
    	try {
    		if (requestCode == REQUEST_SEARCH_NEWS ) { // && resultCode == RESULT_OK) {
	            List<String> results = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            String spokenText = results.get(0);
	            
	            // Do something with spokenText.
	
	            // Confirm back to the user what they asked for news about via text-to-speech
	            mSpeech.speak("The latest news about "+spokenText, TextToSpeech.QUEUE_FLUSH, null);
	            
	            String escapedSpokenText = "";
				try {
					escapedSpokenText = java.net.URLEncoder.encode(spokenText, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
	            // Set the URL
	            String url = "http://data.bbc.co.uk/bbcrd-juicer/articles.json?text="+escapedSpokenText+"&product[]=NewsWeb&content_format[]=TextualFormat&apikey="+apiKey;
	            
	            // Get news headlines
	            SearchNewsTask rq = new SearchNewsTask();
		        rq.setActivity(this);
		        rq.execute(url);
	        } else if (requestCode == REQUEST_VIDEO_CAPTURE) {	        	
	        	String videoUri = intent.getExtras().getString(com.google.android.glass.content.Intents.EXTRA_VIDEO_FILE_PATH);
	            System.out.println("Got Video URI: "+videoUri);
	
	            UploadVideoTask rq = new UploadVideoTask();
		        rq.setActivity(this);
		        rq.execute( videoUri );
	        } else if (requestCode == SAVE_VIDEO_RESPONSE) {

	            List<String> results = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            String spokenText = results.get(0);
	            
	            // Display back to the user what the asked for news about on screen, as text 
	            RemoteViews aRV = new RemoteViews(this.getPackageName(), R.layout.video_upload);
	            
	            LiveCard mLiveCard = new LiveCard(this, "response");           
                aRV.setTextViewText(R.id.title, spokenText);
                aRV.setTextViewText(R.id.progress, "UPLOAD COMPLETE");
                mLiveCard.setViews(aRV);
                Intent mIntent = new Intent(this, MainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mLiveCard.setAction(PendingIntent.getActivity(this, 0, mIntent, 0));
                mLiveCard.publish(LiveCard.PublishMode.REVEAL);
	            
	        }

        } catch (Exception e) {
        	System.out.println("EXCEPTION: "+e.getMessage() );
        	System.out.println("EXCEPTION AS STRING: "+e.toString() );
        }
    }
}
