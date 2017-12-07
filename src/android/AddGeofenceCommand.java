package com.cowbell.cordova.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGeofenceCommand extends AbstractGoogleServiceCommand {
    private List<Geofence> geofencesToAdd;
    private PendingIntent pendingIntent;

    public AddGeofenceCommand(Context context, PendingIntent pendingIntent,
                              List<Geofence> geofencesToAdd) {
        super(context);
        this.geofencesToAdd = geofencesToAdd;
        this.pendingIntent = pendingIntent;
    }

    GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //triggers events only when the user stops for a defined duration within a geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(this.geofencesToAdd);
        return builder.build();
    }

    @Override
    public void ExecuteCustomCode() {
        logger.log(Log.DEBUG, "Adding new geofences...");
        if (geofencesToAdd != null && geofencesToAdd.size() > 0) try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.GeofencingApi
                    .addGeofences(mGoogleApiClient, getGeofencingRequest(), pendingIntent)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                logger.log(Log.DEBUG, "Geofences successfully added");
                                CommandExecuted();
                            } else try {
                                Map<Integer, String> errorCodeMap = new HashMap<Integer, String>();
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE, GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
                                errorCodeMap.put(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES, GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);

                                Integer statusCode = status.getStatusCode();
                                String message = "Adding geofences failed - SystemCode: " + statusCode;
                                JSONObject error = new JSONObject();
                                error.put("message", message);

                                if (statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_NOT_AVAILABLE);
                                } else if (statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES) {
                                    error.put("code", GeofencePlugin.ERROR_GEOFENCE_LIMIT_EXCEEDED);
                                } else {
                                    error.put("code", GeofencePlugin.ERROR_UNKNOWN);
                                }

                                logger.log(Log.ERROR, message);
                                CommandExecuted(error);
                            } catch (JSONException exception) {
                                CommandExecuted(exception);
                            }
                        }
                    });
        } catch (Exception exception) {
            logger.log(LOG.ERROR, "Exception while adding geofences");
            exception.printStackTrace();
            CommandExecuted(exception);
        }
    }
}
