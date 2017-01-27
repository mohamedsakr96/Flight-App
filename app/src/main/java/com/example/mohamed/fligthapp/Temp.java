package com.example.mohamed.fligthapp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by mohamed on 1/19/17.
 */

public class Temp {


    public String getCityCode(String name, Context c,int num)
    {


        String code="";
        try{
            InputStream is = c.getAssets().open("city.json");
            int size =is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            JSONObject jsonObject = new JSONObject(new String(buffer));
            if(num==0) {

                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String x = it.next();
                    if (name.equalsIgnoreCase(jsonObject.getString(x))) {
                        code = x;
                        break;
                    }
                }

                return code;
            }
            else
            {
                code = jsonObject.getString(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    public String getAirlineName(String code,Context c)
    {
        String name="";
        try{
            InputStream is = c.getAssets().open("airline.json");
            int size =is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            JSONObject jsonObject = new JSONObject(new String(buffer));
                name = jsonObject.getString(code);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    //check Network
    public boolean isOnline(Context c)
    {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return  networkInfo!= null && networkInfo.isConnectedOrConnecting();
    }
    public void showAlterDialog(final Context c)
    {
        new AlertDialog.Builder(c).setTitle("Connection Error")
                .setIcon(R.mipmap.disconnect)
                .setMessage("Please Your Connection").setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(c, "Please Conecct To Internet", Toast.LENGTH_SHORT).show();

            }
        }).show();
    }
}
