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

class SearchNewsTask extends AsyncTask<String, String, String> {

    private LiveCard mLiveCard;
    private MainActivity activity;
    private String[] headlines = new String[5];
    
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
                
                responseString = "Search failed, please try again";
                JSONObject jsonObject = new JSONObject(httpResponse); 
                JSONArray articles = jsonObject.getJSONArray("articles");
                JSONObject article = articles.getJSONObject(0);                
                responseString = article.getString("description");
               
                for (int i = 0; i < articles.length(); i++) {
                	try {
                		JSONObject a = articles.getJSONObject(i);
                		if (i <= headlines.length)
                			headlines[i] = new String(a.getString("description"));
                	} catch (Exception e) { }
                }
                
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
        RemoteViews aRV = new RemoteViews(activity.getPackageName(),R.layout.headline_search_result);
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(activity, "headline");           
            aRV.setTextViewText(R.id.headline, "");
            aRV.setTextViewText(R.id.source, "");
            mLiveCard.setViews(aRV);
            Intent mIntent = new Intent(activity, MainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(activity, 0, mIntent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
            
            for (int i = 0; i < headlines.length; i++) {
            	try {
        			activity.mSpeech.speak(headlines[i], TextToSpeech.QUEUE_FLUSH, null);
    				aRV.setTextViewText(R.id.headline, headlines[i]);
    				aRV.setTextViewText(R.id.source, "BBC News");
    				mLiveCard.setViews(aRV);
        			Thread.sleep(10000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
            }
            
        }
    }
}