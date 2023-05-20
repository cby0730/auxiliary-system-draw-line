/*
 * Copyright (c) 2011-2018 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fancynavi.android.app;

import static com.fancynavi.android.app.DataHolder.FOREGROUND_SERVICE_ID;
import static com.fancynavi.android.app.DataHolder.TAG;
import static com.fancynavi.android.app.DataHolder.isDragged;
import static com.fancynavi.android.app.DataHolder.isNavigating;
import static com.fancynavi.android.app.DataHolder.isPipMode;
import static com.fancynavi.android.app.DataHolder.isRouteOverView;
import static com.fancynavi.android.app.MapFragmentView.audioManager;
import static com.fancynavi.android.app.MapFragmentView.clearButton;
import static com.fancynavi.android.app.MapFragmentView.compassButton;
import static com.fancynavi.android.app.MapFragmentView.currentPositionMapLocalModel;
import static com.fancynavi.android.app.MapFragmentView.distanceMarkerMapOverlayList;
import static com.fancynavi.android.app.MapFragmentView.junctionViewImageView;
import static com.fancynavi.android.app.MapFragmentView.laneInformationMapOverlay;
import static com.fancynavi.android.app.MapFragmentView.mapRoute;
import static com.fancynavi.android.app.MapFragmentView.mapRouteGeoBoundingBox;
import static com.fancynavi.android.app.MapFragmentView.navigationControlButton;
import static com.fancynavi.android.app.MapFragmentView.onAudioFocusChangeListener;
import static com.fancynavi.android.app.MapFragmentView.signpostImageView;
import static com.fancynavi.android.app.MapFragmentView.theRoute;
import static com.fancynavi.android.app.MapFragmentView.trafficWarningTextView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.here.android.mpa.customlocation2.CLE2DataManager;
import com.here.android.mpa.customlocation2.CLE2OperationResult;
import com.here.android.mpa.customlocation2.CLE2Request;
import com.here.android.mpa.customlocation2.CLE2Task;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapOverlay;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetAddress;
import org.json.JSONObject;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Main activity which launches map view and handles Android run-time requesting permission.
 */

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    static boolean isMapRotating = false;
    static float lightSensorValue;
    static float azimuth = 0f;
    static boolean isVisible = true;
    static TextToSpeech textToSpeech;
    SensorManager mySensorManager;
    Bundle mViewBundle = new Bundle();
    List<Float> azimuthArrayList = new ArrayList<>();
    private boolean isDarkMode = false;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        float[] mGravity;
        float[] mGeomagnetic;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
//            Log.d(TAG, "onSensorChanged");
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                lightSensorValue = event.values[0];
//                Log.d(TAG, String.valueOf(lightSensorValue));
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = event.values;
            }
            if (!Build.FINGERPRINT.contains("generic")) {
                if (DataHolder.getMap() != null) {
                    if (!isNavigating) {
                        if (lightSensorValue < 30) {
                            if (!isDarkMode) {
                                setTheme(R.style.MSDKUIDarkTheme_WhiteAccent);
                                new MapSchemeChanger(DataHolder.getMap(), DataHolder.getNavigationManager()).darkenMap();
                                ((MapScaleView) findViewById(R.id.map_scale_view)).setColor(Color.WHITE);
                                findViewById(R.id.north_up).setBackgroundResource(R.drawable.compass_dark);
                                isDarkMode = true;
                            }
                        } else {
                            if (isDarkMode) {
                                setTheme(R.style.MSDKUIDarkTheme);
                                new MapSchemeChanger(DataHolder.getMap(), DataHolder.getNavigationManager()).lightenMap();
                                ((MapScaleView) findViewById(R.id.map_scale_view)).setColor(Color.BLACK);
                                findViewById(R.id.north_up).setBackgroundResource(R.drawable.compass_bright);
                                isDarkMode = false;
                            }
                        }
                    } else {
                        Calendar cal = Calendar.getInstance();
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        if (hour < 6 || hour > 18) {
                            if (!isDarkMode) {
                                setTheme(R.style.MSDKUIDarkTheme_WhiteAccent);
                                new MapSchemeChanger(DataHolder.getMap(), DataHolder.getNavigationManager()).darkenMap();
                                ((MapScaleView) findViewById(R.id.map_scale_view)).setColor(Color.WHITE);
                                findViewById(R.id.north_up).setBackgroundResource(R.drawable.compass_dark);
                                isDarkMode = true;
                            }
                        } else {
                            if (isDarkMode) {
                                setTheme(R.style.MSDKUIDarkTheme);
                                new MapSchemeChanger(DataHolder.getMap(), DataHolder.getNavigationManager()).lightenMap();
                                ((MapScaleView) findViewById(R.id.map_scale_view)).setColor(Color.BLACK);
                                findViewById(R.id.north_up).setBackgroundResource(R.drawable.compass_bright);
                                isDarkMode = false;
                            }
                        }
                    }
                }
            }
            if (mGravity != null && mGeomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = (float) Math.toDegrees(orientation[0]);
                    if (azimuthArrayList.size() > 64) {
                        azimuthArrayList.remove(0);
                    }
                    azimuthArrayList.add(azimuth);
                    float averagedAzimuth = getAverageFromFloatArrayList(azimuthArrayList);
                    if (currentPositionMapLocalModel != null && !isNavigating) {
                        float rotatingAngle = new BigDecimal(averagedAzimuth).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                        currentPositionMapLocalModel.setYaw(rotatingAngle);
                        if (!isMapRotating) {
                            if (!isDragged) {
                                compassButton.setRotation(0);
                                DataHolder.getMap().setOrientation(0);
                            }
                        } else {
                            compassButton.setRotation(rotatingAngle * -1);
                            DataHolder.getMap().setCenter(DataHolder.getPositioningManager().getPosition().getCoordinate(), Map.Animation.LINEAR);
                            DataHolder.getMap().setOrientation(rotatingAngle);
                            isDragged = false;
                        }
                    }
                }
            }
        }
    };
    private Configuration configuration;
    private DisplayMetrics metrics;
    private MapFragmentView mapFragmentView;
    //    private LocationRequest locationRequest;
//    private final long UPDATE_INTERVAL = 5 * 1000;
//    private final long FASTEST_INTERVAL = 1000;

    public void hideGuidanceView() {
        View guidanceView = findViewById(R.id.guidance_maneuver_view);
        guidanceView.setVisibility(View.GONE);
    }

    public void hideJunctionView() {
        View junctionView = findViewById(R.id.junctionImageView);
        junctionView.setVisibility(View.GONE);
        View signpostView = findViewById(R.id.signpostImageView);
        signpostView.setVisibility(View.GONE);
    }

    public void setData(Bundle data) {
        mViewBundle = data;
    }

    private static Context context;
    public static Context getAppContext() {
        return MainActivity.context;
    }

    public static Socket clientSocket;//客戶端的socket
    public static BufferedWriter bw;  //取得網路輸出串流
    public static BufferedReader br;  //取得網路輸入串流
    public static JSONObject jsonWrite, jsonRead; //從java伺服器傳遞與接收資料的json
    public static String tmp;

    public static void setTmp( String dataTmp ) { tmp = dataTmp; }

    public static Runnable Connection=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                //輸入 Server 端的 IP
                InetAddress serverIp = InetAddress.getByName("172.20.10.2");
                //自訂所使用的 Port(1024 ~ 65535)
                int serverPort = 7000;
                //建立連線
                clientSocket = new Socket(serverIp, serverPort);
                //取得網路輸出串流
                bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                //取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //檢查是否已連線
                if (clientSocket.isConnected()) {
                    Log.d( "EEERROR", "Correctly get, " + tmp );
                    bw.write(tmp);
                    bw.flush();
                    //宣告一個緩衝,從br串流讀取 Server 端傳來的訊息
                    /*
                    tmp = br.readLine();
                    if (tmp != null)
                        Log.d( "EEERROR", "Correctly get, " + tmp );
                    else
                        Log.d( "EEERROR", "Wrong, " + tmp );
                    */
                }
            } catch (Exception e) {
                //當斷線時會跳到 catch,可以在這裡處理斷開連線後的邏輯
                e.printStackTrace();
                Log.e("EEERROR", "Socket連線=" + e.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        MainActivity.context = getApplicationContext();

        configuration = getResources().getConfiguration();
        metrics = getResources().getDisplayMetrics();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
//                if (textToSpeech.isLanguageAvailable(Locale.TAIWAN) == TextToSpeech.LANG_AVAILABLE) {
//                    textToSpeech.setLanguage(Locale.TAIWAN);
//                }
//                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setLanguage(new Locale("aze"));
                Log.d(TAG, "textToSpeech.getDefaultEngine(): " + textToSpeech.getDefaultEngine());
                Log.d(TAG, "textToSpeech.getDefaultVoice().getName(): " + textToSpeech.getDefaultVoice().getName());
                Log.d(TAG, "textToSpeech.getDefaultVoice().getLocale().getCountry(): " + textToSpeech.getDefaultVoice().getLocale().getCountry());
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                        Log.d(TAG, "onStart: " + s);
                        int streamId = NavigationManager.getInstance().getAudioPlayer().getStreamId();
                        audioManager.requestAudioFocus(onAudioFocusChangeListener, streamId,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                    }

                    @Override
                    public void onDone(String s) {
                        Log.d(TAG, "onDone: " + s);
                        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
                textToSpeech.speak("Naviqasiya sisteminə xoş gəlmisiniz", TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
            }
        });


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        hideGuidanceView();
        hideJunctionView();
        requestPermissions();
//        startLocationUpdates();
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor accelerometerSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (lightSensor != null) {
            mySensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (magneticSensor != null) {
            mySensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometerSensor != null) {
            mySensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Snackbar purgeCacheSnackBar = Snackbar.make(this.findViewById(R.id.mapFragmentView), getString(R.string.here_sdk_v) + com.here.android.mpa.common.Version.getSdkVersion(), Snackbar.LENGTH_LONG);
        purgeCacheSnackBar.setAction(R.string.clear_cache, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CLE2DataManager.getInstance().newPurgeLocalStorageTask().start(new CLE2Task.Callback<CLE2OperationResult>() {
                    @Override
                    public void onTaskFinished(CLE2OperationResult cle2OperationResult, CLE2Request.CLE2Error cle2Error) {
                        Log.d(TAG, "getAffectedLayerIds: " + cle2OperationResult.getAffectedLayerIds());
                        Log.d(TAG, "getAffectedItemCount: " + cle2OperationResult.getAffectedItemCount());
                        Log.d(TAG, "getErrorCode: " + cle2Error.getErrorCode());
                    }
                });
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "here_offline_cache");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                int mPendingIntentId = 528491;
                PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });
        purgeCacheSnackBar.show();

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (!isPipMode) {
            isVisible = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        isVisible = true;
        if (isNavigating) {
            intoGuidanceMode();
            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.75f);
            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
//            DataHolder.getAndroidXMapFragment().setOnTouchListener(mapOnTouchListenerForNavigation);
        } else {
            if (DataHolder.getMap() != null) {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
            }
            isDragged = false;
        }
    }

    private float getAverageFromFloatArrayList(List<Float> list) {
        float sum = 0;
        for (float e : list) {
            sum += e;
        }
        return sum / list.size();
    }

//    protected void startLocationUpdates() {
//
//        // Create the location request to start receiving updates
//        locationRequest = new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(UPDATE_INTERVAL);
//        locationRequest.setFastestInterval(FASTEST_INTERVAL);
//
//        // Create LocationSettingsRequest object using location request
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(locationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        // Check whether location settings are satisfied
//        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(locationSettingsRequest);
//
//        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            checkPermissions();
//            return;
//        }
//        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        // do work here
//                        onLocationChanged(locationResult.getLastLocation());
//                    }
//                },
//                Looper.myLooper());
//    }

//    public void onLocationChanged(Location location) {
//        //updatedLatLng = new GeoCoordinate(location.getLatitude(), location.getLongitude());
//    }

//    public void getLastLocation() {
//        // Get last known recent location using new Google Play Services SDK (v11+)
//        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            checkPermissions();
//            return;
//        }
//        locationClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // GPS location can be null if GPS is switched off
//                        if (location != null) {
//                            onLocationChanged(location);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//    }

//    private boolean checkPermissions() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        } else {
//            requestPermissions();
//            return false;
//        }
//    }

    void intoGuidanceMode() {
        if (DataHolder.getNavigationManager() != null) {
            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
            if (isInMultiWindowMode()) {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                MapModeChanger.intoSimpleMode();
            } else {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.75f);
                MapModeChanger.intoFullMode();
            }
            DataHolder.getMap().setTilt(60);
            isRouteOverView = false;
            if (laneInformationMapOverlay != null) {
                DataHolder.getMap().addMapOverlay(laneInformationMapOverlay);
            }
            trafficWarningTextView.setVisibility(View.VISIBLE);
            junctionViewImageView.setAlpha(1f);
            signpostImageView.setAlpha(1f);
            navigationControlButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
            DataHolder.getNavigationManager().resume();
            if (distanceMarkerMapOverlayList.size() > 0) {
                for (MapOverlay o : distanceMarkerMapOverlayList) {
                    DataHolder.getMap().addMapOverlay(o);
                }
            }
//            if (!DataHolder.isSimpleMode()) {
//                DataHolder.getAndroidXMapFragment().setOnTouchListener(mapOnTouchListenerForNavigation);
//            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!DataHolder.isRoadView && isNavigating) {
            intoGuidanceMode();
        } else {
            isDragged = false;
        }
        //super.onBackPressed();
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    void requestPermissions() {

        final List<String> requiredSDKPermissions = new ArrayList<String>();
        requiredSDKPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredSDKPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredSDKPermissions.add(Manifest.permission.INTERNET);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        requiredSDKPermissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
//        requiredSDKPermissions.add(Manifest.permission.CAMERA);

        ActivityCompat.requestPermissions(this,
                requiredSDKPermissions.toArray(new String[requiredSDKPermissions.size()]),
                REQUEST_CODE_ASK_PERMISSIONS);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int index = 0; index < permissions.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(findViewById(R.id.mapFragmentView), getString(R.string.required_permission) + permissions[index] + getString(R.string.not_granted), Snackbar.LENGTH_LONG).show();
                }
            }

            mapFragmentView = new MapFragmentView(this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (isInMultiWindowMode) {
            if (!isPipMode) {
                Intent intent = new Intent(this, this.getClass());
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            if (isNavigating) {
                MapModeChanger.setMapTilt(0);
                MapModeChanger.setMapZoomLevel(17);
                MapModeChanger.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);
            }
            MapModeChanger.intoSimpleMode();
            MapModeChanger.removeNavigationListeners();
        } else {
            MapModeChanger.intoFullMode();
            if (isNavigating) {
                MapModeChanger.addNavigationListeners();
            }
            if (isNavigating) {
                MapModeChanger.setMapTilt(60);
                MapModeChanger.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        setVisible(true);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            DataHolder.getNotificationManager().cancel(FOREGROUND_SERVICE_ID);
            getBaseContext().getResources().updateConfiguration(configuration, metrics);
//            findViewById(R.id.guidance_next_maneuver_view).setVisibility(View.GONE);
//            findViewById(R.id.map_constraint_layout).setVisibility(View.GONE);
//            findViewById(R.id.guidance_maneuver_view).setVisibility(View.GONE);
//            findViewById(R.id.guidance_maneuver_panel_layout).setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//            TextView distanceTextView = findViewById(R.id.distanceView);
//            distanceTextView.setTextSize(DpConverter.convertDpToPixel(8, this));
            MapModeChanger.intoSimpleMode();
            MapModeChanger.removeNavigationListeners();
            DataHolder.getAndroidXMapFragment().onPictureInPictureModeChanged(isInPictureInPictureMode);
            isPipMode = isInPictureInPictureMode;
        } else {
//            findViewById(R.id.guidance_next_maneuver_view).setVisibility(View.VISIBLE);
//            findViewById(R.id.map_constraint_layout).setVisibility(View.VISIBLE);
//            findViewById(R.id.guidance_maneuver_panel_layout).setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//            TextView distanceTextView = findViewById(R.id.distanceView);
//            distanceTextView.setTextSize(DpConverter.convertDpToPixel(16, this));
            MapModeChanger.intoFullMode();
            if (isNavigating) {
                MapModeChanger.addNavigationListeners();
            }
            MapModeChanger.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
            DataHolder.getAndroidXMapFragment().onPictureInPictureModeChanged(isInPictureInPictureMode);
            isPipMode = isInPictureInPictureMode;
        }
    }

    @Override
    public void onDestroy() {
        mapFragmentView.onDestroy();
        Log.d(TAG, "onDestroy");
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        isNavigating = false;
        isRouteOverView = false;
        mapRoute = null;
        theRoute = null;
        mapRouteGeoBoundingBox = null;
        audioManager = null;
        super.onDestroy();

        try {
            //傳送離線 Action 給 Server 端
            jsonWrite = new JSONObject();
            jsonWrite.put("EEERROR","離線");

            //寫入
            bw.write(jsonWrite + "\n");
            //立即發送
            bw.flush();

            //關閉輸出入串流後,關閉Socket
            bw.close();
            br.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "newConfig.orientation: " + newConfig.orientation);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.guidance_maneuver_view).setVisibility(View.GONE);
            findViewById(R.id.guidance_next_maneuver_view).setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isNavigating) {
                findViewById(R.id.guidance_maneuver_view).setVisibility(View.VISIBLE);
                findViewById(R.id.guidance_next_maneuver_view).setVisibility(View.VISIBLE);
            }
        }
    }
}
