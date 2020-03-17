package com.example.outbrake.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.outbrake.MainActivity;
import com.example.outbrake.Popup;
import com.example.outbrake.R;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapView mapView;
    private com.google.android.material.button.MaterialButton button;
    private Context context;
    private ArrayList<Cluster> clusterArrayList = new ArrayList<>();
    private RadioGroup radioGroup;
    private AlertDialog alertDialog;
    private DatabaseReference databaseReference;
    private LinearLayout linearLayout;
    private static final String url = "https://www.flugowhere.gov.sg/data.json";
    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Cluster cluster = dataSnapshot.getValue(Cluster.class);
            clusterArrayList.add(cluster);
            if(googleMap!=null) {
                googleMap.clear();
                generateClusters();
            }
        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            generateClusters();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            for(Cluster cluster1: clusterArrayList){
                if(dataSnapshot.getKey().equals(cluster1.getId())){
                    clusterArrayList.remove(cluster1);break;
                }
            }
            generateClusters();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    private GoogleMap.OnInfoWindowClickListener onClinicInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            Clinic clinic = (Clinic) marker.getTag();
            Intent intent = new Intent(getActivity(), Popup.class);
            intent.putExtra("name",clinic.getTitle());
            intent.putExtra("address",clinic.getAddress());
            intent.putExtra("tele",clinic.getNumber());
            intent.putExtra("type",clinic.getType());
            startActivity(intent);
        }
    };

    private GoogleMap.OnInfoWindowClickListener onClusterInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if(MainActivity.getFirebaseUser().getDisplayName().equals("Kieran Mendoza")){
                Cluster cluster = (Cluster) marker.getTag();
                generateAddClusterDialog(true, cluster);}
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            generateAddClusterDialog(false,new Cluster());
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps,container,false);
        context = getContext();
        radioGroup = view.findViewById(R.id.maps_rg);
        button = view.findViewById(R.id.maps_add_cluster);
        button.setVisibility(View.GONE);
        button.setOnClickListener(onClickListener);
        linearLayout = view.findViewById(R.id.maps_ll);
        databaseReference = MainActivity.getFirebaseDatabase().getReference().child("hotspot");
        databaseReference.addChildEventListener(childEventListener);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);
        if(mapView!=null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        MapsInitializer.initialize(context);
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(1.357437,103.819313)).zoom(11).bearing(0).tilt(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.maps_rb_cluster:
                        generateClusters();
                        if(MainActivity.getFirebaseUser().getDisplayName().equals("Kieran Mendoza"))
                            button.setVisibility(View.VISIBLE);
                            break;
                    case R.id.maps_rb_clinics:
                        button.setVisibility(View.GONE);
                        generateClinics(url);
                        break;
                }
            }
        });
    }

    private void generateClinics(String url) {
        googleMap.clear();
        if(!MainActivity.isConnectedToInternet(context)){
            MainActivity.showNoConnectionSnackBar();}
        else {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("long")))
                                    .title(jsonObject.getString("clinicName"))
                                    .snippet("Click me for more information")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            marker.setTag(new Clinic(jsonObject.getString("clinicName"), jsonObject.getString("address"), jsonObject.getString("type"), jsonObject.getString("clinicTelephoneNo")));
                        } catch (JSONException e) { e.printStackTrace();}
                    }
                    googleMap.setOnInfoWindowClickListener(onClinicInfoWindowClickListener);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            Volley.newRequestQueue(context).add(jsonArrayRequest);
        }
    }

    private void generateClusters(){
        googleMap.clear();
        googleMap.setOnInfoWindowClickListener(null);
        if(!MainActivity.isConnectedToInternet(context)){
            MainActivity.showNoConnectionSnackBar();}
        else{
            for (Cluster cluster: clusterArrayList){
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(cluster.getLatitude(),cluster.getLongitude()))
                        .title(cluster.getTitle())
                        .snippet(cluster.getNumCases()+" cases")
                        .icon(BitmapDescriptorFactory.defaultMarker()));
                marker.setTag(cluster);
            }
            googleMap.setOnInfoWindowClickListener(onClusterInfoWindowClickListener);
        }
    }

    private boolean isValid(String cases, String lat, String longi, String name){
        if(lat.matches("^\\d*\\.?\\d*$")&&longi.matches("^\\d*\\.?\\d*$")&&
                cases.matches("[0-9]+")&&!cases.equals("")&&!lat.equals("")&&!longi.equals("")&&!name.equals("")){
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(longi);
            if(latitude<90&&latitude>-90&&longitude<180&&longitude>-90)
                return true;
        }
        return false;
    }

    private boolean isValid(String cases, String name){
        if(cases.matches("[0-9]+")&&!cases.equals("")&&!name.equals(""))
            return true;
        return false;
    }

    private void generateAddClusterDialog(final boolean showDelete, final Cluster cluster){
        alertDialog = new AlertDialog.Builder(context).create();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.cluster_add,null);
        TextView textView = view.findViewById(R.id.cluster_title);
        final EditText name = view.findViewById(R.id.cluster_name);
        final EditText cases = view.findViewById(R.id.cluster_cases);
        final EditText lat = view.findViewById(R.id.cluster_lat);
        final EditText longi = view.findViewById(R.id.cluster_long);
        Button button = view.findViewById(R.id.cluster_ok);
        Button delete = view.findViewById(R.id.cluster_delete);
        if(!showDelete)
            delete.setVisibility(View.GONE);
        else {
            textView.setText("Edit Cluster Details");
            delete.setVisibility(View.VISIBLE);
            lat.setVisibility(View.GONE);
            longi.setVisibility(View.GONE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MainActivity.isConnectedToInternet(context))
                    MainActivity.showNoConnectionSnackBar();
                if(!showDelete){
                    if (isValid(cases.getText().toString(),lat.getText().toString(),longi.getText().toString(),name.getText().toString())) {
                        String cluster_name = name.getText().toString();
                        int cluster_cases = Integer.parseInt(cases.getText().toString());
                        double cluster_lat = Double.parseDouble(lat.getText().toString());
                        double cluster_long = Double.parseDouble(longi.getText().toString());
                        cluster.setNumCases(cluster_cases);
                        cluster.setTitle(cluster_name);
                        cluster.setLatitude(cluster_lat);
                        cluster.setLongitude(cluster_long);
                        cluster.setId(databaseReference.push().getKey());
                        databaseReference.child(cluster.getId()).setValue(cluster);
                        alertDialog.dismiss();
                        Snackbar.make(linearLayout,"Success!", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                    else{
                        alertDialog.dismiss();
                        Snackbar.make(linearLayout,"Invalid parameters entered. Please try again.",BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
                else{
                    if(isValid(cases.getText().toString(),name.getText().toString())){
                        int cluster_cases = Integer.parseInt(cases.getText().toString());
                        cluster.setNumCases(cluster_cases);
                        cluster.setTitle(name.getText().toString());
                        Map<String,Object> map = new HashMap<>();
                        map.put("numCases",cluster_cases);
                        map.put("title",name.getText().toString());
                        databaseReference.child(cluster.getId()).updateChildren(map);
                        alertDialog.dismiss();
                        Snackbar.make(linearLayout,"Success!", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                    else{
                        alertDialog.dismiss();
                        Snackbar.make(linearLayout,"Invalid parameters entered. Please try again.",BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MainActivity.isConnectedToInternet(context))
                    MainActivity.showNoConnectionSnackBar();
                databaseReference.child(cluster.getId()).removeValue();
                alertDialog.dismiss();
                Snackbar.make(linearLayout,"Cluster deleted successfully!",BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }

}

class Cluster{
    public String title;
    public int numCases;
    public double latitude;
    public double longitude;
    public String id;

    public Cluster(){}

    public Cluster(int numCases, double latitude, double longitude, String title, String id){
        this.numCases = numCases;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getNumCases() {
        return numCases;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId(){return id;}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNumCases(int numCases) {
        this.numCases = numCases;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setId(String id){
        this.id = id;
    }
}

class Clinic{
    private String title;
    private String address;
    private String type;
    private String number;

    public Clinic(String title, String address, String type, String number) {
        this.title = title;
        this.address = address;
        this.type = type;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }
}





