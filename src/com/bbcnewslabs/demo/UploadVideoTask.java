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
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.RemoteViews;

class UploadVideoTask extends AsyncTask<String, String, String>{
	
    private LiveCard mLiveCard;
    private MainActivity activity;
    private static final String HTTP_UPLOAD_URL = "http://10.100.85.187/~iain/video/upload.php";
    
    public void setActivity(MainActivity a) {
    	System.out.println("In setActivity...");
    	activity = a;
    }
    
	@Override
    protected String doInBackground(String... uri) {
		
		return uri[0];
		
		// @fixme Uploading disabled for demo
		/*
		System.out.println("In doInBackground...");
		// Upload video to server	
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(HTTP_UPLOAD_URL);        
        HttpResponse response = null;
        try {
        	System.out.println("Attempting to create MultipartEntityBuilder ...");
        	MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        	System.out.println("Adding file to upload...");
        	multipartEntityBuilder.addPart("file", new FileBody(new File(uri[0])));        
            httpPost.setEntity( multipartEntityBuilder.build() );
            System.out.println("Uploading file...");
        	response = httpClient.execute( httpPost );
        	System.out.println("File uploaded.");
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            System.out.println("File uploaded, response received.");
            return out.toString();
        } catch (ClientProtocolException e) {
        	return "ClientProtocolException: "+e.getMessage();
        } catch (IOException e) {
            return "IOException: "+e.getMessage();
        } catch (Exception e) {
        	return "Other Exception: "+e.getMessage();
        }
        */
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    
        activity.mSpeech.speak("Please suggest a title", TextToSpeech.QUEUE_FLUSH, null);
        
        // @TODO Add result (with video URI) to response data
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        activity.startActivityForResult(intent, MainActivity.SAVE_VIDEO_RESPONSE);
    }	
}