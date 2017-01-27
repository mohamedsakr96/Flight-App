package com.example.mohamed.fligthapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.media.TransportMediator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AlternativeDirectionsFragment extends Fragment {
    List<AlternativeDirectionsClass> ad_list = new ArrayList<>();
    EditText ad_origin;
    EditText ad_destination;
    ListView ad_listView;
    ImageButton ad_imgbtn;
    Temp t = new Temp();
    String originData;
    String destinationData;
    private DataListener dataListener;


    public AlternativeDirectionsFragment() {
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alternative_directions, container, false);
            ad_origin = (EditText) rootView.findViewById(R.id.ad_origin_editText);
            ad_destination = (EditText) rootView.findViewById(R.id.ad_destination_editText);
            ad_listView = (ListView) rootView.findViewById(R.id.ad_listView);
            ad_imgbtn = (ImageButton) rootView.findViewById(R.id.ad_search_imgbtn);

            ad_imgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (t.isOnline(getActivity())) {
                       // Toast.makeText(getActivity(), " is Online ()", Toast.LENGTH_SHORT).show();
                        originData = ad_origin.getText().toString();
                        destinationData = ad_destination.getText().toString();
                        String originCode = t.getCityCode(originData, getActivity(), 0);
                        String destinationCode = t.getCityCode(destinationData, getActivity(), 0);
                        //Toast.makeText(getActivity(), "DATAAA: " + originCode + "  -  " + destinationCode, Toast.LENGTH_SHORT).show();
                        new FetchAlternativeDirections().execute(new String[]{originCode, destinationCode});
                    } else {
                        //Toast.makeText(getActivity(), "IS OFLINE()", Toast.LENGTH_SHORT).show();
                        t.showAlterDialog(getActivity());
                        // getActivity().finish();
                    }
                }
            });
            ad_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    AlternativeDirectionsClass al = ad_list.get(i);
                    String trip = "";
                    if (al.getTripClass() == 0)
                        trip = "Economy Class";
                    else if (al.getTripClass() == 1)
                        trip = "Business Class";
                    else if (al.getTripClass() == 2)
                        trip = "First Class";


                    String data = "Origin: " + al.getOrigin() + "\nDestination: " + al.getDestination() + "\nPrice: " + al.getValue() + "\nTransfers Number: "
                            + al.getTransfers() + "\nGate: " + al.getGate() + "\nDeparture Date: " + al.getDepartDate() + "\nReturn Date: " + al.getReturnDate() + "\nFound Date: " + al.getFoundDate()
                            + "\nTirp Class: " + trip + "\nDuration: " + al.getDuration() + "\nDistance: " + al.getDistance();

                    dataListener.setFlightData(data);
                   // Toast.makeText(getActivity(), al.getOrigin() + "--" + al.getDestination() + "--" + al.getFoundDate(), Toast.LENGTH_SHORT).show();
                }
            });

        return rootView;
    }


    class FetchAlternativeDirections extends AsyncTask<String,Void,List<AlternativeDirectionsClass>>
    {
        final String TAG = FetchAlternativeDirections.class.getSimpleName();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String pd_str = null;

        @Override
        protected void onPreExecute() {
            ad_list.clear();
        }



        @Override
        protected List<AlternativeDirectionsClass> doInBackground(String... strings) {

            List<AlternativeDirectionsClass> lst = new ArrayList<>();
            try
            {

//http://api.travelpayouts.com/v2/prices/nearest-places-matrix?currency=usd&origin=LED&destination=HKT&show_to_affiliates=true&token=9f0e17f160e47dfdedcb8f04ec27689e

                final String BASE_URL = "http://api.travelpayouts.com/v2/prices/nearest-places-matrix?";
                final String TOKEN = "token";
                final String ORIGIN = "origin";
                final String DESTINATION = "destination";
                final String CURRENCY = "currency";
                final String SHOW_AFFILIATES  = "show_to_affiliates";

                Uri uri =Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(TOKEN,BuildConfig.FLIGTH_API_KEY)
                        .appendQueryParameter(ORIGIN,strings[0])
                        .appendQueryParameter(CURRENCY,"usd")
                        .appendQueryParameter(SHOW_AFFILIATES,"true")
                        .appendQueryParameter(DESTINATION,strings[1])
                        .build();
                URL url = new URL(uri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while(((line = reader.readLine())!= null))
                {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0)
                    return null;
                pd_str = buffer.toString();

                JSONObject object = new JSONObject(pd_str);
                if(object.getBoolean("success"))
                {

                    JSONArray jsonArray = object.getJSONArray("data");
                    int len = jsonArray.length();
                    for (int i = 0 ; i <len;i++)
                    {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        AlternativeDirectionsClass alc = new AlternativeDirectionsClass();
                        alc.setOrigin(originData);
                        alc.setDestination(destinationData);
                        alc.setReturnDate(obj.getString("return_date"));
                        alc.setDepartDate(obj.getString("depart_date"));
                        alc.setFoundDate(obj.getString("found_at"));
                        alc.setGate(obj.getString("gate"));
                        alc.setDistance(obj.getInt("distance"));
                        alc.setTransfers(obj.getInt("number_of_changes"));
                        alc.setValue(obj.getDouble("value"));
                        alc.setDuration(obj.getString("duration"));
                        alc.setTripClass(obj.getInt("trip_class"));
                        lst.add(alc);
                    }

                }


            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();

            }





            return lst;
        }

        @Override
        protected void onPostExecute(List<AlternativeDirectionsClass> alternativeDirectionsClasses) {

            for (AlternativeDirectionsClass ad:alternativeDirectionsClasses)
            {
                ad_list.add(ad);
            }

            if(ad_list.size()==0)
                Toast.makeText(getActivity(), "THERE IS NO DATA FOR THIS DESTINATION", Toast.LENGTH_LONG).show();
            else
                ad_listView.setAdapter(new AlternativeDirectionAdapter(getActivity(),ad_list));

        }
    }
}
