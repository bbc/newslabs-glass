package com.bbcnewslabs.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.glass.timeline.LiveCard;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.widget.RemoteViews;

class RequestTask extends AsyncTask<String, String, String>{

    private LiveCard mLiveCard;
    private static final int SPEECH_REQUEST = 0;
    private static final int RESULT_OK = 1;
    private TextToSpeech mSpeech;
    private MainActivity activity;
    
    public void setActivity(MainActivity a) {
    	activity = a;
    }
    
    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        String responseString = null;
        
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String httpResponse = out.toString();
                
                responseString = "Sorry unable to fetch news, please try again";
                JSONObject jsonObject = new JSONObject(httpResponse); 
                JSONArray articles = jsonObject.getJSONArray("articles");
                JSONObject article = articles.getJSONObject(0);                
                responseString = article.getString("description");
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
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
        return responseString;
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