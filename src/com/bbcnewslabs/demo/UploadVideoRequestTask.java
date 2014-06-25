package com.bbcnewslabs.demo;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.glass.timeline.LiveCard;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

class UploadVideoRequestTask extends AsyncTask<String, String, String>{
	
    private LiveCard mLiveCard;
    private MainActivity activity;
    private static final String HTTP_UPLOAD_URL = "http://localhost/~iain/video/upload.php";
    
    public void setActivity(MainActivity a) {
    	activity = a;
    }
    
	@Override
    protected String doInBackground(String... uri) {
		// Upload video to server
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(HTTP_UPLOAD_URL);        
        HttpResponse response = null;
        
        try {
        	// @todo Get stream for video from uri, add stream to http post
            response = httpclient.execute( httppost );
            response.getEntity().getContent().close();
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        	return "ClientProtocolException: "+e.getMessage();
        } catch (IOException e) {
            //TODO Handle problems..
        	if (response != null){
        	    StatusLine statusLine = response.getStatusLine();
        	    return statusLine.getStatusCode()+":"+uri[0];
        	} else {
        		return uri[0];
        	}
        	//return "IOException: "+e.getMessage();
        } catch (Exception e) {
        	return "Other Exception: "+e.getMessage();
        }
        
		return uri[0];
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
	       
        // Display back to the user what the asked for news about on screen, as text 
        RemoteViews aRV = new RemoteViews(activity.getPackageName(),R.layout.card_text);
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(activity, "response");           
            aRV.setTextViewText(R.id.main_text, "Video saved to "+result);
            mLiveCard.setViews(aRV);
            Intent mIntent = new Intent(activity, MainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(activity, 0, mIntent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
        //Do anything with response..
    }	
}