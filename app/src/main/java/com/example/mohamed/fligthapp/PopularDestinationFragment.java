package com.example.mohamed.fligthapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
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
public class PopularDestinationFragment extends Fragment {
    ListView popular_destination_list;
    ImageButton pd_search;
    EditText origin_edtxt ;
    String origin_data="";

    Temp t = new Temp();
    List<PopularDestinationClass> pdc_list = new ArrayList<>();
    private DataListener dataListener;

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public PopularDestinationFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popular_destination, container, false);
             origin_edtxt = (EditText) rootView.findViewById(R.id.pd_origin_editText);

             pd_search = (ImageButton) rootView.findViewById(R.id.pd_search_imgbtn);

             pd_search.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (t.isOnline(getActivity())) {
                        // Toast.makeText(getActivity(), " is Online ()", Toast.LENGTH_SHORT).show();
                         origin_data = origin_edtxt.getText().toString();
                         //Toast.makeText(getActivity(), "DATAA : " + origin_data, Toast.LENGTH_SHORT).show();
                         String originCode = t.getCityCode(origin_data, getActivity(), 0);
                         new FetchPopularDestination().execute(originCode);
                     } else {
                      //   Toast.makeText(getActivity(), "IS OFLINE()", Toast.LENGTH_SHORT).show();
                         t.showAlterDialog(getActivity());
                         //   getActivity().finish();
                     }
                 }
             });

             popular_destination_list = (ListView) rootView.findViewById(R.id.pd_listView);

             popular_destination_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     PopularDestinationClass dd = pdc_list.get(i);

                     String data = "Origin: " + dd.getOrigin() + "\nDestination: " + dd.getDestination() + "\nPrice: " + dd.getPrice() + "\nTransfers Number: "
                             + dd.getTransfers() + "\nAirline: " + dd.getAirline() + "\nDeparture Date: " + dd.getDepartDate() + "\nReturn Date: " + dd.getDepartDate() + "\nExpires Date: " + dd.getExpiresDate();

                     dataListener.setFlightData(data);

                 }
             });


        return rootView;
    }

    class FetchPopularDestination extends AsyncTask<String,Void,List<PopularDestinationClass>>
    {
        final String TAG = FetchPopularDestination.class.getSimpleName();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String pd_str = null;

        @Override
        protected void onPreExecute() {
            pdc_list.clear();
        }



        @Override
        protected List<PopularDestinationClass> doInBackground(String... strings) {

            List<PopularDestinationClass> lst = new ArrayList<>();
            try
            {

                final String BASE_URL = "http://api.travelpayouts.com/v1/city-directions?";
                final String TOKEN = "token";
                final String ORIGIN = "origin";
                final String CURRENCY = "currency";

                Uri uri =Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(TOKEN,BuildConfig.FLIGTH_API_KEY)
                        .appendQueryParameter(ORIGIN,strings[0])
                        .appendQueryParameter(CURRENCY,"usd")
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
                    JSONObject jsonObject = object.getJSONObject("data");
                    Iterator<String> it = jsonObject.keys();

                    while(it.hasNext())
                    {
                        //Log.e(TAG,it.next());
                        JSONObject obj = jsonObject.getJSONObject(it.next());
                        PopularDestinationClass pd = new PopularDestinationClass();
                        pd.setOrigin(origin_data);
                        String name = t.getCityCode(obj.getString("destination"),getActivity(),1);
                        pd.setDestination(name);

                        pd.setPrice(obj.getInt("price"));
                        pd.setTransfers(obj.getInt("transfers"));
                        String airLine = t.getAirlineName(obj.getString("airline"),getActivity());
                        pd.setAirline(airLine);
                        pd.setFlightNumber(obj.getInt("flight_number"));
                        pd.setDepartDate(obj.getString("departure_at"));
                        pd.setReturnDate(obj.getString("return_at"));
                        pd.setExpiresDate(obj.getString("expires_at"));
                        lst.add(pd);
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
        protected void onPostExecute(List<PopularDestinationClass> popularDestinationClasses) {

            for (PopularDestinationClass pd:popularDestinationClasses)
            {
                pdc_list.add(pd);
            }
            if(pdc_list.size()==0)
                Toast.makeText(getActivity(), "THERE IS NO DATA FOR THIS DESTINATION", Toast.LENGTH_LONG).show();
            else
                popular_destination_list.setAdapter(new PopularDestinationAdapter(getActivity(),pdc_list));

        }
    }
}
