package com.helloworldappclub.ARChemistry;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Justin on 9/24/2016.
 */
public class PubChemConnection {
    public interface PubChemDataListener{
        public void onSuccess(String message);
        public void onFailure(String message);
        public void onCancelled();
    }

    public void loadCID(int CID,PubChemDataListener listener){
        ConnectionThread t=new ConnectionThread();
        t.init(CID,listener);
        t.start();
    }
    private class ConnectionThread extends Thread{
        int CID;
        PubChemDataListener listener;
        public void init(int CID,PubChemDataListener listener){
            this.CID=CID;
            this.listener=listener;
        }
        public void run(){
            try {
                //297 is methane
                URL url=new URL("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/"+Integer.toString(CID)+"/record/JSON/?record_type=3d&response_type=display");
                HttpsURLConnection uc=(HttpsURLConnection)url.openConnection();
                uc.setRequestMethod("GET");

                int code=-1;
                long start=System.currentTimeMillis();
                while(code==-1){
                    code=uc.getResponseCode();
                    if(System.currentTimeMillis()-start>10000){//server timed out, call appropriate listener method
                        listener.onCancelled();
                        return;
                    }
                }
                String body="";
                if (code == 200) {//action was successful
                    //parse message
                    InputStream is = new BufferedInputStream(uc.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        body += line;
                    }
                    listener.onSuccess(body);
                } else {//action failed
                    //parse error message
                    InputStream is = new BufferedInputStream(uc.getErrorStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        body += "" + line;
                    }
                    //call listener failure and pass error message
                    listener.onFailure(body);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
