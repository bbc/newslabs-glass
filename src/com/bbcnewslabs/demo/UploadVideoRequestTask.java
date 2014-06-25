package com.bbcnewslabs.demo;

import com.google.android.glass.timeline.LiveCard;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

class UploadVideoRequestTask extends AsyncTask<String, String, String>{
	
    private LiveCard mLiveCard;
    private MainActivity activity;

    public void setActivity(MainActivity a) {
    	activity = a;
    }
    
	@Override
    protected String doInBackground(String... uri) {
		// @todo Upload video here
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