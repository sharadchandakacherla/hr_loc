package org.marco45.polarheartmonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.test.mock.MockPackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/**
 * This program connect to a bluetooth polar heart rate monitor and display data
 *
 * @author Marco
 */
public class    MainActivity extends Activity implements OnItemSelectedListener, Observer {

    private FirebaseAnalytics mFirebaseAnalytics;
    private int MAX_SIZE = 60; //graph max size
    boolean searchBt = true;
    BluetoothAdapter mBluetoothAdapter;
    List<BluetoothDevice> pairedDevices = new ArrayList<>();
    boolean menuBool = false; //display or not the disconnect option
    ///////
    Button btnShowLocation;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;



    ////////
    GPStracker gps;

    boolean h7 = false; //Was the BTLE tested
    boolean normal = false; //Was the BT tested
    private Spinner spinner1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Main Activity", "Starting Polar HR monitor main activity");
        DataHandler.getInstance().addObserver(this);
        /////////location
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                //execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        btnShowLocation = (Button) findViewById(R.id.button2);

        final TextView tx1=(TextView)findViewById(R.id.Text1);
        final TextView tx2=(TextView)findViewById(R.id.Text2);


        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPStracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    String lat=Double.toString(latitude);
                    String longi=Double.toString(longitude);
                    // \n is for new line
                    tx1.setText(lat);
                    tx2.setText(longi);
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });







        /////////location

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."


        // Start loading the ad in the background.


        //Verify if device is to old for BTLE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {

            Log.i("Main Activity", "old device H7 disbled");
            h7 = true;
        }

        //verify if bluetooth device are enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (DataHandler.getInstance().newValue) {
            //Verify if bluetooth if activated, if not activate it
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter != null){
                if (!mBluetoothAdapter.isEnabled()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.bluetooth)
                            .setMessage(R.string.bluetoothOff)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mBluetoothAdapter.enable();
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    listBT();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    searchBt = false;
                                }
                            })
                            .show();
                } else {
                    listBT();
                }
            }



            // Create Graph

        //LOAD Graph


        //ANALYTIC
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Main");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        //t = GoogleAnalytics.getInstance(this).newTracker("UA-51478243-1");
        //#t.setScreenName("Polar main page");
        //#t.send(new HitBuilders.AppViewBuilder().build());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    }

    protected void onDestroy() {
        super.onDestroy();
        DataHandler.getInstance().deleteObserver(this);
    }

    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.marco45.polarheartmonitor/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    /**
     * Run on startup to list bluetooth paired device
     */
    public void listBT() {
        Log.d("Main Activity", "Listing BT elements");
        if (searchBt) {
            //Discover bluetooth devices
            final List<String> list = new ArrayList<>();
            list.add("");
            pairedDevices.addAll(mBluetoothAdapter.getBondedDevices());
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    list.add(device.getName() + "\n" + device.getAddress());
                }
            }
            if (!h7) {
                Log.d("Main Activity", "Listing BTLE elements");
                final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                        if (!list.contains(device.getName() + "\n" + device.getAddress())) {
                            Log.d("Main Activity", "Adding " + device.getName());
                            list.add(device.getName() + "\n" + device.getAddress());
                            pairedDevices.add(device);
                        }
                    }
                };


                Thread scannerBTLE = new Thread() {
                    public void run() {
                        Log.d("Main Activity", "Starting scanning for BTLE");
                        mBluetoothAdapter.startLeScan(leScanCallback);
                        try {
                            Thread.sleep(5000);
                            Log.d("Main Activity", "Stoping scanning for BTLE");
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        } catch (InterruptedException e) {
                            Log.e("Main Activity", "ERROR: on scanning");
                        }
                    }
                };

                scannerBTLE.start();
            }

            //Populate drop down
            spinner1 = (Spinner) findViewById(R.id.spinner1);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setOnItemSelectedListener(this);
            spinner1.setAdapter(dataAdapter);

            if (DataHandler.getInstance().getID() != 0 && DataHandler.getInstance().getID() < spinner1.getCount())
                spinner1.setSelection(DataHandler.getInstance().getID());
        }
    }

    /**
     * When menu button are pressed
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("Main Activity", "Menu pressed");
        if (id == R.id.action_settings) { //close connection
            menuBool = false;
            Log.d("Main Activity", "disable pressed");
            if (spinner1 != null) {
                spinner1.setSelection(0);
            }
            if (DataHandler.getInstance().getReader() == null) {
                Log.i("Main Activity", "Disabling h7");
                DataHandler.getInstance().getH7().cancel();
                DataHandler.getInstance().setH7(null);
                h7 = false;
            } else {
                Log.i("Main Activity", "Disabling BT");
                DataHandler.getInstance().getReader().cancel();
                DataHandler.getInstance().setReader(null);
                normal = false;
            }
            return true;
        } else if (id == R.id.about) { //about menu

            Log.i("Main Activity", "opening about");
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * When the option is selected in the dropdown we turn on the bluetooth
     */
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {


        if (arg2 != 0) {
            //Actual work
            DataHandler.getInstance().setID(arg2);
            if (!h7 && ((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1]).getName().contains("H7") && DataHandler.getInstance().getReader() == null) {

                Log.i("Main Activity", "Starting h7");
                DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], this));
                h7 = true;
            } else if (!normal && DataHandler.getInstance().getH7() == null) {

                Log.i("Main Activity", "Starting normal");
                DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[arg2 - 1], this));
                DataHandler.getInstance().getReader().start();
                normal = true;
            }
            menuBool = true;

        }

    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setEnabled(menuBool);
        menu.findItem(R.id.action_settings).setVisible(menuBool);
        return true;
    }

    public void onNothingSelected(AdapterView<?> arg0) {


    }

    /**
     * Called when bluetooth connection failed
     */
    public void connectionError() {

        Log.w("Main Activity", "Connection error occured");
        if (menuBool) {//did not manually tried to disconnect
            Log.d("Main Activity", "in the app");
            menuBool = false;
            final MainActivity ac = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), getString(R.string.couldnotconnect), Toast.LENGTH_SHORT).show();
                    //TextView rpm = (TextView) findViewById(R.id.rpm);
                    //rpm.setText("0 BMP");
                    Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
                    if (DataHandler.getInstance().getID() < spinner1.getCount())
                        spinner1.setSelection(DataHandler.getInstance().getID());

                    if (!h7) {

                        Log.w("Main Activity", "starting H7 after error");
                        DataHandler.getInstance().setReader(null);
                        DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        h7 = true;
                    } else if (!normal) {
                        Log.w("Main Activity", "Starting normal after error");
                        DataHandler.getInstance().setH7(null);
                        DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        DataHandler.getInstance().getReader().start();
                        normal = true;
                    }
                }
            });
        }
    }

    public void update(Observable observable, Object data) {
        receiveData();
    }

    /**
     * Update Gui with new value from receiver
     */
    public void receiveData() {
        //ANALYTIC
        //t.setScreenName("Polar Bluetooth Used");
        //t.send(new HitBuilders.AppViewBuilder().build());

        runOnUiThread(new Runnable() {
            public void run() {
                //menuBool=true;
                TextView rpm = (TextView) findViewById(R.id.rpm);
                rpm.setText(DataHandler.getInstance().getLastValue());



                TextView min = (TextView) findViewById(R.id.min);
                min.setText(DataHandler.getInstance().getMin());

                TextView avg = (TextView) findViewById(R.id.avg);
                avg.setText(DataHandler.getInstance().getAvg());

                TextView max = (TextView) findViewById(R.id.max);
                max.setText(DataHandler.getInstance().getMax());
            }
        });
    }

    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.marco45.polarheartmonitor/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
