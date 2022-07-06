package com.echdr.android.echdrapp.ui.tracked_entity_instances;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.echdr.android.echdrapp.R;
import com.echdr.android.echdrapp.data.Sdk;
import com.echdr.android.echdrapp.data.service.ActivityStarter;
import com.echdr.android.echdrapp.data.service.forms.EnrollmentFormService;
import com.echdr.android.echdrapp.data.service.forms.RuleEngineService;
import com.echdr.android.echdrapp.ui.base.ListActivity;
import com.echdr.android.echdrapp.ui.enrollment_form.EnrollmentFormActivity;
import com.echdr.android.echdrapp.ui.enrollment_form.EnrollmentFormModified;
import com.echdr.android.echdrapp.ui.events.EventsActivity;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;


public class ChildDetailsActivity extends ListActivity {

    private String trackedEntityInstanceUid;
    private Disposable disposable;
    private EditText name;
    private String teiUid;
    private TextView cd_no;
    private TextView cd_dob;
    private Spinner cd_gender;
    private EditText address;
    private EditText birthWeight;
    private EditText birthHeight;
    private Spinner ethnicity;
    private EditText GN_Area;
    private Spinner relationship;
    private EditText nic;
    private Spinner occupation;
    private Spinner sector;
    private EditText occu_specification;
    private Spinner highestEduLevel;
    private EditText mother_name;
    private EditText mother_dob;
    private EditText numberOfChildren;
    private EditText caregiver_name;
    private EditText lPhone;
    private EditText mNumber;

    private ImageButton edit_button;
    private RuleEngineService engineService;

    private List<Option> optionList;

    private Button submitButton;



    private ImageView overweightNotEnrolled;
    private ImageView overweightEnrolled;
    private ImageView antopoNotEnrolled;
    private ImageView antopoEnrolled;
    private ImageView supplementaryNotEnrolled;
    private ImageView supplementaryEnrolled;
    private ImageView therapeuticNotEnrolled;
    private ImageView therapeuticEnrolled;
    private ImageView otherHealthNotEnrolled;
    private ImageView otherHealthEnrolled;
    private ImageView stuntingEnrolled;
    private ImageView stuntingNotEnrolled;
    private String orgUnit;
    private Context context;
    protected String[] sexArray;
    protected String[] sex_english_only;

    protected String[] ethinicityArray;
    protected String[] ethinicity_english_only;

    protected String[] sectorArray;
    protected String[] sector_english_only;

    protected String[] eduLevelArray;
    protected String[] eduLevel_english_only;

    protected String[] occupationArray;
    protected String[] occupation_english_only;

    protected String[] relationshipArray;
    protected String[] relationship_english_only;

    private String anthropometryEnrollmentID;
    private String otherEnrollmentID;
    private String overweightEnrollmentID;
    private String stuntingEnrollmentID;
    private String supplementaryEnrollmentID;
    private String therapeuticEnrollmentID;


    private enum IntentExtra {
        TRACKED_ENTITY_INSTANCE_UID
    }

    public static Intent getTrackedEntityInstancesActivityIntent(Context context, String uid) {
        Intent intent = new Intent(context, ChildDetailsActivity.class);
        intent.putExtra(IntentExtra.TRACKED_ENTITY_INSTANCE_UID.name(), uid);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_details);

        cd_no = findViewById(R.id.cd_no);
        cd_dob = findViewById(R.id.cd_dob);
        cd_gender = findViewById(R.id.sex);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        birthWeight = findViewById(R.id.birthWeight);
        birthHeight = findViewById(R.id.birthHeight);
        ethnicity = findViewById(R.id.ethnicity);
        GN_Area = findViewById(R.id.GN_Area);
        relationship = findViewById(R.id.relationship);
        nic = findViewById(R.id.nic);
        occupation = findViewById(R.id.occupation);
        sector = findViewById(R.id.sector);
        occu_specification = findViewById(R.id.occu_specifcation1);
        highestEduLevel = findViewById(R.id.highestEduLevel);
        mother_name = findViewById(R.id.mother_name);
        mother_dob = findViewById(R.id.mother_dob);
        numberOfChildren = findViewById(R.id.numberOfChildren);
        caregiver_name = findViewById(R.id.caregiver_name);
        lPhone = findViewById(R.id.lPhone);
        mNumber = findViewById(R.id.mNumber);
        edit_button = findViewById(R.id.edit_btn);
        submitButton = findViewById(R.id.submit);

        context = this;

        overweightNotEnrolled = findViewById(R.id.NotEnOverWeight);
        overweightEnrolled = findViewById(R.id.EnOverWeight);
        antopoNotEnrolled = findViewById(R.id.NotEnAntopo);
        antopoEnrolled = findViewById(R.id.EnAntopo);
        supplementaryNotEnrolled = findViewById(R.id.NotEnSupplementary);
        supplementaryEnrolled = findViewById(R.id.EnSupplementary);
        therapeuticNotEnrolled = findViewById(R.id.NotEnTera);
        therapeuticEnrolled = findViewById(R.id.EnTera);
        otherHealthNotEnrolled = findViewById(R.id.NotEnOtherHealth);
        otherHealthEnrolled = findViewById(R.id.EnOtherHealth);
        stuntingEnrolled = findViewById(R.id.EnStunting);
        stuntingNotEnrolled = findViewById(R.id.NotEnStunting);

        teiUid = getIntent().getStringExtra(IntentExtra.TRACKED_ENTITY_INSTANCE_UID.name());
        trackedEntityInstanceUid = teiUid;

        //setting spinners

        ArrayAdapter<CharSequence> sexadapter = ArrayAdapter.createFromResource(context,
                R.array.sex,
                android.R.layout.simple_spinner_item);
        sexadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cd_gender.setAdapter(sexadapter);
        cd_gender.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        ArrayAdapter<CharSequence> ethinicityadapter = ArrayAdapter.createFromResource(context,
                R.array.ethnicity,
                android.R.layout.simple_spinner_item);
        ethinicityadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ethnicity.setAdapter(ethinicityadapter);
        ethnicity.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        ArrayAdapter<CharSequence> eduadapter = ArrayAdapter.createFromResource(context,
                R.array.highestEdu,
                android.R.layout.simple_spinner_item);
        eduadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        highestEduLevel.setAdapter(eduadapter);
        highestEduLevel.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        ArrayAdapter<CharSequence> sectoradapter = ArrayAdapter.createFromResource(context,
                R.array.sector,
                android.R.layout.simple_spinner_item);
        sectoradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sector.setAdapter(sectoradapter);
        sector.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        ArrayAdapter<CharSequence> occuadapter = ArrayAdapter.createFromResource(context,
                R.array.occupation,
                android.R.layout.simple_spinner_item);
        occuadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        occupation.setAdapter(occuadapter);
        occupation.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        ArrayAdapter<CharSequence> relationadapter = ArrayAdapter.createFromResource(context,
                R.array.relationship,
                android.R.layout.simple_spinner_item);
        relationadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationship.setAdapter(relationadapter);
        relationship.setOnItemSelectedListener(new EnrollmentTypeSpinnerClass());

        sexArray = getResources().getStringArray(R.array.sex);
        ethinicityArray = getResources().getStringArray(R.array.ethnicity);
        sectorArray = getResources().getStringArray(R.array.sector);
        eduLevelArray = getResources().getStringArray(R.array.highestEdu);
        occupationArray = getResources().getStringArray(R.array.occupation);
        relationshipArray = getResources().getStringArray(R.array.relationship);


        sex_english_only = getResources().getStringArray(R.array.sex_english_only);
        ethinicity_english_only = getResources().getStringArray(R.array.ethinicity_english_only);
        sector_english_only = getResources().getStringArray(R.array.sector_english_only);
        eduLevel_english_only = getResources().getStringArray(R.array.eduLevel_english_only);
        occupation_english_only = getResources().getStringArray(R.array.occupation_english_only);
        relationship_english_only = getResources().getStringArray(R.array.relationship_english_only);

        try{
            cd_no.setText(getValueListener("h2ATdtJguMq"));

        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            occu_specification.setText(getValueListener("s7Rde0kFOFb"));

        }catch (Exception e){
            e.printStackTrace();
        }


        try{
            cd_dob.setText(getValueListener("qNH202ChkV3"));

        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            name.setText(getValueListener("zh4hiarsSD5"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            address.setText(getValueListener("D9aC5K6C6ne"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            birthWeight.setText(getValueListener("Fs89NLB2FrA"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            birthHeight.setText(getValueListener("LpvdWM4YuRq"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            GN_Area.setText(getValueListener("upQGjAHBjzu"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            nic.setText(getValueListener("Gzjb3fp9FSe"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            mother_name.setText(getValueListener("K7Fxa2wv2Rx"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            mother_dob.setText(getValueListener("kYfIkz2M6En"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            numberOfChildren.setText(getValueListener("Gy4bCBxNuo4"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            caregiver_name.setText(getValueListener("hxCXbI5J2YS"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            lPhone.setText(getValueListener("cpcMXDhQouL"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            mNumber.setText(getValueListener("LYRf4eIUVuN"));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            cd_gender.setSelection(
                    getSpinnerSelection("lmtzQrlHMYF", sexArray));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //select ethnicity
            ethnicity.setSelection(
                    getSpinnerSelection("b9CoAneYYys", ethinicityArray));

        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //select sector
            sector.setSelection(
                    getSpinnerSelection("igjlkmMF81X", sectorArray));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //select education
            highestEduLevel.setSelection(
                    getSpinnerSelection("GMNSaaq4xST", eduLevelArray));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //select occupation
            occupation.setSelection(
                    getSpinnerSelection("Srxv0vniOnf", occupationArray));
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //select relationship
            relationship.setSelection(
                    getSpinnerSelection("ghN8XfnlU5V", relationshipArray));

        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("tei is " + teiUid );
        System.out.println("Spinner is 1" + getDataElement("b9CoAneYYys") );
        System.out.println("Spinner is 2" + getDataElement("igjlkmMF81X") );
        System.out.println("Spinner is 3" + getDataElement("GMNSaaq4xST") );
        System.out.println("Spinner is 4" + getDataElement("Srxv0vniOnf") );
        System.out.println("Spinner is 5" + getDataElement("ghN8XfnlU5V") );

        getEnrollment();
        EnrollToPrograms();

        edit_button.setOnClickListener(view ->{
            name.setEnabled(true);
            address.setEnabled(true);
            birthHeight.setEnabled(true);
            birthWeight.setEnabled(true);
            GN_Area.setEnabled(true);
            nic.setEnabled(true);
            mother_name.setEnabled(true);
            mother_dob.setEnabled(true);
            numberOfChildren.setEnabled(true);
            caregiver_name.setEnabled(true);
            lPhone.setEnabled(true);
            mNumber.setEnabled(true);
            occu_specification.setEnabled(true);

        });

        submitButton.setOnClickListener(view -> {






            String nameChild = name.getText().toString();
            String addressChild = address.getText().toString();
            String birthHeightChild = birthHeight.getText().toString();
            String birthWeightChild = birthWeight.getText().toString();
            String ethnicityChild = ethnicity.getSelectedItem().toString();
            String gnAreaChild = GN_Area.getText().toString();
            String relationChild = relationship.getSelectedItem().toString();
            String nationalId = nic.getText().toString();
            String occupation_sep = occu_specification.getText().toString();
            String occupationChild = occupation.getSelectedItem().toString();
            String sectorChild = sector.getSelectedItem().toString();
            String highestEdu = highestEduLevel.getSelectedItem().toString();
            String momName = mother_name.getText().toString();
            String momDob = mother_dob.getText().toString();
            String numberOfChil = numberOfChildren.getText().toString();
            String careName = caregiver_name.getText().toString();
            String landNumber = lPhone.getText().toString();
            String mobileNumber = mNumber.getText().toString();

            String gender = cd_gender.getSelectedItem().toString();

            saveDataElement("zh4hiarsSD5", nameChild);
            saveDataElement("D9aC5K6C6ne", addressChild);
            saveDataElement("LpvdWM4YuRq", birthHeightChild);
            saveDataElement("Fs89NLB2FrA", birthWeightChild);
            saveDataElement("b9CoAneYYys", ethnicityChild);
            saveDataElement("upQGjAHBjzu", gnAreaChild);
            saveDataElement("ghN8XfnlU5V", relationChild);
            saveDataElement("Gzjb3fp9FSe", nationalId);
            saveDataElement("Srxv0vniOnf", occupationChild);
            saveDataElement("s7Rde0kFOFb", occupation_sep);
            saveDataElement("igjlkmMF81X", sectorChild);
            saveDataElement("GMNSaaq4xST", highestEdu);
            saveDataElement("K7Fxa2wv2Rx", momName);
            saveDataElement("kYfIkz2M6En", momDob);
            saveDataElement("Gy4bCBxNuo4", numberOfChil);
            saveDataElement("hxCXbI5J2YS", careName);
            saveDataElement("cpcMXDhQouL", landNumber);
            saveDataElement("LYRf4eIUVuN", mobileNumber);
            saveDataElement("lmtzQrlHMYF", gender);

            finish();


        });




    }

    class EnrollmentTypeSpinnerClass implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            //Toast.makeText(v.getContext(), "Your choose :" +
            //sexArray[position], Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private String getValueListener(String dataElement) {

        String currentValue = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(teiUid)
                .byTrackedEntityAttribute().eq(dataElement)
                .one().blockingGet().value();



        return currentValue;
    }

    /*
    private void setSpinner(String optionSetUid, Spinner spinnerName) {
        optionList = Sdk.d2().optionModule().options().byOptionSetUid().eq(optionSetUid).blockingGet();
        List<String> optionListNames = new ArrayList<>();
        for (Option option : optionList) optionListNames.add(option.displayName());
        spinnerName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, optionListNames));

    }
     */

    private int getSpinnerSelection(String dataElement, String [] array)
    {
        int itemPosition = -1;
        String stringElement = getDataElement(dataElement);
        for(int i =0; i<array.length; i++)
        {
            if(array[i].equals(stringElement))
            {
                itemPosition = i;
            }
        }
        System.out.println("Selected item is " + String.valueOf(itemPosition));
        return itemPosition;
    }

    private String getDataElement(String dataElement) {
        TrackedEntityAttributeValueObjectRepository valueRepository =
                Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                        .value(
                                dataElement,
                                teiUid
                                //dataElement
                                //getIntent().getStringExtra(EnrollmentFormModified.IntentExtra.TEI_UID.name()
                                //)
                        );
        String currentValue = valueRepository.blockingExists() ?
                valueRepository.blockingGet().value() : "";

        return currentValue;
    }


    private void saveDataElement(String dataElement, String value){
        TrackedEntityAttributeValueObjectRepository valueRepository = null;
        try {
            valueRepository = Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(
                            dataElement,
                            teiUid

                    );
        }catch (Exception e)
        {
            System.out.println(e.toString());
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
        }

    }

    private void getEnrollment(){

        // get anthropometry latest enrollment
        List<Enrollment> AnthropometryStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("hM6Yt9FQL0n")
                //.orderByLastUpdated(RepositoryScope.OrderByDirection.DESC)
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        for(int i=0;i<AnthropometryStatus.size(); i++)
        {
            System.out.print("Date :");
            System.out.print(AnthropometryStatus.get(i).created());
            System.out.print(" uid :");
            System.out.print(AnthropometryStatus.get(i).uid());
            System.out.print(" status :");
            System.out.println(AnthropometryStatus.get(i).status().toString());
        }

        System.out.println("Anthropometry size " + String.valueOf(AnthropometryStatus.size()) );

        if(!AnthropometryStatus.isEmpty())
        {
            System.out.println("Anthropometry is " + AnthropometryStatus.get(0).status().toString() );
            anthropometryEnrollmentID = AnthropometryStatus.get(0).uid();
            if ( AnthropometryStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                antopoEnrolled.setVisibility(View.VISIBLE);
                antopoNotEnrolled.setVisibility(View.GONE);
            }
        }

        // get other health/non health latest enrollment
        List<Enrollment> otherStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("iUgzznPsePB")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        if(!otherStatus.isEmpty())
        {
            otherEnrollmentID =  otherStatus.get(0).uid();
            if ( otherStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                otherHealthEnrolled.setVisibility(View.VISIBLE);
                otherHealthNotEnrolled.setVisibility(View.GONE);
            }
        }

        // get other overweight/obesity latest enrollment
        List<Enrollment> overweightStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("JsfNVX0hdq9")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        if(!overweightStatus.isEmpty())
        {
            overweightEnrollmentID =  overweightStatus.get(0).uid();
            if ( overweightStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                overweightEnrolled.setVisibility(View.VISIBLE);
                overweightNotEnrolled.setVisibility(View.GONE);
            }
        }

        // get other stunting latest enrollment
        List<Enrollment> stuntingStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("lSSNwBMiwrK")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        if(!stuntingStatus.isEmpty())
        {
            stuntingEnrollmentID = stuntingStatus.get(0).uid();
            if ( stuntingStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                stuntingEnrolled.setVisibility(View.VISIBLE);
                stuntingNotEnrolled.setVisibility(View.GONE);
            }
        }

        // get supplementary latest enrollment
        List<Enrollment> supplementaryStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("tc6RsYbgGzm")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        if(!supplementaryStatus.isEmpty())
        {
            supplementaryEnrollmentID = supplementaryStatus.get(0).uid();
            if ( supplementaryStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                supplementaryEnrolled.setVisibility(View.VISIBLE);
                supplementaryNotEnrolled.setVisibility(View.GONE);
            }
        }

        // get therapeutic latest enrollment
        List<Enrollment> therapeuticStatus = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq("CoGsKgEG4O0")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();

        if(!therapeuticStatus.isEmpty())
        {
            therapeuticEnrollmentID = therapeuticStatus.get(0).uid();
            if ( therapeuticStatus.get(0).status().equals(EnrollmentStatus.ACTIVE)) {
                therapeuticEnrolled.setVisibility(View.VISIBLE);
                therapeuticNotEnrolled.setVisibility(View.GONE);
            }
        }

        /*

        List<Enrollment> enrollments = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(trackedEntityInstanceUid)
                .blockingGet();

        List<Enrollment> enroll = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(trackedEntityInstanceUid)
                .byProgram().eq("hM6Yt9FQL0n")
                .orderByLastUpdated(RepositoryScope.OrderByDirection.ASC)
                .blockingGet();

        if(!enroll.isEmpty())
        {
            System.out.print("Newest Enrollment date ");
            System.out.print(enroll.get(0).created());
            System.out.print(" id ");
            System.out.println(enroll.get(0).uid());
        }

        for (Enrollment v: enrollments) {
            System.out.println(v.program());
        }

        // Only show the enrolled if the enrollment status is active
        for (Enrollment v: enrollments) {

            if (v.program().equals("hM6Yt9FQL0n") && v.status().equals(EnrollmentStatus.ACTIVE)) {
                antopoEnrolled.setVisibility(View.VISIBLE);
                antopoNotEnrolled.setVisibility(View.GONE);
            }

            if(v.program().equals("iUgzznPsePB") && v.status().equals(EnrollmentStatus.ACTIVE)){
                otherHealthEnrolled.setVisibility(View.VISIBLE);
                otherHealthNotEnrolled.setVisibility(View.GONE);
            }

            if(v.program().equals("JsfNVX0hdq9") && v.status().equals(EnrollmentStatus.ACTIVE)){
                overweightEnrolled.setVisibility(View.VISIBLE);
                overweightNotEnrolled.setVisibility(View.GONE);
            }

            if(v.program().equals("lSSNwBMiwrK") && v.status().equals(EnrollmentStatus.ACTIVE)){
                stuntingEnrolled.setVisibility(View.VISIBLE);
                stuntingNotEnrolled.setVisibility(View.GONE);
            }

            if(v.program().equals("tc6RsYbgGzm") && v.status().equals(EnrollmentStatus.ACTIVE)){
                supplementaryEnrolled.setVisibility(View.VISIBLE);
                supplementaryNotEnrolled.setVisibility(View.GONE);
            }

            if(v.program().equals("CoGsKgEG4O0") && v.status().equals(EnrollmentStatus.ACTIVE)){
                therapeuticEnrolled.setVisibility(View.VISIBLE);
                therapeuticNotEnrolled.setVisibility(View.GONE);
            }

        }
        */

    }

    private void EnrollToPrograms(){

        List<TrackedEntityInstance> s = Sdk.d2().trackedEntityModule()
                .trackedEntityInstances().byUid().eq(teiUid).blockingGet();

        for (TrackedEntityInstance v: s) {
            orgUnit = v.organisationUnit();
            System.out.println("Organization Unit: " + orgUnit);
        }

        String orgUnit2 = Sdk.d2().organisationUnitModule().organisationUnits()
                .byProgramUids(Collections.singletonList("hM6Yt9FQL0n"))
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .one().blockingGet().uid();
        System.out.println("Organization Unit 2 : " + orgUnit2);

        orgUnit = orgUnit2;


        overweightNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "JsfNVX0hdq9", orgUnit), false);

            }
        });

        overweightEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "JsfNVX0hdq9",
                        teiUid, overweightEnrollmentID);
                startActivity(intent);
            }
        });

        antopoNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "hM6Yt9FQL0n", orgUnit), false);
                 */
                // Get the latest enrollment

                List<Enrollment> AnthropometryStatus = Sdk.d2().enrollmentModule().enrollments()
                        .byTrackedEntityInstance().eq(teiUid)
                        .byProgram().eq("hM6Yt9FQL0n")
                        .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                        .blockingGet();

                String anthropometryEnrollmentID = "";

                // The child should have at least one enrollment
                if(!AnthropometryStatus.isEmpty())
                {
                    anthropometryEnrollmentID = AnthropometryStatus.get(0).uid();
                }
                else
                {
                    return;
                }

                // set the enrollment status to active based on the enrollment ID
                EnrollmentObjectRepository rep = Sdk.d2().enrollmentModule().enrollments()
                        .uid(anthropometryEnrollmentID);
                try {
                    rep.setStatus(EnrollmentStatus.ACTIVE);
                } catch (D2Error d2Error) {
                    d2Error.printStackTrace();
                    Toast.makeText(context, "re-enrolling unsuccessful",
                            Toast.LENGTH_LONG).show();
                }

                Intent intent = EventsActivity.getIntent(getApplicationContext(), "hM6Yt9FQL0n",
                        teiUid, anthropometryEnrollmentID);
                startActivity(intent);

            }
        });

        antopoEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "hM6Yt9FQL0n",
                        teiUid, anthropometryEnrollmentID);
                startActivity(intent);

            }
        });

        otherHealthNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "iUgzznPsePB", orgUnit), false);

            }
        });

        otherHealthEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "iUgzznPsePB",
                        teiUid, otherEnrollmentID);
                startActivity(intent);

            }
        });

        stuntingNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "lSSNwBMiwrK", orgUnit), false);

            }
        });

        stuntingEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "lSSNwBMiwrK",
                        teiUid, stuntingEnrollmentID);
                startActivity(intent);

            }
        });

        supplementaryNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "tc6RsYbgGzm", orgUnit), false);

            }
        });

        supplementaryEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "tc6RsYbgGzm",
                        teiUid, supplementaryEnrollmentID);
                startActivity(intent);

            }
        });

        therapeuticNotEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityStarter.startActivity(ChildDetailsActivity.this,
                        EnrollmentFormActivity.getFormActivityIntent(getApplicationContext(), trackedEntityInstanceUid, "CoGsKgEG4O0", orgUnit), false);

            }
        });

        therapeuticEnrolled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EventsActivity.getIntent(getApplicationContext(), "CoGsKgEG4O0",
                        teiUid, therapeuticEnrollmentID);
                startActivity(intent);

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }


}
