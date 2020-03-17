package com.example.outbrake.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.outbrake.MainActivity;
import com.example.outbrake.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;

import java.util.Calendar;

public class TravelFragment extends Fragment {

    private static final int RC_SIGN_IN = 1;
    private Activity activity;
    private Context context;
    private TextView selectStart;
    private TextView selectEnd;
    private TextView selectCountry;
    private Button submitApplication;
    private ScrollView scrollView;
    private RadioGroup radioGroup;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel, container, false);
        activity = getActivity();
        context = getContext();

        selectCountry = view.findViewById(R.id.travel_select_country);
        selectEnd = view.findViewById(R.id.travel_select_end_date);
        selectStart = view.findViewById(R.id.travel_select_start_date);
        submitApplication = view.findViewById(R.id.travel_submit_application);
        scrollView = view.findViewById(R.id.travel_scroll_view);
        radioGroup = view.findViewById(R.id.travel_purpose);

        //listTravels = new ArrayList<>();
        databaseReference = MainActivity.getFirebaseDatabase().getReference().child("Travel Declarations").child(MainActivity.getDisplayName());
        selectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CountryPicker.Builder builder = new CountryPicker.Builder().with(context).listener(new OnCountryPickerListener() {
                    @Override
                    public void onSelectCountry(Country country) {
                        selectCountry.setText(country.getName());
                    }
                });
                CountryPicker countryPicker = builder.build();
                countryPicker.showDialog((AppCompatActivity) getActivity());
            }
        });
        selectEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                selectEnd.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        selectStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                selectStart.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        submitApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RadioButton radioButton = activity.findViewById(radioGroup.getCheckedRadioButtonId());
                if (selectCountry.getText().length() == 0 || selectStart.getText().length() == 0 || selectEnd.getText().length() == 0)
                    Snackbar.make(scrollView, "Please complete all fields before submitting.", BaseTransientBottomBar.LENGTH_LONG).show();
                else if (!MainActivity.isConnectedToInternet(context))
                    MainActivity.showNoConnectionSnackBar();
                else {
                    new MaterialAlertDialogBuilder(getContext()).setTitle("Confirmation").setMessage("Are you sure you want to submit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.setValue(new Travel(selectStart.getText().toString(), selectEnd.getText().toString(), selectCountry.getText().toString(), radioButton.getText().toString()));
                            Snackbar.make(scrollView, "Submission updated!", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("No", null).show();
                }
            }
        });
        return view;
    }

}

class Travel {
    public String startDate;
    public String endDate;
    public String destination;
    public String purpose;

    public Travel() {
    }

    public Travel(String startDate, String endDate, String destination, String purpose) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.destination = destination;
        this.purpose = purpose;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDestination() {
        return destination;
    }

    public String getPurpose() {
        return purpose;
    }
}
