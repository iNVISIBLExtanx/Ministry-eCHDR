package com.echdr.android.echdrapp.ui.event_form;

import static android.text.TextUtils.isEmpty;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.echdr.android.echdrapp.R;
import com.echdr.android.echdrapp.data.Sdk;
import com.echdr.android.echdrapp.data.service.forms.EventFormService;
import com.echdr.android.echdrapp.data.service.forms.RuleEngineService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.processors.PublishProcessor;

public class AnthropometryActivityNew extends AppCompatActivity {

    private String eventUid;
    private String programUid;
    private PublishProcessor<Boolean> engineInitialization;
    private RuleEngineService engineService;
    private FormType formType;
    private GraphView heightGraph;
    private GraphView weightGraph;
    private String selectedChild;
    private String sex;
    private TextView textView_Date;
    private ImageView datePicker;

    private String orgUnit;
    private TextView AgeInWeeksTxt;
    private Context context;
    private DatePickerDialog.OnDateSetListener setListener;
    private EditText heightTxt;
    private EditText weightTxt;
    private Button saveButton;
    private Button plotGraphButton;
    private TrackedEntityAttributeValue birthday;

    Map<Integer, Integer> heightValues;
    Map<Integer, Integer> weightValues;

    Map<Integer, double[]> heightDataWHO;
    Map<Integer, double[]> weightDataWHO;

    private enum IntentExtra {
        EVENT_UID, PROGRAM_UID, OU_UID, TYPE, TEI_ID
    }

    public enum FormType {
        CREATE, CHECK
    }

    public static Intent getFormActivityIntent(Context context, String eventUid,
                                               String programUid, String orgUnitUid,
                                               FormType type, String teiID) {
        Intent intent = new Intent(context, AnthropometryActivityNew.class);
        intent.putExtra(IntentExtra.EVENT_UID.name(), eventUid);
        intent.putExtra(IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(IntentExtra.OU_UID.name(), orgUnitUid);
        intent.putExtra(IntentExtra.TYPE.name(), type.name());
        intent.putExtra(IntentExtra.TEI_ID.name(), teiID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anthropometry_new);

        textView_Date = findViewById(R.id.anthropometryDate);
        datePicker = findViewById(R.id.anthropometry_date_pick);
        heightTxt = findViewById(R.id.anthropometryHeight);
        weightTxt = findViewById(R.id.anthropometryWeight);
        saveButton = findViewById(R.id.anthropometrySave);
        heightGraph = findViewById(R.id.heightforageAnthropometry);
        weightGraph = findViewById(R.id.weightforageAnthropometry);
        AgeInWeeksTxt = findViewById(R.id.ageInWeeks);
        plotGraphButton = findViewById(R.id.plotGraph);

        eventUid = getIntent().getStringExtra(IntentExtra.EVENT_UID.name());
        programUid = getIntent().getStringExtra(IntentExtra.PROGRAM_UID.name());
        selectedChild = getIntent().getStringExtra(IntentExtra.TEI_ID.name());
        formType = FormType.valueOf(getIntent().getStringExtra(IntentExtra.TYPE.name()));
        orgUnit = getIntent().getStringExtra(IntentExtra.OU_UID.name());

        engineInitialization = PublishProcessor.create();

        // Get the birthday of the child
        birthday = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(selectedChild)
                .byTrackedEntityAttribute().eq("qNH202ChkV3")
                .one().blockingGet();

        // Get the sex of the child
        TrackedEntityAttributeValue sex_d = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(selectedChild)
                .byTrackedEntityAttribute().eq("lmtzQrlHMYF")
                .one().blockingGet();
        
        sex = sex_d.value();

        context = this;

        heightValues = new HashMap<>();
        weightValues = new HashMap<>();
        selectDataSets();


        Date date = new Date();
        String s_day          = (String) DateFormat.format("dd",   date); // 20
        String s_monthNumber  = (String) DateFormat.format("MM",   date); // 06
        String s_year         = (String) DateFormat.format("yyyy", date); // 2013

        final int year = Integer.parseInt(s_year);
        final int month = Integer.parseInt(s_monthNumber);
        final int day = Integer.parseInt(s_day);

        textView_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked et date");
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        context, android.R.style.Theme_Holo_Light_Dialog, setListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();

            }
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(year, month, day);
            }
        });

        String date_string = "";
        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;

                String date_string = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", dayOfMonth) ;
                textView_Date.setText(date_string);
                setAgeInWeeks();
            }
        };


        // Load the existing values - form.CHECK
        if(formType == AnthropometryActivityNew.FormType.CHECK)
        {
            // set date
            try{
                String prev_date = getDataElement("YB21tVtxZ0z");
                if(!prev_date.isEmpty())
                {
                    textView_Date.setText(prev_date);
                }
            }
            catch (Exception e)
            {
                textView_Date.setText("");
            }

            heightTxt.setText(getDataElement("cDXlUgg1WiZ"));
            weightTxt.setText(getDataElement("rBRI27lvfY5"));

            String Currentweight;
            if (weightTxt.getText().toString().isEmpty()) {
                Currentweight = "";
            } else {
                Currentweight = String.valueOf(
                        Float.parseFloat(weightTxt.getText().toString()) / 1000f);
            }

            // First set age in weeks because change color uses its value
            setAgeInWeeks();

            ChangeColor(heightTxt, heightTxt.getText().toString(), heightDataWHO, true);
            ChangeColor(weightTxt, Currentweight, weightDataWHO, true);



        }else{
            textView_Date.setText("Click here to set Date");
        }

        heightTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ChangeColor(heightTxt, s.toString(), heightDataWHO, true);
            }
        });

        weightTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ChangeColor(weightTxt, s.toString(), weightDataWHO, false);
            }
        });

        saveButton.setOnClickListener(v -> {
            saveElements();
            finishEnrollment();
        });

        plotGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveElements();
                weightGraph.removeAllSeries();
                heightGraph.removeAllSeries();
                plotGraph();
                plotDataElements();
                drawLineGraph();
            }
        });



        plotGraph();
        plotDataElements();
        drawLineGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        EventFormService.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        EventFormService.clear();
        setResult(RESULT_OK);
        finish();
    }



    private void saveElements()
    {
        if(textView_Date.getText().toString().equals("Click here to set Date")||
                textView_Date.getText().toString().isEmpty())
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setMessage("Date Not Selected");
            builder1.setCancelable(true);

            builder1.setNegativeButton(
                    "Close",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return;
        }

        /*
        System.out.println(getDataElement("YB21tVtxZ0z")); // Date
        System.out.println(getDataElement("cDXlUgg1WiZ")); // height
        //System.out.println(getDataElement("SOAtQfInRoy")); // length for age
        //System.out.println(getDataElement("b4Gpl5ayBe3")); // age in months
        System.out.println(getDataElement("rBRI27lvfY5")); // weight
        //System.out.println(getDataElement("bJHCnjX02PN")); // weight for age
        //System.out.println(getDataElement("jnzg5BvOj5T")); // weight for lenght
        */
        saveDataElement("YB21tVtxZ0z", textView_Date.getText().toString());
        saveDataElement("cDXlUgg1WiZ", heightTxt.getText().toString());
        saveDataElement("rBRI27lvfY5", weightTxt.getText().toString());

    }

    private void ChangeColor(EditText text, String s,
                             Map<Integer, double[]> data, boolean height) {
        int currentAge = 0;
        if(!AgeInWeeksTxt.getText().toString().isEmpty() &&
                !AgeInWeeksTxt.getText().toString().equals("Age in weeks"))
            currentAge = Integer.parseInt(AgeInWeeksTxt.getText().toString());

        float currentValue;
        if (s.isEmpty()) {
            currentValue = 0;
            text.setBackgroundColor(Color.WHITE);
            return;
        } else {
            if (height) {
                currentValue = Float.parseFloat(s);
            } else {
                currentValue = Float.parseFloat(s) / 1000f;
            }
        }

        int category = 0;
        try {
            System.out.println("Change color : " + currentAge +" currentValue" + currentValue);
            double[] array = data.get(currentAge);
            for (category = 0; category < 4; ) {

                assert array != null;
                if (array[category] < currentValue) {
                    category++;
                } else {
                    break;
                }
            }
            category = category - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Suitable Category is " + category);

        switch (category) {
            case -1:
                text.setBackgroundColor(Color.RED);
                break;
            case 0:
                text.setBackgroundColor(Color.rgb(255, 165, 0)); // orange
                break;
            case 1:
                text.setBackgroundColor(Color.YELLOW);
                break;
            case 2:
                text.setBackgroundColor(Color.GREEN);
                break;
            case 3:
                text.setBackgroundColor(Color.rgb(215, 31, 226)); // purple color
                break;

        }

    }

    private void setAgeInWeeks()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Parse event date
            Date dob = formatter.parse(birthday.value());
            Date eventDate = formatter.parse(textView_Date.getText().toString());

            // Calculate age in weeks
            long diffInMillies = Math.abs(eventDate.getTime() - dob.getTime());
            int diff = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) / 7;

            AgeInWeeksTxt.setText(String.valueOf(diff));

        } catch (Exception error) {
            System.out.print("Error in parsing date field: " + error.toString());
        }

    }


    private void selectDataSets()
    {
        DataValuesWHO d = DataValuesWHO.getInstance();
        if(sex.equals("Male"))
        {
            d.initializeheightForAgeBoys();
            d.initializeweightForAgeBoys();
            heightDataWHO = d.getHeightForAgeBoys();
            weightDataWHO = d.getWeightForAgeBoys();
        }else
        {
            d.initializeweightForAgeGirls();
            d.initializeheightForAgeGirls();
            heightDataWHO = d.getHeightForAgeGirls();
            weightDataWHO = d.getWeightForAgeGirls();

        }

    }

    private void plotGraph()
    {

        //setup charts and background
        DataValuesWHO d = DataValuesWHO.getInstance();

        if (sex.equals("Male")) {
            for (int i = 3; i > -1; i--) {
                heightGraph.addSeries(d.heightForAgeBoysValues(i, 60));  // earlier changed from 60 to 260
                weightGraph.addSeries(d.weightForAgeBoys(i, 60));        // earlier changed from 60 to 260
            }

        } else {
            for (int i = 3; i > -1; i--) {
                heightGraph.addSeries(d.heightForAgeGirlsValues(i, 60));  // earlier changed from 60 to 260
                weightGraph.addSeries(d.weightForAgeGirlsValues(i, 60));  // earlier changed from 60 to 260
            }

        }

        heightGraph.getViewport().setBackgroundColor(Color.rgb(215, 31, 226));
        weightGraph.getViewport().setBackgroundColor(Color.rgb(215, 31, 226));

        heightGraph.getViewport().setMaxX(60); // earlier changed from 60 to 260
        heightGraph.getViewport().setMaxY(130);

        weightGraph.getViewport().setMaxX(60); // changed from 60 to 260
        weightGraph.getViewport().setMaxY(30);

        // don't show anomalies ( might be redundant when zooming is enabled)
        weightGraph.getViewport().setYAxisBoundsManual(true);
        heightGraph.getViewport().setYAxisBoundsManual(true);
        weightGraph.getViewport().setXAxisBoundsManual(true);
        heightGraph.getViewport().setXAxisBoundsManual(true);

        // enable zooming
        weightGraph.getViewport().setScalable(true);
        heightGraph.getViewport().setScalable(true);
        weightGraph.getViewport().setScalableY(true);
        heightGraph.getViewport().setScalableY(true);


    }

    private void plotDataElements()
    {
        List<String> j = new ArrayList<>();
        j.add(selectedChild);

        // get all anthropometry data of the selected child
        List<Event> eventRepository = Sdk.d2().eventModule().events()
                .byTrackedEntityInstanceUids(j)
                .byProgramUid().eq("hM6Yt9FQL0n")
                .blockingGet();

        for(int i=0; i < eventRepository.size(); i++)
        {
            /*
            System.out.println("Event ID" + eventRepository.get(i).uid());
            System.out.println("Event date : " + getDataElementFromEvent(
                    "YB21tVtxZ0z", eventRepository.get(i).uid()));
            System.out.println("Event height : " + getDataElementFromEvent(
                    "cDXlUgg1WiZ", eventRepository.get(i).uid()));
            System.out.println("Event weight : " + getDataElementFromEvent(
                    "rBRI27lvfY5", eventRepository.get(i).uid()));
            */

            prepareDataPoints(
                    getDataElementFromEvent(
                            "YB21tVtxZ0z", eventRepository.get(i).uid()),
                    getDataElementFromEvent(
                            "cDXlUgg1WiZ", eventRepository.get(i).uid()),
                    getDataElementFromEvent(
                            "rBRI27lvfY5", eventRepository.get(i).uid()));
        }

    }

    private String getDataElementFromEvent(String dataElement, String captureEvent)
    {
        TrackedEntityDataValueObjectRepository valueRepository =
                Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                        .value(
                                captureEvent,
                                dataElement
                        );

        String currentValue = valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";

        return currentValue;
    }

    private void prepareDataPoints(String date, String height, String weight)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Parse event date
            Date dob = formatter.parse(birthday.value());
            Date eventDate = formatter.parse(date);

            // Calculate age in weeks
            long diffInMillies = Math.abs(eventDate.getTime() - dob.getTime());
            int diff = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) / 7;

            // Enter to the data values
            heightValues.put(diff, Integer.parseInt(height));
            weightValues.put(diff, Integer.parseInt(weight));

        } catch (Exception error) {
            System.out.print("Error in parsing date field: " + error.toString());
        }
    }

    private void drawLineGraph()
    {
        LineGraphSeries<DataPoint> height_series = new LineGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> weight_series = new LineGraphSeries<DataPoint>();

        height_series.appendData(
                new DataPoint(0,0), true, 61
        );
        weight_series.appendData(
                new DataPoint(0,0), true, 61
        );

        for(int i=0; i< 60; i++)
        {
            if(heightValues.containsKey(i))
            {
                height_series.appendData(
                        new DataPoint(i, heightValues.get(i)), true, 61);
            }
        }

        for(int i=0; i< 60; i++)
        {
            if(weightValues.containsKey(i))
            {
                weight_series.appendData(
                        new DataPoint(i, weightValues.get(i)/1000f), true, 61);
            }
        }


        height_series.setColor(Color.BLACK);
        height_series.setThickness(5);
        weight_series.setColor(Color.BLACK);
        weight_series.setThickness(5);

        heightGraph.addSeries(height_series);
        weightGraph.addSeries(weight_series);

    }

    private void selectDate(int year, int month, int day)
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context, android.R.style.Theme_Holo_Light_Dialog, setListener, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    private String getDataElement(String dataElement) {
        TrackedEntityDataValueObjectRepository valueRepository =
                Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                        .value(
                                eventUid,
                                dataElement
                        );

        String currentValue = valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";

        return currentValue;
    }

    private void saveDataElement(String dataElement, String value) {
        TrackedEntityDataValueObjectRepository valueRepository;
        try {
            valueRepository = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                    .value(
                            EventFormService.getInstance().getEventUid(),
                            dataElement
                    );
        } catch (Exception e) {
            EventFormService.getInstance().init(
                    Sdk.d2(),
                    eventUid,
                    programUid,
                    getIntent().getStringExtra(AnthropometryActivityNew.IntentExtra.OU_UID.name()));
            valueRepository = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                    .value(
                            EventFormService.getInstance().getEventUid(),
                            dataElement
                    );
        }

        String currentValue = valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";

        if (currentValue == null)
            currentValue = "";

        try {
            if (!isEmpty(value)) {
                valueRepository.blockingSet(value);
            } else {
                valueRepository.blockingDeleteIfExist();
            }
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
        } finally {
            if (!value.equals(currentValue)) {
                engineInitialization.onNext(true);
            }
        }
    }

    private void finishEnrollment() {
        setResult(RESULT_OK);
        finish();
    }



}