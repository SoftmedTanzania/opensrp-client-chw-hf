package org.smartregister.chw.hf.provider;

import static org.smartregister.util.Utils.getName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.core.provider.CorePmtctRegisterProvider;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.pmtct.fragment.BasePmtctRegisterFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.Utils;

import java.util.Set;

import timber.log.Timber;

public class HfPmtctRegisterProvider extends CorePmtctRegisterProvider {

    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public HfPmtctRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        this.onClickListener = onClickListener;
        this.visibleColumns = visibleColumns;
        this.context = context;
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {

            String firstName = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));


            String dob = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false);
            String dobString = org.smartregister.family.util.Utils.getDuration(dob);

            String dod = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOD, false);
            String patientName = getName(firstName, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));


            if (StringUtils.isNotBlank(dod)) {
                dobString = org.smartregister.family.util.Utils.getDuration(dod, dob);
                dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

                patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context) + " " + context.getString(R.string.deceased_brackets);
                viewHolder.patientName.setText(patientName);
                viewHolder.patientName.setTextColor(Color.GRAY);
                viewHolder.patientName.setTypeface(viewHolder.patientName.getTypeface(), Typeface.ITALIC);
                viewHolder.dueWrapper.setVisibility(View.GONE);
            } else {
                patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context);
                viewHolder.patientName.setText(patientName);
                viewHolder.patientName.setTextColor(Color.BLACK);
                viewHolder.patientName.setTypeface(viewHolder.patientName.getTypeface(), Typeface.NORMAL);
                viewHolder.patientColumn.setOnClickListener(onClickListener);
                viewHolder.dueButton.setOnClickListener(onClickListener);
                viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
                viewHolder.registerColumns.setOnClickListener(v -> viewHolder.dueButton.performClick());
            }


            viewHolder.textViewGender.setText(updateMemberGender(pc));
            viewHolder.textViewVillage.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true));

            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.dueButton.setTag(pc);
            viewHolder.dueButton.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.FOLLOW_UP_VISIT);
            viewHolder.registerColumns.setOnClickListener(onClickListener);


        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void updateDueColumn(Context context, RegisterViewHolder viewHolder, PmtctFollowUpRule pmtctFollowUpRule) {
        if (!HfPmtctDao.hasTheClientTransferedOut(pmtctFollowUpRule.getBaseEntityId()) && !HfPmtctDao.isTheClientLostToFollowup(pmtctFollowUpRule.getBaseEntityId())) {
            if (pmtctFollowUpRule.getDueDate() != null) {
                if (pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.NOT_DUE_YET)) {
                    setVisitButtonNextDueStatus(context, FpUtil.sdf.format(pmtctFollowUpRule.getDueDate()), viewHolder.dueButton);
                    viewHolder.dueButton.setVisibility(View.GONE);
                }
                if (pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE)) {
                    viewHolder.dueButton.setVisibility(View.VISIBLE);
                    setVisitButtonDueStatus(context, String.valueOf(Days.daysBetween(new DateTime(pmtctFollowUpRule.getDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
                } else if (pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE)) {
                    viewHolder.dueButton.setVisibility(View.VISIBLE);
                    setVisitButtonOverdueStatus(context, String.valueOf(Days.daysBetween(new DateTime(pmtctFollowUpRule.getOverDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
                } else if (pmtctFollowUpRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.VISIT_DONE)) {
                    setVisitDone(context, viewHolder.dueButton);
                }
            }
        } else {
            int followupStatus;
            int followupStatusColor;
            if (HfPmtctDao.hasTheClientTransferedOut(pmtctFollowUpRule.getBaseEntityId())) {
                followupStatus = R.string.transfer_out;
                followupStatusColor = context.getResources().getColor(org.smartregister.pmtct.R.color.medium_risk_text_orange);
            } else {
                followupStatus = R.string.lost_to_followup;
                followupStatusColor = context.getResources().getColor(org.smartregister.pmtct.R.color.alert_urgent_red);
            }

            viewHolder.dueButton.setVisibility(View.VISIBLE);
            viewHolder.dueButton.setTextColor(followupStatusColor);
            viewHolder.dueButton.setText(followupStatus);
            viewHolder.dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.colorless_btn_selector);
            viewHolder.dueButton.setOnClickListener(null);
        }
    }
}
