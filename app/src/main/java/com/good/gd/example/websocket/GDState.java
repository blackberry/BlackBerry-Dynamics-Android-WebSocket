/**
 * Copyright (c) 2021 BlackBerry Limited. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.good.gd.example.websocket;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.error.GDNotAuthorizedError;
import com.good.gd.utility.GDAuthTokenCallback;
import com.good.gd.utility.GDUtility;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles GD events. An instance of this class is passed to authorize(), which
 * will retain it as the callback for incoming GDAppEvents. We use our internal
 * Notification system to transmit any events onward to interested objects.
 */
public class GDState implements GDAppEventListener,
        GDAuthTokenCallback {

    // notification constants
    public static final String GD_CONFIG_CHANGED            = "gd_config_changed";
    public static final String GD_AUTHORIZED                = "gd_authorized";
    public static final String GD_NOT_AUTHORIZED_TEMPORARY  = "gd_not_authorized_temporary";
    public static final String GD_NOT_AUTHORIZED_PERMANENT  = "gd_not_authorized_permanent";
    public static final String GD_AUTH_TOKEN_SUCCESS        = "gd_auth_token_success";
    public static final String GD_AUTH_TOKEN_FAILURE        = "gd_auth_token_failure";

	// Push type enum
	public enum PushType {

		DEFAULT("2"), SENDER_ONLY("3"), SENDER_MESSAGE("4");

		public final String value;
		PushType(String value) {
			this.value = value;
		}

		public static PushType pushTypeFor(String value) {
			return SENDER_MESSAGE.value.equals(value)	? SENDER_MESSAGE :
					SENDER_ONLY.value.equals(value)		? SENDER_ONLY :
														DEFAULT;
		}

        @Override
        public String toString() {
            return SENDER_MESSAGE.value.equals(value)	? "Sender and Message" :
                    SENDER_ONLY.value.equals(value)		? "Sender Only" :
                            "No Details";
        }
    }

    public enum AppPolicyStatusType {
        APP_POLICY_INVALID,
        APP_POLICY_EMPTY,
        APP_POLICY_VALID
    }

    // singleton
    private static GDState _instance = null;
    private static final String TAG = "GDState";
    private boolean isAuthorized = false;         // set when we receive GDAppEventAuthorized
    private boolean okayToUseGDAPI = false;       // set when we receive GDAppEventAuthorized
    private boolean receivedAuthResponse = false; // set when we get the first auth response
    private boolean firstTimeAuthorized = false;  // set when we authorized first time
    private String authResult = null;
    private String gdAuthToken = null;
    private boolean gdAuthTokenRequested = false;

    // Singleton
    private GDState() {
        super();
    }

    public static synchronized GDState sharedInstance() {
        if (_instance == null) {
            _instance = new GDState();
        }
        return _instance;
    }

    public void getNewAuthToken() {

       gdAuthToken = null;
        try {
            (new GDUtility()).getGDAuthToken("CONNECT_AUTH", "somedomain.com", this);
        } catch (GDNotAuthorizedError gdnae) {
            Log.e(TAG,"getNewAuthToken: exception from getGDAuthToken()" );
        }
        gdAuthTokenRequested = true;
    }

    // GDAppEventListener methods

    /**
     * onGDEvent - handles events from the GD library including authorization
     * and withdrawal of authorization.
     */
    @Override
    public void onGDEvent(GDAppEvent appEvent) {
        Log.i(TAG, "onGDEvent(" + appEvent + ")");

        switch (appEvent.getEventType()) {
            case GDAppEventAuthorized:
                onAuthorized(appEvent);
                break;

            case GDAppEventNotAuthorized:
                onNotAuthorized(appEvent);
                break;

            case GDAppEventRemoteSettingsUpdate:
                break;

            case GDAppEventPolicyUpdate:
            	updateAppPolicy();
            	break;

            case GDAppEventServicesUpdate:
            	break;

            case GDAppEventEntitlementsUpdate:
                break;
        }
    }

    private void onAuthorized(GDAppEvent appEvent) {

        isAuthorized = true;
        okayToUseGDAPI = true;

        if (gdAuthToken == null && !gdAuthTokenRequested) {
            getNewAuthToken();
        }
    }

    private void onNotAuthorized(GDAppEvent appEvent) {
        receivedAuthResponse = true;
        isAuthorized = false;
        switch (appEvent.getResultCode()) {

            // startup errors - will only happen during the initial startup phase
            case GDErrorActivationFailed:
            case GDErrorProvisioningFailed:
            // removed in gd sdk rev950
            // case GDErrorProvisioningCancelled:
            case GDErrorPushConnectionTimeout:
                Log.i(TAG, "Ignoring startup error " + appEvent.getResultCode());
                okayToUseGDAPI = false;
                authResult = "Error: " + appEvent.getResultCode();
                break;

            // permanent errors, can't be reversed
            case GDErrorSecurityError:              // internal error, container can't be unlocked
            case GDErrorAppDenied:                  // app not allowed; container is wiped
            case GDErrorWiped:                      // policy violation; container is wiped
                Log.i(TAG, "BlackBerry Connect will shut down");
                okayToUseGDAPI = false;
                authResult = "Error: " + appEvent.getResultCode();
                break;

            // temporary errors, can be reversed
            case GDErrorRemoteLockout:              // locked remotely by the server
            case GDErrorPasswordChangeRequired:     // password change forced
            case GDErrorBlocked:                    // app blocked, can be reversed
            case GDErrorIdleLockout:                // idle timeout triggered
                okayToUseGDAPI = true;
                authResult = "Authorization failure: " + appEvent.getResultCode();
                break;

            default:
                // unknown error
                Log.i(TAG, "Ignoring unknown error " + appEvent.getResultCode());
                okayToUseGDAPI = true;
                authResult = "";
        }
    }

    private void updateAppPolicy() {
    	if (isAuthorized()) {
    		Map<String, Object> policy = GDAndroid.getInstance().getApplicationPolicy();
	    	Log.i(TAG, "updateAppPolicy: policy=" + policy);


    	}
    }

    // GDAuthTokenCallback implementation

    @Override
    public void onGDAuthTokenSuccess(final String token) {
        gdAuthToken = token;
        gdAuthTokenRequested = false;
        Log.i(TAG, "onGDAuthTokenSuccess: token = " + token);
    }

    @Override
    public void onGDAuthTokenFailure(final int errCode, final String errMsg) {
        gdAuthTokenRequested = false;
        Log.i(TAG, "onGDAuthTokenFailure: errCode=" + errCode + " [\"" + errMsg + "\"]");
    }

    public String getApplicationPolicyForKey(String key) {
        Map<String, Object> policy = GDAndroid.getInstance().getApplicationPolicy();
        return (String) policy.get(key);
    }
    // true if we are authorized, i.e. last auth event we got was GDAppEventAuthorized
    public boolean isAuthorized() {
        return isAuthorized;
    }

    public String getAuthResult() {
        return authResult;
    }

    public String getGdAuthToken() {
        return gdAuthToken;
    }

    public String getContainerId(String defaultValue) {
        if (!TextUtils.isEmpty(gdAuthToken)) {
            String gdAuthTokenDecode =
                    new String(Base64.decode(gdAuthToken.getBytes(UTF_8), Base64.DEFAULT), UTF_8);
            return gdAuthTokenDecode.split("\\|")[2].replace("-", "");
        }
        Log.w(TAG, String.format("Unable to acquire valid auth token, returning default value %s", defaultValue));
        return defaultValue;
    }

    // true if we can use the GD API; basically isAuthorized or the last auth event we
    // got was a GDAppEventNotAuthorized with a GDErrorIdleLockout or a GDErrorBlocked.
    public boolean okayToUseGDAPI() {
        return okayToUseGDAPI;
    }
}
