package com.bahador.recaptcha;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("Registered")
public class ReCaptcha extends AppCompatActivity implements View.OnClickListener {

    Button btnVerify;
    TextView tvVerify;
    String TAG = ReCaptcha.class.getSimpleName();

    //you have to get your own API key from https://www.google.com/recaptcha/admin
    //use reCaptcha Ver2 -> android app and put your own package name in necessary places

    String SITE_KEY = "6LfQ95MUAAAAAFz-RJXelLHFzBwDfW-YZOIy8C3U";
    String SITE_SECRET_KEY = "6LfQ95MUAAAAAIziKFP0fX6B73_4aJWe6fsnnpD9";

    RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnVerify = findViewById(R.id.buttonverify);
        tvVerify = findViewById(R.id.textvf);
        btnVerify.setOnClickListener(this);

        queue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                        if (!response.getTokenResult().isEmpty()) {
                            handleCaptchaResult(response.getTokenResult());
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.d(TAG, "Error message: "
                            + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                } else {
                    Log.d(TAG, "Unknown type of error: " + e.getMessage());
                }
            }
        });

    }

    void handleCaptchaResult(final String responseToken) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        tvVerify.setText("You're not a Robot");
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "Error message: " + ex.getMessage());

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error message: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret", SITE_SECRET_KEY);
                params.put("response", responseToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }
}
