package com.fancynavi.android.app;

import static com.fancynavi.android.app.DataHolder.TAG;
import static com.fancynavi.android.app.DataHolder.getActivity;
import static com.fancynavi.android.app.DataHolder.getNavigationManager;
import static com.fancynavi.android.app.DataHolder.isDragged;
import static com.fancynavi.android.app.DataHolder.isNavigating;
import static com.fancynavi.android.app.DataHolder.isPipMode;
import static com.fancynavi.android.app.DataHolder.mapOnTouchListenerForNavigation;
import static com.fancynavi.android.app.MainActivity.isMapRotating;
import static com.fancynavi.android.app.MainActivity.isVisible;
import static com.fancynavi.android.app.MainActivity.textToSpeech;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.here.android.mpa.cluster.BasicClusterStyle;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.cluster.ClusterTheme;
import com.here.android.mpa.cluster.ClusterViewObject;
import com.here.android.mpa.common.ApplicationContext;
import com.here.android.mpa.common.CopyrightLogoPosition;
import com.here.android.mpa.common.DataNotReadyException;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.PositioningManager.OnPositionChangedListener;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.common.TrafficSign;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.customlocation2.CLE2CorridorRequest;
import com.here.android.mpa.customlocation2.CLE2DataManager;
import com.here.android.mpa.customlocation2.CLE2Geometry;
import com.here.android.mpa.customlocation2.CLE2PointGeometry;
import com.here.android.mpa.customlocation2.CLE2ProximityRequest;
import com.here.android.mpa.customlocation2.CLE2Request;
import com.here.android.mpa.customlocation2.CLE2Result;
import com.here.android.mpa.guidance.AudioPlayerDelegate;
import com.here.android.mpa.guidance.LaneInformation;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.RoutingZoneNotification;
import com.here.android.mpa.guidance.RoutingZoneRestrictionsChecker;
import com.here.android.mpa.guidance.SafetySpotNotification;
import com.here.android.mpa.guidance.SafetySpotNotificationInfo;
import com.here.android.mpa.guidance.TrafficNotification;
import com.here.android.mpa.guidance.TrafficNotificationInfo;
import com.here.android.mpa.guidance.TrafficWarner;
import com.here.android.mpa.guidance.VoiceCatalog;
import com.here.android.mpa.guidance.VoiceGuidanceOptions;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.LocalMesh;
import com.here.android.mpa.mapping.Location;
import com.here.android.mpa.mapping.LocationInfo;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCartoMarker;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapLocalModel;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapOverlay;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.here.android.mpa.mapping.PositionIndicator;
import com.here.android.mpa.mapping.SafetySpotObject;
import com.here.android.mpa.mapping.TrafficEvent;
import com.here.android.mpa.mapping.TrafficEventObject;
import com.here.android.mpa.mapping.TransitAccessObject;
import com.here.android.mpa.mapping.TransitStopObject;
import com.here.android.mpa.mapping.customization.CustomizableScheme;
import com.here.android.mpa.mapping.customization.CustomizableVariables;
import com.here.android.mpa.mapping.customization.ZoomRange;
import com.here.android.mpa.prefetcher.MapDataPrefetcher;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.DynamicPenalty;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteElement;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.routing.RoutingZone;
import com.here.android.mpa.routing.RoutingZoneRestriction;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.GeocodeResult;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.ReverseGeocodeRequest;
import com.here.android.mpa.search.SearchRequest;
import com.here.android.mpa.tce.TollCostError;
import com.here.android.mpa.tce.TollCostOptions;
import com.here.android.mpa.tce.TollCostRequest;
import com.here.android.mpa.tce.TollCostResult;
import com.here.msdkui.common.measurements.MeasurementUnit;
import com.here.msdkui.common.measurements.SpeedConverter;
import com.here.msdkui.common.measurements.UnitSystem;
import com.here.msdkui.guidance.GuidanceEstimatedArrivalView;
import com.here.msdkui.guidance.GuidanceEstimatedArrivalViewData;
import com.here.msdkui.guidance.GuidanceEstimatedArrivalViewListener;
import com.here.msdkui.guidance.GuidanceEstimatedArrivalViewPresenter;
import com.here.msdkui.guidance.GuidanceManeuverData;
import com.here.msdkui.guidance.GuidanceManeuverListener;
import com.here.msdkui.guidance.GuidanceManeuverPresenter;
import com.here.msdkui.guidance.GuidanceManeuverView;
import com.here.msdkui.guidance.GuidanceNextManeuverData;
import com.here.msdkui.guidance.GuidanceNextManeuverListener;
import com.here.msdkui.guidance.GuidanceNextManeuverPresenter;
import com.here.msdkui.guidance.GuidanceNextManeuverView;
import com.here.msdkui.guidance.GuidanceSpeedData;
import com.here.msdkui.guidance.GuidanceSpeedLimitView;
import com.here.msdkui.guidance.GuidanceStreetLabelData;
import com.here.msdkui.guidance.GuidanceStreetLabelListener;
import com.here.msdkui.guidance.GuidanceStreetLabelPresenter;
import com.here.msdkui.guidance.GuidanceStreetLabelView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;

class MapFragmentView {
    static MapOverlay laneInformationMapOverlay;
    static GeoPosition currentGeoPosition;
    static Button navigationControlButton;
    static Button clearButton;
    static ImageView junctionViewImageView;
    static ImageView signpostImageView;
    static MapLocalModel currentPositionMapLocalModel;
    static Button compassButton;
    static TextView trafficWarningTextView;
    static List<MapOverlay> distanceMarkerMapOverlayList = new ArrayList<>();
    static NavigationListeners navigationListeners;
    static GeoBoundingBox mapRouteGeoBoundingBox;
    private final OnPositionChangedListener positionChangedListener = new OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
            currentGeoPosition = geoPosition;
            mapScaleView.updateScale();
            if (!DataHolder.isRouteOverView && !DataHolder.isDragged && !DataHolder.isNavigating) {
                if (geoPosition.isValid()) {
//                    Log.d(TAG, "geoPosition.isValid()");
                    DataHolder.getMap().setCenter(geoPosition.getCoordinate(), Map.Animation.LINEAR);
                } else if (lastKnownLocation.isValid()) {
//                    Log.d(TAG, "lastKnownLocation.isValid()");
                    DataHolder.getMap().setCenter(lastKnownLocation, Map.Animation.LINEAR);
                }
            }
//            https://developer.here.com/documentation/android-premium/3.18/api_reference_java/index.html?com%2Fhere%2Fandroid%2Fmpa%2Fcommon%2FGeoPosition.html
//            Log.d(TAG, "geoPosition.getCoordinate(): " + geoPosition.getCoordinate().getLatitude() + ", " + geoPosition.getCoordinate().getLongitude() + " / geoPosition.getPositionTechnology(): " + geoPosition.getPositionTechnology() + " / geoPosition.getPositionSource(): " + geoPosition.getPositionSource());
            RoadElement roadElement = DataHolder.getPositioningManager().getRoadElement();
            if (roadElement != null) {
                if (roadElement.getSpeedLimit() >= 0) {
                    guidanceSpeedLimitView.setVisibility(View.VISIBLE);
                    speedLabelTextView.setVisibility(View.VISIBLE);
                    guidanceSpeedLimitView.setCurrentSpeedData(new GuidanceSpeedData(geoPosition.getSpeed(), (double) roadElement.getSpeedLimit()));
                }
            } else {
                guidanceSpeedLimitView.setVisibility(View.GONE);
                speedLabelTextView.setVisibility(View.GONE);
            }
            GeoCoordinate geoPositionGeoCoordinate = geoPosition.getCoordinate();
            geoPositionGeoCoordinate.setAltitude(1);
            currentPositionMapLocalModel.setAnchor(geoPositionGeoCoordinate);
            if (DataHolder.isNavigating) {
                currentPositionMapLocalModel.setYaw((float) geoPosition.getHeading());
            }
            if (!DataHolder.isNavigating && DataHolder.getMap().getZoomLevel() >= 17) {
                positionAccuracyMapCircle.setCenter(geoPositionGeoCoordinate);
                float radius = (geoPosition.getLatitudeAccuracy() + geoPosition.getLongitudeAccuracy()) / 2;
                if (radius > 0) {
                    positionAccuracyMapCircle.setRadius(radius);
                }
            }
            if (DataHolder.isNavigating) {
                if (endGuidanceDirectionalGeoPolyline != null) {
                    endGuidanceDirectionalGeoPolyline.clear();
                    endGuidanceDirectionalGeoPolyline.add(destinationLocationGeoCoordinate);
                    endGuidanceDirectionalGeoPolyline.add(geoPositionGeoCoordinate);
                    endGuidanceDirectionalMapPolyline.setGeoPolyline(endGuidanceDirectionalGeoPolyline);
                }
            }

            if (geoPosition.getPositionTechnology() == 8 || geoPosition.getPositionTechnology() == 0) {
                guidanceSpeedView.setVisibility(View.VISIBLE);
                guidanceSpeedLimitView.setVisibility(View.VISIBLE);
                speedLabelTextView.setVisibility(View.VISIBLE);
                positionIndicator.setVisible(false);
                currentPositionMapLocalModel.setVisible(true);
                positionAccuracyMapCircle.setVisible(true);
                if (!DataHolder.isNavigating && DataHolder.getMap().getZoomLevel() >= 17) {
                    positionAccuracyMapCircle.setLineWidth(16);
                    positionAccuracyMapCircle.setLineColor(Color.argb(64, 0, 255, 0));
                    positionAccuracyMapCircle.setFillColor(Color.argb(32, 0, 255, 0));
                }
                gpsStatusImageView.setImageResource(R.drawable.ic_gps_fixed_white_24dp);
                gpsStatusImageView.setImageTintList(DataHolder.getActivity().getResources().getColorStateList(R.color.green));
                if (geoPosition.isValid()) {
                    if (UnitLocale.getDefault() == UnitLocale.ImperialUs) {
                        guidanceSpeedView.setText(String.valueOf((int) new SpeedConverter().convert(geoPosition.getSpeed(), MeasurementUnit.METERS_PER_SECOND, MeasurementUnit.MILES_PER_HOUR).getValue()));
                    } else {
                        guidanceSpeedView.setText(String.valueOf((int) new SpeedConverter().convert(geoPosition.getSpeed(), MeasurementUnit.METERS_PER_SECOND, MeasurementUnit.KILOMETERS_PER_HOUR).getValue()));
                    }
                }

            } else if (geoPosition.getPositionTechnology() < 8 && geoPosition.getPositionTechnology() > 0) {
                guidanceSpeedView.setVisibility(View.GONE);
                guidanceSpeedLimitView.setVisibility(View.GONE);
                speedLabelTextView.setVisibility(View.GONE);
                positionIndicator.setVisible(true);
                currentPositionMapLocalModel.setVisible(false);
                positionAccuracyMapCircle.setVisible(false);
                if (!DataHolder.isNavigating && DataHolder.getMap().getZoomLevel() >= 17) {
                    positionAccuracyMapCircle.setLineWidth(16);
                    positionAccuracyMapCircle.setLineColor(Color.argb(64, 255, 255, 0));
                    positionAccuracyMapCircle.setFillColor(Color.argb(32, 255, 255, 0));
                }
                gpsStatusImageView.setImageResource(R.drawable.ic_gps_not_fixed_white_24dp);
                gpsStatusImageView.setImageTintList(DataHolder.getActivity().getResources().getColorStateList(R.color.yellow));
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {
//            Log.d(TAG, "locationMethod: " + locationMethod.toString());
//            Log.d(TAG, "locationStatus: " + locationStatus.toString());
            if (locationStatus.equals(PositioningManager.LocationStatus.OUT_OF_SERVICE) || locationStatus.equals(PositioningManager.LocationStatus.TEMPORARILY_UNAVAILABLE)) {
                gpsStatusImageView.setImageResource(R.drawable.ic_gps_off_white_24dp);
                gpsStatusImageView.setImageTintList(DataHolder.getActivity().getResources().getColorStateList(R.color.red));
                guidanceSpeedView.setVisibility(View.GONE);
                speedLabelTextView.setVisibility(View.GONE);
//                gpsSwitch.setEnabled(false);
            }
        }
    };
    static Route theRoute;
    private final NavigationManager.LaneInformationListener laneInformationListener = new NavigationManager.LaneInformationListener() {

        @Override
        public void onLaneInformation(List<LaneInformation> list, RoadElement roadElement) {
            super.onLaneInformation(list, roadElement);
            isLaneDisplaying = false;
            if (laneInformationMapOverlay != null) {
                DataHolder.getMap().removeMapOverlay(laneInformationMapOverlay);
            }
            laneDcmLinearLayout = new LinearLayout(DataHolder.getActivity());
            laneDcmLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            laneDcmLinearLayout.setVisibility(View.VISIBLE);
            laneInfoLinearLayoutOverlay = new LinearLayout(DataHolder.getActivity());
            laneInfoLinearLayoutOverlay.setOrientation(LinearLayout.VERTICAL);
            laneInfoLinearLayoutOverlay.setVisibility(View.VISIBLE);
            if (list.size() > 0) {
                for (LaneInformation laneInformation : list) {
                    LaneInformation.RecommendationState recommendationState = laneInformation.getRecommendationState();
                    EnumSet<LaneInformation.Direction> directions = laneInformation.getDirections();
                    int laneDirectionCategory = 0;
                    ImageView laneDcmImageView = new ImageView(DataHolder.getActivity());
                    for (LaneInformation.Direction direction : directions) {
                        laneDirectionCategory += direction.value();
                    }

                    Drawable laneDcmIcon = LaneDirectionCategoryPresenter.getLaneDirectionCategoryPresenter(laneDirectionCategory, DataHolder.getActivity());
                    laneDcmImageView.setImageDrawable(laneDcmIcon);
                    isLaneDisplaying = LaneDirectionCategoryPresenter.isLaneDirectionCategoryShowing();
                    laneDcmImageView.setCropToPadding(false);
                    switch (theRoute.getRoutePlan().getRouteOptions().getTransportMode()) {
                        case CAR:
                        case TRUCK:
                            switch (recommendationState) {
                                case HIGHLY_RECOMMENDED:
                                    laneDcmImageView.setBackgroundColor(Color.argb(192, 0, 160, 0));
                                    break;
                                case RECOMMENDED:
                                    laneDcmImageView.setBackgroundColor(Color.argb(64, 0, 128, 0));
                                    break;
                                default:
                                    laneDcmImageView.setBackgroundColor(Color.argb(32, 64, 64, 64));
                            }
                            break;
                        default:
                            laneDcmImageView.setBackgroundColor(Color.argb(32, 64, 64, 64));
                    }

                    int laneDcmImageViewPadding = (int) DpConverter.convertDpToPixel(4, DataHolder.getActivity());
                    laneDcmLinearLayout.addView(laneDcmImageView);
                    laneDcmImageView.setPadding(laneDcmImageViewPadding, laneDcmImageViewPadding, laneDcmImageViewPadding, laneDcmImageViewPadding);
                }
                laneInfoLinearLayoutOverlay.addView(laneDcmLinearLayout);
                ImageView downArrowImageView = new ImageView(DataHolder.getActivity());
                downArrowImageView.setImageResource(R.drawable.ic_arrow_point_to_down);
                laneInfoLinearLayoutOverlay.addView(downArrowImageView);

                laneInfoLinearLayoutOverlay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        laneInfoLinearLayoutOverlay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        laneInformationMapOverlay.setAnchorPoint(DataHolder.getMapOverlayAnchorPoint(laneInfoLinearLayoutOverlay.getWidth(), laneInfoLinearLayoutOverlay.getHeight()));
                    }
                });
                laneInformationMapOverlay = new MapOverlay(laneInfoLinearLayoutOverlay, roadElement.getGeometry().get(roadElement.getGeometry().size() - 1));

                if (!DataHolder.isRouteOverView && isLaneDisplaying) {
                    try {
                        DataHolder.getMap().addMapOverlay(laneInformationMapOverlay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    private MapState previousMapState = null;
    private GeoJSONTileLoader roadkillGeoJSONTileLoader;
    private MapContainer roadkillGeoJsonTileMapContainer;
    private MapContainer searchRequestResultMapContainer;
    private boolean isSatelliteMap = false;
    private boolean isLaneDisplaying = false;
    private int maneuverIconId;
    private PositionIndicator positionIndicator;
    private CustomRasterTileOverlay customRasterTileOverlay;
    private GeoPolyline endGuidanceDirectionalGeoPolyline;
    private MapPolyline endGuidanceDirectionalMapPolyline;
    private final KeyguardManager keyguardManager;
    private CLE2ProximityRequest cle2ProximityRequest;
    private AndroidXMapFragment androidXMapFragment;
    private LinearLayout distanceMarkerLinearLayout;
    private ImageView distanceMarkerFreeIdImageView;
    private TextView distanceMarkerDistanceValue;
    private TrafficWarner trafficWarner;
    private boolean isPositionLogging = false;
    private MapSchemeChanger mapSchemeChanger;
    private LinearLayout laneDcmLinearLayout;
    private LinearLayout safetyCamLinearLayout;
    private LinearLayout laneInfoLinearLayoutOverlay;
    private double distanceToSafetyCamera;
    private OfflineMapDownloader offlineMapDownloader;
    private HereRouter hereRouter;
    private List<TrafficSign> lastTrafficSignList = new ArrayList<>();
    private RoadElement lastRoadElement;
    private String signName;
    private ElectronicHorizonActivation electronicHorizonActivation;
    private Snackbar searchResultSnackbar;
    private String searchResultString;
    private MapMarker selectedFeatureMapMarker;
    private MapCircle positionAccuracyMapCircle;
    private ImageView gpsStatusImageView;
    private GeoPolyline croppedRoute;
    private List<GeoCoordinate> routeShapePointGeoCoordinateList;
    private ImageView signImageView1;
    private ImageView signImageView2;
    private ImageView signImageView3;
    private Switch gpsSwitch;
    private boolean safetyCameraAhead;
    private GeoCoordinate safetyCameraLocation;
    private double safetyCameraSpeedLimit;
    private int safetyCameraSpeedLimitKM;
    private ImageView safetyCamImageView;
    private TextView safetyCamTextView;
    private TextView safetyCamSpeedTextView;
    private MapMarker safetyCameraMapMarker;
    private MapScaleView mapScaleView;
    static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    static AudioManager audioManager;
    private int speedLimitLinearLayoutHeight;
    private View speedLimitLinearLayout;
    private VoiceActivation voiceActivation;
    private LinearLayout mainLinearLayout;
    private boolean activateHereAdvancedPositioning;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button carRouteButton;
    private Button truckRouteButton;
    private Button scooterRouteButton;
    private Button bikeRouteButton;
    private Button pedsRouteButton;
    private Button trafficButton;
    private Button satMapButton;
    private Button minimizeMapButton;
    private Button searchButton;
    private Button downloadOfflineMapButton;
    private EditText searchTextBar;
    private LinearLayout searchBarLinearLayout;
    private boolean trafficEnabled;
    private ProgressBar progressBar;
    private TextView progressingTextView;
    private final NavigationManager.TrafficRerouteListener trafficRerouteListener = new NavigationManager.TrafficRerouteListener() {
        @Override
        public void onTrafficRerouted(RouteResult routeResult) {
            super.onTrafficRerouted(routeResult);
            int currentRouteDuration = DataHolder.getNavigationManager().getTta(Route.TrafficPenaltyMode.OPTIMAL, 0).getDuration();
            int alternativeRouteDuration = routeResult.getRoute().getTtaIncludingTraffic(0).getDuration();
            Log.d(TAG, "currentRouteDuration:" + currentRouteDuration);
            Log.d(TAG, "alternativeRouteDuration:" + alternativeRouteDuration);
            if (alternativeRouteDuration < currentRouteDuration - 60) {
                int timeSavedInMinute = (currentRouteDuration - alternativeRouteDuration) / 60;

                if (!isPipMode && !DataHolder.getActivity().isInMultiWindowMode()) {
                    DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
                    alternativeMapRoute = new MapRoute(routeResult.getRoute());
                    alternativeMapRoute.setZIndex(90);
                    alternativeMapRoute.setColor(Color.GREEN);
                    alternativeMapRoute.setOutlineColor(Color.DKGRAY);

                    GeoBoundingBox alternativeRouteGeoBoundingBox = routeResult.getRoute().getBoundingBox();
                    GeoBoundingBoxDimensionCalculator geoBoundingBoxDimensionCalculator = new GeoBoundingBoxDimensionCalculator(alternativeRouteGeoBoundingBox);
                    alternativeRouteGeoBoundingBox.expand((float) (geoBoundingBoxDimensionCalculator.getHeightInMeters() * 0.8), (float) (geoBoundingBoxDimensionCalculator.getWidthInMeters() * 0.6));
                    DataHolder.getMap().zoomTo(alternativeRouteGeoBoundingBox, Map.Animation.BOW, 0);

                    DataHolder.getMap().addMapObject(alternativeMapRoute);
                    DataHolder.getMap().setTilt(0);
                    trafficSignMapContainer.setVisible(false);
                    new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.5f);
                }

                textToSpeech.speak(DataHolder.getAndroidXMapFragment().getString(R.string.alternative_route_to_avoid_congestion) + timeSavedInMinute + DataHolder.getAndroidXMapFragment().getString(R.string.minute), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                Snackbar trafficReRoutedSnackBar = Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), R.string.found_better_route, Snackbar.LENGTH_LONG);
                trafficReRoutedSnackBar.setDuration(10000);
                trafficReRoutedSnackBar.setAction(R.string.detour, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textToSpeech.speak(DataHolder.getAndroidXMapFragment().getString(R.string.alternative_route_applied), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                        Log.d(TAG, "traffic rerouted.");
                        theRoute = routeResult.getRoute();
                        resetMapRoute(theRoute);
                        safetyCameraAhead = false;
                        safetyCameraMapMarker.setTransparency(0);
                        safetyCamLinearLayout.setVisibility(View.GONE);
                        cle2CorridorRequestForRoute(theRoute.getRouteGeometry(), 70);
                        DataHolder.getNavigationManager().setRoute(theRoute);
                    }
                });
                trafficReRoutedSnackBar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
                        DataHolder.getMap().removeMapObject(alternativeMapRoute);
                        DataHolder.getMap().setTilt(60);
                        trafficSignMapContainer.setVisible(true);
                        if (isPipMode) {
                            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);
                            DataHolder.getMap().setZoomLevel(17);
                            MapModeChanger.setMapTilt(0);
                        } else {
                            if (DataHolder.getActivity().isInMultiWindowMode()) {
                                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                            } else {
                                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.75f);
                            }
                        }
                    }
                });
                trafficReRoutedSnackBar.show();
            }
        }

        @Override
        public void onTrafficRerouteFailed(TrafficNotification trafficNotification) {
            Log.d(TAG, "onTrafficRerouteFailed");
            super.onTrafficRerouteFailed(trafficNotification);
        }

        @Override
        public void onTrafficRerouteBegin(TrafficNotification trafficNotification) {
            Log.d(TAG, "onTrafficRerouteBegin");
            super.onTrafficRerouteBegin(trafficNotification);
        }

        @Override
        public void onTrafficRerouteState(TrafficEnabledRoutingState trafficEnabledRoutingState) {
            super.onTrafficRerouteState(trafficEnabledRoutingState);

        }
    };
    static MapRoute mapRoute;
    private MapRoute alternativeMapRoute;
    private MapContainer trafficSignMapContainer;
    private ClusterLayer trafficSignClusterLayer;
    private boolean foregroundServiceStarted;
    private CoreRouter coreRouter;
    //HERE SDK UI KIT components
    private GuidanceManeuverView guidanceManeuverView;
    private GuidanceManeuverPresenter guidanceManeuverPresenter;
    private GuidanceEstimatedArrivalViewPresenter guidanceEstimatedArrivalViewPresenter;
    private GuidanceStreetLabelPresenter guidanceStreetLabelPresenter;
    private GuidanceEstimatedArrivalView guidanceEstimatedArrivalView;
    private GuidanceSpeedLimitView guidanceSpeedLimitView;
    private GuidanceStreetLabelView guidanceStreetLabelView;
    private GuidanceNextManeuverView guidanceNextManeuverView;
    private GuidanceNextManeuverPresenter guidanceNextManeuverPresenter;
    private TextView guidanceSpeedView;
    private TextView speedLabelTextView;
    private final ArrayList<GeoCoordinate> waypointList = new ArrayList<>();
    private final ArrayList<MapMarker> userInputWaypoints = new ArrayList<>();
    private ArrayList<MapMarker> wayPointIcons = new ArrayList<>();
    private final ArrayList<MapMarker> placeSearchResultIcons = new ArrayList<>();
    private final long simulationSpeedMs = 10; //defines the speed of navigation simulation
    private GeoCoordinate lastKnownLocation;
    private final NavigationManager.ManeuverEventListener maneuverEventListener = new NavigationManager.ManeuverEventListener() {
        @Override
        public void onManeuverEvent() {
            super.onManeuverEvent();
            if (!isVisible) {
                new NavigationNotificationPusher(maneuverIconId);
            }
        }
    };
    private GeoCoordinate destinationLocationGeoCoordinate;
    private double proceedingDistance = 0;

    /* Navigation Listeners */
    private final TrafficWarner.Listener trafficWarnerListener = new TrafficWarner.Listener() {
        @Override
        public void onTraffic(TrafficNotification trafficNotification) {
            String warningText = "";
            List<TrafficNotificationInfo> trafficNotificationInfoList = trafficNotification.getInfoList();
            for (TrafficNotificationInfo trafficNotificationInfo : trafficNotificationInfoList) {
                TrafficNotificationInfo.Type trafficNotificationInfoType = trafficNotificationInfo.getType();
                TrafficEvent.Severity trafficEventSeverity = trafficNotificationInfo.getSeverity();
                if (trafficEventSeverity == TrafficEvent.Severity.VERY_HIGH || trafficEventSeverity == TrafficEvent.Severity.BLOCKING) {
                    long trafficNotificationInfoDistanceInMeters = trafficNotificationInfo.getDistanceInMeters();
                    String trafficNotificationInfoDistanceInLocalFormat;
                    if (UnitLocale.getDefault() == UnitLocale.ImperialUs || UnitLocale.getDefault() == UnitLocale.Imperial) {
                        trafficNotificationInfoDistanceInLocalFormat = DistanceFormatter.formatLengthMetersToMileYard(trafficNotificationInfoDistanceInMeters, 1);
                    } else {
                        trafficNotificationInfoDistanceInLocalFormat = DistanceFormatter.formatLengthMetersToKilometers(trafficNotificationInfoDistanceInMeters, 1);
                    }

                    switch (trafficNotificationInfoType) {
                        case ON_ROUTE:
                        case NEAR_STOPOVER:
                        case NEAR_START:
                            if ((trafficNotificationInfoDistanceInMeters / 500) * 500 > 0) {
                                warningText = trafficNotificationInfoDistanceInLocalFormat + " " + DataHolder.getAndroidXMapFragment().getString(R.string.distance_to_road_congestion_display);
//                                textToSpeech.speak(trafficNotificationInfoDistanceInLocalFormat + " " + DataHolder.getAndroidXMapFragment().getString(R.string.distance_to_road_congestion), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                            } else {
                                warningText = DataHolder.getAndroidXMapFragment().getString(R.string.passing_road_congestion);
                                textToSpeech.speak(warningText, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                            }
                            break;
                        case ON_HIGHWAY:
                            if ((trafficNotificationInfoDistanceInMeters / 500) * 500 > 0) {
                                warningText = trafficNotificationInfoDistanceInLocalFormat + " " + DataHolder.getAndroidXMapFragment().getString(R.string.distance_to_road_congestion_display);
//                                textToSpeech.speak(trafficNotificationInfoDistanceInLocalFormat + " " + DataHolder.getAndroidXMapFragment().getString(R.string.distance_to_road_congestion), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                            } else {
                                warningText = DataHolder.getAndroidXMapFragment().getString(R.string.passing_road_congestion);
                                textToSpeech.speak(DataHolder.getAndroidXMapFragment().getString(R.string.passing_congestion_please_drive_patiently), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                            }
                            break;
                        case NEAR_DESTINATION:
                            warningText = DataHolder.getAndroidXMapFragment().getString(R.string.congestion_around_destination_display);
                            textToSpeech.speak(DataHolder.getAndroidXMapFragment().getString(R.string.attention_congestion_around_destination), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                            break;
                    }
                    showTrafficWarningTextView(trafficWarningTextView, warningText);

                }
            }
        }
    };
    private final NavigationManager.RealisticViewListener realisticViewListener = new NavigationManager.RealisticViewListener() {
        @Override
        public void onRealisticViewNextManeuver(NavigationManager.AspectRatio aspectRatio, Image junction, Image signpost) {
            Log.d(TAG, "onRealisticViewNextManeuver: getDistanceToNextManeuver=" + DataHolder.getNavigationManager().getNextManeuverDistance());

        }

        @Override
        public void onRealisticViewShow(NavigationManager.AspectRatio aspectRatio, Image junction, Image signpost) {
            Log.d(TAG, "onRealisticViewShow: getNextManeuverDistance=" + DataHolder.getNavigationManager().getNextManeuverDistance());
            int screenOrientation = DataHolder.getAndroidXMapFragment().getResources().getConfiguration().orientation;
            if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.7f, 0.75f);
            }
            junctionViewImageView.requestLayout();
            signpostImageView.requestLayout();
            int jvViewWidth = mainLinearLayout.getWidth() / 3;
            int jvViewHeight;
            switch (aspectRatio) {
                case AR_16x9:
                    jvViewHeight = jvViewWidth / 16 * 9;
                    break;
                case AR_5x3:
                    jvViewHeight = jvViewWidth / 5 * 3;
                    break;
                case AR_4x3:
                    jvViewHeight = jvViewWidth / 4 * 3;
                    break;
                default:
                    jvViewHeight = jvViewWidth * 2;
            }
            junctionViewImageView.getLayoutParams().height = jvViewHeight;
            junctionViewImageView.getLayoutParams().width = jvViewWidth;
            signpostImageView.getLayoutParams().height = jvViewHeight;
            signpostImageView.getLayoutParams().width = jvViewWidth;
            Bitmap junctionBitmap = junction.getBitmap((int) junction.getWidth(), (int) junction.getHeight());
            Bitmap signpostBitMap = signpost.getBitmap((int) signpost.getWidth(), (int) signpost.getHeight());
            junctionViewImageView.setImageBitmap(junctionBitmap);
            signpostImageView.setImageBitmap(signpostBitMap);
            junctionViewImageView.setVisibility(View.VISIBLE);
            signpostImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRealisticViewHide() {
            junctionViewImageView.setVisibility(View.GONE);
            signpostImageView.setVisibility(View.GONE);
            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.75f);
        }
    };

    private final NavigationManager.PositionListener positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            positionIndicator.setVisible(false);
            GeoCoordinate geoPositionGeoCoordinate = geoPosition.getCoordinate();
            geoPositionGeoCoordinate.setAltitude(1);
            GeoCoordinate geoPositionGeoCoordinateOnGround = geoPosition.getCoordinate();
            geoPositionGeoCoordinateOnGround.setAltitude(0);

            /* Manual control map movement and rotation during navigation */
//            DataHolder.getMap().setZoomLevel(17);
//            DataHolder.getMap().setCenter(geoPositionGeoCoordinate, Map.Animation.LINEAR);
//            DataHolder.getActivity().setVisible(!keyguardManager.inKeyguardRestrictedInputMode());
//            Double horizontalOffset = new ShiftMapCenter().getTransformCenterOffset(DataHolder.getMap())[0];
//            float orientationWithPerspectiveOffset = (float) (geoPosition.getHeading() - ((horizontalOffset - 0.5) * 60));
//            DataHolder.getMap().setOrientation(orientationWithPerspectiveOffset);
//            Log.d(TAG, "orientationWithPerspectiveOffset:" + orientationWithPerspectiveOffset);
//            Log.d(TAG, "geoPosition.getHeading():" + geoPosition.getHeading());

            if (DataHolder.getPositioningManager().getRoadElement() != null) {
                RoadElement roadElement = DataHolder.getPositioningManager().getRoadElement();
                List<GeoCoordinate> geoCoordinateList = roadElement.getGeometry();
                RoadElement.FormOfWay formOfWay = roadElement.getFormOfWay();
                String routeName = roadElement.getRouteName();

                if (geoPosition.getSpeed() > roadElement.getSpeedLimit()) {
                    guidanceSpeedView.setTextColor(Color.argb(255, 255, 0, 0));
                    speedLabelTextView.setTextColor(Color.argb(255, 255, 0, 0));
                } else {
                    if (DataHolder.getMap().getMapScheme().contains("hybrid") || DataHolder.getMap().getMapScheme().contains("night")) {
                        guidanceSpeedView.setTextColor(Color.argb(255, 255, 255, 255));
                        speedLabelTextView.setTextColor(Color.argb(255, 255, 255, 255));
                    } else {
                        guidanceSpeedView.setTextColor(Color.argb(255, 0, 0, 0));
                        speedLabelTextView.setTextColor(Color.argb(255, 0, 0, 0));
                    }
                }

                if (DataHolder.isNavigating) {
//                    try {
//                        Log.d(TAG, "getNavigationManager().getTta: " + getNavigationManager().getTta(Route.TrafficPenaltyMode.OPTIMAL, true).getDuration());
//                    } catch (Exception e) {
//                        Log.d(TAG, "No valid TTA.");
//                    }
                    List<RoutingZone> routingZoneList = RoutingZoneRestrictionsChecker.getRoutingZones(DataHolder.getPositioningManager().getRoadElement());
                    String roadName = DataHolder.getPositioningManager().getRoadElement().getRoadName();
                    if (routingZoneList.size() > 0) {
                        for (RoutingZone routingZone : routingZoneList) {
                            String routingZoneId = routingZone.getId();
                            String routingZoneName = routingZone.getName();

                            Log.d(TAG, roadName + " routingZoneId: " + routingZoneId + " routingZoneName: " + routingZoneName);
                            List<RoutingZoneRestriction> routingZoneRestrictionList = routingZone.getRestrictions();
                            for (RoutingZoneRestriction routingZoneRestriction : routingZoneRestrictionList) {
                                String routingZoneLicensePlateLastDigits = routingZoneRestriction.getLicensePlateLastDigits();
                                Date routingZoneRestrictionTimeBegin = routingZoneRestriction.getTimeBegin();
                                Date routingZoneRestrictionTimeEnd = routingZoneRestriction.getTimeEnd();
                                List<RouteOptions.TransportMode> transportModeList = routingZoneRestriction.getTransportTypes();
                                for (RouteOptions.TransportMode transportMode : transportModeList) {
                                    int transportModeValue = transportMode.value();
                                }

                            }
                        }
                    }

                    if (formOfWay == RoadElement.FormOfWay.MOTORWAY && !routeName.equals("")) {
                        String layerId = "TWN_HWAY_MILEAGE";
                        int radius = 200;
                        cle2ProximityRequest = new CLE2ProximityRequest(layerId, geoPositionGeoCoordinateOnGround, radius);
                        cle2ProximityRequest.setConnectivityMode(CLE2Request.CLE2ConnectivityMode.OFFLINE);
                        cle2ProximityRequest.setCachingEnabled(true);
                        cle2ProximityRequest.execute(new CLE2Request.CLE2ResultListener() {
                            @Override
                            public void onCompleted(CLE2Result result, String error) {
//                                Log.d(TAG, "cle2ProximityRequest completed: " + result.getConnectivityModeUsed());
                                /*Display mileage and route number on the upper right corner*/
                                if (error.equals(CLE2Request.CLE2Error.NONE)) {
                                    List<CLE2Geometry> geometries = result.getGeometries();
                                    List<Double> distanceList = new ArrayList<>();
                                    if (geometries.size() > 0) {
                                        for (CLE2Geometry cle2Geometry : geometries) {
                                            CLE2PointGeometry cle2PointGeometry = (CLE2PointGeometry) cle2Geometry;
                                            Double distance = cle2PointGeometry.getPoint().distanceTo(geoPositionGeoCoordinateOnGround);
                                            java.util.Map<String, String> geometryAttributeMap = cle2PointGeometry.getAttributes();
                                            String freeWayId = geometryAttributeMap.get("FREE_WAY_ID");
//                                            Log.d(TAG, distance + " : " + geometryAttributeMap.get("DISTANCE_VALUE"));
                                            if (routeName.equals(freeWayId)) {
                                                distanceList.add(distance);
                                            }
                                        }
                                        double minimumDistance = Collections.min(distanceList);
                                        CLE2Geometry geometry = geometries.get(distanceList.indexOf(minimumDistance));
                                        java.util.Map<String, String> geometryAttributeMap = geometry.getAttributes();
                                        String distanceValue = geometryAttributeMap.get("DISTANCE_VALUE");
//                                        Log.d(TAG, "selected : " + geometryAttributeMap.get("DISTANCE_VALUE"));
                                        String freeWayId = geometryAttributeMap.get("FREE_WAY_ID");
                                        if (routeName.equals(freeWayId) && !DataHolder.isRouteOverView) {
                                            Drawable routeIconDrawable = RouteIconPresenter.getRouteIconName(freeWayId, DataHolder.getActivity());
//                                            Log.d(TAG, freeWayId + " \\ " + routeIconDrawable + " \\ " + distanceValue);
                                            if (routeIconDrawable != null) {
                                                distanceMarkerFreeIdImageView.setBackground(routeIconDrawable);
                                                distanceMarkerFreeIdImageView.setVisibility(View.VISIBLE);
                                            }
                                            distanceMarkerDistanceValue.setText(distanceValue);
                                            distanceMarkerDistanceValue.setVisibility(View.VISIBLE);
                                            distanceMarkerLinearLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            distanceMarkerLinearLayout.setVisibility(View.GONE);
                                        }
                                    } else {
                                        distanceMarkerLinearLayout.setVisibility(View.GONE);
                                    }
                                } else {
                                    distanceMarkerLinearLayout.setVisibility(View.GONE);
                                    Log.d(TAG, "CLE2ResultError: " + error);
                                }
                            }
                        });
                    } else {
                        distanceMarkerLinearLayout.setVisibility(View.GONE);
                    }
                }

                /* Traffic Sign display*/
                if (!roadElement.equals(lastRoadElement)) {
//                    Log.d(TAG, "!roadElement.equals(lastRoadElement)");
                    List<TrafficSign> targetTrafficSignList = new ArrayList<>();
                    GeoCoordinate trafficSignGeoCoordinate = null;
                    try {
                        List<TrafficSign> trafficSignList = roadElement.getTrafficSigns();
                        for (TrafficSign trafficSign : trafficSignList) {
                            double distanceToSign = geoCoordinateList.get(0).distanceTo(trafficSign.coordinate);
                            if (distanceToSign > 0 && !lastTrafficSignList.equals(trafficSignList)) {
                                targetTrafficSignList.add(trafficSign);
                                trafficSignGeoCoordinate = trafficSign.coordinate;
                            }
                        }
                        lastTrafficSignList = targetTrafficSignList;
                    } catch (DataNotReadyException e) {
                        e.printStackTrace();
                    }
                    if (trafficSignGeoCoordinate != null && lastKnownLocation != null) {
                        Log.d(TAG, "trafficSignGeoCoordinate: " + trafficSignGeoCoordinate);
                        Log.d(TAG, "lastKnownLocation: " + lastKnownLocation);
                        Log.d(TAG, "lastKnownLocation.distanceTo(trafficSignGeoCoordinate: " + lastKnownLocation.distanceTo(trafficSignGeoCoordinate));
                        Log.d(TAG, "geoPosition.getCoordinate().distanceTo(trafficSignGeoCoordinate): " + geoPosition.getCoordinate().distanceTo(trafficSignGeoCoordinate));
                        if (lastKnownLocation.distanceTo(trafficSignGeoCoordinate) > geoPosition.getCoordinate().distanceTo(trafficSignGeoCoordinate)) {
                            Log.d(TAG, "show sign");
                            DataHolder.isSignShowing = false;
                            TrafficSignPresenter.setSignImageViews(signImageView1, signImageView2, signImageView3);
                            TrafficSignPresenter.showTrafficSigns(targetTrafficSignList, roadElement, DataHolder.getActivity());
                        }
                    }

                }
                lastRoadElement = roadElement;
            } else {
                guidanceSpeedView.setVisibility(View.INVISIBLE);
                guidanceSpeedLimitView.setVisibility(View.INVISIBLE);
            }
            if (safetyCameraAhead) {
                distanceToSafetyCamera -= proceedingDistance;
                Log.d(TAG, "distanceToSafetyCamera: " + distanceToSafetyCamera);
                if (distanceToSafetyCamera < 0) {
                    safetyCameraAhead = false;
                    safetyCameraMapMarker.setTransparency(0);
                    MediaPlayer mediaPlayer = MediaPlayer.create(DataHolder.getActivity(), R.raw.hint);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                        }
                    });
                    if (mediaPlayer != null) {
                        int streamId = NavigationManager.getInstance().getAudioPlayer().getStreamId();
                        audioManager.requestAudioFocus(onAudioFocusChangeListener, streamId,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                        mediaPlayer.start();
                    }
                    safetyCamLinearLayout.setVisibility(View.GONE);
//                    gpsStatusImageView.setVisibility(View.VISIBLE);
                } else {
                    if (DataHolder.isNavigating) {
                        safetyCamLinearLayout.setVisibility(View.VISIBLE);
                        safetyCamTextView.setText((int) distanceToSafetyCamera + "m");
                    }
//                    safetyCamSpeedTextView.setText(safetyCameraSpeedLimitKM + "km/h");
//                    gpsStatusImageView.setVisibility(View.INVISIBLE);
                }

            } else {
                safetyCameraAhead = false;
                safetyCameraMapMarker.setTransparency(0);
                safetyCamLinearLayout.setVisibility(View.GONE);
//                    gpsStatusImageView.setVisibility(View.VISIBLE);
            }

//            if (electronicHorizonActivation != null) {
//                electronicHorizonActivation.startElectronicHorizonUpdate();
//            }
//            Log.d(TAG, "croppedRoute.getNearestIndex(geoPosition.getCoordinate()): " + croppedRoute.getNearestIndex(geoPosition.getCoordinate()) + "/" + croppedRoute.getAllPoints().size());
            if (routeShapePointGeoCoordinateList != null) {
                if (routeShapePointGeoCoordinateList.size() > 1) {
                    if (croppedRoute.getNearestIndex(geoPosition.getCoordinate()) == croppedRoute.getAllPoints().size() - 1) {
                        cle2CorridorRequestForRoute(routeShapePointGeoCoordinateList, 70);
                    }
                }
            }

            DataHolder.setLastMapCenter(DataHolder.getMap().getCenter());
            DataHolder.setLastMapZoom(DataHolder.getMap().getZoomLevel());

            if (lastKnownLocation != null) {
                proceedingDistance = lastKnownLocation.distanceTo(geoPosition.getCoordinate());
                if (lastKnownLocation.distanceTo(geoPosition.getCoordinate()) > 0) {
                    lastKnownLocation = geoPosition.getCoordinate();
                }
            } else {
                lastKnownLocation = geoPosition.getCoordinate();
            }
        }

    };
    private final NavigationManager.NavigationManagerEventListener navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onRunningStateChanged() {
        }

        @Override
        public void onDestinationReached() {
            super.onDestinationReached();
            Log.d(TAG, "super.onDestinationReached();");
        }

        @Override
        public void onNavigationModeChanged() {
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
            minimizeMapButton = DataHolder.getActivity().findViewById(R.id.minimize_map_button);
            minimizeMapButton.setVisibility(View.GONE);
            distanceMarkerLinearLayout.setVisibility(View.GONE);
            mapSchemeChanger.navigationMapOff();
            DataHolder.isNavigating = false;
            DataHolder.isRoadView = false;
            DataHolder.isRouteOverView = true;
            junctionViewImageView.setVisibility(View.GONE);
            signpostImageView.setVisibility(View.GONE);
            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
            if (DataHolder.getActivity().isInMultiWindowMode()) {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.7f);
            } else {
                new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
            }
            DataHolder.getMap().setTilt(0);
//            if (mapRouteGeoBoundingBox != null) {
//                DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, 0f);
//            }
            navigationControlButton.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
            removeNavigationListeners();
            audioManager = null;
            onAudioFocusChangeListener = null;
            stopForegroundService();

        }

        @Override
        public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
//            Log.d(TAG, "mapUpdateMode is: " + mapUpdateMode);
        }

        @Override
        public void onRouteUpdated(Route route) {
            resetMapRoute(route);
        }

        @Override
        public void onCountryInfo(String s, String s1) {
        }
    };
    private final NavigationManager.SafetySpotListener safetySpotListener = new NavigationManager.SafetySpotListener() {
        @Override
        public void onSafetySpot(SafetySpotNotification safetySpotNotification) {
            super.onSafetySpot(safetySpotNotification);
            VoiceCatalog voiceCatalog = voiceActivation.getVoiceCatalog();
            VoiceGuidanceOptions voiceGuidanceOptions = DataHolder.getNavigationManager().getVoiceGuidanceOptions();
            voiceGuidanceOptions.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(100003001));
            safetyCameraAhead = true;
            List<SafetySpotNotificationInfo> safetySpotNotificationInfoList = safetySpotNotification.getSafetySpotNotificationInfos();
            for (int i = 0; i < safetySpotNotificationInfoList.size(); i++) {
                SafetySpotNotificationInfo safetySpotInfo = safetySpotNotificationInfoList.get(i);
                safetyCameraLocation = safetySpotInfo.getSafetySpot().getCoordinate();
                /* Adding MapMarker to indicate selected safety camera */
                safetyCameraMapMarker.setCoordinate(safetyCameraLocation);
                Image icon = new Image();
                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_pin, 128, 128));
                safetyCameraMapMarker.setIcon(icon);
                safetyCameraMapMarker.setAnchorPoint(DataHolder.getMapMarkerAnchorPoint(safetyCameraMapMarker));
                safetyCameraMapMarker.setTransparency(1);
                distanceToSafetyCamera = safetySpotInfo.getDistance();
                safetyCameraSpeedLimit = safetySpotInfo.getSafetySpot().getSpeedLimit1();
                if (safetyCameraSpeedLimit * 3.6 % 10 >= 8 || safetyCameraSpeedLimit * 3.6 % 10 <= 2) {
                    safetyCameraSpeedLimitKM = (int) ((Math.round((safetyCameraSpeedLimit * 3.6) / 10)) * 10);
                } else {
                    safetyCameraSpeedLimitKM = (int) (Math.round((safetyCameraSpeedLimit * 3.6)));
                }
                textToSpeech.speak(DataHolder.getAndroidXMapFragment().getString(R.string.speed_camera_ahead_voice) + safetyCameraSpeedLimitKM + DataHolder.getAndroidXMapFragment().getString(R.string.kilometers_per_hour), TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
            }
        }
    };
    private final NavigationManager.RerouteListener rerouteListener = new NavigationManager.RerouteListener() {
        @Override
        public void onRerouteBegin() {
            Log.d(TAG, "onRerouteBegin");
            super.onRerouteBegin();
            safetyCameraAhead = false;
            safetyCameraMapMarker.setTransparency(0);
            safetyCamLinearLayout.setVisibility(View.GONE);
//            gpsStatusImageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRerouteEnd(RouteResult routeResult, RoutingError routingError) {
            super.onRerouteEnd(routeResult, routingError);
            if (routingError == RoutingError.NONE) {
                Log.d(TAG, "onRerouteEnd");
                resetMapRoute(routeResult.getRoute());
                cle2CorridorRequestForRoute(routeResult.getRoute().getRouteGeometry(), 70);
            }
        }
    };
    private final MapGesture.OnGestureListener customOnGestureListener = new MapGesture.OnGestureListener() {

        @Override
        public void onPanStart() {
            DataHolder.isDragged = true;
        }

        @Override
        public void onPanEnd() {
            DataHolder.setLastMapCenter(DataHolder.getMap().getCenter());
            DataHolder.setLastMapZoom(DataHolder.getMap().getZoomLevel());
        }

        @Override
        public void onMultiFingerManipulationStart() {

        }

        @Override
        public void onMultiFingerManipulationEnd() {
            DataHolder.setLastMapCenter(DataHolder.getMap().getCenter());
            DataHolder.setLastMapZoom(DataHolder.getMap().getZoomLevel());
        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> list) {
//            Log.d(TAG, "onMapObjectsSelected: " + list.size());
            for (ViewObject viewObject : list) {
                if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                    if (viewObject.equals(currentPositionMapLocalModel)) {
                        isMapRotating = !isMapRotating;
                    } else if (viewObject.equals(positionAccuracyMapCircle)) {
                        isMapRotating = !isMapRotating;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onTapEvent(PointF pointF) {
            searchBarLinearLayout.setVisibility(View.GONE);
            if (searchResultSnackbar != null) {
                searchResultSnackbar.dismiss();
            }
//            searchRequestResultMapContainer.removeAllMapObjects();
            InputMethodManager inputMethodManager = (InputMethodManager) DataHolder.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchTextBar.getWindowToken(), 0);
            new SearchResultHandler(DataHolder.getActivity().findViewById(R.id.mapFragmentView), pointF, DataHolder.getMap());
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF pointF) {
            DataHolder.isDragged = true;
//            searchBarLinearLayout.setVisibility(View.GONE);
//            touchToAddWaypoint(pointF);
//            switchUiControls(View.VISIBLE);
//            InputMethodManager inputMethodManager = (InputMethodManager) m_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputMethodManager.hideSoftInputFromWindow(searchTextBar.getWindowToken(), 0);
//            m_map.setCenter(pointF, Map.Animation.LINEAR, m_map.getZoomLevel(), m_map.getOrientation(), m_map.getTilt());
            return false;
        }

        @Override
        public void onPinchLocked() {

        }

        @Override
        public boolean onPinchZoomEvent(float v, PointF pointF) {
            DataHolder.isDragged = true;
            DataHolder.setLastMapCenter(DataHolder.getMap().getCenter());
            DataHolder.setLastMapZoom(DataHolder.getMap().getZoomLevel());
            mapScaleView.updateScale();
            compassButton.setRotation(DataHolder.getMap().getMapState().getOrientation() * -1);
            return false;
        }

        @Override
        public void onRotateLocked() {

        }

        @Override
        public boolean onRotateEvent(float v) {
            DataHolder.isDragged = true;
            DataHolder.setLastMapCenter(DataHolder.getMap().getCenter());
            DataHolder.setLastMapZoom(DataHolder.getMap().getZoomLevel());
            mapScaleView.updateScale();
            compassButton.setRotation(DataHolder.getMap().getMapState().getOrientation() * -1);
            return false;
        }

        @Override
        public boolean onTiltEvent(float v) {
            DataHolder.isDragged = true;
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF pointF) {
            searchRequestResultMapContainer.removeAllMapObjects();
            GeoCoordinate touchPointGeoCoordinate = DataHolder.getMap().pixelToGeo(pointF);
            InputMethodManager inputMethodManager = (InputMethodManager) DataHolder.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchTextBar.getWindowToken(), 0);
            ReverseGeocodeRequest reverseGeocodeRequest = new ReverseGeocodeRequest(touchPointGeoCoordinate);
            reverseGeocodeRequest.addCustomHeader("HouseNumberMode", "Streetlevel");
            reverseGeocodeRequest.execute(new ResultListener<com.here.android.mpa.search.Location>() {
                @Override
                public void onCompleted(com.here.android.mpa.search.Location location, ErrorCode errorCode) {
                    if (errorCode == ErrorCode.NONE) {
                        if (location != null) {
//                            for (int i = 0; i < location.getAccessPoints().size(); i++) {
//                                Log.d(TAG, location.getAccessPoints().get(i).getAccessType());
//                                Log.d(TAG, location.getAccessPoints().get(i).getCoordinate().toString());
//                            }
//                            Log.d(TAG, location.getAddress().getAdditionalData().toString());
                            new SearchResultHandler(DataHolder.getActivity().findViewById(R.id.mapFragmentView), location, DataHolder.getMap());
                        } else {
                            Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.unable_to_find_an_address_at) + touchPointGeoCoordinate.toString(), Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.reverse_geocode) + " " + errorCode.name(), Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            return false;
        }

        @Override
        public void onLongPressRelease() {
        }

        @Override
        public boolean onTwoFingerTapEvent(PointF pointF) {
            return false;
        }
    };
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
        }
    };
    private final NavigationManager.NewInstructionEventListener newInstructionEventListener = new NavigationManager.NewInstructionEventListener() {
        @Override
        public void onNewInstructionEvent() {
            super.onNewInstructionEvent();
            Log.d(TAG, "getNavigationManager().getNextManeuver().getAction():" + getNavigationManager().getNextManeuver().getAction());
            Log.d(TAG, "getNavigationManager().getNextManeuver().getDistanceToNextManeuver():" + getNavigationManager().getNextManeuver().getDistanceToNextManeuver());
            Log.d(TAG, "getNavigationManager().getNextManeuver().getTurn():" + getNavigationManager().getNextManeuver().getTurn());
            Log.d(TAG, "getNavigationManager().getNextManeuver().getIcon().value():" + getNavigationManager().getNextManeuver().getIcon().name());
            getNavigationManager().getNextManeuver().getRouteElements().forEach(it -> {
                Log.d(TAG, "it.getRoadElement().getRoadName(): " + it.getRoadElement().getRoadName());
                Log.d(TAG, "it.getRoadElement().getPluralType(): " + it.getRoadElement().getPluralType());
                Log.d(TAG, "it.getRoadElement().getFormOfWay(): " + it.getRoadElement().getFormOfWay());
                Log.d(TAG, "it.getRoadElement().getNumberOfLanes(): " + it.getRoadElement().getNumberOfLanes());
                Log.d(TAG, "it.getRoadElement().getAttributes(): " + it.getRoadElement().getAttributes());
            });
//            if (!isVisible) {
//                new NavigationNotificationPusher(maneuverIconId);
//            }
        }
    };

    private final NavigationManager.AudioFeedbackListener audioFeedbackListener = new NavigationManager.AudioFeedbackListener() {
        @Override
        public void onAudioStart() {
            super.onAudioStart();
            int streamId = NavigationManager.getInstance().getAudioPlayer().getStreamId();
            if (audioManager != null) {
                audioManager.requestAudioFocus(onAudioFocusChangeListener, streamId,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            }
        }

        @Override
        public void onAudioEnd() {
            super.onAudioEnd();
            if (audioManager != null) {
                audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            }
        }
    };

    private GeoCoordinate lastSecondLocation = null;

    private final NavigationManager.RoutingZoneListener routingZoneListener = new NavigationManager.RoutingZoneListener() {
        @Override
        public void onRoutingZoneUpdated(@NonNull List<RoutingZone> list) {
            Log.d(TAG, "onRoutingZoneUpdated");
            super.onRoutingZoneUpdated(list);
        }

        @Override
        public void onRoutingZoneAhead(@NonNull RoutingZoneNotification routingZoneNotification) {
            Log.d(TAG, "onRoutingZoneAhead");
            super.onRoutingZoneAhead(routingZoneNotification);
        }
    };
    private List<Thread> prefetchMapDataThreadList = new ArrayList<>();
    private String routeLengthInLocalUnit;
    private MapDataPrefetcher mapDataPrefetcher;
    MapMarker.OnDragListener mapMarkerOnDragListenerForRoute = new MapMarker.OnDragListener() {
        @Override
        public void onMarkerDrag(MapMarker mapMarker) {

        }

        @Override
        public void onMarkerDragEnd(MapMarker mapMarker) {
            int mapMarkerIndex = Integer.valueOf(mapMarker.getTitle());
            wayPointIcons.get(mapMarkerIndex).setCoordinate(mapMarker.getCoordinate());
            waypointList.clear();
            RoutePlan routePlan = theRoute.getRoutePlan();
            RouteOptions.TransportMode transportMode = routePlan.getRouteOptions().getTransportMode();
            routePlan.removeAllWaypoints();
            for (MapMarker waypointMapMarker : wayPointIcons) {
//                Log.d(TAG, "mapMarkerIndex: " + waypointMapMarker.getTitle() + " getCoordinate: " + mapMarker.getCoordinate());
                routePlan.addWaypoint(new RouteWaypoint(waypointMapMarker.getCoordinate()));
                waypointList.add(waypointMapMarker.getCoordinate());
            }
            hereRouter.setRoutePlan(routePlan);
            hereRouter.setWaypoints(waypointList);
            calculateRoute(prepareRouteOptions(transportMode));
        }

        @Override
        public void onMarkerDragStart(MapMarker mapMarker) {

        }
    };

    MapFragmentView(AppCompatActivity activity) {
        DataHolder.setActivity(activity);
        keyguardManager = (KeyguardManager) DataHolder.getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        MapEngine mapEngine = MapEngine.getInstance();
        ApplicationContext applicationContext = new ApplicationContext(activity);
        applicationContext.setAppIdCode("MdgrnOU2OsdYuPbPNz2t", "jeXznopbGvE8KRhVK3wkMw");
        applicationContext.setLicenseKey("UPTaWVOoRFajEFCEsbKeU90tlM+YxolaCWOX8pRWgNq9JVXegK83ohjN7ou/ix1kisqafUQ0/L1vTu0tXpN8V01n31OSmVp9ZWpWIILLc5sQTDKSewAMmmIoYAOd54Zf336wAuK9daZvrK3i4aDnHRrF0m81inch8cRxjTetOYWX4CFDyRXpsxbfvokFZTFoABIWs2GkBTBNXgwOS6o701o/5/ehvs9cjWttI/qSuUZAWD8wjEmtP0INaal6AlQpis5Bmm4cwl4z8qGM6gy2j+pZsEo9M41PAIgPwSDOgMdBqpUJwHjsN1uFEOCSYteAKBZXMX91JyPy670qc1jVBzv9EIDTnaleTr1xKJDDLhL7TmrJeXqMLiKPqjfcIXQhlwp4GIr5V4Hs9WF17aep49+YQgqr61WUZ5gPSGryeavV6zUMhZujZ9cXM0BPga+OHjuGgVk323XQBvOZhV9LQVx3FDpNbMk0NO4GhUfnQMxWKdVYygEN8EAUJw8e6gxDAMgxGy33wIWvKhZG1PTf/3DR/vyo1ywEQKftrdXCruf4mSVtzIB8DnPJvZ4vamxbf9vZQoT4DZbTYZxWiDlqws82GRQq687rek/mosA1Uq0nUgPMT4y9PpePuXQ78zVY3ttKF0wWpzh6jMSa6buBeKr/hszJlbqersym64V6cSU=");
        applicationContext.setMapVariant(MapEngine.MapVariant.GLOBAL);
        applicationContext.enableEnhancedMapMatcher(true);
        mapEngine.addMapDataDownloadListener(new MapEngine.OnMapDownloadListener() {
            @Override
            public void onMapDataDownloadStart() {
                Log.d(TAG, "MapEngine onMapDataDownloadStart()");
            }

            @Override
            public void onMapDataDownloadInProgress() {
                Log.d(TAG, "MapEngine onMapDataDownloadInProgress()");

            }

            @Override
            public void onMapDataDownloadEnd() {
                Log.d(TAG, "MapEngine onMapDataDownloadEnd()");
            }
        });
        mapEngine.init(applicationContext, new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    initAndroidXMapFragment();
                } else {
                    Log.d(TAG, "error: " + error);
                }
            }
        });

    }

    static void intoRouteOverView() {
        if (DataHolder.getNavigationManager() != null) {
            DataHolder.getNavigationManager().pause();
        }
        DataHolder.isRoadView = false;
        DataHolder.isRouteOverView = true;
        if (laneInformationMapOverlay != null) {
            DataHolder.getMap().removeMapOverlay(laneInformationMapOverlay);
        }
        if (distanceMarkerMapOverlayList.size() > 0) {
            for (MapOverlay o : distanceMarkerMapOverlayList) {
                DataHolder.getMap().removeMapOverlay(o);
            }
        }
        trafficWarningTextView.setVisibility(View.INVISIBLE);
        DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
        DataHolder.getMap().setTilt(0);
        DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, 0f);
        navigationControlButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
        junctionViewImageView.setAlpha(0f);
        signpostImageView.setAlpha(0f);
        DataHolder.getAndroidXMapFragment().setOnTouchListener(DataHolder.emptyMapOnTouchListener);
    }

    private void initGuidanceEstimatedArrivalView(NavigationManager navigationManager) {

        guidanceEstimatedArrivalViewPresenter = new GuidanceEstimatedArrivalViewPresenter(navigationManager);
        guidanceEstimatedArrivalViewPresenter.addListener(new GuidanceEstimatedArrivalViewListener() {
            @Override
            public void onDataChanged(GuidanceEstimatedArrivalViewData guidanceEstimatedArrivalViewData) {
                Log.d(TAG, "onDataChanged");
                guidanceEstimatedArrivalView.setEstimatedArrivalData(guidanceEstimatedArrivalViewData);
                if (guidanceEstimatedArrivalViewData != null) {
                    Log.d(TAG, "guidanceEstimatedArrivalViewData " + guidanceEstimatedArrivalViewData.getEta());
                }
            }
        });
    }

    private void showTrafficWarningTextView(TextView textView, String warningString) {
        textView.setText(warningString);
        textView.setVisibility(View.VISIBLE);
        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                textView.setVisibility(View.GONE);
                textView.setText("");
            }
        }.start();
    }

    private void hideTrafficSigns() {
        signName = "";
        signImageView1.setVisibility(View.GONE);
        signImageView2.setVisibility(View.GONE);
        signImageView3.setVisibility(View.GONE);
    }

    private void clearDistanceMarkerMapOverlay() {
        if (distanceMarkerMapOverlayList.size() > 0) {
            for (MapOverlay o : distanceMarkerMapOverlayList) {
                DataHolder.getMap().removeMapOverlay(o);
            }
        }
    }

    private void cle2CorridorRequestForRoute(List<GeoCoordinate> geoCoordinateList, int radius) {
        clearDistanceMarkerMapOverlay();
        distanceMarkerMapOverlayList.clear();
        CLE2DataManager.getInstance().newPurgeLocalStorageTask().start();
        List<GeoCoordinate> croppedShapePointGeoCoordinateList = new ArrayList<>();
        int distance = 0;
        int shapePointIndex = 0;
        /*
        while (shapePointIndex < geoCoordinateList.size() - 1) {
            if (shapePointIndex < routeShapePointGeoCoordinateList.size() - 1) {

                Context context = MainActivity.getAppContext();
                try {
                    FileOutputStream fos = context.openFileOutput("route.txt", Context.MODE_APPEND);
                    fos.write( ( "經緯度 : " + ( routeShapePointGeoCoordinateList.get(shapePointIndex) ) + " " ).getBytes());
                    fos.write( ( "距離 : " + routeShapePointGeoCoordinateList.get(shapePointIndex).distanceTo(routeShapePointGeoCoordinateList.get(shapePointIndex + 1)) + "\n" ).getBytes());
                    fos.flush();
                    fos.close();

                } catch (Exception e) {
                    Log.d("EEERROR", "route" );
                }
            }

            shapePointIndex += 1;
        }

        shapePointIndex = 0;
        */


        while (shapePointIndex < geoCoordinateList.size() - 1) {
//            Log.d(TAG, routeShapePointGeoCoordinateList.size() + " / " + shapePointIndex);
            if (shapePointIndex < routeShapePointGeoCoordinateList.size() - 1) {
                distance += routeShapePointGeoCoordinateList.get(shapePointIndex).distanceTo(routeShapePointGeoCoordinateList.get(shapePointIndex + 1));

                if (distance < 10000) {
                    croppedShapePointGeoCoordinateList.add(routeShapePointGeoCoordinateList.get(shapePointIndex));
                    routeShapePointGeoCoordinateList.remove(shapePointIndex);
                } else {
                    break;
                }
            } else {
                break;
            }
            shapePointIndex += 1;
        }
        if (croppedShapePointGeoCoordinateList.size() > 1) {
            croppedRoute = new GeoPolyline(croppedShapePointGeoCoordinateList);
            CLE2CorridorRequest cle2CorridorRequest = new CLE2CorridorRequest("TWN_HWAY_MILEAGE", croppedShapePointGeoCoordinateList, radius);
            cle2CorridorRequest.setConnectivityMode(CLE2Request.CLE2ConnectivityMode.AUTO);
            cle2CorridorRequest.setCachingEnabled(true);
            cle2CorridorRequest.execute(new CLE2Request.CLE2ResultListener() {
                @Override
                public void onCompleted(CLE2Result cle2Result, String s) {
                    int numberOfStoredGeometries = CLE2DataManager.getInstance().getNumberOfStoredGeometries("TWN_HWAY_MILEAGE");
                    Log.d(TAG, "CLE2CorridorRequest numberOfStoredGeometries: " + numberOfStoredGeometries);
                    for (CLE2Geometry cle2Geometry : cle2Result.getGeometries()) {
                        CLE2PointGeometry cle2PointGeometry = (CLE2PointGeometry) cle2Geometry;
                        String distanceValue = cle2PointGeometry.getAttributes().get("DISTANCE_VALUE");
                        if (distanceValue != null && distanceValue.endsWith("0K")) {
                            TextView distanceMarkerTextView = new TextView(DataHolder.getActivity());
                            distanceMarkerTextView.setText(distanceValue);
                            distanceMarkerTextView.setTextScaleX(0.8f);
                            distanceMarkerTextView.setBackgroundColor(Color.argb(128, 6, 70, 39));
                            distanceMarkerTextView.setTextColor(DataHolder.getActivity().getResources().getColor(R.color.white));
                            MapOverlay distanceMarkerMapOverlay = new MapOverlay(distanceMarkerTextView, cle2PointGeometry.getPoint());
                            DataHolder.getMap().addMapOverlay(distanceMarkerMapOverlay);
                            distanceMarkerMapOverlayList.add(distanceMarkerMapOverlay);
                        }
                    }
                }
            });
        }
    }

    private void initGuidanceStreetLabelView(Context context, NavigationManager navigationManager, Route route) {
        guidanceStreetLabelView = DataHolder.getActivity().findViewById(R.id.guidance_street_label_view);
        guidanceStreetLabelView.setVisibility(View.VISIBLE);
        guidanceStreetLabelPresenter = new GuidanceStreetLabelPresenter(context, navigationManager, route);
        guidanceStreetLabelPresenter.addListener(new GuidanceStreetLabelListener() {
            @Override
            public void onDataChanged(GuidanceStreetLabelData guidanceStreetLabelData) {
                guidanceStreetLabelView.setCurrentStreetData(guidanceStreetLabelData);
            }
        });
        guidanceStreetLabelView.setOnTouchListener(mapOnTouchListenerForNavigation);
    }

    private Thread thread;
    private long lastDistance;
    private void initGuidanceManeuverView(Context context, NavigationManager navigationManager, Route route) {
        guidanceManeuverView = DataHolder.getActivity().findViewById(R.id.guidance_maneuver_view);
        guidanceManeuverPresenter = new GuidanceManeuverPresenter(context, navigationManager, route);
        guidanceManeuverView.setVisibility(View.GONE);
        guidanceManeuverPresenter.addListener(new GuidanceManeuverListener() {
            @Override
            public void onDataChanged(@Nullable GuidanceManeuverData guidanceManeuverData) {
                guidanceManeuverView.setManeuverData(guidanceManeuverData);
                maneuverIconId = guidanceManeuverData.getIconId();

                if ( guidanceManeuverData.getDistance() != lastDistance ) {
                    Context context = MainActivity.getAppContext();
                    try {

                        String dataTmp = guidanceManeuverData.getDistance() + "~" + guidanceManeuverData.getInfo2() + "~" + navigationManager.getNextManeuver().getAngle() + "~" +
                                        navigationManager.getNextManeuver().getMapOrientation() + "~" + navigationManager.getNextManeuver().getIcon().name() ;
/*
                        if ( guidanceManeuverData.getDistance() <= 3 )
                            dataTmp = dataTmp + "\n\nYou are about to reach a turning intersection,\nplease turn " + navigationManager.getNextManeuver().getAngle()
                                    + " degrees( " + navigationManager.getNextManeuver().getIcon().name() + " ) to enter the intersection,"
                                    + "\nand the next road direction is point to " + navigationManager.getNextManeuver().getMapOrientation() + ".\n";
*/
                        MainActivity.setTmp(dataTmp);
                        thread = new Thread(MainActivity.Connection);
                        thread.start();
/*
                        FileOutputStream fos = context.openFileOutput("routePlan.txt", Context.MODE_APPEND);
                        fos.write((dataTmp + "\n" ).getBytes());
                        fos.flush();
                        fos.close();
*/

                    } catch (Exception e) {
                        Log.d("EEERROR", "dead");
                    }
                }

                lastDistance = guidanceManeuverData.getDistance();
            }

            @Override
            public void onDestinationReached() {
            }
        });
    }

    private void initGuidanceNextManeuverView(Context context, NavigationManager navigationManager, Route route) {
        guidanceNextManeuverView = DataHolder.getActivity().findViewById(R.id.guidance_next_maneuver_view);
        guidanceNextManeuverPresenter = new GuidanceNextManeuverPresenter(context, navigationManager, route);
        guidanceNextManeuverView.setVisibility(View.GONE);
        guidanceNextManeuverPresenter.addListener(new GuidanceNextManeuverListener() {
            @Override
            public void onDataChanged(GuidanceNextManeuverData guidanceNextManeuverData) {
                guidanceNextManeuverView.setNextManeuverData(guidanceNextManeuverData);
            }
        });

    }

    private void switchUiControls(int visibility) {
        DataHolder.getActivity().findViewById(R.id.vehicleTypeTableLayout).setVisibility(visibility);
        compassButton.setVisibility(visibility);
        carRouteButton.setVisibility(visibility);
        truckRouteButton.setVisibility(visibility);
        scooterRouteButton.setVisibility(visibility);
        bikeRouteButton.setVisibility(visibility);
        pedsRouteButton.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            carRouteButton.setAlpha(1.0f);
            truckRouteButton.setAlpha(1.0f);
            scooterRouteButton.setAlpha(1.0f);
            bikeRouteButton.setAlpha(1.0f);
            pedsRouteButton.setAlpha(1.0f);
        }
    }

    private void switchUILocale(UnitLocale unitLocale) {
        if (unitLocale == UnitLocale.ImperialUs) {
            guidanceManeuverView.setUnitSystem(UnitSystem.IMPERIAL_US);
            guidanceNextManeuverView.setUnitSystem(UnitSystem.IMPERIAL_US);
            guidanceSpeedLimitView.setUnitSystem(UnitSystem.IMPERIAL_US);
        } else if (unitLocale == UnitLocale.Imperial) {
            guidanceManeuverView.setUnitSystem(UnitSystem.IMPERIAL_UK);
            guidanceNextManeuverView.setUnitSystem(UnitSystem.IMPERIAL_UK);
            guidanceSpeedLimitView.setUnitSystem(UnitSystem.IMPERIAL_UK);
        } else {
            guidanceManeuverView.setUnitSystem(UnitSystem.METRIC);
            guidanceNextManeuverView.setUnitSystem(UnitSystem.METRIC);
            guidanceSpeedLimitView.setUnitSystem(UnitSystem.METRIC);
        }
    }

    private void switchGuidanceUiViews(int visibility) {
        if (DataHolder.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DataHolder.getActivity().findViewById(R.id.guidance_maneuver_view).setVisibility(visibility);
            DataHolder.getActivity().findViewById(R.id.guidance_next_maneuver_view).setVisibility(visibility);
        }
        DataHolder.getActivity().findViewById(R.id.guidance_speed_limit_view).setVisibility(visibility);
        DataHolder.getActivity().findViewById(R.id.guidance_speed_view).setVisibility(visibility);
        DataHolder.getActivity().findViewById(R.id.guidance_street_label_view).setVisibility(visibility);
//        m_activity.findViewById(R.id.guidance_estimated_arrival_view).setVisibility(visibility);
        DataHolder.getActivity().findViewById(R.id.guidance_speed_limit_view).setVisibility(visibility);

    }

    private void switchGuidanceUiPresenters(Boolean switchOn) {
        if (switchOn) {
            guidanceManeuverPresenter.resume();
//            guidanceEstimatedArrivalViewPresenter.resume();
//            guidanceSpeedPresenter.resume();
            guidanceStreetLabelPresenter.resume();
            guidanceNextManeuverPresenter.resume();
        } else {
            if (guidanceManeuverPresenter != null) {
                guidanceManeuverPresenter.pause();
            }
            if (guidanceEstimatedArrivalViewPresenter != null) {
                guidanceEstimatedArrivalViewPresenter.pause();
            }
            if (guidanceStreetLabelPresenter != null) {
                guidanceStreetLabelPresenter.pause();
            }
            if (guidanceNextManeuverPresenter != null) {
                guidanceNextManeuverPresenter.pause();
            }
        }
    }

    private void resetMapRoute(Route route) {
//        DataHolder.getNavigationManager().setRoute(route);
        trafficSignMapContainer.removeAllMapObjects();
        safetyCameraAhead = false;
        safetyCameraMapMarker.setTransparency(0);
        if (mapRoute != null) {
            DataHolder.getMap().removeMapObject(mapRoute);
        }
        mapRoute = new MapRoute(route);
        mapRoute.setZIndex(100);
        mapRoute.setColor(Color.argb(255, 243, 174, 255)); //F3AEFF
        mapRoute.setOutlineColor(Color.argb(255, 78, 0, 143)); //4E008F
        mapRoute.setTraveledColor(Color.DKGRAY);
        mapRoute.setUpcomingColor(Color.LTGRAY);
        mapRoute.setTrafficEnabled(true);
        DataHolder.getMap().addMapObject(mapRoute);
        new Thread(() -> getTrafficSignsForRoute(route)).start();

    }

    private void touchToAddWaypoint(PointF p) {
        DataHolder.isDragged = true;
        if (selectedFeatureMapMarker != null) {
            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
        }
        GeoCoordinate touchPointGeoCoordinate = DataHolder.getMap().pixelToGeo(p);
        MapMarker tourhPointMapMarker = new MapMarker(touchPointGeoCoordinate);
        tourhPointMapMarker.setDraggable(true);
        userInputWaypoints.add(tourhPointMapMarker);
        tourhPointMapMarker.setAnchorPoint(DataHolder.getMapMarkerAnchorPoint(tourhPointMapMarker));
        DataHolder.getMap().addMapObject(tourhPointMapMarker);
        DataHolder.getActivity().findViewById(R.id.vehicleTypeTableLayout).setVisibility(View.VISIBLE);
        carRouteButton.setVisibility(View.VISIBLE);
        truckRouteButton.setVisibility(View.VISIBLE);
        scooterRouteButton.setVisibility(View.VISIBLE);
        bikeRouteButton.setVisibility(View.VISIBLE);
        pedsRouteButton.setVisibility(View.VISIBLE);
        navigationControlButton.setVisibility(View.VISIBLE);
        clearButton.setVisibility(View.VISIBLE);
    }

    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) DataHolder.getActivity().getSupportFragmentManager().findFragmentById(R.id.mapFragmentView);
    }

    /*
     * Android 8.0 (API level 26) limits how frequently background apps can retrieve the user's
     * current location. Apps can receive location updates only a few times each hour.
     * See href="https://developer.android.com/about/versions/oreo/background-location-limits.html
     * In order to retrieve location updates more frequently start a foreground service.
     * See https://developer.android.com/guide/components/services.html#Foreground
     */
    private void startForegroundService() {
        if (!foregroundServiceStarted) {
            foregroundServiceStarted = true;
            Intent startIntent = new Intent(DataHolder.getActivity(), ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            DataHolder.getActivity().getApplicationContext().startService(startIntent);
        }
    }

    private void stopForegroundService() {
        if (foregroundServiceStarted) {
            foregroundServiceStarted = false;
            Intent stopIntent = new Intent(DataHolder.getActivity(), ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
            DataHolder.getActivity().getApplicationContext().startService(stopIntent);
        }
    }

    private void customizeMapScheme(Map map) {
        /*Map Customization - Start*/
        CustomizableScheme customizedColorScheme;
        String customizedColorSchemeName = "colorScheme";
        if (map != null && map.getCustomizableScheme(customizedColorSchemeName) == null) {
            map.createCustomizableScheme(customizedColorSchemeName, Map.Scheme.CARNAV_DAY);
            customizedColorScheme = map.getCustomizableScheme(customizedColorSchemeName);
            ZoomRange range = new ZoomRange(0.0, 20.0);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY0_WIDTH, 40, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY1_WIDTH, 40, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY2_WIDTH, 30, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY3_WIDTH, 20, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_WIDTH, 10, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY0_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY1_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY2_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY3_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY0_TUNNELCOLOR, Color.GRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY1_TUNNELCOLOR, Color.GRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY2_TUNNELCOLOR, Color.GRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY3_TUNNELCOLOR, Color.GRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_TUNNELCOLOR, Color.GRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY0_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY1_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY2_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY3_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
//            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_STREETPOLYLINEATTRIBUTE_TOLL_COLOR, Color.LTGRAY, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY0_FONTSTYLE_SIZE, 30, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY1_FONTSTYLE_SIZE, 30, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY2_FONTSTYLE_SIZE, 30, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY3_FONTSTYLE_SIZE, 20, range);
            customizedColorScheme.setVariableValue(CustomizableVariables.Street.CATEGORY4_FONTSTYLE_SIZE, 20, range);


            map.setMapScheme(customizedColorScheme);
            map.setLandmarksVisible(false);
            map.setExtrudedBuildingsVisible(false);
            DataHolder.getMap().setLandmarksVisible(false);
            map.setCartoMarkersVisible(false);


        } else {
            if (map != null) {
                map.setMapScheme(Map.Scheme.CARNAV_NIGHT);
            }
        }
        /*Map Customization - End*/
    }


    private void createPosition3dObj() {
//            DataHolder.getMap().removeMapObject(currentPositionMapLocalModel);
        currentPositionMapLocalModel = new MapLocalModel();
        LocalModelLoader localModelLoader = new LocalModelLoader(DataHolder.getActivity(), R.raw.arrow_new_obj);
        LocalMesh localMesh = new LocalMesh();
        localMesh.setVertices(localModelLoader.getObjVertices());
        localMesh.setVertexIndices(localModelLoader.getObjIndices());
        localMesh.setTextureCoordinates(localModelLoader.getObjTexCoords());

        currentPositionMapLocalModel.setMesh(localMesh);
        Image image = null;
        try {
            image = new Image();
            image.setImageResource(R.drawable.grad);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPositionMapLocalModel.setTexture(image); //an Image object
        currentPositionMapLocalModel.setScale(6.0f);
        currentPositionMapLocalModel.setDynamicScalingEnabled(true);
        currentPositionMapLocalModel.setZIndex(1000);
        DataHolder.getMap().addMapObject(currentPositionMapLocalModel);
    }

    private void initJunctionView() {
        junctionViewImageView = DataHolder.getActivity().findViewById(R.id.junctionImageView);
        junctionViewImageView.setVisibility(View.GONE);
        signpostImageView = DataHolder.getActivity().findViewById(R.id.signpostImageView);
        signpostImageView.setVisibility(View.GONE);
    }

    private void intoNavigationMode() {
        initJunctionView();
        DataHolder.getMap().setExtrudedBuildingsVisible(false);
        DataHolder.getMap().setLandmarksVisible(false);
        DataHolder.getMap().removeClusterLayer(trafficSignClusterLayer);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                Log.d(TAG, "onAudioFocusChange: " + i);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            minimizeMapButton = DataHolder.getActivity().findViewById(R.id.minimize_map_button);
            minimizeMapButton.setVisibility(View.VISIBLE);
            minimizeMapButton.setBackgroundResource(R.drawable.round_button_off);
            minimizeMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataHolder.isPipMode = true;
                    Rational aspectRatio = new Rational(23, 10);
                    PictureInPictureParams pictureInPictureParams = new PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build();
                    DataHolder.getActivity().enterPictureInPictureMode(pictureInPictureParams);
                }
            });
        }

        safetyCameraAhead = false;
        safetyCamLinearLayout.setVisibility(View.GONE);
//        zoomInButton.setVisibility(View.GONE);
//        zoomOutButton.setVisibility(View.GONE);
//        gpsStatusImageView.setVisibility(View.GONE);
        gpsSwitch.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        downloadOfflineMapButton.setVisibility(View.GONE);
        switchUiControls(View.GONE);
        initGuidanceManeuverView(DataHolder.getActivity(), DataHolder.getNavigationManager(), theRoute);
        initGuidanceNextManeuverView(DataHolder.getActivity(), DataHolder.getNavigationManager(), theRoute);
        initGuidanceStreetLabelView(DataHolder.getActivity(), DataHolder.getNavigationManager(), theRoute);
        switchUILocale(UnitLocale.getDefault());
        switchGuidanceUiViews(View.VISIBLE);
        switchGuidanceUiPresenters(true);

        EnumSet<NavigationManager.NaturalGuidanceMode> naturalGuidanceModes = EnumSet.of(
                NavigationManager.NaturalGuidanceMode.JUNCTION,
                NavigationManager.NaturalGuidanceMode.STOP_SIGN,
                NavigationManager.NaturalGuidanceMode.TRAFFIC_LIGHT
        );
        DataHolder.getNavigationManager().setTrafficAvoidanceMode(NavigationManager.TrafficAvoidanceMode.MANUAL);
        DataHolder.getNavigationManager().setLaneAssistanceMode(NavigationManager.LaneAssistanceMode.CURRENT_VIEW);
//        DataHolder.getNavigationManager().setRouteRequestInterval(180);
//        DataHolder.getNavigationManager().setDistanceWithUTurnToTriggerStopoverReached(100);

        DataHolder.getNavigationManager().setNaturalGuidanceMode(naturalGuidanceModes);
        if (DataHolder.getActivity().isInMultiWindowMode()) {
            DataHolder.getMap().setTilt(0);
            DataHolder.getMap().setZoomLevel(17);
            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW_NOZOOM);
            MapModeChanger.intoSimpleMode();
        } else {
            DataHolder.getMap().setTilt(60);
            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
            /* Activate this line if you want to control map movement and rotation manually during navigation */
//            DataHolder.getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
            MapModeChanger.intoFullMode();
        }
        DataHolder.getNavigationManager().startNavigation(theRoute);
        DataHolder.getPositioningManager().setMapMatchingEnabled(true);
        DataHolder.isRoadView = true;
        /* Voice Guidance init */
        VoiceCatalog voiceCatalog = voiceActivation.getVoiceCatalog();
        VoiceGuidanceOptions voiceGuidanceOptions = DataHolder.getNavigationManager().getVoiceGuidanceOptions();
        voiceGuidanceOptions.setVoiceSkin(voiceCatalog.getLocalVoiceSkin(voiceActivation.getDesiredVoiceId()));

        EnumSet<NavigationManager.AudioEvent> audioEventEnumSet = EnumSet.of(
//                NavigationManager.AudioEvent.SAFETY_SPOT,
                NavigationManager.AudioEvent.MANEUVER,
                NavigationManager.AudioEvent.ROUTE,
                NavigationManager.AudioEvent.GPS,
                NavigationManager.AudioEvent.SPEED_LIMIT
        );
        DataHolder.getNavigationManager().setEnabledAudioEvents(audioEventEnumSet);
        AudioPlayerDelegate audioPlayerDelegate = new AudioPlayerDelegate() {
            @Override
            public boolean playText(String s) {
                Log.d(TAG, "playText: " + s);
                /*
                Context context = MainActivity.getAppContext();
                try {
                    FileOutputStream fos = context.openFileOutput("New123.txt", Context.MODE_APPEND);
                    fos.write( ( s + "\n" ).getBytes());
                    fos.flush();
                    fos.close();

                } catch (Exception e) {
                    Log.d("EEERROR", "playText: " + s);
                }
                */
                return false;
            }

            @Override
            public boolean playFiles(String[] strings) {
                int voiceFileIndex = 0;
                for (String string : strings) {
                    Log.d(TAG, "playFiles: " + voiceFileIndex + " --> " + string);
                    voiceFileIndex++;
                }
//                /* Manually play voice instructions */
//                new Thread(new Runnable() {
//                    public void run() {
//                        PlayVoiceInstructionFiles playVoiceInstructionFiles = new PlayVoiceInstructionFiles(strings);
//                        playVoiceInstructionFiles.setOnPlayingVoiceInstructionListener(new PlayVoiceInstructionFiles.onPlayingVoiceInstructionListener() {
//                            @Override
//                            public void onCompleted() {
//                                Log.d(TAG, "onCompleted()");
//                                audioManager.abandonAudioFocus(onAudioFocusChangeListener);
//                            }
//
//                            @Override
//                            public void onStarted() {
//                                Log.d(TAG, "onStarted()");
//                                if (audioManager != null) {
//                                    int streamId = NavigationManager.getInstance().getAudioPlayer().getStreamId();
//                                    audioManager.requestAudioFocus(onAudioFocusChangeListener, streamId,
//                                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
//                                }
//                            }
//                        });
//                        playVoiceInstructionFiles.play();
//                    }
//                }).start();
                return false;
            }
        };
        DataHolder.getNavigationManager().getAudioPlayer().setDelegate(audioPlayerDelegate);
//        DataHolder.getNavigationManager().getAudioPlayer().setVolume(0);
        androidXMapFragment.getMapGesture().removeOnGestureListener(customOnGestureListener);
        routeShapePointGeoCoordinateList = theRoute.getRouteGeometry();
        cle2CorridorRequestForRoute(routeShapePointGeoCoordinateList, 70);
    }

    private RouteOptions prepareRouteOptions(RouteOptions.TransportMode transportMode) {
        EnumSet<Map.PedestrianFeature> pedestrianFeatureEnumSet = EnumSet.of(
                Map.PedestrianFeature.BRIDGE,
                Map.PedestrianFeature.CROSSWALK,
                Map.PedestrianFeature.ELEVATOR,
                Map.PedestrianFeature.ESCALATOR,
                Map.PedestrianFeature.STAIRS,
                Map.PedestrianFeature.TUNNEL
        );
        RouteOptions routeOptions = new RouteOptions();
        switch (transportMode) {
            case CAR:
                navigationControlButton.setEnabled(true);
                navigationControlButton.setAlpha(1f);
                routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
                routeOptions.setHighwaysAllowed(true);
                DataHolder.getMap().setMapScheme(Map.Scheme.NORMAL_DAY);
                DataHolder.getMap().setFleetFeaturesVisible(EnumSet.noneOf(Map.FleetFeature.class));
                DataHolder.getMap().setPedestrianFeaturesVisible(EnumSet.noneOf(Map.PedestrianFeature.class));
                break;
            case TRUCK:
                navigationControlButton.setEnabled(true);
                navigationControlButton.setAlpha(1f);
                routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK);
                routeOptions.setHighwaysAllowed(true);
                EnumSet<Map.FleetFeature> fleetFeatureEnumSet = EnumSet.of(
                        Map.FleetFeature.CONGESTION_ZONES,
                        Map.FleetFeature.ENVIRONMENTAL_ZONES,
                        Map.FleetFeature.TRUCK_RESTRICTIONS);
                DataHolder.getMap().setFleetFeaturesVisible(fleetFeatureEnumSet);
                DataHolder.getMap().setMapScheme(Map.Scheme.TRUCK_DAY);
                break;
            case SCOOTER:
                navigationControlButton.setEnabled(true);
                navigationControlButton.setAlpha(1f);
                routeOptions.setTransportMode(RouteOptions.TransportMode.SCOOTER);
                routeOptions.setRouteType(RouteOptions.Type.FASTEST);
                DataHolder.getMap().setMapScheme(Map.Scheme.REDUCED_DAY);
                DataHolder.getMap().setFleetFeaturesVisible(EnumSet.noneOf(Map.FleetFeature.class));
                DataHolder.getMap().setPedestrianFeaturesVisible(EnumSet.noneOf(Map.PedestrianFeature.class));
                break;
            case BICYCLE:
                navigationControlButton.setEnabled(false);
                navigationControlButton.setAlpha(0.3f);
                routeOptions.setTransportMode(RouteOptions.TransportMode.BICYCLE);
                routeOptions.setHighwaysAllowed(false);
                DataHolder.getMap().setMapScheme(Map.Scheme.HYBRID_DAY);
                DataHolder.getMap().setFleetFeaturesVisible(EnumSet.noneOf(Map.FleetFeature.class));
                DataHolder.getMap().setPedestrianFeaturesVisible(EnumSet.noneOf(Map.PedestrianFeature.class));
                break;
            case PEDESTRIAN:
                navigationControlButton.setEnabled(true);
                navigationControlButton.setAlpha(1f);
                routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);
                DataHolder.getMap().setPedestrianFeaturesVisible(pedestrianFeatureEnumSet);
                DataHolder.getMap().setMapScheme(Map.Scheme.PEDESTRIAN_DAY);
                DataHolder.getMap().setFleetFeaturesVisible(EnumSet.noneOf(Map.FleetFeature.class));
                routeOptions.setHighwaysAllowed(false);
                break;
        }
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routeOptions.setRouteCount(1);
        return routeOptions;
    }

    private void retryRouting(Context context, RoutingError routingError, RouteOptions routeOptions) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(context.getString(R.string.route_calculation_failed) + routingError.name());
        alertDialogBuilder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                calculateRoute(routeOptions);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(DataHolder.getActivity().getResources().getColor(R.color.green));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(DataHolder.getActivity().getResources().getColor(R.color.red));
    }

    private void getRouteInfo(Route route) {
        switch ((route.getRoutePlan().getRouteOptions().getTransportMode())) {
            case CAR:
            case TRUCK:
                for (RoutingZone routingZone : route.getRoutingZones()) {
                    Log.d(TAG, "routingZone.getId(): " + routingZone.getId());
                    Log.d(TAG, "routingZone.getType().name(): " + routingZone.getType().name());
                    Log.d(TAG, "routingZone.getName(): " + routingZone.getName());
                    for (RoutingZoneRestriction routingZoneRestriction : routingZone.getRestrictions()) {
                        for (RouteOptions.TransportMode transportMode : routingZoneRestriction.getTransportTypes()) {
                            Log.d(TAG, "transportMode.name(): " + transportMode.name());
                        }
                    }
                }
                TollCostOptions tollCostOptions = new TollCostOptions();
                tollCostOptions.setCurrency("TWD");
                new TollCostRequest(route, tollCostOptions).execute(new TollCostRequest.Listener<TollCostResult>() {
                    @Override
                    public void onComplete(TollCostResult tollCostResult, TollCostError tollCostError) {
                        if (tollCostError.getErrorCode() == TollCostError.ErrorCode.SUCCESS) {
                            Log.d(TAG, "getTransportMode: " + route.getRoutePlan().getRouteOptions().getTransportMode());
                            Log.d(TAG, "getErrorCode: " + tollCostError.getErrorCode());
                            Log.d(TAG, "getErrorMessage: " + tollCostError.getErrorMessage());
                            Log.d(TAG, "getTotalTollCost: " + tollCostResult.getTotalTollCost());
                            Log.d(TAG, "getTollCostByCountry: " + tollCostResult.getTollCostByCountry());
                            Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.route_of) + " " + route.getRoutePlan().getRouteOptions().getTransportMode() + " / " + routeLengthInLocalUnit + " / " + DataHolder.getAndroidXMapFragment().getString(R.string.toll_fee) + tollCostResult.getTotalTollCost().doubleValue(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.route_of) + " " + route.getRoutePlan().getRouteOptions().getTransportMode() + " / " + routeLengthInLocalUnit, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            default:
                Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.route_of) + " " + route.getRoutePlan().getRouteOptions().getTransportMode() + " / " + routeLengthInLocalUnit, Snackbar.LENGTH_LONG).show();
        }
    }

    private void getTrafficSignsForRoute(Route route) {
        trafficSignClusterLayer.removeMarkers(trafficSignClusterLayer.getMarkers());
        if (DataHolder.getMap().getZoomLevel() >= 12) {
            DataHolder.getMap().removeClusterLayer(trafficSignClusterLayer);
            trafficSignMapContainer.setVisible(true);
        } else {
            DataHolder.getMap().addClusterLayer(trafficSignClusterLayer);
            trafficSignMapContainer.setVisible(false);
        }
        List<RouteElement> routeElementList = route.getRouteElements().getElements();
        for (RouteElement routeElement : routeElementList) {
            RoadElement roadElementOfCalculatedRoute = routeElement.getRoadElement();
            try {
                List<GeoCoordinate> roadElementGeometry = roadElementOfCalculatedRoute.getGeometry();
                GeoCoordinate lastPointOfRoadElement = roadElementGeometry.get(roadElementGeometry.size() - 1);
                for (TrafficSign trafficSign : roadElementOfCalculatedRoute.getTrafficSigns()) {
                    GeoCoordinate trafficSignGeoCoordinate = trafficSign.coordinate;
                    if (lastPointOfRoadElement.distanceTo(trafficSignGeoCoordinate) == 0) {
                        int trafficSignType = trafficSign.type;
                        try {
                            Image icon = new Image();
                            Bitmap trafficSignBitmap = BitmapFactory.decodeResource(DataHolder.getActivity().getResources(), TrafficSignPresenter.getTrafficSignImageResourceName(trafficSignType));
                            float aspectRatio = (float) trafficSignBitmap.getHeight() / (float) trafficSignBitmap.getWidth();
                            Bitmap iconBitmap = Bitmap.createScaledBitmap(trafficSignBitmap, 64, (int) (64 * aspectRatio), false);
                            icon.setBitmap(iconBitmap);
                            MapMarker trafficSignMapMarker = new MapMarker(trafficSignGeoCoordinate).setIcon(icon);
                            trafficSignMapContainer.addMapObject(trafficSignMapMarker);
                            if (!isNavigating) {
                                trafficSignClusterLayer.addMarker(trafficSignMapMarker);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (DataNotReadyException e) {
                e.printStackTrace();
            }
        }
    }

    private void prefetchMapDataForRoute(Route route, int radius) {
        mapDataPrefetcher.cancelAllRequests();
        mapDataPrefetcher.clearMapDataCache();
        MapDataPrefetcher.Request estimateMapDataSizeRequest = mapDataPrefetcher.estimateMapDataSize(route, radius);
        MapDataPrefetcher.Request fetchMapDataRequest = mapDataPrefetcher.fetchMapData(route, radius);
    }

    private void calculateRoute(RouteOptions routeOptions) {
        searchBarLinearLayout.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        downloadOfflineMapButton.setVisibility(View.GONE);
        if (selectedFeatureMapMarker != null) {
            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
        }
        hereRouter = new HereRouter(DataHolder.getActivity(), routeOptions);
        if (wayPointIcons.size() == 0) {
            for (int i = 0; i < userInputWaypoints.size(); i++) {
                MapMarker mapMarker = userInputWaypoints.get(i);
                RouteWaypoint routeWaypoint = new RouteWaypoint(mapMarker.getCoordinate());
                GeoCoordinate waypointNavigableGeoCoordinate = routeWaypoint.getNavigablePosition();
                waypointList.add(waypointNavigableGeoCoordinate);
                DataHolder.getMap().removeMapObject(mapMarker);
            }
            if (mapRoute != null) {
                DataHolder.getMap().removeMapObject(mapRoute);
            }
            wayPointIcons = hereRouter.getOutputWaypointIcons();
        }
        hereRouter.setWaypoints(waypointList);
        hereRouter.setContext(DataHolder.getActivity());
        hereRouter.createRouteForNavi();
        for (MapMarker m : wayPointIcons) {
            m.setAnchorPoint(DataHolder.getMapMarkerAnchorPoint(m));
            DataHolder.getMap().addMapObject(m);
        }
//        Log.d(TAG, "wayPointIcons: " + wayPointIcons.size());


        if (DataHolder.getMap().isTrafficInfoVisible()) {
            DynamicPenalty dynamicPenalty = new DynamicPenalty();
            dynamicPenalty.setTrafficPenaltyMode(Route.TrafficPenaltyMode.OPTIMAL);
            coreRouter.setDynamicPenalty(dynamicPenalty);
        }

        progressBar = DataHolder.getActivity().findViewById(R.id.progress_bar);
        progressingTextView = DataHolder.getActivity().findViewById(R.id.progressing_text_view);
//        Log.d(TAG, "Route Calculation Started.");
        coreRouter.calculateRoute(hereRouter.getRoutePlan(), new Router.Listener<List<RouteResult>, RoutingError>() {
            @Override
            public void onProgress(int i) {
                if (i < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressingTextView.setText(R.string.calculating);
                    progressingTextView.setVisibility(View.VISIBLE);
                    progressBar.setProgress(i);
                } else {
                    progressingTextView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCalculateRouteFinished(List<RouteResult> routeResults, RoutingError routingError) {
                androidXMapFragment.setMapMarkerDragListener(mapMarkerOnDragListenerForRoute);

                if (routingError == RoutingError.NONE) {
                    if (routeResults.get(0).getRoute() != null) {
                        theRoute = routeResults.get(0).getRoute();
                        int theRouteTtaDurationExcludintTraffic = theRoute.getTtaExcludingTraffic(0).getDuration();
                        int routeTtaDurationIncludintTraffic = theRoute.getTtaIncludingTraffic(0).getDuration();
                        RouteOptions.TransportMode theRouteTransportMode = theRoute.getRoutePlan().getRouteOptions().getTransportMode();
                        RouteOptions.SpeedProfile theRouteSpeedProfile = theRoute.getRoutePlan().getRouteOptions().getSpeedProfile();
                        Log.d(TAG, theRouteTransportMode + " / " + theRouteSpeedProfile + " --> ttaExcludingTraffic: " + theRouteTtaDurationExcludintTraffic);
                        Log.d(TAG, theRouteTransportMode + " / " + theRouteSpeedProfile + " --> ttaIncludingTraffic: " + routeTtaDurationIncludintTraffic);
                        trafficSignMapContainer.removeAllMapObjects();
                        DataHolder.isRouteOverView = true;
                        destinationLocationGeoCoordinate = theRoute.getRoutePlan().getWaypoint(theRoute.getRoutePlan().getWaypointCount() - 1).getNavigablePosition();
                        if (endGuidanceDirectionalMapPolyline != null) {
                            DataHolder.getMap().removeMapObject(endGuidanceDirectionalMapPolyline);
                            endGuidanceDirectionalMapPolyline = null;
                            endGuidanceDirectionalGeoPolyline = null;
                        }
                        endGuidanceDirectionalGeoPolyline = new GeoPolyline();
                        endGuidanceDirectionalGeoPolyline.add(theRoute.getRoutePlan().getWaypoint(0).getOriginalPosition());
                        endGuidanceDirectionalGeoPolyline.add(theRoute.getRoutePlan().getWaypoint(theRoute.getRoutePlan().getWaypointCount() - 1).getNavigablePosition());
                        endGuidanceDirectionalMapPolyline = new MapPolyline(endGuidanceDirectionalGeoPolyline);

                        endGuidanceDirectionalMapPolyline.setLineColor(Color.argb(128, 255, 0, 0));
                        endGuidanceDirectionalMapPolyline.setPatternStyle(MapPolyline.PatternStyle.DASH_PATTERN);
                        endGuidanceDirectionalMapPolyline.setDashPrimaryLength(12);
                        endGuidanceDirectionalMapPolyline.setDashSecondaryLength(6);
                        endGuidanceDirectionalMapPolyline.setLineWidth(4);
                        DataHolder.getMap().addMapObject(endGuidanceDirectionalMapPolyline);
                        clearDistanceMarkerMapOverlay();

                        resetMapRoute(theRoute);
                        mapRouteGeoBoundingBox = theRoute.getBoundingBox();
                        GeoBoundingBoxDimensionCalculator geoBoundingBoxDimensionCalculator = new GeoBoundingBoxDimensionCalculator(mapRouteGeoBoundingBox);
                        mapRouteGeoBoundingBox.expand((float) (geoBoundingBoxDimensionCalculator.getHeightInMeters() * 0.8), (float) (geoBoundingBoxDimensionCalculator.getWidthInMeters() * 0.6));
                        DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, Map.MOVE_PRESERVE_ORIENTATION);
                        navigationControlButton.setText(R.string.start_navigation);

                        Log.d(TAG, "DataHolder.getNavigationManager().getDistanceUnit(): " + DataHolder.getNavigationManager().getDistanceUnit().name());
                        if (UnitLocale.getDefault() == UnitLocale.ImperialUs || UnitLocale.getDefault() == UnitLocale.Imperial) {
                            routeLengthInLocalUnit = DistanceFormatter.formatLengthMetersToMileYard(theRoute.getLength(), 2);
                        } else {
                            routeLengthInLocalUnit = DistanceFormatter.formatLengthMetersToKilometers(theRoute.getLength(), 2);
                        }
                        Log.d(TAG, "route.getLength():" + theRoute.getLength() + " m");
                        Log.d(TAG, "routeLengthInLocalUnit:" + routeLengthInLocalUnit);

                        //Check TrafficSigns from route calculation result
                        new Thread(() -> getTrafficSignsForRoute(theRoute)).start();

                        //Check toll costs and routing zones
                        new Thread(() -> getRouteInfo(theRoute)).start();

                        //Prefetch map data for route
                        if (prefetchMapDataThreadList.size() > 0) {
                            for (Thread thread : prefetchMapDataThreadList) {
                                thread.interrupt();
                                thread = null;
                            }
                        }
                        prefetchMapDataThreadList.clear();
                        Thread prefetchMapDataThread = new Thread(() -> prefetchMapDataForRoute(theRoute, 1000));
                        prefetchMapDataThreadList.add(prefetchMapDataThread);
                        for (Thread thread : prefetchMapDataThreadList) {
                            thread.start();
                        }

                        androidXMapFragment.getMapGesture().removeOnGestureListener(customOnGestureListener);


                    } else {
                        Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), R.string.cant_find_a_route, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.error) + routingError.name(), Snackbar.LENGTH_LONG).show();
                    progressingTextView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    if (mapRoute != null) {
                        DataHolder.getMap().removeMapObject(mapRoute);
                    }
                }
//                retryRouting(m_activity, routingError, routeOptions);
            }
        });
    }

    void onDestroy() {
        /* Stop the navigation when app is destroyed */
        CLE2DataManager.getInstance().newPurgeLocalStorageTask().start();
        if (DataHolder.getNavigationManager() != null) {
            stopForegroundService();
            DataHolder.getNavigationManager().stop();
        }
    }

    private void initAndroidXMapFragment() {

        DataHolder.setAndroidXMapFragment(getMapFragment());
        androidXMapFragment = DataHolder.getAndroidXMapFragment();
        androidXMapFragment.setCopyrightLogoPosition(CopyrightLogoPosition.TOP_CENTER);

//        String diskCacheRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "here_offline_cache";
        /* Starting from Android 10 (see Scoped storage), disk cache path must be set under application-specific file directory. */
//        String diskCacheRoot = DataHolder.getActivity().getFilesDir().getParent() + File.separator + "files" + File.separator + ".here-maps";
//        Log.d(TAG, "defaultCachePath: " + diskCacheRoot);
//        MapSettings.setDiskCacheRootPath(diskCacheRoot);
        /* Preload voice skins of zh_TW, zh_CN and en_US */
//        try {
//            InputStream inputStream = DataHolder.getActivity().getAssets().open("voices/voices-download.zip");
//            Unzip.unzip(inputStream, new File(diskCacheRoot + File.separator + "voices-download"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//         Retrieve intent name from manifest
        String intentName = "";
        try {
            ApplicationInfo ai = DataHolder.getActivity().getPackageManager().getApplicationInfo(DataHolder.getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            intentName = bundle.getString("INTENT_NAME");
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(this.getClass().toString(), "Failed to find intent name, NameNotFound: " + e.getMessage());
        }

        Log.d(TAG, "intentName:" + intentName);

        if (androidXMapFragment != null) androidXMapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                Log.d(TAG, "onEngineInitializationCompleted error: " + error);
                if (error == Error.NONE) {
                    /* Entrance */
                    DataHolder.setMap(androidXMapFragment.getMap());
//                    TrafficUpdater trafficUpdater = TrafficUpdater.getInstance();
//                    Log.d(TAG, "trafficUpdater.isAutoUpdateEnabled(): " + trafficUpdater.isAutoUpdateEnabled());
//                    trafficUpdater.setRefreshInterval(120);
//                    DataHolder.getMap().setProjectionMode(Map.Projection.MERCATOR);
                    mainLinearLayout = DataHolder.getActivity().findViewById(R.id.main_linear_layout);

                    coreRouter = new CoreRouter();
                    navigationListeners = new NavigationListeners();
                    mapScaleView = DataHolder.getActivity().findViewById(R.id.map_scale_view);
                    mapScaleView.setMap(DataHolder.getMap());
                    mapScaleView.setColor(R.color.black);
                    DataHolder.getMap().setFadingAnimations(false);
                    mapSchemeChanger = new MapSchemeChanger(DataHolder.getMap());

                    DataHolder.getMap().setFleetFeaturesVisible(EnumSet.of(
                            Map.FleetFeature.CONGESTION_ZONES,
//                            Map.FleetFeature.TRUCK_RESTRICTIONS,
                            Map.FleetFeature.ENVIRONMENTAL_ZONES));

                    trafficSignMapContainer = new MapContainer();
                    DataHolder.getMap().addMapObject(trafficSignMapContainer);

                    /*Cluster layer for traffic signs*/
                    trafficSignClusterLayer = new ClusterLayer();
//                    Image clusterIconImage = new Image();
//                    Bitmap trafficSignClusterBackground = BitmapFactory.decodeResource(DataHolder.getActivity().getResources(), R.drawable.empty_sign_black_background);
//                    float aspectRatio = (float) trafficSignClusterBackground.getHeight() / (float) trafficSignClusterBackground.getWidth();
//                    Bitmap iconBitmap = Bitmap.createScaledBitmap(trafficSignClusterBackground, 128, (int) (128 * aspectRatio), false);
//                    clusterIconImage.setBitmap(iconBitmap);
                    trafficSignClusterLayer.setClusterCreateListener(new ClusterLayer.ClusterCreateListener() {
                        @Override
                        public void onClusterCreated(@NonNull ClusterViewObject clusterViewObject) {
                            clusterViewObject.getMarkers().forEach(it -> it.setZIndex(200));
                        }
                    });
                    BasicClusterStyle basicClusterStyle = new BasicClusterStyle();
                    basicClusterStyle.setFillColor(Color.DKGRAY);
                    basicClusterStyle.setStrokeColor(Color.BLACK);
                    basicClusterStyle.setFontColor(Color.WHITE);
                    ClusterTheme clusterTheme = new ClusterTheme();
                    clusterTheme.setStyle(basicClusterStyle);
                    trafficSignClusterLayer.setTheme(clusterTheme);
                    DataHolder.getMap().addClusterLayer(trafficSignClusterLayer);
                    /*Cluster layer for traffic signs*/

                    Geocoder androidGeocoder = new Geocoder(DataHolder.getActivity(), Locale.ENGLISH);
                    DataHolder.isNavigating = false;
                    GeoCoordinate defaultMapCenter = new GeoCoordinate(25.03304289712915, 121.56442945435184);
                    DataHolder.getMap().setCenter(defaultMapCenter, Map.Animation.NONE);

                    if (DataHolder.getLastMapCenter() != null) {
                        DataHolder.getMap().setCenter(DataHolder.getLastMapCenter(), Map.Animation.NONE);
                        DataHolder.getMap().setZoomLevel(DataHolder.getLastMapZoom());
                    }

                    DataHolder.isDragged = false;

                    /* Rotate compass icon*/
                    DataHolder.getMap().addTransformListener(new Map.OnTransformListener() {
                        @Override
                        public void onMapTransformStart() {
                        }

                        @Override
                        public void onMapTransformEnd(MapState mapState) {
//                            Log.d(TAG, "" + mapState.getCenter());
                            if (previousMapState != null) {
                                if (!DataHolder.isNavigating) {
                                    if (mapState.getZoomLevel() > 8) {
                                        GeoCoordinate mapCenterGeoCoordinate = DataHolder.getMap().getCenter();
                                        try {
                                            List<Address> addresses = androidGeocoder.getFromLocation(mapCenterGeoCoordinate.getLatitude(), mapCenterGeoCoordinate.getLongitude(), 1);
                                            if (addresses.size() > 0) {
                                                for (Address address : addresses) {
                                                    String countryName = address.getCountryName();
                                                    String adminAreaName = address.getAdminArea();
                                                    if (mapState.getZoomLevel() >= 12) {
                                                        DataHolder.getMap().removeClusterLayer(trafficSignClusterLayer);
                                                        trafficSignMapContainer.setVisible(true);
                                                    } else {
                                                        DataHolder.getMap().addClusterLayer(trafficSignClusterLayer);
                                                        trafficSignMapContainer.setVisible(false);
                                                    }
                                                    if (countryName != null && adminAreaName != null) {
                                                        if (countryName.equals("China") || countryName.equals("中国")) {
                                                            if (mapState.getZoomLevel() >= 6 && mapState.getZoomLevel() <= 22) {
                                                                if (customRasterTileOverlay == null) {
                                                                    customRasterTileOverlay = new CustomRasterTileOverlay();
                                                                    if (customRasterTileOverlay.getTileUrl() == null) {
                                                                        String[] subDomainsArray = {"a", "b", "c"};
                                                                        customRasterTileOverlay.setSubDomains(subDomainsArray);
                                                                        customRasterTileOverlay.setTileUrl("https://%s.tile.openstreetmap.org/%s/%s/%s.png");
                                                                    }
                                                                    DataHolder.getMap().addRasterTileSource(customRasterTileOverlay);
                                                                }
                                                            }
                                                        } else if (countryName.equals("Taiwan")) {
//                                                            if (adminAreaName.equals("Taipei City")) {
//                                                                if (mapState.getZoomLevel() >= 15 && mapState.getZoomLevel() <= 22) {
//                                                                    if (customRasterTileOverlay == null) {
//                                                                        customRasterTileOverlay = new CustomRasterTileOverlay();
//                                                                        if (customRasterTileOverlay.getTileUrl() == null) {
//                                                                            customRasterTileOverlay.setTileUrl("https://raw.githubusercontent.com/aquawill/taipei_city_parking_layer/master/tiles/%s/%s/%s.png");
//                                                                        }
//                                                                        DataHolder.getMap().addRasterTileSource(customRasterTileOverlay);
//                                                                    }
//                                                                }
//                                                            }
                                                            if (previousMapState.getCenter().distanceTo(mapState.getCenter()) > 0 || previousMapState.getZoomLevel() != mapState.getZoomLevel()) {
                                                                roadkillGeoJsonTileMapContainer.removeAllMapObjects();
                                                                previousMapState = mapState;
                                                                if (DataHolder.getMap().getBoundingBox() != null) {
                                                                    roadkillGeoJSONTileLoader.getTilesWithMapBoundingBox(DataHolder.getMap().getBoundingBox(), mapState.getZoomLevel());
                                                                    roadkillGeoJSONTileLoader.setOnTileRequestCompletedListener(new GeoJSONTileLoader.OnTileRequestCompletedListener() {
                                                                        @Override
                                                                        public void onCompleted() {
                                                                            List<GeoJSONTileLoader.PointResult> pointResultList = roadkillGeoJSONTileLoader.getPointResultList();
                                                                            for (GeoJSONTileLoader.PointResult pointResult : pointResultList) {
                                                                                try {
                                                                                    String type = pointResult.getProperties().getString("type");
                                                                                    String dayNight = pointResult.getProperties().getString("day_night");
                                                                                    String recommendedType = pointResult.getProperties().getString("rec_type");
                                                                                    String season = pointResult.getProperties().getString("season");
                                                                                    String routeDescription = pointResult.getProperties().getString("rt_desc");
                                                                                    MapMarker geoJSONTileMapMarker = new MapMarker(pointResult.getGeoCoordinate());
                                                                                    geoJSONTileMapMarker.setTitle(recommendedType);
                                                                                    geoJSONTileMapMarker.setDescription(season + "\n" + routeDescription);
                                                                                    switch (type) {
                                                                                        case "鳥類":
                                                                                            if (dayNight.equals("晚上")) {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_bird_night, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            } else {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_bird_day, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            }
                                                                                            break;
                                                                                        case "哺乳類":
                                                                                            if (dayNight.equals("晚上")) {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_mammal_night, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            } else {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_mammal_day, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            }
                                                                                            break;
                                                                                        case "兩生類":
                                                                                            if (dayNight.equals("晚上")) {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_frog_night, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            } else {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_frog_day, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            }
                                                                                            break;
                                                                                        case "爬行類":
                                                                                            if (recommendedType.equals("烏龜")) {
                                                                                                if (dayNight.equals("晚上")) {
                                                                                                    Image icon = new Image();
                                                                                                    icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_turtle_night, 64, 64));
                                                                                                    geoJSONTileMapMarker.setIcon(icon);
                                                                                                } else {
                                                                                                    Image icon = new Image();
                                                                                                    icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_turtle_day, 64, 64));
                                                                                                    geoJSONTileMapMarker.setIcon(icon);
                                                                                                }
                                                                                            } else {
                                                                                                if (dayNight.equals("晚上")) {
                                                                                                    Image icon = new Image();
                                                                                                    icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_snake_night, 64, 64));
                                                                                                    geoJSONTileMapMarker.setIcon(icon);
                                                                                                } else {
                                                                                                    Image icon = new Image();
                                                                                                    icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_snake_day, 64, 64));
                                                                                                    geoJSONTileMapMarker.setIcon(icon);
                                                                                                }
                                                                                            }
                                                                                            break;
                                                                                        case "陸蟹":
                                                                                            if (dayNight.equals("晚上")) {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_crab_night, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            } else {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_crab_day, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            }
                                                                                            break;
                                                                                        case "昆蟲":
                                                                                            if (dayNight.equals("晚上")) {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_butterfly_night, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            } else {
                                                                                                Image icon = new Image();
                                                                                                icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_butterfly_day, 64, 64));
                                                                                                geoJSONTileMapMarker.setIcon(icon);
                                                                                            }
                                                                                            break;
                                                                                    }
                                                                                    roadkillGeoJsonTileMapContainer.addMapObject(geoJSONTileMapMarker);
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
//                                                                                Log.d(TAG, "getLineStringResultList().size(): " + roadkillGeoJSONTileLoader.getLineStringResultList().size());
//                                                                                Log.d(TAG, "getPolygonResultList().size(): " + roadkillGeoJSONTileLoader.getPolygonResultList().size());
                                                                        }
                                                                    });
//                                                                        if (geoJsonTileMapContainer == null) {
//                                                                            geoJsonTileMapContainer = geoJsonTileLoader.getMapContainer();
//                                                                            DataHolder.getMap().addMapObject(geoJsonTileMapContainer);
//                                                                        }
                                                                }
                                                            }
                                                        } else {
                                                            if (customRasterTileOverlay != null) {
                                                                DataHolder.getMap().removeRasterTileSource(customRasterTileOverlay);
                                                                customRasterTileOverlay = null;
                                                            }

                                                        }
                                                    } else {
                                                        if (customRasterTileOverlay != null) {
                                                            DataHolder.getMap().removeRasterTileSource(customRasterTileOverlay);
                                                            customRasterTileOverlay = null;
                                                        }

                                                    }
                                                }
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                previousMapState = mapState;
                            }

                            if (DataHolder.isNavigating || DataHolder.getMap().

                                    getZoomLevel() < 17) {
                                positionAccuracyMapCircle.setLineWidth(0);
                                positionAccuracyMapCircle.setFillColor(Color.argb(0, 0, 0, 0));
                            }
                            compassButton.setRotation(mapState.getOrientation() * -1);

                        }
                    });

                    DataHolder.getMap().addSchemeChangedListener(s -> Log.d(TAG, "onMapSchemeChanged: " + s));

                    androidXMapFragment.addOnMapRenderListener(new OnMapRenderListener() {
                        @Override
                        public void onPreDraw() {
                        }

                        @Override
                        public void onPostDraw(boolean b, long l) {
                        }

                        @Override
                        public void onSizeChanged(int i, int i1) {
                            if (!DataHolder.isNavigating) {
                                if (DataHolder.getActivity().isInMultiWindowMode()) {
                                    new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.7f);
                                } else {
                                    new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                                }
                            } else {
                                if (DataHolder.getActivity().isInMultiWindowMode()) {
                                    new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                                } else {
                                    new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.75f);
                                }
                            }
                            if (DataHolder.isRouteOverView && mapRouteGeoBoundingBox != null) {
                                DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, Map.MOVE_PRESERVE_ORIENTATION);
                            }
                        }

                        @Override
                        public void onGraphicsDetached() {

                        }

                        @Override
                        public void onRenderBufferCreated() {

                        }
                    });
                    activateHereAdvancedPositioning = true;
                    DataHolder.setPositioningManager(new PositioningManagerActivator(PositioningManager.LocationMethod.GPS_NETWORK, activateHereAdvancedPositioning).getPositioningManager());
                    DataHolder.getPositioningManager().addListener(new WeakReference<>(positionChangedListener));
                    positionIndicator = androidXMapFragment.getPositionIndicator();
                    positionIndicator.setSmoothPositionChange(true);
                    positionIndicator.setAccuracyIndicatorVisible(true);
                    positionIndicator.setVisible(true);
                    trafficWarningTextView = DataHolder.getActivity().findViewById(R.id.traffic_warning_text_view);

                    roadkillGeoJSONTileLoader = new GeoJSONTileLoader(DataHolder.getActivity(), "https://xyz.api.here.com/hub/spaces/ppwW5xZ4/tile/web/%s_%s_%s?access_token=AJt8bGnvRgWmUd0RppoLxQA&tags=");
                    roadkillGeoJsonTileMapContainer = new MapContainer();
                    DataHolder.getMap().addMapObject(roadkillGeoJsonTileMapContainer);

                    searchRequestResultMapContainer = new MapContainer();
                    DataHolder.getMap().addMapObject(searchRequestResultMapContainer);
                    gpsSwitch = DataHolder.getActivity().findViewById(R.id.gps_switch);
                    gpsSwitch.setChecked(true);
//                        gpsSwitch.setEnabled(false);
                    gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            DataHolder.getPositioningManager().stop();
                            DataHolder.getNavigationManager().stop();
                            DataHolder.getPositioningManager().removeListener(positionChangedListener);
                            DataHolder.setPositioningManager(null);
                            positionIndicator.setVisible(false);
                            currentPositionMapLocalModel.setVisible(false);
                            positionAccuracyMapCircle.setVisible(false);
                            gpsStatusImageView.setImageResource(R.drawable.ic_gps_off_white_24dp);
                            gpsStatusImageView.setImageTintList(DataHolder.getActivity().getResources().getColorStateList(R.color.red));
                            if (isChecked) {
//                                Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), "開啟硬體GPS", Snackbar.LENGTH_SHORT).show();
                                DataHolder.setPositioningManager(new PositioningManagerActivator(PositioningManager.LocationMethod.GPS_NETWORK, activateHereAdvancedPositioning).getPositioningManager());
//                                DataHolder.getNavigationManager().startTracking();
                            } else {
//                                Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), "關閉硬體GPS", Snackbar.LENGTH_SHORT).show();
                                DataHolder.setPositioningManager(new PositioningManagerActivator(PositioningManager.LocationMethod.NETWORK, activateHereAdvancedPositioning).getPositioningManager());
                                DataHolder.getNavigationManager().stop();
                                guidanceSpeedView.setVisibility(View.INVISIBLE);
                                guidanceSpeedLimitView.setVisibility(View.INVISIBLE);
                                speedLabelTextView.setVisibility(View.INVISIBLE);
                            }
                            DataHolder.setPositioningManager(DataHolder.getPositioningManager());
                            DataHolder.getPositioningManager().addListener(new WeakReference<>(positionChangedListener));
                        }
                    });

                    signImageView1 = DataHolder.getActivity().findViewById(R.id.sign_imageView_1);
                    signImageView2 = DataHolder.getActivity().findViewById(R.id.sign_imageView_2);
                    signImageView3 = DataHolder.getActivity().findViewById(R.id.sign_imageView_3);

                    if (DataHolder.getActivity().isInMultiWindowMode()) {
                        new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.7f);
                    } else {
                        new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                    }
                    DataHolder.getMap().setMapScheme(Map.Scheme.NORMAL_DAY);
                    DataHolder.getMap().setMapDisplayLanguage(Locale.getDefault());
                    DataHolder.getMap().setSafetySpotsVisible(true);
                    DataHolder.getMap().setExtrudedBuildingsVisible(true);
                    DataHolder.getMap().setLandmarksVisible(true);
                    DataHolder.getMap().setExtendedZoomLevelsEnabled(false);

                    switchGuidanceUiViews(View.GONE);
                    gpsStatusImageView = DataHolder.getActivity().findViewById(R.id.gps_status_image_view);
                    /* Listeners of map buttons */
                    compassButton = DataHolder.getActivity().findViewById(R.id.north_up);
                    compassButton.setOnClickListener(v -> {
                        if (searchResultSnackbar != null) {
                            searchResultSnackbar.dismiss();
                        }
                        isMapRotating = false;
                        DataHolder.getMap().setOrientation(0);
                        compassButton.setRotation(0);
                        DataHolder.getMap().setTilt(0);
                        DataHolder.getMap().setZoomLevel(16);
                        if (DataHolder.getActivity().isInMultiWindowMode()) {
                            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.7f);
                        } else {
                            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
                        }
                        if (!DataHolder.isRouteOverView) {
                            DataHolder.getMap().setCenter(DataHolder.getPositioningManager().getLastKnownPosition().getCoordinate(), Map.Animation.LINEAR);
                        } else {
                            if (mapRouteGeoBoundingBox != null) {
                                DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, Map.MOVE_PRESERVE_ORIENTATION);
                            }
                        }
                    });
//                        zoomInButton = DataHolder.getActivity().findViewById(R.id.zoom_in);
//                        zoomInButton.setOnClickListener(v -> {
//                            double zoomLevel = DataHolder.getMap().getZoomLevel();
//                            DataHolder.getMap().setZoomLevel(zoomLevel + 1);
//                        });
//                        zoomOutButton = DataHolder.getActivity().findViewById(R.id.zoom_out);
//                        zoomOutButton.setOnClickListener(v -> {
//                            double zoomLevel = DataHolder.getMap().getZoomLevel();
//                            DataHolder.getMap().setZoomLevel(zoomLevel - 1);
//                        });
                    carRouteButton = DataHolder.getActivity().findViewById(R.id.car_route);
                    truckRouteButton = DataHolder.getActivity().findViewById(R.id.truck_route);
                    scooterRouteButton = DataHolder.getActivity().findViewById(R.id.scooter_route);
                    bikeRouteButton = DataHolder.getActivity().findViewById(R.id.bike_route);
                    pedsRouteButton = DataHolder.getActivity().findViewById(R.id.peds_route);
                    carRouteButton.setOnClickListener(vCarRouteButton -> {
                        calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.CAR));
                        carRouteButton.setAlpha(1.0f);
                        truckRouteButton.setAlpha(0.3f);
                        scooterRouteButton.setAlpha(0.3f);
                        bikeRouteButton.setAlpha(0.3f);
                        pedsRouteButton.setAlpha(0.3f);

                    });
                    truckRouteButton.setOnClickListener(vTruckRouteButton -> {
                        calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.TRUCK));
                        carRouteButton.setAlpha(0.3f);
                        truckRouteButton.setAlpha(1.0f);
                        scooterRouteButton.setAlpha(0.3f);
                        bikeRouteButton.setAlpha(0.3f);
                        pedsRouteButton.setAlpha(0.3f);
                    });
                    scooterRouteButton.setOnClickListener(vScooterRouteButton -> {
                        calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.SCOOTER));
                        carRouteButton.setAlpha(0.3f);
                        truckRouteButton.setAlpha(0.3f);
                        scooterRouteButton.setAlpha(1.0f);
                        bikeRouteButton.setAlpha(0.3f);
                        pedsRouteButton.setAlpha(0.3f);
                    });
                    bikeRouteButton.setOnClickListener(vBikeRouteButton -> {
                        calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.BICYCLE));
                        carRouteButton.setAlpha(0.3f);
                        truckRouteButton.setAlpha(0.3f);
                        scooterRouteButton.setAlpha(0.3f);
                        bikeRouteButton.setAlpha(1.0f);
                        pedsRouteButton.setAlpha(0.3f);
                    });
                    pedsRouteButton.setOnClickListener(vPedsRouteButton -> {
                        calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.PEDESTRIAN));
                        carRouteButton.setAlpha(0.3f);
                        truckRouteButton.setAlpha(0.3f);
                        scooterRouteButton.setAlpha(0.3f);
                        bikeRouteButton.setAlpha(0.3f);
                        pedsRouteButton.setAlpha(1.0f);
                    });
                    trafficButton = DataHolder.getActivity().findViewById(R.id.traffic_button);
                    trafficButton.setBackgroundResource(R.drawable.round_button_off);
                    trafficButton.setOnClickListener(v -> {
                        if (!DataHolder.getMap().isTrafficInfoVisible()) {
                            trafficEnabled = true;
                            DataHolder.getMap().setTrafficInfoVisible(true);
                            mapSchemeChanger.trafficMapOn();
                            trafficButton.setBackgroundResource(R.drawable.round_button_on);
                        } else {
                            trafficEnabled = false;
                            DataHolder.getMap().setTrafficInfoVisible(false);
                            mapSchemeChanger.trafficMapOff();
                            trafficButton.setBackgroundResource(R.drawable.round_button_off);
                        }
                    });
                    satMapButton = DataHolder.getActivity().findViewById(R.id.sat_map_button);
                    satMapButton.setBackgroundResource(R.drawable.round_button_off);
                    satMapButton.setOnClickListener(v -> {
                        if (!isSatelliteMap) {
                            isSatelliteMap = true;
                            satMapButton.setBackgroundResource(R.drawable.round_button_on);
                            mapSchemeChanger.satelliteMapOn();

                        } else {
                            isSatelliteMap = false;
                            satMapButton.setBackgroundResource(R.drawable.round_button_off);
                            mapSchemeChanger.satelliteMapOff();
                        }
                    });
                    searchButton = DataHolder.getActivity().findViewById(R.id.search_button);
                    downloadOfflineMapButton = DataHolder.getActivity().findViewById(R.id.download_button);
                    offlineMapDownloader = new OfflineMapDownloader();
                    downloadOfflineMapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), R.string.checking_offline_map, Snackbar.LENGTH_SHORT).show();
                            downloadOfflineMapButton.setVisibility(View.GONE);
                            offlineMapDownloader.downloadOfflineMapPackageOnMapCenter(DataHolder.getMap().getCenter());
                        }
                    });
                    searchTextBar = DataHolder.getActivity().findViewById(R.id.search_input_text);
                    searchBarLinearLayout = DataHolder.getActivity().findViewById(R.id.search_bar_linear_layout);
                    searchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (searchResultSnackbar != null) {
                                searchResultSnackbar.dismiss();
                            }
                            searchTextBar.setText("");
                            searchBarLinearLayout.setVisibility(View.VISIBLE);
                            searchTextBar.requestFocus();
                            InputMethodManager inputMethodManager = (InputMethodManager) DataHolder.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(searchTextBar, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                    searchTextBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                String inputString = searchTextBar.getText().toString();
                                if (inputString.matches("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$")) {
                                    double inputLatitude = Double.parseDouble(inputString.split(",")[0]);
                                    double inputLongitude = Double.parseDouble(inputString.split(",")[1]);
                                    GeoCoordinate inputGeoCoordinate = new GeoCoordinate(inputLatitude, inputLongitude);
                                    DataHolder.getMap().setCenter(inputGeoCoordinate, Map.Animation.BOW);
                                    isDragged = true;
                                    InputMethodManager inputMethodManager = (InputMethodManager) DataHolder.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(searchTextBar.getWindowToken(), 0);
                                    ReverseGeocodeRequest reverseGeocodeRequest = new ReverseGeocodeRequest(inputGeoCoordinate);
                                    reverseGeocodeRequest.addCustomHeader("HouseNumberMode", "Streetlevel");
                                    reverseGeocodeRequest.execute(new ResultListener<com.here.android.mpa.search.Location>() {
                                        @Override
                                        public void onCompleted(com.here.android.mpa.search.Location location, ErrorCode errorCode) {
                                            if (errorCode == ErrorCode.NONE) {
                                                if (location != null) {
                                                    new SearchResultHandler(DataHolder.getActivity().findViewById(R.id.mapFragmentView), location, DataHolder.getMap());
                                                } else {
                                                    Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.unable_to_find_an_address_at) + inputString, Snackbar.LENGTH_INDEFINITE).show();
                                                }
                                            } else {
                                                Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), errorCode.name(), Snackbar.LENGTH_INDEFINITE).show();
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "searchTextBar.getText().toString(): " + searchTextBar.getText().toString());
//                                   /*Place search request*/
                                    new SearchResultHandler(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getMap().getCenter(), searchTextBar.getText().toString(), DataHolder.getMap());
                                    /*Geocode request*/
//                                    new SearchResultHandler(searchTextBar.getText().toString(), DataHolder.getMap().getCenter(), 99999);
                                }
                                searchTextBar.clearFocus();
                                InputMethodManager inputMethodManager = (InputMethodManager) DataHolder.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(searchTextBar.getWindowToken(), 0);
                                return true;
                            }
                            return false;
                        }
                    });

                    DataHolder.getActivity().findViewById(R.id.log_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPositionLogging) {
                                isPositionLogging = true;
                                DataHolder.getActivity().findViewById(R.id.log_button).setBackgroundTintList(DataHolder.getActivity().getResources().getColorStateList(R.color.red));
                                DataHolder.getPositioningManager().setLogType(EnumSet.of(
                                        PositioningManager.LogType.RAW,
                                        PositioningManager.LogType.MATCHED,
                                        PositioningManager.LogType.DATA_SOURCE
                                ));
                                Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.gpx_logging) + DataHolder.getActivity().getFilesDir().getAbsolutePath() + File.separator + "gpx/", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });

                    navigationControlButton = DataHolder.getActivity().findViewById(R.id.startGuidance);
                    navigationControlButton.setText(R.string.create_route);
                    navigationControlButton.setOnClickListener(v -> {
                        if (theRoute != null) {
//                                stopNavigationManager();
                            if (laneInformationMapOverlay != null) {
                                DataHolder.getMap().removeMapOverlay(laneInformationMapOverlay);
                            }
                            DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.LINEAR, 0f);
                            laneInformationMapOverlay = null;
                            if (DataHolder.getNavigationManager() != null) {
                                DataHolder.getNavigationManager().stop();
                            }
                            DataHolder.getMap().setTilt(0);
                            switchGuidanceUiPresenters(false);
                            startNavigation(theRoute, true);
                        } else {
                            calculateRoute(prepareRouteOptions(RouteOptions.TransportMode.CAR));
                            carRouteButton.setAlpha(1.0f);
                            truckRouteButton.setAlpha(0.5f);
                            scooterRouteButton.setAlpha(0.5f);
                            bikeRouteButton.setAlpha(0.5f);
                            pedsRouteButton.setAlpha(0.5f);
                        }
                    });
                    clearButton = DataHolder.getActivity().findViewById(R.id.clear);
                    clearButton.setOnClickListener(v -> resetMap());
                    safetyCamLinearLayout = DataHolder.getActivity().findViewById(R.id.safety_cam_linear_layout);
                    safetyCamImageView = DataHolder.getActivity().findViewById(R.id.safety_cam_image_view);
                    safetyCamTextView = DataHolder.getActivity().findViewById(R.id.safety_cam_text_view);
//                        safetyCamSpeedTextView = m_activity.findViewById(R.id.safety_cam_speed_text_view);
                    distanceMarkerLinearLayout = DataHolder.getActivity().findViewById(R.id.distance_marker_linear_layout);
                    distanceMarkerFreeIdImageView = DataHolder.getActivity().findViewById(R.id.distance_marker_freeway_id);
                    distanceMarkerDistanceValue = DataHolder.getActivity().findViewById(R.id.distance_marker_distance_value);

                    androidXMapFragment.getMapGesture().addOnGestureListener(customOnGestureListener, 0, false);

                    /* Download voice */
                    voiceActivation = new VoiceActivation(DataHolder.getActivity());
                    voiceActivation.setContext(DataHolder.getActivity());
                    String currentISO3Country = Locale.getDefault().getISO3Country();
                    Log.d(TAG, "Locale.getDefault().getISO3Country(): " + currentISO3Country);
                    if (currentISO3Country.equals("TWN")) {
                        voiceActivation.setDesiredVoiceId(217);
                    } else if (currentISO3Country.equals("CHN")) {
                        voiceActivation.setDesiredVoiceId(31000);
                    } else {
                        voiceActivation.setDesiredVoiceId(206);
                    }
                    voiceActivation.downloadCatalogAndSkin();

                    /* adding rotatable position indicator to the map */
                    if (currentPositionMapLocalModel == null) {
                        createPosition3dObj();
                    }
                    positionAccuracyMapCircle = new MapCircle();
                    positionAccuracyMapCircle.setRadius(1f);
                    positionAccuracyMapCircle.setLineWidth(0);
                    positionAccuracyMapCircle.setFillColor(Color.argb(0, 0, 0, 0));
                    positionAccuracyMapCircle.setLineColor(Color.argb(0, 0, 0, 0));
                    positionAccuracyMapCircle.setCenter(defaultMapCenter);
                    DataHolder.getMap().addMapObject(positionAccuracyMapCircle);

                    /* init safetyCameraMapMarker */
                    safetyCameraMapMarker = new MapMarker();
                    DataHolder.getMap().addMapObject(safetyCameraMapMarker);

                    Typeface tf = Typeface.createFromAsset(DataHolder.getActivity().getAssets(), "fonts/SFDigitalReadout-Medium.ttf");
                    guidanceSpeedView = DataHolder.getActivity().findViewById(R.id.guidance_speed_view);
                    guidanceSpeedView.setTypeface(tf);

                    guidanceSpeedView.setVisibility(View.GONE);

//                    Get speed and position every second to refresh guidanceSpeedView

                    final Handler handler = new Handler();
                    final int delay = 1000; // 1000 milliseconds == 1 second
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (!isNavigating) {
                                GeoPosition thisSecondLocation = DataHolder.getPositioningManager().getLastKnownPosition();
//                                Log.d(TAG, "newLocation getLatitude: " + thisSecondLocation.getCoordinate().getLatitude());
//                                Log.d(TAG, "newLocation getLongitude: " + thisSecondLocation.getCoordinate().getLongitude());
                                if (lastSecondLocation != null && thisSecondLocation.getCoordinate().isValid()) {
                                    if (lastSecondLocation.distanceTo(thisSecondLocation.getCoordinate()) > 0) {
                                        lastSecondLocation = thisSecondLocation.getCoordinate();
                                        if (UnitLocale.getDefault() == UnitLocale.ImperialUs) {
                                            guidanceSpeedView.setText(String.valueOf((int) new SpeedConverter().convert(thisSecondLocation.getSpeed(), MeasurementUnit.METERS_PER_SECOND, MeasurementUnit.MILES_PER_HOUR).getValue()));
                                        } else {
                                            guidanceSpeedView.setText(String.valueOf((int) new SpeedConverter().convert(thisSecondLocation.getSpeed(), MeasurementUnit.METERS_PER_SECOND, MeasurementUnit.KILOMETERS_PER_HOUR).getValue()));
                                        }
                                    } else {
                                        guidanceSpeedView.setText("0");
                                    }
                                } else {
                                    lastSecondLocation = thisSecondLocation.getCoordinate();
                                }
                            }
                            handler.postDelayed(this, delay);
                        }
                    }, delay);

                    speedLabelTextView = DataHolder.getActivity().findViewById(R.id.speed_label_text_view);
                    speedLabelTextView.setTypeface(tf);
                    if (UnitLocale.getDefault() == UnitLocale.ImperialUs) {
                        speedLabelTextView.setText(R.string.mph);
                    } else {
                        speedLabelTextView.setText(R.string.kph);
                    }
                    speedLabelTextView.setVisibility(View.GONE);
                    guidanceSpeedLimitView = DataHolder.getActivity().findViewById(R.id.guidance_speed_limit_view);

                    TextView distanceTextView = DataHolder.getActivity().findViewById(R.id.distanceView);
                    distanceTextView.setTextSize(DpConverter.convertDpToPixel(16, DataHolder.getActivity()));
                    startNavigationManager();
//                    DataHolder.getNavigationManager().startTracking();
                    downloadOfflineMapButton.setVisibility(View.VISIBLE);

                    /*Implementation of MapDataPrefetcher*/
                    mapDataPrefetcher = MapDataPrefetcher.getInstance();
                    ProgressBar mapDataPrefetchProgressBar = DataHolder.getActivity().findViewById(R.id.mapDataPrefetchProgressBar);
                    MapDataPrefetcher.Listener mapDataPrefetcherListener = new MapDataPrefetcher.Listener() {
                        @Override
                        public void onProgress(int i, float v) {
                            Log.d(TAG, "MapDataPrefetcher.Listener().onProgress(): requestID: " + i + " progress: " + v);
                            if (v < 100 && v > 0) {
                                mapDataPrefetchProgressBar.setVisibility(View.VISIBLE);
                                mapDataPrefetchProgressBar.setProgress((int) v);
                            } else {
                                mapDataPrefetchProgressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onStatus(int i, PrefetchStatus prefetchStatus) {
                            Log.d(TAG, "MapDataPrefetcher.Listener().onStatus(): " + i + " prefetchStatus: " + prefetchStatus.name());
                            switch (prefetchStatus) {
                                case PREFETCH_SUCCESS:
                                case PREFETCH_FAILURE:
                                case PREFETCH_CANCELLED:
                                    mapDataPrefetchProgressBar.setVisibility(View.GONE);
                                    break;

                            }
                        }

                        @Override
                        public void onCachePurged(boolean b) {
                            Log.d(TAG, "MapDataPrefetcher.Listener().onCachePurged(): success: " + b);
                        }

                        @Override
                        public void onDataSizeEstimated(int i, boolean b, long l) {
                            Log.d(TAG, "MapDataPrefetcher.Listener().onDataSizeEstimated(): requestId: " + i + " success: " + b + " dataSizeKB: " + l);
                        }
                    };
                    mapDataPrefetcher.addListener(mapDataPrefetcherListener);
                    /*Custom Route test code*/

//                    RouteWaypoint start = new RouteWaypoint(new GeoCoordinate(43.080341960303485, 12.355455933138728));
//                    RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(43.08029372595563, 12.35537241209641));
//                    List<RouteWaypoint> waypoints = Arrays.asList(start, destination);
//
//                    FTCRRouteOptions routeOptions = new FTCRRouteOptions();
//                    routeOptions.setUseTraffic(true)
//                            .setTransportMode(FTCRRouteOptions.TransportMode.CAR)
//                            .setRouteType(FTCRRouteOptions.Type.FASTEST);
//
//                    FTCRRouter router = new FTCRRouter();
//                    FTCRRoutePlan ftcrRoutePlan = new FTCRRoutePlan(waypoints, routeOptions);
////                    ftcrRoutePlan.setOverlay("OVERLAYTAIPEIPORT");
//                    router.calculateRoute(ftcrRoutePlan, new FTCRRouter.Listener() {
//                        @Override
//                        public void onCalculateRouteFinished(@NonNull List<FTCRRoute> list, @NonNull FTCRRouter.ErrorResponse errorResponse) {
//                            Log.d(TAG, "errorResponse.getMessage(): " + errorResponse.getMessage());
//                            if (errorResponse.getErrorCode() == RoutingError.NONE && !list.isEmpty()) {
//                                list.forEach(ftcrRoute -> {
//                                    Log.d(TAG, "ftcrRoute.getLength()): " + ftcrRoute.getLength());
//                                    if (ftcrRoute.getLength() > 0) {
//                                        FTCRMapRoute ftcrMapRoute = new FTCRMapRoute(ftcrRoute);
//                                        ftcrMapRoute.setZIndex(100);
//                                        ftcrMapRoute.setColor(Color.argb(255, 243, 174, 255)); //F3AEFF
//                                        DataHolder.getMap().addMapObject(ftcrMapRoute);
//                                        DataHolder.getMap().zoomTo(ftcrRoute.getBoundingBox(), Map.Animation.BOW, 0);
//                                    }
//                                });
//                            }
//                        }
//                    });
                } else {
                    Snackbar.make(DataHolder.getActivity().findViewById(R.id.mapFragmentView), DataHolder.getAndroidXMapFragment().getString(R.string.cannot_initialize_map_with_error) + error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startNavigation(Route route, boolean zoomToRoute) {
        resetMapRoute(route);
        for (MapMarker m : placeSearchResultIcons) {
            DataHolder.getMap().removeMapObject(m);
        }
        DataHolder.getMap().setTilt(0);
        if (zoomToRoute) {
            DataHolder.getMap().zoomTo(mapRouteGeoBoundingBox, Map.Animation.NONE, 0f);
        }
        DataHolder.isDragged = false;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataHolder.getActivity());
        alertDialogBuilder.setTitle(R.string.navigation);
        alertDialogBuilder.setMessage(R.string.choose_mode);
        alertDialogBuilder.setNegativeButton(R.string.navigation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                addNavigationListeners();
                DataHolder.isNavigating = true;
                hideTrafficSigns();
                DataHolder.isSignShowing = false;
                navigationControlButton.setText(R.string.restart);
                intoNavigationMode();
                DataHolder.isRouteOverView = false;
                NavigationManager.Error error = DataHolder.getNavigationManager().startNavigation(theRoute);
                trafficWarner = DataHolder.getNavigationManager().getTrafficWarner();
                trafficWarner.init();
                trafficWarner.addListener(new WeakReference<>(trafficWarnerListener));
                mapSchemeChanger.navigationMapOn();
                navigationControlButton.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);
                Log.e("Error: ", "NavigationManager.Error: " + error);
                startForegroundService();
            }
        });


        alertDialogBuilder.setPositiveButton(R.string.simulation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enterSimulation();
                    }
                }, 1000);

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(DataHolder.getActivity().getResources().getColor(R.color.green));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(DataHolder.getActivity().getResources().getColor(R.color.red));
    }

    private void enterSimulation() {
        getActivity().setVolumeControlStream((getNavigationManager().getAudioPlayer().getStreamId()));
        addNavigationListeners();
        hideTrafficSigns();
        intoNavigationMode();
        navigationControlButton.setText(R.string.restart);
        DataHolder.isNavigating = true;
        DataHolder.isSignShowing = false;
        DataHolder.isRouteOverView = false;
        trafficWarner = DataHolder.getNavigationManager().getTrafficWarner();
        trafficWarner.init();
        trafficWarner.addListener(new WeakReference<>(trafficWarnerListener));
        mapSchemeChanger.navigationMapOn();
        NavigationManager.Error error = DataHolder.getNavigationManager().simulate(theRoute, simulationSpeedMs);
        Log.e("Error: ", "NavigationManager.Error: " + error);
        navigationControlButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
        startForegroundService();
    }

    private void startNavigationManager() {
        DataHolder.setNavigationManager(NavigationManager.getInstance());
        DataHolder.getNavigationManager().setMap(DataHolder.getMap());

        if (UnitLocale.getDefault() == UnitLocale.ImperialUs) {
            DataHolder.getNavigationManager().setDistanceUnit(NavigationManager.UnitSystem.IMPERIAL_US);
        } else if (UnitLocale.getDefault() == UnitLocale.ImperialUs) {
            DataHolder.getNavigationManager().setDistanceUnit(NavigationManager.UnitSystem.IMPERIAL);
        } else {
            DataHolder.getNavigationManager().setDistanceUnit(NavigationManager.UnitSystem.METRIC);
        }

        mapSchemeChanger = new MapSchemeChanger(DataHolder.getMap(), DataHolder.getNavigationManager());
        navigationListeners.setSafetySpotListener(safetySpotListener);
        navigationListeners.setPositionListener(positionListener);
//        navigationListeners.setRoutingZoneListener(routingZoneListener);
        DataHolder.getNavigationManager().addPositionListener(new WeakReference<>(navigationListeners.getPositionListener()));
        DataHolder.getNavigationManager().addSafetySpotListener(new WeakReference<>(navigationListeners.getSafetySpotListener()));
//        DataHolder.getNavigationManager().addRoutingZoneListener(new WeakReference<>(navigationListeners.getRoutingZoneListener()));
    }


    private void resetMap() {
        guidanceSpeedView.setTextColor(Color.argb(255, 0, 0, 0));
        speedLabelTextView.setTextColor(Color.argb(255, 0, 0, 0));
        clearDistanceMarkerMapOverlay();
        trafficSignClusterLayer.removeMarkers(trafficSignClusterLayer.getMarkers());
        CLE2DataManager.getInstance().newPurgeLocalStorageTask().start();
        DataHolder.getMap().setExtrudedBuildingsVisible(true);
        DataHolder.getMap().setLandmarksVisible(true);
        DataHolder.getMap().setMapScheme(Map.Scheme.NORMAL_DAY);
        DataHolder.getMap().setFleetFeaturesVisible(EnumSet.noneOf(Map.FleetFeature.class));
        DataHolder.getMap().setPedestrianFeaturesVisible(EnumSet.noneOf(Map.PedestrianFeature.class));
        DataHolder.getMap().removeClusterLayer(trafficSignClusterLayer);
        trafficSignMapContainer.removeAllMapObjects();
        safetyCameraMapMarker.setTransparency(0);
        safetyCamLinearLayout.setVisibility(View.GONE);
        distanceToSafetyCamera = -1;
        DataHolder.getMap().removeMapObject(endGuidanceDirectionalMapPolyline);
        trafficWarningTextView.setVisibility(View.GONE);
        trafficWarningTextView.setText("");
        distanceMarkerLinearLayout.setVisibility(View.GONE);
        mapSchemeChanger = new MapSchemeChanger(DataHolder.getMap());
        if (trafficWarner != null) {
            trafficWarner.stop();
        }
        mapSchemeChanger.navigationMapOff();

        if (searchResultSnackbar != null) {
            searchResultSnackbar.dismiss();
        }
        isMapRotating = false;
        DataHolder.isNavigating = false;
        if (DataHolder.getNavigationManager() != null) {
            if (DataHolder.getNavigationManager().getRunningState() == NavigationManager.NavigationState.RUNNING) {
                DataHolder.getNavigationManager().stop();
            }
            if (gpsSwitch.isActivated()) {
//                DataHolder.getNavigationManager().startTracking();
            }
        }
        if (DataHolder.getNavigationManager() != null) {
            removeNavigationListeners();
        }
//        DataHolder.getNavigationManager() = null;
        switchGuidanceUiViews(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        downloadOfflineMapButton.setVisibility(View.VISIBLE);
//        gpsStatusImageView.setVisibility(View.VISIBLE);
        gpsSwitch.setVisibility(View.VISIBLE);
        DataHolder.getActivity().findViewById(R.id.junctionImageView).setVisibility(View.INVISIBLE);
        DataHolder.getActivity().findViewById(R.id.signpostImageView).setVisibility(View.INVISIBLE);
//        if (!DataHolder.getActivity().isInMultiWindowMode()) {
//            zoomInButton.setVisibility(View.VISIBLE);
//            zoomOutButton.setVisibility(View.VISIBLE);
//        } else {
//            zoomInButton.setVisibility(View.GONE);
//            zoomOutButton.setVisibility(View.GONE);
//        }
        navigationControlButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
        if (!placeSearchResultIcons.isEmpty()) {
            for (MapMarker m : placeSearchResultIcons) {
                DataHolder.getMap().removeMapObject(m);
            }
        }
        placeSearchResultIcons.clear();
        DataHolder.isRouteOverView = false;
        if (coreRouter != null) {
            if (coreRouter.isBusy()) {
                coreRouter.cancel();
            }
        }
        androidXMapFragment.setOnTouchListener(null);

        navigationControlButton.setText(R.string.create_route);
        theRoute = null;
        switchGuidanceUiPresenters(false);

        if (DataHolder.getActivity().isInMultiWindowMode()) {
            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.7f);
        } else {
            new ShiftMapCenter().setTransformCenter(DataHolder.getMap(), 0.5f, 0.6f);
        }

        DataHolder.getMap().setTilt(0);

        if (mapRoute != null) {
            DataHolder.getMap().removeMapObject(mapRoute);
        }
        if (alternativeMapRoute != null) {
            DataHolder.getMap().removeMapObject(alternativeMapRoute);
        }
        if (!userInputWaypoints.isEmpty()) {
            for (MapMarker mkr : userInputWaypoints) {
                DataHolder.getMap().removeMapObject(mkr);
            }
        }
        if (!wayPointIcons.isEmpty()) {
            for (MapMarker mkr : wayPointIcons) {
                DataHolder.getMap().removeMapObject(mkr);
            }
        }
        wayPointIcons.clear();
        userInputWaypoints.clear();
        waypointList.clear();
        DataHolder.isDragged = false;

//        northUpButton.callOnClick();
        androidXMapFragment.getMapGesture().addOnGestureListener(customOnGestureListener, 0, false);
        switchUiControls(View.GONE);
        compassButton.setVisibility(View.VISIBLE);

        EnumSet<Map.LayerCategory> poiLayers = EnumSet.of(
                Map.LayerCategory.POI_ICON,
                Map.LayerCategory.POI_LABEL,
                Map.LayerCategory.POINT_ADDRESS
        );
        DataHolder.getMap().setVisibleLayers(poiLayers, true);
        if (laneInformationMapOverlay != null) {
            DataHolder.getMap().removeMapOverlay(laneInformationMapOverlay);
        }

    }

    private void removeNavigationListeners() {
        DataHolder.getNavigationManager().removeNavigationManagerEventListener(navigationListeners.getNavigationManagerEventListener());
        DataHolder.getNavigationManager().removeSafetySpotListener(navigationListeners.getSafetySpotListener());
        DataHolder.getNavigationManager().removeRealisticViewListener(navigationListeners.getRealisticViewListener());
        DataHolder.getNavigationManager().removePositionListener(navigationListeners.getPositionListener());
        DataHolder.getNavigationManager().removeLaneInformationListener(navigationListeners.getLaneInformationListener());
        DataHolder.getNavigationManager().removeRerouteListener(navigationListeners.getRerouteListener());
        DataHolder.getNavigationManager().removeTrafficRerouteListener(navigationListeners.getTrafficRerouteListener());
//        DataHolder.getNavigationManager().removeNewInstructionEventListener(navigationListeners.getNewInstructionEventListener());
    }

    private void addNavigationListeners() {
        DataHolder.getActivity().findViewById(R.id.mapFragmentView).getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        navigationListeners.setLaneInformationListener(laneInformationListener);
        navigationListeners.setNavigationManagerEventListener(navigationManagerEventListener);
        navigationListeners.setPositionListener(positionListener);
        navigationListeners.setRealisticViewListener(realisticViewListener);
        navigationListeners.setRerouteListener(rerouteListener);
        navigationListeners.setTrafficRerouteListener(trafficRerouteListener);
        navigationListeners.setSafetySpotListener(safetySpotListener);
        navigationListeners.setManeuverEventListener(maneuverEventListener);
//        navigationListeners.setNewInstructionEventListener(newInstructionEventListener);
        navigationListeners.setAudioFeedbackListener(audioFeedbackListener);

//        DataHolder.getNavigationManager().addNewInstructionEventListener(new WeakReference<>(navigationListeners.getNewInstructionEventListener()));
        DataHolder.getNavigationManager().addNavigationManagerEventListener(new WeakReference<>(navigationListeners.getNavigationManagerEventListener()));
        DataHolder.getNavigationManager().addSafetySpotListener(new WeakReference<>(navigationListeners.getSafetySpotListener()));
        DataHolder.getNavigationManager().setRealisticViewMode(NavigationManager.RealisticViewMode.DAY);
        DataHolder.getNavigationManager().addRealisticViewAspectRatio(NavigationManager.AspectRatio.AR_16x9);
        DataHolder.getNavigationManager().addRealisticViewListener(new WeakReference<>(navigationListeners.getRealisticViewListener()));
        DataHolder.getNavigationManager().addPositionListener(new WeakReference<>(navigationListeners.getPositionListener()));
        DataHolder.getNavigationManager().addLaneInformationListener(new WeakReference<>(navigationListeners.getLaneInformationListener()));
//        if (route.getFirstManeuver().getTransportMode() == RouteOptions.TransportMode.CAR || route.getFirstManeuver().getTransportMode() == RouteOptions.TransportMode.TRUCK) {
//            DataHolder.getNavigationManager().addLaneInformationListener(new WeakReference<>(navigationListeners.getLaneInformationListener()));
//        }
        DataHolder.getNavigationManager().addRerouteListener(new WeakReference<>(navigationListeners.getRerouteListener()));
        DataHolder.getNavigationManager().addTrafficRerouteListener(new WeakReference<>(navigationListeners.getTrafficRerouteListener()));
        DataHolder.getNavigationManager().addManeuverEventListener(new WeakReference<>(navigationListeners.getManeuverEventListener()));
        DataHolder.getNavigationManager().addAudioFeedbackListener(new WeakReference<>(navigationListeners.getAudioFeedbackListener()));
    }

    class SearchResultHandler {
        SearchResultHandler(String inputString, GeoCoordinate center, int radius) {
            GeocodeRequest geocodeRequest = new GeocodeRequest(inputString);
            geocodeRequest.setCollectionSize(1);
            geocodeRequest.setSearchArea(center, radius);
            geocodeRequest.execute(new ResultListener<List<GeocodeResult>>() {
                @Override
                public void onCompleted(List<GeocodeResult> geocodeResults, ErrorCode errorCode) {
                    if (errorCode == ErrorCode.NONE) {
                        searchRequestResultMapContainer.removeAllMapObjects();
                        Log.d(TAG, "GeocodeRequest geocodeResults.size():" + geocodeResults.size());
                        if (geocodeResults.size() > 0) {
                            GeocodeResult geocodeResult = geocodeResults.get(0);
                            Log.d(TAG, "geocodeResult.getLocation().getAddress().getText(): " + geocodeResult.getLocation().getAddress().getText());
                            Log.d(TAG, "geocodeResult.getMatchLevel(): " + geocodeResult.getMatchLevel());
                            if (geocodeResult.getLocation().getCoordinate() != null) {
                                DataHolder.isDragged = true;
                                showSelectionFocus(geocodeResult.getLocation().getCoordinate(), geocodeResult.getLocation().getAddress().getText(), geocodeResult.getMatchLevel());
                                map.setCenter(geocodeResult.getLocation().getCoordinate(), Map.Animation.BOW);
                            }
                        } else {
                            Log.e(TAG, "GeocodeRequest geocodeResults.size():" + geocodeResults.size());
                        }
                    } else {
                        Log.e(TAG, "GeocodeRequest ErrorCode:" + errorCode.name());
                    }

                }
            });
            searchBarLinearLayout.setVisibility(View.GONE);
        }

        String placesSearchResultTitle;
        private final Map map = DataHolder.getMap();

        SearchResultHandler(View view, GeoCoordinate geoCoordinate, String inputString, Map map) {
            searchRequestResultMapContainer.removeAllMapObjects();
            if (selectedFeatureMapMarker != null) {
                map.removeMapObject(selectedFeatureMapMarker);
            }
            SearchRequest request = new SearchRequest(inputString);
            request.setSearchCenter(geoCoordinate);
            request.execute(new ResultListener<DiscoveryResultPage>() {
                @Override
                public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
                    if (errorCode == ErrorCode.NONE) {
                        searchRequestResultMapContainer.removeAllMapObjects();
                        if (discoveryResultPage.getPlaceLinks().size() > 0) {
                            List<GeoCoordinate> geoCoordinateList = new ArrayList<>();
                            List<PlaceLink> discoveryResultPlaceLinks = discoveryResultPage.getPlaceLinks();
                            for (PlaceLink placeLink : discoveryResultPlaceLinks) {
                                Log.d(TAG, placeLink.getTitle() + " " + placeLink.getAverageRating());
                                GeoCoordinate placesSearchResultGeoCoordinate = placeLink.getPosition();
                                geoCoordinateList.add(placesSearchResultGeoCoordinate);
                                placesSearchResultTitle = placeLink.getTitle();
                                if (placesSearchResultGeoCoordinate != null) {
                                    showSelectionFocus(placesSearchResultGeoCoordinate, placesSearchResultTitle, placeLink.getCategory().getName());
                                    searchBarLinearLayout.setVisibility(View.GONE);
                                }
                            }
                            if (geoCoordinateList.size() > 1) {
                                GeoBoundingBox searchResultGeoBoundingBox = GeoBoundingBox.getBoundingBoxContainingGeoCoordinates(geoCoordinateList);
                                GeoBoundingBoxDimensionCalculator geoBoundingBoxDimensionCalculator = new GeoBoundingBoxDimensionCalculator(searchResultGeoBoundingBox);
                                searchResultGeoBoundingBox.expand((float) (geoBoundingBoxDimensionCalculator.getHeightInMeters() * 0.2), (float) (geoBoundingBoxDimensionCalculator.getWidthInMeters() * 0.2));
                                map.zoomTo(searchResultGeoBoundingBox, Map.Animation.BOW, 0);
                            } else {
                                if (discoveryResultPlaceLinks.get(0).getBoundingBox() != null) {
                                    map.zoomTo(discoveryResultPlaceLinks.get(0).getBoundingBox(), Map.Animation.BOW, 0);
                                } else {
                                    map.setCenter(geoCoordinateList.get(0), Map.Animation.BOW, 17, 0, 0);
                                }
                            }
                            DataHolder.isDragged = true;
                        } else {
                            Snackbar.make(view, R.string.no_result_returned, Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(view, R.string.no_result_returned + " : " + errorCode, Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
        }

        SearchResultHandler(View view, com.here.android.mpa.search.Location location, Map map) {
            searchRequestResultMapContainer.removeAllMapObjects();
            if (selectedFeatureMapMarker != null) {
                map.removeMapObject(selectedFeatureMapMarker);
            }
            this.showSelectionFocus(location.getCoordinate(), location.getAddress().getText(), "");
            showResultSnackbar(location.getCoordinate(), location.getAddress().getText(), view, Snackbar.LENGTH_INDEFINITE);
        }

        SearchResultHandler(View view, PointF pointF, Map map) {
//            searchRequestResultMapContainer.removeAllMapObjects();
//            if (selectedFeatureMapMarker != null) {
//                map.removeMapObject(selectedFeatureMapMarker);
//            }
            List<ViewObject> selectedMapObjects = DataHolder.getMap().getSelectedObjectsNearby(pointF);
            if (selectedMapObjects.size() > 0) {
                Log.d(TAG, selectedMapObjects.get(0).getClass().getName());
                switch (selectedMapObjects.get(0).getClass().getName()) {
                    case "com.here.android.mpa.mapping.MapCartoMarker":
                        if (selectedFeatureMapMarker != null) {
                            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
                        }
                        MapCartoMarker selectedMapCartoMarker = (MapCartoMarker) selectedMapObjects.get(0);
                        Location location = selectedMapCartoMarker.getLocation();
                        String placeName = location.getInfo().getField(LocationInfo.Field.PLACE_NAME);
                        String category = location.getInfo().getField(LocationInfo.Field.PLACE_CATEGORY);
                        searchResultString = placeName + " / " + category;
                        showSelectionFocus(location.getCoordinate(), placeName, category);
                        showResultSnackbar(location.getCoordinate(), searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        break;
                    case "com.here.android.mpa.mapping.TransitAccessObject":
                        if (selectedFeatureMapMarker != null) {
                            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
                        }
                        TransitAccessObject selectedTransitAccessObject = (TransitAccessObject) selectedMapObjects.get(0);
                        GeoCoordinate transitAccessObjectGeoCoordinate = selectedTransitAccessObject.getCoordinate();
                        searchResultString = selectedTransitAccessObject.getTransitAccessInfo().getName();
                        showSelectionFocus(transitAccessObjectGeoCoordinate, searchResultString, selectedTransitAccessObject.getTransitAccessInfo().getAttributes().toString());
                        showResultSnackbar(transitAccessObjectGeoCoordinate, searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        break;
                    case "com.here.android.mpa.mapping.TransitStopObject":
                        if (selectedFeatureMapMarker != null) {
                            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
                        }
                        TransitStopObject selectedTransitStopObject = (TransitStopObject) selectedMapObjects.get(0);
                        GeoCoordinate transitStopObjectGeoCoordinate = selectedTransitStopObject.getCoordinate();
                        searchResultString = selectedTransitStopObject.getTransitStopInfo().getOfficialName();
                        showSelectionFocus(transitStopObjectGeoCoordinate, searchResultString, selectedTransitStopObject.getTransitStopInfo().getAttributes().toString());
                        showResultSnackbar(transitStopObjectGeoCoordinate, searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        break;
                    case "com.here.android.mpa.mapping.TrafficEventObject":
                        if (selectedFeatureMapMarker != null) {
                            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
                        }
                        TrafficEventObject trafficEventObject = (TrafficEventObject) selectedMapObjects.get(0);
                        GeoCoordinate trafficEventObjectGeoCoordinate = trafficEventObject.getCoordinate();
                        String trafficEventShortText = trafficEventObject.getTrafficEvent().getEventText();
                        String trafficEventAffectedStreet = trafficEventObject.getTrafficEvent().getFirstAffectedStreet();
                        searchResultString = trafficEventShortText + " / " + trafficEventAffectedStreet;
                        showSelectionFocus(trafficEventObjectGeoCoordinate, trafficEventShortText, trafficEventAffectedStreet);
                        showResultSnackbar(trafficEventObjectGeoCoordinate, searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        break;
                    case "com.here.android.mpa.mapping.SafetySpotObject":
                        if (selectedFeatureMapMarker != null) {
                            DataHolder.getMap().removeMapObject(selectedFeatureMapMarker);
                        }
                        SafetySpotObject safetySpotObject = (SafetySpotObject) selectedMapObjects.get(0);
                        safetyCameraSpeedLimit = safetySpotObject.getSafetySpotInfo().getSpeedLimit1();
                        int safetyCameraSpeedLimitKM;
                        if (safetyCameraSpeedLimit * 3.6 % 10 >= 8 || safetyCameraSpeedLimit * 3.6 % 10 <= 2) {
                            safetyCameraSpeedLimitKM = (int) ((Math.round((safetyCameraSpeedLimit * 3.6) / 10)) * 10);
                        } else {
                            safetyCameraSpeedLimitKM = (int) (Math.round((safetyCameraSpeedLimit * 3.6)));
                        }
                        searchResultString = DataHolder.getAndroidXMapFragment().getString(R.string.safety_camera_ahead) + safetyCameraSpeedLimitKM + " km/h";
                        showSelectionFocus(safetySpotObject.getSafetySpotInfo().getCoordinate(), searchResultString, safetySpotObject.getSafetySpotInfo().getType().name());
                        showResultSnackbar(safetySpotObject.getSafetySpotInfo().getCoordinate(), searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        break;
                    case "com.here.android.mpa.mapping.MapMarker":
                        MapMarker selectedMapMarker = (MapMarker) selectedMapObjects.get(0);
                        selectedMapMarker.setSvgIconScaling(2f);
                        Log.d(TAG, selectedMapMarker.getTitle() + " " + selectedMapMarker.getDescription());
                        if (selectedMapMarker.getTitle() != null && selectedMapMarker.getDescription() != null) {
                            searchResultString = selectedMapMarker.getTitle() + " | " + selectedMapMarker.getDescription();
//                            showSelectionFocus(selectedMapMarker.getCoordinate(), selectedMapMarker.getTitle(), selectedMapMarker.getDescription());
                            showResultSnackbar(selectedMapMarker.getCoordinate(), searchResultString, view, Snackbar.LENGTH_INDEFINITE);
                        }
                        break;
                }
            }
        }

        private void showSelectionFocus(GeoCoordinate geoCoordinate, String title, String description) {
            selectedFeatureMapMarker = new MapMarker();
            selectedFeatureMapMarker.setCoordinate(geoCoordinate);
            Image icon = new Image();
            icon.setBitmap(VectorDrawableConverter.getBitmapFromVectorDrawable(DataHolder.getActivity(), R.drawable.ic_search_result));
            selectedFeatureMapMarker.setIcon(icon);
            selectedFeatureMapMarker.setTitle(title);
            selectedFeatureMapMarker.setDescription(description);
            selectedFeatureMapMarker.setAnchorPoint(DataHolder.getMapMarkerAnchorPoint(selectedFeatureMapMarker));
            searchRequestResultMapContainer.addMapObject(selectedFeatureMapMarker);
        }

        private void showResultSnackbar(GeoCoordinate waypointMapMakerGeoCoordinate, String stringToShow, View view, int duration) {

            searchResultSnackbar = Snackbar.make(view, stringToShow, duration);
            searchResultSnackbar.setAction(R.string.add_waypoint, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchRequestResultMapContainer.removeAllMapObjects();
                    addingWaypointMapMarker(waypointMapMakerGeoCoordinate);
                    map.setCenter(waypointMapMakerGeoCoordinate, Map.Animation.LINEAR);
                    DataHolder.isDragged = true;
                    switchUiControls(View.VISIBLE);
                }
            });
//            searchResultSnackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                @Override
//                public void onDismissed(Snackbar transientBottomBar, int event) {
//                    super.onDismissed(transientBottomBar, event);
//                    if (selectedFeatureMapMarker != null) {
//                        map.removeMapObject(selectedFeatureMapMarker);
//                    }
//                }
//            });
            searchResultSnackbar.show();
        }

        private void addingWaypointMapMarker(GeoCoordinate geoCoordinate) {
            MapMarker mapMarker = new MapMarker(geoCoordinate);
            mapMarker.setDraggable(true);
            userInputWaypoints.add(mapMarker);
            mapMarker.setAnchorPoint(DataHolder.getMapMarkerAnchorPoint(mapMarker));
            DataHolder.getMap().addMapObject(mapMarker);
            carRouteButton.setVisibility(View.VISIBLE);
            truckRouteButton.setVisibility(View.VISIBLE);
            scooterRouteButton.setVisibility(View.VISIBLE);
            bikeRouteButton.setVisibility(View.VISIBLE);
            pedsRouteButton.setVisibility(View.VISIBLE);
            navigationControlButton.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
        }
    }

}
