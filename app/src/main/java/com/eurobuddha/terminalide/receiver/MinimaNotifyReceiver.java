package com.eurobuddha.terminalide.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.minimarex.minimaapi.MinimaAPI;
import org.minimarex.minimaapi.MinimaAPILogger;
import org.minimarex.minimaapi.MinimaAPIMessages;

import java.util.Objects;

public class MinimaNotifyReceiver extends BroadcastReceiver {

    //This DB is static so remains longer than the lifespan of the receiver
    private static ReceiverDB mDB = null;

    @Override
    public void onReceive(Context zContext, Intent zIntent) {

        String action = zIntent.getAction();

        if (Objects.equals(action, MinimaAPIMessages.MINIMA_API_NOTIFY)) {

            //Check MinimaID - this is set by you when you register
            if(!MinimaAPI.checkMinimaID(zContext, zIntent)){
                return;
            }

            //Get the DB
            if(mDB == null){
                mDB = new ReceiverDB(zContext);
            }else{
                if(!mDB.isOpen()){
                    mDB.reOpen();
                }
            }

            //Delete the 0ld
            mDB.deleteOldMessages();

            //Get the data
            String message = zIntent.getStringExtra(MinimaAPIMessages.MINIMA_API_NOTIFY_DATA);

            //Convert to a JSON
            try {
                JSONObject jsonresp = new JSONObject(message);

                String event        = jsonresp.getString("event");
                JSONObject data     = jsonresp.getJSONObject("data");

                //Only the LOGS for now
                if(event.equals("MINIMALOG")){

                    //Insert into DB
                    mDB.insertEvent("MINIMALOG", data.toString());
                }

            } catch (JSONException e) {
                MinimaAPILogger.log("Invalid JSON received from Minima-Core");
            }

        }else{
            MinimaAPILogger.log("Minima Terminal RECEIVED UNKNOWN action : "+action);
        }
    }
}
