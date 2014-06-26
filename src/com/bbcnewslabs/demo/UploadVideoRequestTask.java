package com.bbcnewslabs.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

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
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HTTP_UPLOAD_URL);        
        HttpResponse response = null;
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();        
        try {
        	multipartEntityBuilder.addPart("file", new FileBody(new File(uri[0])));        
            httpPost.setEntity( multipartEntityBuilder.build() );
        	response = httpClient.execute( httpPost );
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            return out.toString();
        } catch (ClientProtocolException e) {
        	return "ClientProtocolException: "+e.getMessage();
        } catch (IOException e) {
            return "IOException: "+e.getMessage();
        } catch (Exception e) {
        	return "Other Exception: "+e.getMessage();
        }
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
	       
        // Display back to the user what the asked for news about on screen, as text 
        RemoteViews aRV = new RemoteViews(activity.getPackageName(),R.layout.card_text);
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(activity, "response");           
            aRV.setTextViewText(R.id.main_text, result);
            mLiveCard.setViews(aRV);
            Intent mIntent = new Intent(activity, MainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(activity, 0, mIntent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
        //Do anything with response..
    }	
}