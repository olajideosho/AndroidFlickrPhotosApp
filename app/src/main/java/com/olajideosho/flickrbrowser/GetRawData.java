package com.olajideosho.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALIZED, FAILED_OR_EMPTY, OK}

class GetRawData extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";
    private DownloadStatus mDownloadStatus;
    private OnDownloadComplete mCallBack;

    interface OnDownloadComplete{
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callBack) {
        mDownloadStatus = DownloadStatus.IDLE;
        mCallBack = callBack;
    }

    void runInSameThread(String s){
        Log.d(TAG, "runInSameThread: starts");
//        onPostExecute(doInBackground(s));
        //        execute(s);
        if(mCallBack != null){
            mCallBack.onDownloadComplete(doInBackground(s), mDownloadStatus);
        }
        Log.d(TAG, "runInSameThread: ends");
    }

    @Override
    protected void onPostExecute(String s) {
//        Log.d(TAG, "onPostExecute: " + s);
        if(mCallBack != null){
            mCallBack.onDownloadComplete(s, mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        if(strings == null){
            mDownloadStatus = DownloadStatus.NOT_INITIALIZED;
            return null;
        }
        
        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: Response code was: " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//            String line;
//            while(null != (line = reader.readLine())){
            for(String line = reader.readLine(); line != null; line = reader.readLine()){
                result.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e){
            Log.e(TAG, "doInBackground: Invalid URL: " + e.getMessage());
        } catch (IOException e){
            Log.e(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch(SecurityException e){
            Log.e(TAG, "doInBackground: Security Exception. Needs Permissions: " + e.getMessage());
        } finally{
            if(connection != null){
                connection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                } catch (IOException e){
                    Log.e(TAG, "doInBackground: Error closing stream" + e.getMessage());
                }
            }
        }

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
}