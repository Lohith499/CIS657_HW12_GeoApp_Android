/**
 * Created by Lohith and Brain
 */

package com.example.lohit.hw4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.lohit.hw4.webservice.WeatherService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.parceler.Parcels;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.lohit.hw4.webservice.WeatherService.*;
import static android.text.TextUtils.split;
import static com.example.lohit.hw4.webservice.WeatherService.BROADCAST_WEATHER;



public class HomePage extends AppCompatActivity  {
    DatabaseReference topRef;
    EditText x1,y1,x2,y2;
    Button bcal,bclear,search;
    TextView dist,bear,error, p1Temp,p2Temp,p1Summary,p2Summary;
    String dmeasure, bmeasure;
    ImageView p1Icon,p2Icon;
    public static int HISTORY_RESULT = 2;
    public static int SETTINGS_RESULT = 1;
    public static int LOCATION_SEARCH = 3;



    public static List<LocationLookup> allHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Intent intentcheck = getIntent();

        x1 = (EditText) findViewById(R.id.lat1);
        y1 = (EditText) findViewById(R.id.long1);
        x2 = (EditText) findViewById(R.id.lat2);
        y2 = (EditText) findViewById(R.id.long2);
        search = (Button) findViewById(R.id.button3);
        bcal = (Button) findViewById(R.id.bCalculate);
        bclear = (Button) findViewById(R.id.bClear);
        dist = (TextView) findViewById(R.id.textViewdistance);
        bear = (TextView) findViewById(R.id.textViewbearinf);
        p1Temp= (TextView) findViewById(R.id.temp1);
        p2Temp= (TextView) findViewById(R.id.temp2);
        p1Summary= (TextView) findViewById(R.id.summary1);
        p2Summary= (TextView) findViewById(R.id.summary2);
        p1Icon = (ImageView) findViewById(R.id.icon1);
        p2Icon = (ImageView) findViewById(R.id.icon2);

        allHistory = new ArrayList<LocationLookup>();


        if (intentcheck.hasExtra("dselected")){
            dmeasure = getIntent().getStringExtra("dselected");
        }else
        {
            dmeasure= "Kilometers";
        }
        if (intentcheck.hasExtra("bselected")){
            bmeasure = getIntent().getStringExtra("bselected");
        }else
        {
            bmeasure= "Degrees";
        }

        if (intentcheck.hasExtra("coordindate")){

            String[] s = split(getIntent().getStringExtra("coordindate"),",");
            x1.setText(s[0]);
            y1.setText(s[1]);
            x2.setText(s[2]);
            y2.setText(s[3]);
            update();
        }else
        {

        }


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomePage.this, LocationSearchActivity.class);
                startActivityForResult(intent, LOCATION_SEARCH );


            }
        });


        //error = (TextView) findViewById(R.id.editText);

        bcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                keyboardhide();
                update();

            }
        });
        bclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardhide();
                x1.setText("");
                x2.setText("");
                y1.setText("");
                y2.setText("");
                dist.setText("");
                bear.setText("");
                //error.setText("");
                setWeatherViews(View.INVISIBLE);
            }
        });


    }

    private void setWeatherViews(int visible) {
        p1Icon.setVisibility(visible);
        p2Icon.setVisibility(visible);
        p1Summary.setVisibility(visible);
        p2Summary.setVisibility(visible);
        p1Temp.setVisibility(visible);
        p2Temp.setVisibility(visible);
    }
    public void update(){
        String sx1 = x1.getText().toString();
        String sy1 = y1.getText().toString();
        String sx2 = x2.getText().toString();
        String sy2 = y2.getText().toString();

        if (sx1.length()==0 || sx2.length()==0 || sy1.length()==0 || sy2.length()==0)
        {
            // error.setText("Please Enter all fields");
            return;
        }

        Location loc1 = new Location("");
        loc1.setLatitude(Double.parseDouble(x1.getText().toString()));
        loc1.setLongitude(Double.parseDouble(y1.getText().toString()));


        Location loc2 = new Location("");
        loc2.setLatitude(Double.parseDouble(x2.getText().toString()));
        loc2.setLongitude(Double.parseDouble(y2.getText().toString()));
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        Double lat1D;
        Double lon1D;
        Double lat2D;
        Double lon2D;

        lat1D = (Double.parseDouble(sx1));
        lon1D = (Double.parseDouble(sy1));
        lat2D = (Double.parseDouble(sx2));
        lon2D = (Double.parseDouble(sy2));


        LocationLookup entry = new LocationLookup();
        entry.setOrigLat(lat1D);
        entry.setOrigLng(lon1D);
        entry.setEndLat(lat2D);
        entry.setEndLng(lon2D);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        entry.setTimestamp(fmt.print(DateTime.now()));
        topRef.push().setValue(entry);



        float distanceInMeters = (loc1.distanceTo(loc2) / 1000) ; //kms

        if (dmeasure.compareTo("Kilometers") != 0){

            distanceInMeters = distanceInMeters * Float.valueOf("1.6");
            Double d = Double.parseDouble(Float.toString(distanceInMeters));

            dist.setText( df.format(d)+" Miles");

        } else {
            Double d = Double.parseDouble(Float.toString(distanceInMeters));
            dist.setText(df.format(d)+" Kms");
        }


        float bearingbetween = (loc1.bearingTo(loc2)); //degress

        if (bmeasure.compareTo("Degrees") != 0){

            bearingbetween = bearingbetween * Float.valueOf("17.777");

            Double b = Double.parseDouble(Float.toString(bearingbetween));

            bear.setText(df.format(b)+"Mils");
        } else {
            Double b = Double.parseDouble(Float.toString(bearingbetween));
            bear.setText(df.format(b)+"Degrees");
        }

        WeatherService.startGetWeather(this, Double.toString(lat1D), Double.toString(lon1D), "p1");
        WeatherService.startGetWeather(this, Double.toString(lat2D), Double.toString(lon2D), "p2");
        setWeatherViews(View.VISIBLE);
        /*HistoryContent.HistoryItem item = new
                HistoryContent.HistoryItem(this.x1.getText().toString(),
                this.y1.getText().toString(), this.x2.getText().toString(), this.y2.getText().toString(), DateTime.now());
        HistoryContent.addItem(item); */
    }


    @Override
    public void onResume(){
        super.onResume();
        allHistory.clear();

        topRef = FirebaseDatabase.getInstance().getReference("history");
        topRef.addChildEventListener (chEvListener);
        IntentFilter weatherFilter = new IntentFilter(BROADCAST_WEATHER);
        LocalBroadcastManager.getInstance(this).registerReceiver(weatherReceiver,
                weatherFilter);
         setWeatherViews(View.INVISIBLE);
        //topRef.addValueEventListener(valEvListener);
    }
    @Override
    public void onPause(){
        super.onPause();
        topRef.removeEventListener(chEvListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(weatherReceiver);
    }


    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           Log.d("LOHITH", "onReceive: " + intent);
            Bundle bundle = intent.getExtras();
            double temp = bundle.getDouble("TEMPERATURE");
            String summary = bundle.getString("SUMMARY");
            String icon = bundle.getString("ICON").replaceAll("-", "_");
            String key = bundle.getString("KEY");
            int resID = getResources().getIdentifier(icon , "drawable",
                    getPackageName());
            setWeatherViews(View.VISIBLE);
            if (key.equals("p1")) {
                p1Summary.setText(summary);
                p1Temp.setText(Double.toString(temp));
                p1Icon.setImageResource(resID);
                p1Icon.setVisibility(View.INVISIBLE);
            } else {
                p2Summary.setText(summary);
                p2Temp.setText(Double.toString(temp));
                p2Icon.setImageResource(resID);
            }
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.settings:
                Intent intent1 =new Intent(this,Settings.class);
                String sx1 = x1.getText().toString();
                String sy1 = y1.getText().toString();
                String sx2 = x2.getText().toString();
                String sy2= y2.getText().toString();
                String s =  sx1+","+sy1+","+sx2+","+sy2;
                intent1.putExtra("dselected", dmeasure);
                intent1.putExtra("bselected", bmeasure);
                intent1.putExtra("coordindate", s);
                this.startActivity(intent1);

                return true;

            case R.id.action_history:
                Intent intent = new Intent(HomePage.this, HistoryActivity.class);
                startActivityForResult(intent, HISTORY_RESULT );
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SETTINGS_RESULT) {
            this.dmeasure = data.getStringExtra("bearingUnits");
            this.bmeasure = data.getStringExtra("distanceUnits");
            update();
        } else if (resultCode == HISTORY_RESULT) {
            String[] vals = data.getStringArrayExtra("item");
            this.x1.setText(vals[0]);
            this.y1.setText(vals[1]);
            this.x2.setText(vals[2]);
            this.y2.setText(vals[3]);
            this.update(); // code that updates the calcs.
        }else if(requestCode == LOCATION_SEARCH) {
            Parcelable par = data.getParcelableExtra("TRIP");
            LocationLookup locationLookup = Parcels.unwrap(par);
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            String s1,s2,s3,s4;
            s1= df.format(locationLookup.getOrigLat())+"";
            s2= df.format(locationLookup.getOrigLng())+"";
            s3= df.format(locationLookup.getEndLat())+"";
            s4= df.format(locationLookup.getEndLng())+"";
            this.x1.setText(s1);
            this.y1.setText(s2);
            this.x2.setText(s3);
            this.y2.setText(s4);

           /* this.x1.setText(String.valueOf(locationLookup.getOrigLat()));
            this.y1.setText(String.valueOf(locationLookup.getOrigLng()));
            this.x2.setText(String.valueOf(locationLookup.getEndLat()));
            this.y2.setText(String.valueOf(locationLookup.getEndLng())); */
            this.update();
        }
    }

    public void keyboardhide(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(x1.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(y1.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(x2.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(y2.getWindowToken(), 0);
    }



    private ChildEventListener chEvListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LocationLookup entry = (LocationLookup)
                    dataSnapshot.getValue(LocationLookup.class);
            entry.setKey(dataSnapshot.getKey());
            allHistory.add(entry);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            LocationLookup entry = (LocationLookup)
                    dataSnapshot.getValue(LocationLookup.class);
            List<LocationLookup> newHistory = new ArrayList<LocationLookup>();
            for (LocationLookup t : allHistory) {
                if (!t.getKey().equals(dataSnapshot.getKey())) {
                    newHistory.add(t);
                }
            }
            allHistory = newHistory;
        }
        @Override

        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

}
