package com.helloworldappclub.ARChemistry;

import com.unity3d.player.*;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnityPlayerActivity extends Activity
{
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
	private int currentCID=-1;
    private JSONObject atoms;
    private JSONObject bonds;
    private JSONObject coords;
    private int counter=0;

    private final int TEST_CID=15600;
    //15600 decane
    //297 methane
    //2519 caffeine
    //5957 ATP
    //962 water
    //24526 diatomic chlorine
    //702 ethanol


	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(R.layout.activity_main);
		FrameLayout f=(FrameLayout)findViewById(R.id.activity_main_frameLayout);
		f.addView(mUnityPlayer);
		mUnityPlayer.requestFocus();
	}

	public void selectCompound(View view){
        PubChemConnection p=new PubChemConnection();
		p.loadCID(TEST_CID+counter, new PubChemConnection.PubChemDataListener() {
			@Override
			public void onSuccess(String message) {
				Log.d("Success",message);
                try {
                    JSONObject json=new JSONObject(message);
                    JSONObject pc_compounds=json.getJSONArray("PC_Compounds").getJSONObject(0);
                    atoms=pc_compounds.getJSONObject("atoms");
                    bonds=pc_compounds.getJSONObject("bonds");
                    coords=pc_compounds.getJSONArray("coords").getJSONObject(0);
                    Log.d("atoms",atoms.toString());
                    Log.d("bonds",bonds.toString());
                    Log.d("coords",coords.toString());
                    currentCID=TEST_CID+counter;
                    counter++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

			@Override
			public void onFailure(String message) {
				Log.d("Failure",message);
			}

			@Override
			public void onCancelled() {
				Log.d("Cancelled","Cancelled");
			}
		});
	}

    public int[] jsonArrayToIntArray(JSONArray jsonArray){
        int[] i=new int[jsonArray.length()];
        for(int j=0;j<jsonArray.length();j++){
            try {
                i[j]=jsonArray.getInt(j);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    public double[] jsonArrayToDoubleArray(JSONArray jsonArray){
        double[] i=new double[jsonArray.length()];
        for(int j=0;j<jsonArray.length();j++){
            try {
                i[j]=jsonArray.getDouble(j);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return i;
    }
	public int getCID(){
        return currentCID;
    }
    public int[] getAID(){
        try {
            return jsonArrayToIntArray(atoms.getJSONArray("aid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int[] getElement(){
        try {
            return jsonArrayToIntArray(atoms.getJSONArray("element"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int[] getAID1(){
        try {
            return jsonArrayToIntArray(bonds.getJSONArray("aid1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int[] getAID2(){
        try {
            return jsonArrayToIntArray(bonds.getJSONArray("aid2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int[] getOrder(){
        try {
            return jsonArrayToIntArray(bonds.getJSONArray("order"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public double[] getXCoords(){
        try {
            return jsonArrayToDoubleArray(coords.getJSONArray("conformers").getJSONObject(0).getJSONArray("x"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public double[] getYCoords(){
        try {
            return jsonArrayToDoubleArray(coords.getJSONArray("conformers").getJSONObject(0).getJSONArray("y"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public double[] getZCoords(){
        try {
            return jsonArrayToDoubleArray(coords.getJSONArray("conformers").getJSONObject(0).getJSONArray("z"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
	@Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
