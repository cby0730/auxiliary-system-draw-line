package com.fancynavi.android.app;

import android.util.Log;

import androidx.annotation.Nullable;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.common.TrafficSign;
import com.here.android.mpa.electronic_horizon.DataNotReadyException;
import com.here.android.mpa.electronic_horizon.ElectronicHorizon;
import com.here.android.mpa.electronic_horizon.Link;
import com.here.android.mpa.electronic_horizon.LinkInformation;
import com.here.android.mpa.electronic_horizon.LinkRange;
import com.here.android.mpa.electronic_horizon.MapAccessor;
import com.here.android.mpa.electronic_horizon.PathTree;
import com.here.android.mpa.electronic_horizon.Position;
import com.here.android.mpa.routing.Route;

import java.util.List;

import static com.fancynavi.android.app.DataHolder.TAG;

class ElectronicHorizonActivation {

    ElectronicHorizon electronicHorizon;
    MapAccessor mapAccessor;

    public ElectronicHorizonActivation() {
        electronicHorizon = new ElectronicHorizon();
        electronicHorizon.setListener(new ElectronicHorizon.Listener() {
            @Override
            public void onNewPosition(Position position) {
                LinkRange linkRange = position.getPathTree().getLinks();
                Log.d(TAG, "linkRange.getSize() : " + linkRange.getSize());
                for (Link link : linkRange) {
                    getTrafficSigns(link);

                }
            }

            @Override
            public void onTreeReset() {
            }

            @Override
            public void onLinkAdded(PathTree path, Link link) {
            }

            @Override
            public void onLinkRemoved(PathTree path, Link link) {
            }

            @Override
            public void onPathAdded(PathTree path) {
            }

            @Override
            public void onPathRemoved(PathTree path) {
            }

            @Override
            public void onChildDetached(PathTree parent, PathTree child) {
            }
        });
    }

    public void startElectronicHorizonUpdate() {
        electronicHorizon.update();
    }

    public void setRoute(Route route) {
        electronicHorizon.setRoute(route);
    }

    public void ElectronicHorizonUpdate() {
        electronicHorizon.update();
    }

    @Nullable
    private GeoPolyline getTrafficSigns(Link link) {
        GeoPolyline geoPolyline;
        mapAccessor = electronicHorizon.getMapAccessor();
        try {
            geoPolyline = mapAccessor.getLinkPolyline(link);
            Link.Direction linkDirection = link.getDirection();
//            LinkInformation linkInformation = mapAccessor.getLinkInformation(link);
            List<GeoCoordinate> pointList = geoPolyline.getAllPoints();
            for (GeoCoordinate point : pointList) {
                RoadElement roadElement = RoadElement.getRoadElement(point, "CHT");
                List<TrafficSign> trafficSignList = roadElement.getTrafficSigns();
                for (TrafficSign trafficSign : trafficSignList) {
                    int trafficSignType = trafficSign.type;
                    TrafficSign.Direction trafficSignDirection = trafficSign.direction;
                    Log.d(TAG, "linkDirection: " + linkDirection);
                    Log.d(TAG, "trafficSignType: " + trafficSignType);
                    Log.d(TAG, "trafficSignDirection: " + trafficSignDirection);
                }
            }
        } catch (DataNotReadyException dataNotReadyException) {
            // should never happen when map data was already loaded
            return null;
        } catch (com.here.android.mpa.common.DataNotReadyException e) {
            return null;
        }
        return geoPolyline;
    }

    private void findWarningSigns(PathTree pathTree) {
        MapAccessor mapAccessor = electronicHorizon.getMapAccessor();

        for (Link link : pathTree.getLinks()) {
            LinkInformation linkInformation;
            try {
                linkInformation = mapAccessor.getLinkInformation(link);
            } catch (DataNotReadyException dataNotReadyException) {
                return;
            }

        }
    }
}
