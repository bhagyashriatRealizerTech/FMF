package realizer.com.makemepopular.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by shree on 2/10/2017.
 */
public class GpsListener {

    public static GpsListener refrence = null ;
    public LocationManager locationManager = null;
    public LocationListener locationListener = null;
    public Location location = null;

    public static GpsListener getInstance(){
        if(refrence == null){
            refrence = new GpsListener();
        }
        return refrence;
    }

    public void startGpsCallBack(Context activityContext){
        locationManager = (LocationManager) activityContext.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new mylocationlistener();
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        location = locationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            Singleton.setCurrentLatitude(location.getLatitude());
            Singleton.setCurrentLongitude(location.getLongitude());
        }
    }

    public class mylocationlistener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Singleton.setCurrentLatitude(location.getLatitude());
                Singleton.setCurrentLongitude(location.getLongitude());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public void stopGpsCallBack(){
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public void startGpsCallbackAgain(Context activityContext){
        locationManager = (LocationManager) activityContext.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new mylocationlistener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            Singleton.setCurrentLatitude(location.getLatitude());
            Singleton.setCurrentLongitude(location.getLongitude());
        }
    }

}
