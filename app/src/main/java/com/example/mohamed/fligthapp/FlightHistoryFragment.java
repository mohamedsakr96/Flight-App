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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
public class FlightHistoryFragment extends Fragment {
    EditText origin,destination;
    Spinner sort,tripClass;
    ImageButton search;
    ListView fh_listView;
    String [] sorting = {"Sorted By","route","price","distance_unit_price"};
    String [] tripCls = {"Trip Class","Economy Class","Business Class","First Class"};
    ArrayAdapter<String> adapter1,adapter2;
    int data_trip_class =0;
    String data_sorting_by = "price";
    List<AlternativeDirectionsClass> fh_list = new ArrayList<>();
    Temp t = new Temp();
    String origin_data;
    String destination_data ;
    private DataListener dataListener;

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public FlightHistoryFragment() {
    }

    /*
    public String getCityCode(String name)
    {
        String code="";
        try{
            InputStream is = getActivity().getAssets().open("cont.json");
            int size =is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            Toast.makeText(getActivity(), new String(buffer), Toast.LENGTH_SHORT).show();
            JSONObject jsonObject = new JSONObject(new String(buffer));
            Iterator<String> it = jsonObject.keys();
            while(it.hasNext())
            {
                String x=it.next();
                if(name.equalsIgnoreCase(jsonObject.getString(x))) {
                    code = x;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }
*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flight_history, container, false);

            origin = (EditText) rootView.findViewById(R.id.fh_origin_editText);
            destination = (EditText) rootView.findViewById(R.id.fh_destination_editText);
            sort = (Spinner) rootView.findViewById(R.id.spinner_sorting_by);
            tripClass = (Spinner) rootView.findViewById(R.id.spinner_trip_class);
            fh_listView = (ListView) rootView.findViewById(R.id.fh_listView);
            search = (ImageButton) rootView.findViewById(R.id.fh_search_imgbtn);
            adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sorting);
            adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tripCls);
            sort.setAdapter(adapter1);
            tripClass.setAdapter(adapter2);
/*
        if(savedInstanceState!=null)
        {
            origin.setText(savedInstanceState.getString("origin_key"));
            destination.setText(savedInstanceState.getString("destination_key"));
            sort.setSelection(savedInstanceState.getInt("sort_key"));
            tripClass.setSelection(savedInstanceState.getInt("class_trip_key"));
        }
*/
            sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i != 0)
                        data_sorting_by = ((TextView) view).getText().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            tripClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i != 0)
                        data_trip_class = i - 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (t.isOnline(getActivity())) {
                        //Toast.makeText(getActivity(), " is Online ()", Toast.LENGTH_SHORT).show();
                        origin_data = origin.getText().toString();
                        destination_data = destination.getText().toString();

                        String originCode = t.getCityCode(origin_data, getActivity(), 0);
                        String destinationCode = t.getCityCode(destination_data, getActivity(), 0);
                      //  Toast.makeText(getActivity(), originCode + " --- " + destinationCode, Toast.LENGTH_SHORT).show();
                        new FetchFlightHistory().execute(new String[]{originCode, destinationCode, data_sorting_by, data_trip_class + ""});
                    } else {
                    //    Toast.makeText(getActivity(), "IS OFLINE()", Toast.LENGTH_SHORT).show();
                        t.showAlterDialog(getActivity());
                        //   getActivity().finish();
                    }
                }
            });
            fh_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    AlternativeDirectionsClass alt = fh_list.get(i);

                    String data ="Origin: " + origin_data + "\nDestination: " + destination_data+ "\nPrice: " + alt.getValue() + "\nTransfers Number: "
                            + alt.getTransfers() + "\nDistance: "
                            + alt.getDistance() + "\nDeparture Date: " + alt.getDepartDate() + "\nReturn Date: " + alt.getReturnDate() + "\nFound Date: " + alt.getFoundDate();


                    dataListener.setFlightData(data);

                    Toast.makeText(getActivity(), alt.getOrigin() + "--" + alt.getDestination() + "--" + alt.getFoundDate(), Toast.LENGTH_SHORT).show();


                }
            });
        return rootView;
    }


    class FetchFlightHistory extends AsyncTask<String,Void,List<AlternativeDirectionsClass>>
    {
        final String TAG = FetchFlightHistory.class.getSimpleName();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String pd_str = null;

        @Override
        protected void onPreExecute() {
            fh_list.clear();
        }



        @Override
        protected List<AlternativeDirectionsClass> doInBackground(String... strings) {

            List<AlternativeDirectionsClass> lst = new ArrayList<>();
            try
            {

//http://api.travelpayouts.com/v2/prices/latest?currency=usd&period_type=year&page=1&limit=30&show_to_affiliates=true&sorting=price&trip_class=0&token=PutHereYourToken
                final String BASE_URL = "http://api.travelpayouts.com/v2/prices/latest?";
                final String TOKEN = "token";
                final String ORIGIN = "origin";
                final String DESTINATION = "destination";
                final String CURRENCY = "currency";
                final String PAGE = "page";
                final String PERIOD_TYPE = "period_type";
                final String LIMIT = "limit";
                final String SORTING = "sorting";
                final String TRIP_CLASS = "trip_class";
                final String SHOW_AFFILIATES  = "show_to_affiliates";

                Uri uri =Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(TOKEN,BuildConfig.FLIGTH_API_KEY)
                        .appendQueryParameter(ORIGIN,strings[0])
                        .appendQueryParameter(CURRENCY,"usd")
                        .appendQueryParameter(PAGE,"1")
                        .appendQueryParameter(LIMIT,"30")
                        .appendQueryParameter(PERIOD_TYPE,"year")
                        .appendQueryParameter(SORTING,strings[2])
                        .appendQueryParameter(TRIP_CLASS,strings[3])
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
                        alc.setOrigin(origin_data);
                        alc.setDestination(destination_data);
                        alc.setReturnDate(obj.getString("return_date"));
                        alc.setDepartDate(obj.getString("depart_date"));
                        alc.setFoundDate(obj.getString("found_at"));

                        alc.setDistance(obj.getInt("distance"));
                        alc.setTransfers(obj.getInt("number_of_changes"));
                        alc.setValue(obj.getDouble("value"));
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
                Log.e(TAG,"XXXXXXXXXXXXXXXXXXXX");
            }





            return lst;
        }

        @Override
        protected void onPostExecute(List<AlternativeDirectionsClass> alternativeDirectionsClasses) {

            for (AlternativeDirectionsClass ad:alternativeDirectionsClasses)
            {
                fh_list.add(ad);
            }

            if(fh_list.size()==0)
                Toast.makeText(getActivity(), "THERE IS NO DATA FOR THIS DESTINATION", Toast.LENGTH_LONG).show();
            else
                fh_listView.setAdapter(new AlternativeDirectionAdapter(getActivity(),fh_list));

        }
    }
}

