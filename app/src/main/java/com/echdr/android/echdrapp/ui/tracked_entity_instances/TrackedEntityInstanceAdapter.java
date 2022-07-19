package com.echdr.android.echdrapp.ui.tracked_entity_instances;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.DataSource;
import androidx.paging.PagedListAdapter;

import com.echdr.android.echdrapp.R;
import com.echdr.android.echdrapp.data.Sdk;
import com.echdr.android.echdrapp.data.service.ActivityStarter;
import com.echdr.android.echdrapp.data.service.DateFormatHelper;
import com.echdr.android.echdrapp.ui.base.DiffByIdItemCallback;
import com.echdr.android.echdrapp.ui.base.ListItemWithSyncHolder;
import com.echdr.android.echdrapp.ui.tracker_import_conflicts.TrackerImportConflictsAdapter;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.text.MessageFormat;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.echdr.android.echdrapp.data.service.AttributeHelper.teiSubtitle1;
import static com.echdr.android.echdrapp.data.service.AttributeHelper.teiSubtitle2First;
import static com.echdr.android.echdrapp.data.service.AttributeHelper.teiSubtitle2Second;
import static com.echdr.android.echdrapp.data.service.AttributeHelper.teiTitle;
import static com.echdr.android.echdrapp.data.service.ImageHelper.getBitmap;
import static com.echdr.android.echdrapp.data.service.StyleBinderHelper.setBackgroundColor;
import static com.echdr.android.echdrapp.data.service.StyleBinderHelper.setState;

public class TrackedEntityInstanceAdapter extends PagedListAdapter<TrackedEntityInstance, ListItemWithSyncHolder> {

    private final AppCompatActivity activity;
    private DataSource<?, TrackedEntityInstance> source;
    private Context context;


    public TrackedEntityInstanceAdapter(AppCompatActivity activity) {
        super(new DiffByIdItemCallback<>());
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public ListItemWithSyncHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ListItemWithSyncHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemWithSyncHolder holder, int position) {
        TrackedEntityInstance trackedEntityInstance = getItem(position);
        List<TrackedEntityAttributeValue> values = trackedEntityInstance.trackedEntityAttributeValues();
        holder.title.setText(valueAt(values, "zh4hiarsSD5"));

        //holder.subtitle1.setText(valueAt(values, teiSubtitle1(trackedEntityInstance)));
        //holder.subtitle2.setText(setSubtitle2(values, trackedEntityInstance));

        // show gender
        holder.subtitle1.setText(valueAt(values, "lmtzQrlHMYF"));

        // show date of birth
        holder.subtitle2.setText(valueAt(values, "qNH202ChkV3"));


        holder.rightText.setText(DateFormatHelper.formatDate(trackedEntityInstance.created()));
        //holder.title.setText();
        setImage(trackedEntityInstance, holder);
        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setOnClickListener(view -> {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
            builder1.setMessage("Are you sure you want to delete the child?");
            builder1.setCancelable(true);

            builder1.setNegativeButton(
                    "Close",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            return;
                        }
                    });
            builder1.setPositiveButton(
                    "confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                Sdk.d2().trackedEntityModule().trackedEntityInstances().uid(trackedEntityInstance.uid()).blockingDelete();
                                invalidateSource();
                                notifyDataSetChanged();
                            } catch (D2Error d2Error) {
                                d2Error.printStackTrace();
                            }
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        });
        if (trackedEntityInstance.state() == State.TO_POST ||
                trackedEntityInstance.state() == State.TO_UPDATE) {
            holder.sync.setVisibility(View.VISIBLE);
            holder.sync.setOnClickListener(v -> {
                holder.sync.setVisibility(View.GONE);
                RotateAnimation rotateAnim = new RotateAnimation(0f, 359f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnim.setDuration(2500);
                rotateAnim.setRepeatMode(Animation.INFINITE);
                holder.syncIcon.startAnimation(rotateAnim);

                Disposable disposable = syncTei(trackedEntityInstance.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                },
                                Throwable::printStackTrace,
                                () -> {
                                    holder.syncIcon.clearAnimation();
                                    invalidateSource();
                                }
                        );
            });
        } else {
            holder.sync.setVisibility(View.GONE);
            holder.sync.setOnClickListener(null);
        }
        setBackgroundColor(R.color.colorAccentDark, holder.icon);

        setState(trackedEntityInstance.state(), holder.syncIcon);

        /*Enrollment enroll = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(trackedEntityInstance.uid())
                .byProgram().eq("hM6Yt9FQL0n")
                .one().blockingGet();
        */


        //TODO make the latest enrollment
        List<Enrollment> enroll = Sdk.d2().enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(trackedEntityInstance.uid())
                .byProgram().eq("hM6Yt9FQL0n")
                .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                .blockingGet();
        

        if (enroll != null)
        {
            try {
                Enrollment otherEnrollmentID = enroll.get(0);
                if(otherEnrollmentID.status().equals(EnrollmentStatus.ACTIVE))
                {
                    holder.subtitle1.setText(otherEnrollmentID.status().toString());
                    holder.subtitle1.setTextColor(Color.rgb(34,139, 34));
                }else
                {
                    holder.subtitle1.setText(otherEnrollmentID.status().toString());
                    holder.subtitle1.setTextColor(Color.RED);
                }
            }
            catch (Exception e)
            {
                holder.subtitle1.setText("");

            }

        }
            //holder.subtitle1.setText(enroll.status().toString());


        //remove conflicts showing
        setConflicts(trackedEntityInstance.uid(), holder);

        holder.itemView.setOnClickListener(view -> {
            ActivityStarter.startActivity(
                    activity,
                    ChildDetailsActivityNew.getTrackedEntityInstancesActivityIntent(
                            activity,
                            trackedEntityInstance.uid()
                    ),false
            );
            //System.out.println(trackedEntityInstance.uid());

            // menna methana tama thana

        });

    }

    private Observable<D2Progress> syncTei(String teiUid) {
        return Sdk.d2().trackedEntityModule().trackedEntityInstances()
                .byUid().eq(teiUid)
                .upload();
    }

    private String valueAt(List<TrackedEntityAttributeValue> values, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : values) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue.value();
            }
        }

        return null;
    }

    private String getVsalue(List<TrackedEntityAttributeValue> values, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : values) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue.value();
            }
        }

        return null;
    }

    private String setSubtitle2(List<TrackedEntityAttributeValue> values, TrackedEntityInstance trackedEntityInstance) {
        String firstSubtitle = valueAt(values, teiSubtitle2First(trackedEntityInstance));
        String secondSubtitle = valueAt(values, teiSubtitle2Second(trackedEntityInstance));
        if (firstSubtitle != null) {
            if (secondSubtitle != null) {
                return MessageFormat.format("{0} - {1}", firstSubtitle, secondSubtitle);
            } else {
                return firstSubtitle;
            }
        } else {
            if (secondSubtitle != null) {
                return secondSubtitle;
            } else {
                return null;
            }
        }
    }

    private void setConflicts(String trackedEntityInstanceUid, ListItemWithSyncHolder holder) {
        TrackerImportConflictsAdapter adapter = new TrackerImportConflictsAdapter();
        holder.recyclerView.setAdapter(adapter);
        adapter.setTrackerImportConflicts(Sdk.d2().importModule().trackerImportConflicts()
                .byTrackedEntityInstanceUid().eq(trackedEntityInstanceUid).blockingGet());
    }

    private void setImage(TrackedEntityInstance trackedEntityInstance, ListItemWithSyncHolder holder) {
        Bitmap teiImage = getBitmap(trackedEntityInstance);
        if (teiImage != null) {
            holder.icon.setVisibility(View.INVISIBLE);
            holder.bitmap.setImageBitmap(teiImage);
            holder.bitmap.setVisibility(View.VISIBLE);
        } else {
            holder.bitmap.setVisibility(View.GONE);
            holder.icon.setImageResource(R.drawable.ic_person_black_24dp);
            holder.icon.setVisibility(View.VISIBLE);
        }
    }

    public void setSource(DataSource<?, TrackedEntityInstance> dataSource) {
        this.source = dataSource;
    }

    public void invalidateSource() {
        source.invalidate();
    }


}
