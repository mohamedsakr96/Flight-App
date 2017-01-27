package com.example.mohamed.fligthapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

@RequiresApi(api = Build.VERSION_CODES.N)
public class CheapestTicketsFragment extends Fragment {
    EditText origin,destination;
    ImageButton departDate,returnDate,search;
    TextView departDateTxt,returnDateTxt;
    ListView cht_list;
    String departData,returnData;
    String destinationD;
    List<PopularDestinationClass> ct_list = new ArrayList<>();
    int d_year,d_month,d_day;
    Temp t = new Temp();

    int r_year,r_month,r_day;
    String origin_data ="";
    String destination_data="";
    String destinationCode;
    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    private DataListener dataListener;



    public CheapestTicketsFragment() {
    }
    Calendar myCalendar= Calendar.getInstance(); //global

    public void showDatePickerDialogD(){

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), d_date, d_year, d_month, d_day);
        datePickerDialog.show();
    }
    public void showDatePickerDialogR(){

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), r_date, r_year, r_month, r_day);
        datePickerDialog.show();
    }



    DatePickerDialog.OnDateSetListener d_date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            d_year = year;
            d_month = monthOfYear + 1;
            d_day = dayOfMonth;
            if(d_month<10)
               departData = d_year+"-0"+d_month;
            else
                departData = d_year+"-"+d_month;
            Toast.makeText(getActivity(), "DER : "+departData, Toast.LENGTH_SHORT).show();
            departDateTxt.setText(departData);

        }

    };
    DatePickerDialog.OnDateSetListener r_date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            r_year = year;
            r_month = monthOfYear + 1;
            r_day = dayOfMonth;
            if(r_month<10)
                returnData = r_year+"-0"+r_month;
            else
                returnData = r_year+"-"+r_month;
            Toast.makeText(getActivity(), "RET : "+returnData, Toast.LENGTH_SHORT).show();
            returnDateTxt.setText(returnData);
        }

    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_cheapest_tickets, container, false);
            origin = (EditText) rootView.findViewById(R.id.ct_origin_editText);
            destination = (EditText) rootView.findViewById(R.id.ct_destination_editText);
            departDate = (ImageButton) rootView.findViewById(R.id.ct_depart_date_imgbtn);
            returnDate = (ImageButton) rootView.findViewById(R.id.ct_return_date_imgbtn);
            search = (ImageButton) rootView.findViewById(R.id.ct_search_imgbtn);
            departDateTxt = (TextView) rootView.findViewById(R.id.ct_depart_date_txtvw);
            returnDateTxt = (TextView) rootView.findViewById(R.id.ct_return_date_txtvw);
            cht_list = (ListView) rootView.findViewById(R.id.ct_listView);

        search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (t.isOnline(getActivity())) {
                        //Toast.makeText(getActivity(), " is Online ()", Toast.LENGTH_SHORT).show();
                        origin_data = origin.getText().toString();
                        destination_data = destination.getText().toString();

                        String originCode = t.getCityCode(origin_data, getActivity(), 0);
                        destinationCode = t.getCityCode(destination_data, getActivity(), 0);
                        //   Toast.makeText(getActivity(), originD+"-"+destinationD+"-"+departData+"-"+returnData, Toast.LENGTH_SHORT).show();
                        new FetchCheapestTickets().execute(new String[]{originCode, destinationCode, returnData, departData});


                    } else {
                      //  Toast.makeText(getActivity(), "IS OFLINE()", Toast.LENGTH_SHORT).show();
                        t.showAlterDialog(getActivity());
                        //  getActivity().finish();
                    }
                }
            });

            cht_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    PopularDestinationClass ct = ct_list.get(i);

                    String data = "Origin: " + origin_data+"\nDestination: "+destination_data+"\nPrice: " + ct.getPrice() + "\nAirline: " + ct.getAirline() + "\nFlight Number: " + ct.getFlightNumber() + "\nDeparture Date: " + ct.getDepartDate() + "\nReturn Date: " + ct.getDepartDate() + "\nExpires Date: " + ct.getExpiresDate();
                    dataListener.setFlightData(data);

                }
            });


            d_year = r_year = myCalendar.get(Calendar.YEAR);
            d_month = r_month = myCalendar.get(Calendar.MONTH);
            d_day = r_day = myCalendar.get(Calendar.DAY_OF_MONTH);
            departDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialogD();
                }
            });
            returnDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialogR();
                }
            });


        return rootView;
    }

    class FetchCheapestTickets extends AsyncTask<String,Void,List<PopularDestinationClass>>
    {
        final String TAG = PopularDestinationFragment.FetchPopularDestination.class.getSimpleName();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String pd_str = null;


        @Override
        protected void onPreExecute() {
            ct_list.clear();
        }



        @Override
        protected List<PopularDestinationClass> doInBackground(String... strings) {

            List<PopularDestinationClass> lst = new ArrayList<>();
            try
            {

//http://api.travelpayouts.com/v1/prices/cheap?origin=MOW&destination=HKT&depart_date=2016-11&return_date=2016-12&token=PutHereYourToken

                final String BASE_URL = "http://api.travelpayouts.com/v1/prices/cheap?";
                final String TOKEN = "token";
                final String ORIGIN = "origin";
                final String DESTINATION = "destination";
                final String DEPARTDATE = "depart_date";
                final String RETURNDATE = "return_date";
               // final String CURRENCY = "currency";

                Uri uri =Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(TOKEN,BuildConfig.FLIGTH_API_KEY)
                        .appendQueryParameter(ORIGIN,strings[0])
                        .appendQueryParameter(DESTINATION,strings[1])
                        .appendQueryParameter(DEPARTDATE,strings[2])
                        .appendQueryParameter(RETURNDATE,strings[3])

                        .build();
                URL url = new URL(uri.toString());
                Log.e(TAG,uri.toString());


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

                    JSONObject object1 = jsonObject.getJSONObject(destinationCode);
                    Iterator<String> it = object1.keys();
                    while (it.hasNext())
                    {
                        JSONObject obj = object1.getJSONObject(it.next());
                        PopularDestinationClass pdd = new PopularDestinationClass();
                        pdd.setOrigin(origin_data);
                        pdd.setDestination(destination_data);
                        pdd.setPrice(obj.getInt("price"));
                        String name = t.getAirlineName(obj.getString("airline"),getActivity());
                        pdd.setAirline(name);
                        pdd.setFlightNumber(obj.getInt("flight_number"));
                        pdd.setDepartDate(obj.getString("departure_at"));
                        pdd.setReturnDate(obj.getString("return_at"));

                        pdd.setExpiresDate(obj.getString("expires_at"));
                        lst.add(pdd);
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

            for (PopularDestinationClass ct:popularDestinationClasses)
            {
                ct_list.add(ct);
            }
            if(ct_list.size()==0)
                Toast.makeText(getActivity(), "THERE IS NO DATA FOR THIS DESTINATION", Toast.LENGTH_LONG).show();
            else
                cht_list.setAdapter(new PopularDestinationAdapter(getActivity(),ct_list));

        }
    }

}
