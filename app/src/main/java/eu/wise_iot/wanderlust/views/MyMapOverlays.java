package eu.wise_iot.wanderlust.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.constants.Constants;
import eu.wise_iot.wanderlust.controllers.DatabaseController;
import eu.wise_iot.wanderlust.controllers.DatabaseEvent;
import eu.wise_iot.wanderlust.controllers.DatabaseListener;
import eu.wise_iot.wanderlust.controllers.PoiController;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseObject.PoiDao;
import eu.wise_iot.wanderlust.views.dialog.PoiViewDialog;

/**
 * MyMapFragment:
 *
 * @author Fabian Schwander
 * @license MIT
 */
public class MyMapOverlays implements Serializable, DatabaseListener {
    private static final String TAG = "MyMapOverlays";
    private Activity activity;
    private MapView mapView;
    private Polyline currentTour;

    private MyLocationNewOverlay myLocationNewOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> poiHashtagOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> poiOverlay;
    private Marker positionMarker;
    private Marker focusedPositionMarker;
    private ArrayList<Polyline> lines;


    public MyMapOverlays(Activity activity, MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;
        this.currentTour = null;
        initPoiOverlay();
        //populatePoiOverlay();
        mapView.getOverlays().add(poiOverlay);
        mapView.getOverlays().add(poiHashtagOverlay);

        initScaleBarOverlay();
        initMyLocationNewOverlay();
//        initGpxTourlistOverlay();
    }

    /**
     * initialize scalebar and its position
     */
    private void initScaleBarOverlay() {
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        //set position of scale bar
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 3 * 1, dm.heightPixels / 10 * 9);
        mapView.getOverlays().add(scaleBarOverlay);
    }

    public void setTour(Polyline polyline) {
        if (this.currentTour == null) {
            this.currentTour = polyline;
            this.currentTour.setWidth(10);
            this.currentTour.setColor(Color.RED);

            mapView.getOverlays().add(this.currentTour);
        } else {
            this.currentTour = polyline;
        }
        mapView.invalidate();
    }

    private void initMyLocationNewOverlay() {
        // create location provider and add network provider to the already included gps provider
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(activity);
        locationProvider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        Log.i(TAG, "Location sources: " + locationProvider.getLocationSources());

        myLocationNewOverlay = new MyLocationNewOverlay(locationProvider, mapView);
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    private void initPoiOverlay() {
        // add items with on click listener plus define actions for clicks
        List<OverlayItem> poiList = new ArrayList<>();
        List<OverlayItem> poiHashtagList = new ArrayList<>();


        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> listener = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem poiOverlayItem) {
                long poiId = Long.valueOf(poiOverlayItem.getUid());
                PoiController controller = new PoiController();
                controller.getPoiById(poiId, event -> {
                    switch (event.getType()) {
                        case OK:
                            Poi poi = (Poi) event.getModel();


                            FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                            // make sure that no other dialog is running
                            Fragment prevFragment = activity.getFragmentManager().findFragmentByTag(Constants.DISPLAY_FEEDBACK_DIALOG);
                            if (prevFragment != null)
                                fragmentTransaction.remove(prevFragment);
                            fragmentTransaction.addToBackStack(null);

                            PoiViewDialog dialogFragment = PoiViewDialog.newInstance(poi);
                            dialogFragment.show(fragmentTransaction, Constants.DISPLAY_FEEDBACK_DIALOG);
                            break;
                        default:
                            //TODO some kind of toast?
                    }
                });
                return true;
            }

            @Override
            public boolean onItemLongPress(final int index, final OverlayItem overlayItem) {
                // TODO: maybe add action when item is pressed long?
                return false;
            }
        };

        poiOverlay = new ItemizedOverlayWithFocus<>(activity, poiList,listener);
        poiHashtagOverlay = new ItemizedOverlayWithFocus<>(activity, poiHashtagList,listener);
    }

//    private void initGpxTourlistOverlay() { // FIXME: overlay not working yet -> enable drawing routes!
//        GpxParser gpxParser = new GpxParser(activity);
//        List<TrackPoint> gpxList = gpxParser.getTrackPointList(R.raw.gpx1);
//        ArrayList<GeoPoint> geoPointList = new ArrayList<>();
//
//        for (TrackPoint model : gpxList) {
//            GeoPoint newPoint = new GeoPoint(model.getLatitude(), model.getLongitude());
//            geoPointList.add(newPoint);
//        }
//
//        RoadManager roadManager = new OSRMRoadManager(activity);
//        Road road = roadManager.getRoad(geoPointList);
//        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
//        mapView.getOverlays().add(roadOverlay);
//        mapView.invalidate();
//    }


    /**
     * Updates the Hashtag Poi layer
     *
     * @param poiList the list with poi to be added in the layer
     */
    public void updateHashtagPoiLayer(List<Poi> poiList) {

        poiHashtagOverlay.removeAllItems();
        for (Poi poi : poiList) {
            addPoiToHashtagOverlay(poi);
        }
        mapView.invalidate();
        showPoiHashtagLayer(true);

    }

    /**
     * Creates an OverlayItem from a poi with item considering the poi type
     */
    private OverlayItem poiToOverlayItem(Poi poi) {
        Drawable drawable;
        boolean hasImage = poi.getImageCount() > 0;
        switch ((int) poi.getType()) {
            case Constants.TYPE_VIEW:
                if (hasImage)
                    drawable = activity.getResources().getDrawable(R.drawable.poi_sight);
                else
                    drawable = activity.getResources().getDrawable(R.drawable.poi_sight_no_img);
                break;
            case Constants.TYPE_RESTAURANT:
                if (hasImage)
                    drawable = activity.getResources().getDrawable(R.drawable.poi_resaurant);
                else
                    drawable = activity.getResources().getDrawable(R.drawable.poi_resaurant_no_img);
                break;
            case Constants.TYPE_REST_AREA:
                if (hasImage)
                    drawable = activity.getResources().getDrawable(R.drawable.poi_resting);
                else
                    drawable = activity.getResources().getDrawable(R.drawable.poi_resting_no_img);
                break;
            case Constants.TYPE_FLORA_FAUNA:
                if (hasImage)
                    drawable = activity.getResources().getDrawable(R.drawable.poi_fauna_flora);
                else
                    drawable = activity.getResources().getDrawable(R.drawable.poi_fauna_flora_no_img);
                break;
            default:
                drawable = activity.getResources().getDrawable(R.drawable.poi_error);
        }

        OverlayItem overlayItem = new OverlayItem(Long.toString(poi.getPoi_id()), poi.getTitle(),
                poi.getDescription(), new GeoPoint(poi.getLatitude(), poi.getLongitude()));

        overlayItem.setMarker(drawable);
        return overlayItem;
    }

    /**
     * Adds a poi on the mapview regular MapOverlay
     */
    public void addPoiToOverlay(Poi poi) {
        OverlayItem overlayItem = poiToOverlayItem(poi);
        poiOverlay.addItem(overlayItem);
    }

    /**
     * Adds a poi to the mapview to the hashtagPoiOverlay
     */
    public void addPoiToHashtagOverlay(Poi poi) {
        OverlayItem overlayItem = poiToOverlayItem(poi);
        poiHashtagOverlay.addItem(overlayItem);
    }


    public void addPositionMarker(GeoPoint geoPoint) {
        if (geoPoint != null) {
            Drawable drawable = activity.getResources().getDrawable(R.drawable.ic_location_on_highlighted_40dp);

            positionMarker = new Marker(mapView);
            positionMarker.setIcon(drawable);
            positionMarker.setPosition(geoPoint);
            positionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            positionMarker.setTitle(activity.getString(R.string.msg_last_known_position_marker));

            mapView.getOverlays().add(positionMarker);
            mapView.invalidate();
        }
    }


    /**
     * Take all pois from the database and add
     * them to the map overlay
     */
    public void populatePoiOverlay() {

        poiOverlay.removeAllItems();
        List<Poi> pois = PoiDao.getInstance().find();
        for (Poi poi : pois) {
            addPoiToOverlay(poi);
        }
        mapView.invalidate();
    }

    public void removePositionMarker() {
        mapView.getOverlays().remove(positionMarker);
    }

    void showPoiLayer(boolean setVisible) {
        showPoiHashtagLayer(false);
        if (setVisible) {
            if (!mapView.getOverlays().contains(poiOverlay)) {
                mapView.getOverlays().add(poiOverlay);
            }
        } else {
            mapView.getOverlays().remove(poiOverlay);
        }
        mapView.invalidate();
    }

    void showPoiHashtagLayer(boolean setVisible) {
        if (setVisible) {
            if (!mapView.getOverlays().contains(poiHashtagOverlay)) {
                mapView.getOverlays().add(poiHashtagOverlay);
            }
        } else {
            mapView.getOverlays().remove(poiHashtagOverlay);
        }
        mapView.invalidate();
    }

    @Override
    public void update(DatabaseEvent event) {

        if (event.getType() == DatabaseEvent.SyncType.POIAREA) {
            populatePoiOverlay();
        } else if (event.getType() == DatabaseEvent.SyncType.SINGLEPOI) {
            //More efficient, Stamm approves
            Poi poi = (Poi) event.getObj();
            addPoiToOverlay(poi);
            mapView.invalidate();
        } else if (event.getType() == DatabaseEvent.SyncType.DELETESINGLEPOI) {
            Poi poi = (Poi) event.getObj();
            for (int i = 0; i < poiOverlay.size(); i++) {
                if (Long.parseLong(poiOverlay.getItem(i).getUid()) == poi.getPoi_id()) {
                    poiOverlay.removeItem(i);
                    break;
                }
            }
            mapView.invalidate();
        } else if (event.getType() == DatabaseEvent.SyncType.EDITSINGLEPOI) {
            Poi poi = (Poi) event.getObj();
            for (int i = 0; i < poiOverlay.size(); i++) {
                if (Long.parseLong(poiOverlay.getItem(i).getUid()) == poi.getPoi_id()) {
                    poiOverlay.removeItem(i);
                    addPoiToOverlay(poi);
                    break;
                }
            }
            mapView.invalidate();
        }
    }

    public MyLocationNewOverlay getMyLocationNewOverlay() {
        return myLocationNewOverlay;
    }

    public void addFocusedPositionMarker(GeoPoint geoPoint) {
        if (focusedPositionMarker != null) {
            removeFocusedPositionMarker();
        }
        if (lines != null) {
            clearPolylines();
        }

        if (geoPoint != null) {
            Drawable drawable = activity.getResources().getDrawable(R.drawable.ic_location_on_highlighted_40dp);

            focusedPositionMarker = new Marker(mapView);
            focusedPositionMarker.setIcon(drawable);
            focusedPositionMarker.setPosition(geoPoint);
            focusedPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            focusedPositionMarker.setTitle(activity.getString(R.string.msg_last_known_position_marker));

            mapView.getOverlays().add(focusedPositionMarker);
            mapView.invalidate();
        }
    }

    public void removeFocusedPositionMarker() {
        mapView.getOverlays().remove(focusedPositionMarker);
    }

    public void addPolyline(ArrayList<GeoPoint> geoPoints) {
        if (focusedPositionMarker != null) {
            removeFocusedPositionMarker();
        }
        if (lines == null) {
            lines = new ArrayList<>();
        }

        Polyline polyline = new Polyline();

        polyline.setPoints(geoPoints);
        polyline.setColor(activity.getResources().getColor(R.color.highlight_main_transparent75));

        lines.add(polyline);

        mapView.getOverlays().add(polyline);
        mapView.invalidate();
    }

    public void clearPolylines() {
        if (lines != null) {
            mapView.getOverlays().removeAll(lines);
            lines = null;
        }
    }

}