package com.echdr.android.echdrapp.ui.event_form;

import static android.text.TextUtils.isEmpty;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.echdr.android.echdrapp.R;
import com.echdr.android.echdrapp.data.Sdk;
import com.echdr.android.echdrapp.data.service.forms.EventFormService;
import com.echdr.android.echdrapp.data.service.forms.RuleEngineService;

import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.util.Date;

import io.reactivex.processors.PublishProcessor;

public class OtherEvaluationActivity extends AppCompatActivity {

    private String eventUid;
    private String programUid;
    private String selectedChild;
    private OtherEvaluationActivity.FormType formType;
    private String orgUnit;

    private TextView textView_Date;
    private CheckBox checkbox_Food_Insecurity;
    private CheckBox checkbox_Inadequate_Water;
    private CheckBox checkbox_Poor_Income;

    private Button saveButton;
    private ImageView datePicker;

    private PublishProcessor<Boolean> engineInitialization;
    private RuleEngineService engineService;
    private DatePickerDialog.OnDateSetListener setListener;
    private Context context;

    private enum IntentExtra {
        EVENT_UID, PROGRAM_UID, OU_UID, TYPE, TEI_ID
    }

    public enum FormType {
        CREATE, CHECK
    }

    public static Intent getFormActivityIntent(Context context, String eventUid,
                                               String programUid, String orgUnitUid,
                                               OtherEvaluationActivity.FormType type, String teiID) {
        Intent intent = new Intent(context, OtherEvaluationActivity.class);
        intent.putExtra(OtherEvaluationActivity.IntentExtra.EVENT_UID.name(), eventUid);
        intent.putExtra(OtherEvaluationActivity.IntentExtra.PROGRAM_UID.name(), programUid);
        intent.putExtra(OtherEvaluationActivity.IntentExtra.OU_UID.name(), orgUnitUid);
        intent.putExtra(OtherEvaluationActivity.IntentExtra.TYPE.name(), type.name());
        intent.putExtra(OtherEvaluationActivity.IntentExtra.TEI_ID.name(), teiID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_evaluation);

        textView_Date = findViewById(R.id.otherEvaluationDate);
        datePicker         = findViewById(R.id.risk_factor_date_pick);
        checkbox_Poor_Income  = findViewById(R.id.Poor_income_Checkbox);
        checkbox_Food_Insecurity = findViewById(R.id.Food_insecurity_Checkbox);
        checkbox_Inadequate_Water  = findViewById(R.id.Inadequate_water_Checkbox);
        saveButton         = findViewById(R.id.otherEvaluationSave);

        context = this;

        eventUid = getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.EVENT_UID.name());
        programUid = getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.PROGRAM_UID.name());
        selectedChild = getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.TEI_ID.name());
        formType = OtherEvaluationActivity.FormType.valueOf(getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.TYPE.name()));
        orgUnit = getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.OU_UID.name());


        engineInitialization = PublishProcessor.create();


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

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;


                String date = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", dayOfMonth) ;
                textView_Date.setText(date);
            }
        };

        // Load the existing values - form.CHECK
        if(formType == OtherEvaluationActivity.FormType.CHECK)
        {
            System.out.println(getDataElement("cLNSXKlqjqA")); // food insecurity
            System.out.println(getDataElement("Zr5SvpMT2y0")); // communicable disease
            System.out.println(getDataElement("nXJSGsaPznl")); // inadequate child care
            System.out.println(getDataElement("r1YtZtTBbKZ")); // inadequate water sanitation
            System.out.println(getDataElement("riZnnab24ef")); // poor income
            System.out.println(getDataElement("cmqwQ5zk66F")); // evaluation date

            // set date
            try{
                String prev_date = getDataElement("cmqwQ5zk66F");
                if(!prev_date.isEmpty())
                {
                    textView_Date.setText(prev_date);
                }
            }
            catch (Exception e)
            {
                textView_Date.setText("");
            }

            // set poor income
            try{
                if(getDataElement("riZnnab24ef").equals("true"))
                {
                    checkbox_Poor_Income.setChecked(true);
                }
            }
            catch (Exception e)
            {
                checkbox_Poor_Income.setChecked(false);
            }

            // set food insecurity
            try{
                if(getDataElement("cLNSXKlqjqA").equals("true"))
                {
                    checkbox_Food_Insecurity.setChecked(true);
                }
            }
            catch (Exception e)
            {
                checkbox_Food_Insecurity.setChecked(false);
            }

            // set inadequate water
            try{
                if(getDataElement("r1YtZtTBbKZ").equals("true"))
                {
                    checkbox_Inadequate_Water.setChecked(true);
                }
            }
            catch (Exception e)
            {
                checkbox_Inadequate_Water.setChecked(false);
            }


        }
        else{
            textView_Date.setText("Click here to set Date");
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveElements();
            }
        });


        if (EventFormService.getInstance().init(
                Sdk.d2(),
                eventUid,
                programUid,
                getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.OU_UID.name())))
            this.engineService = new RuleEngineService();
    }


    @Override
    protected void onResume(){
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

    private void finishEnrollment() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (formType == OtherEvaluationActivity.FormType.CREATE)
            EventFormService.getInstance().delete();
        setResult(RESULT_CANCELED);
        finish();
    }

    private String getDataElement(String dataElement){
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

    private void saveDataElement(String dataElement, String value){
        TrackedEntityDataValueObjectRepository valueRepository;
        try {
            valueRepository = Sdk.d2().trackedEntityModule().trackedEntityDataValues()
                    .value(
                            EventFormService.getInstance().getEventUid(),
                            dataElement
                    );
        }catch (Exception e)
        {
            EventFormService.getInstance().init(
                    Sdk.d2(),
                    eventUid,
                    programUid,
                    getIntent().getStringExtra(OtherEvaluationActivity.IntentExtra.OU_UID.name()));
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

        try{
            if(!isEmpty(value))
            {
                valueRepository.blockingSet(value);
            }else
            {
                valueRepository.blockingDeleteIfExist();
            }
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
        }finally {
            if (!value.equals(currentValue)) {
                engineInitialization.onNext(true);
            }
        }
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

        System.out.println(getDataElement("cLNSXKlqjqA")); // food insecurity
        System.out.println(getDataElement("Zr5SvpMT2y0")); // communicable disease
        System.out.println(getDataElement("nXJSGsaPznl")); // inadequate child care
        System.out.println(getDataElement("r1YtZtTBbKZ")); // inadequate water sanitation
        System.out.println(getDataElement("riZnnab24ef")); // poor income
        System.out.println(getDataElement("cmqwQ5zk66F")); // evaluation date


        saveDataElement("cmqwQ5zk66F", textView_Date.getText().toString());
        saveDataElement("cLNSXKlqjqA", checkbox_Food_Insecurity.isChecked() ? "true" : "");
        saveDataElement("r1YtZtTBbKZ", checkbox_Inadequate_Water.isChecked() ? "true" : "");
        saveDataElement("riZnnab24ef", checkbox_Poor_Income.isChecked() ? "true" : "");

        finishEnrollment();
    }

    private void selectDate(int year, int month, int day)
    {
        System.out.println("Clicked et date");
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context, android.R.style.Theme_Holo_Light_Dialog, setListener, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    private String makeDate(int year, int month, int DayofMonth)
    {
        return DayofMonth + "/" + month + "/" + year;
    }

}
