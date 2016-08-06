package com.nuclearrambo.hamtuner;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SeekBar coarseFrequencyTuner;
    SeekBar fineFrequencyTuner;
    TextView frequencyTV;
    TextView status;
    double coarseFrequency;
    double fineFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coarseFrequencyTuner = (SeekBar)findViewById(R.id.frequencyBar);
        coarseFrequencyTuner.setMax(20);
        fineFrequencyTuner = (SeekBar)findViewById(R.id.fineFrequencyBar);
        frequencyTV = (TextView)findViewById(R.id.frequencyValue);
        status = (TextView)findViewById(R.id.statusMsg);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        coarseFrequencyTuner.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFrequency();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fineFrequencyTuner.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFrequency();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateFrequency(){
        DecimalFormat df = new DecimalFormat("000.000");
        coarseFrequency = map(coarseFrequencyTuner.getProgress(), 0, 20, 144.000, 146.000);
        fineFrequency = map(fineFrequencyTuner.getProgress(), 0, 100, 000.000, 000.500);
        String frequencyString = String.format("%.4f", coarseFrequency + fineFrequency);
        frequencyTV.setText(frequencyString+"MHz");

        updateHamRadio(frequencyString);
    }

    public void updateHamRadio(String frequency){
        HamUpdateTask updateFrequency = new HamUpdateTask();
        updateFrequency.execute(frequency);
    }

    public double map(int x, float in_min, float in_max, double out_min, double out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private class HamUpdateTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String url = "http://192.168.1.35/submit";
            String response = "";
            int responseCode;
            String frequency = params[0];
            URL hamURL;
            try{
                hamURL = new URL(url);
                HttpURLConnection hamConn = (HttpURLConnection)hamURL.openConnection();
                hamConn.setReadTimeout(15000);
                hamConn.setConnectTimeout(15000);
                hamConn.setRequestMethod("POST");
                hamConn.setDoInput(true);
                hamConn.setDoOutput(true);

                HashMap<String, String> postData = new HashMap<>();
                postData.put("frequency", frequency);
                postData.put("squelch", "1");
                postData.put("submit", "");
                OutputStream os = hamConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8")
                );
                writer.write(getPostDataString(postData));
                writer.flush();
                writer.close();
                os.close();

                responseCode = hamConn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(hamConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }

                }
                else {
                    response="";

                }
                } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.d("debug", response);
            return response;
        }

        @Override
        public void onPostExecute(String response){
            status.setText("STATUS: "+response);
        }
    }
    public String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
