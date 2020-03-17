package com.example.outbrake.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.outbrake.MainActivity;
import com.example.outbrake.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    private static final int RC_SIGN_IN = 1;
    private ArrayList<News> list;
    private ScrollView scrollView;
    private TextView info;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseReference;
    private String[] news;
    private NewsApiClient newsApiClient;
    private Context context;
    private static final String API_KEY = "1ca2d2aff9cd4a75850a2c166c3caa9d";
    private static final String url = "https://pyrostore.nushhwboard.ml/api/covid-19";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        context = getContext();

        scrollView = view.findViewById(R.id.news_sv);
        info = view.findViewById(R.id.news_dorscon);
        generateLocalInfo(url);
        list = new ArrayList<>();
        return view;
    }

    private void generateNews(){
        newsApiClient = new NewsApiClient(API_KEY);
        newsApiClient.getEverything(new EverythingRequest.Builder()
                .q("coronavirus").build(), new NewsApiClient.ArticlesResponseCallback() {
            @Override
            public void onSuccess(ArticleResponse articleResponse) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    private void generateLocalInfo(String url){
        if(!MainActivity.isConnectedToInternet(context))
            MainActivity.showNoConnectionSnackBar();
        else{
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String dorscon = response.getString("dorscon");
                        String hospitalised = response.getJSONObject("caseData").getString("Hospitalised");
                        String stable = response.getJSONObject("caseData").getString("Hospitalised (Stable)");
                        String critical = response.getJSONObject("caseData").getString("Hospitalised (Critical)");
                        String death = response.getJSONObject("caseData").getString("Death");
                        String discharged = response.getJSONObject("caseData").getString("Discharged");
                        String total_cases = response.getJSONObject("caseData").getString("Total Confirmed Cases");
                        String last_updated = response.getString("lastUpdated");
                        info.setText("Dorscon level: " + dorscon + "\n" + "Total cases: " + total_cases + "\n"
                                + "Stable: " + stable + "\n" + "Critical: " + critical + "\n" + "Discharged: " + discharged + "\n"
                                + "Dead: " + death + "\n" + "Last updated as of:\n" + last_updated + "\n___\n");
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) { error.printStackTrace(); }
            });
            Volley.newRequestQueue(context).add(jsonObjectRequest);
        }
    }

}

class News {
    public String title;

    public News(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
