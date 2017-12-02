package com.cowbell.cordova.geofence;

/*
 * Apache 2.0 License
 *
 * Copyright (c) Sebastian Katzer 2017
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * The receiver activity is triggered when a notification is clicked by a user.
 * The activity calls the background callback and brings the launch intent
 * up to foreground.
 */
public class ClickReceiver extends Activity {

    /**
     * Called when local notification was clicked to launch the main intent.
     *
     * @param state Saved instance state
     */
    @Override
    public void onCreate (Bundle state) {
        super.onCreate(state);

        Intent intent      = getIntent();
        Bundle bundle      = intent.getExtras();
        Context context    = getApplicationContext();

        if (bundle == null)
            return;


        String pkgName  = context.getPackageName();
        Intent intentLaunch = context
                .getPackageManager()
                .getLaunchIntentForPackage(pkgName);

        if (intentLaunch == null)
            return;

        intentLaunch.putExtra("geofence.notification.data", intent.getStringExtra("geofence.notification.data"));
        intentLaunch.addFlags(
                FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(intentLaunch);

        //TODO:
        GeofencePlugin.sendJavascriptFromClick(intent.getStringExtra("geofence.notification.data"));
    }

    /**
     * Fixes "Unable to resume activity" error.
     * Theme_NoDisplay: Activities finish themselves before being resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

}
