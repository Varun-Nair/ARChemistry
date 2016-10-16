package com.helloworldappclub.ARChemistry;

import com.unity3d.player.*;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UnityPlayerActivity extends Activity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private int currentCID=-1;
    private String currentMolecule="";
    private JSONObject atoms;
    private JSONObject bonds;
    private JSONObject coords;
    private int counter=0;

    private final int TEST_CID=15600;
    private HashMap<String,Integer> results;
    private ArrayList<String> moleculeNames;
    private SearchView searchView;
    private ListView moleculeList;

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

        results=new HashMap<>();
        searchView = (SearchView) findViewById(R.id.searchview);
        moleculeList = (ListView) findViewById(R.id.listview);
        moleculeNames=new ArrayList<>();
        moleculeList.setVisibility(View.GONE);
        moleculeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name=moleculeNames.get(position);
                name=name.substring(0,name.indexOf(";"));
                currentMolecule=name;
                loadCID(results.get(moleculeNames.get(position)));
                moleculeList.setVisibility(View.GONE);
            }
        });

        searchView.setQueryHint("Molecule Name");

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchAsyncTask s=new SearchAsyncTask();
                s.setText(query);
                s.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
    }

    public class SearchAsyncTask extends AsyncTask<String, Void, String>{
        private String searchTerm;
        public void setText(String searchTerm){
            this.searchTerm=searchTerm;
        }

        @Override
        protected String doInBackground(String... arg0) {
            Log.d("Searchterm",searchTerm);
           // "term=((%22name%22%5BSynonym%5D)%20AND%201%3A3%5BConformerCount3D%5D)"
            try {
                Log.d("lol","lol");
                Log.d("lololol","lol");
                Document doc1 = Jsoup.connect("https://www.ncbi.nlm.nih.gov/pccompound?term=(%22"+searchTerm+"%22%5BSynonym%5D)%20AND%201%3A3%5BConformerCount3D%5D").get();
                Log.d("Document", doc1.toString());
                Elements elements = doc1.getElementsByClass("title");
                Document doc2 = Jsoup.parse(elements.toString());
                Elements linkElements = doc2.select("a");
//                Log.d("H3H3", elements.toString());
                Log.d("Links", linkElements.toString());
                results=new HashMap<>();
                moleculeNames=new ArrayList<>();
                for(int i=0;i<linkElements.size();i++){
                    Element e=linkElements.get(i);
                    String name=e.html();
                    String id=e.attr("href");
                    id=id.replace("//pubchem.ncbi.nlm.nih.gov/compound/","");
                    int CID=Integer.decode(id);
                    name=name.replace("<b>","");
                    name=name.replace("</b>","");
                    Log.d("Element",name);
                    Log.d("CID",Integer.toString(CID));
                    results.put(name,CID);
                    moleculeNames.add(name);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        moleculeList.setVisibility(View.VISIBLE);
                        ArrayAdapter a=new ArrayAdapter(getBaseContext(),android.R.layout.simple_list_item_1,moleculeNames);
                        moleculeList.setAdapter(a);
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
                Log.d("ExceptionError", e.toString());
            }
                return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("ARChemistry Result", result);
        }
    }

    public void loadCID(int CID){
        PubChemConnection p=new PubChemConnection();
        p.loadCID(CID, new PubChemConnection.PubChemDataListener() {
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

    public void selectCompound(View view){

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
    public String getMolecule(){
        return currentMolecule;
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


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Log.d("pressed","pressed");
        if(moleculeList.getVisibility()==View.VISIBLE){
            Log.d("Invisible","invisible");
            moleculeList.setVisibility(View.GONE);
        }
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
