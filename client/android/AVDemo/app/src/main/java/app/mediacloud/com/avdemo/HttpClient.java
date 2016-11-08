package app.mediacloud.com.avdemo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by youni on 2016/11/3.
 */

class HttpClient{

    public static String Post(String urlStr, Map<String,Object> body){
        HttpURLConnection conn = null;
        OutputStream out = null;
        BufferedInputStream in = null;

        try {
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", ("application/json; charset=utf-8").replaceAll("\\s", ""));
            conn.setRequestProperty("portal","bj.mediacloud.app");

            out = conn.getOutputStream();

            JSONObject json = new JSONObject();

            for(Map.Entry<String,Object> entry:body.entrySet()){

                json.put(entry.getKey(),entry.getValue());
            }

            String jsonUser = json.toString();

            out.write(jsonUser.getBytes());

            int code = conn.getResponseCode();

            Log.d("HTTP","the response code : " + code);

            if (code == 200){
                in = new BufferedInputStream(conn.getInputStream());

                int len = -1;
                byte[] buff = new byte[1024];

                ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

                while((len = in.read(buff)) > 0){
                    bout.write(buff,0,len);
                }

                bout.close();
                String jsonStr = new String(bout.toByteArray());

                Log.i("TAG", "recv the body : " + jsonStr);

                return jsonStr;
            }else{
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                conn.disconnect();
            }

            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    public static String Get(String urlStr, Map<String,String> header){
        HttpURLConnection conn = null;
        OutputStream out = null;
        BufferedInputStream in = null;

        try {
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", ("application/json; charset=utf-8").replaceAll("\\s", ""));
            conn.setRequestProperty("portal","bj.mediacloud.app");


            if(header != null){
                for(Map.Entry<String,String> entry:header.entrySet()){

                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }

            }

            int code = conn.getResponseCode();

            Log.d("HTTP","the response code : " + code);

            if (code == 200){
                in = new BufferedInputStream(conn.getInputStream());

                int len = -1;
                byte[] buff = new byte[1024];

                ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);

                while((len = in.read(buff)) > 0){
                    bout.write(buff,0,len);
                }

                bout.close();
                String jsonStr = new String(bout.toByteArray());

                Log.i("TAG", "recv the body : " + jsonStr);

                return jsonStr;
            }else{
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                conn.disconnect();
            }

            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }
}