package com.example.outbrake.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.outbrake.MainActivity;
import com.example.outbrake.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

public class ThermometerFragment extends Fragment {
    private Context context;
    private String[] displayedValues = {"33", "34", "35", "36", "37", "38", "39", "40", "41"};
    private String[] displayedValues_1 = {".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};
    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thermometer, container, false);
        context = getContext();
        final ScrollView scrollView = view.findViewById(R.id.thermo_scroll_view);
        final NumberPicker numberPicker = view.findViewById(R.id.thermo_log_np);
        final NumberPicker numberPicker_1 = view.findViewById(R.id.thermo_log_np_1);
        final Button submit_btn = view.findViewById(R.id.thermo_log_submit);

        initNumPickers(numberPicker, numberPicker_1);
        final DatabaseReference databaseReference = MainActivity.getFirebaseDatabase().getReference().child("Temperature").child(MainActivity.getDisplayName());
        databaseReference.addChildEventListener(childEventListener);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isConnectedToInternet(getContext())) {
                    MainActivity.showNoConnectionSnackBar();
                } else{
                    new MaterialAlertDialogBuilder(getContext()).setTitle("Confirmation").setMessage("Are you sure you want to submit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.push().setValue(new TemperatureSubmission(displayedValues[numberPicker.getValue()], displayedValues_1[numberPicker_1.getValue()]));
                            Snackbar.make(scrollView, "Submission updated!", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("No", null).show();

                }

            }
        });

        //change_am.setOnClickListener(onClickListener);
        //change_pm.setOnClickListener(onClickListener);

        return view;
    }

    private void initNumPickers(NumberPicker numberPicker, NumberPicker numberPicker1) {
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker1.setDisplayedValues(displayedValues_1);
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(displayedValues_1.length - 1);
    }
}

class TemperatureSubmission {
    public String whole;
    public String decimal;
    public String calendar;

    public TemperatureSubmission() {
    }

    public TemperatureSubmission(String whole, String decimal) {
        this.whole = whole;
        this.decimal = decimal;
        Calendar temp = Calendar.getInstance();
        String year = String.valueOf(temp.get(Calendar.YEAR));
        String month = String.valueOf(temp.get(Calendar.MONTH));
        String day = String.valueOf(temp.get(Calendar.DAY_OF_MONTH));
        this.calendar = year + "-" + month + "-" + day;

    }

    public String getCalendar() {
        return calendar;
    }
}

    /*private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerFragment timePickerFragment = new TimePickerFragment();
            switch (v.getId()){
                case R.id.thermo_reminder_am_btn:{
                    timePickerFragment.setListener(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            TextView am
                        }
                    });
                }

            }

            timePickerFragment.show(getFragmentManager(),String.valueOf(v.getId()));
        }
    };

}

class TimePickerFragment extends DialogFragment{
    private TimePickerDialog.OnTimeSetListener mListener;
    private Context context;

    public void setListener(TimePickerDialog.OnTimeSetListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(context, mListener, hour, minute, DateFormat.is24HourFormat(context));
    }
*/
