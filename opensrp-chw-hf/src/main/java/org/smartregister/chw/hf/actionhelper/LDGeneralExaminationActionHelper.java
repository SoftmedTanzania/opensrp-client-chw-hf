package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-09
 */
public class LDGeneralExaminationActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private String general_condition;
    private String pulse_rate;
    private String respiratory_rate;
    private String temperature;
    private String systolic;
    private String diastolic;
    private String urineProtein;
    private String urineAcetone;
    private String fundal_height;
    private String contraction_frequency;
    private String fetal_heart_rate;
    private final Context context;
    private final MemberObject memberObject;

    public LDGeneralExaminationActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
    }

    @Override
    public String getPreProcessed() {
        JSONObject generalExaminationForm = FormUtils.getFormUtils().getFormJson(Constants.JsonForm.LDVisit.getLdGeneralExamination());
        try {
            if (generalExaminationForm != null) {

                JSONArray fields = generalExaminationForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
                JSONObject fundalHeight = JsonFormUtils.getFieldJSONObject(fields, "fundal_height");
                JSONObject lie = JsonFormUtils.getFieldJSONObject(fields, "lie");

                if (fundalHeightCaptured(memberObject.getBaseEntityId()) && fundalHeight != null) {
                    fundalHeight.put("hidden", true);
                }

                if (featalLieCaptured(memberObject.getBaseEntityId()) && lie != null) {
                    lie.put("hidden", true);
                }

            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return generalExaminationForm.toString();
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        general_condition = JsonFormUtils.getFieldValue(jsonPayload, "general_condition");
        pulse_rate = JsonFormUtils.getFieldValue(jsonPayload, "pulse_rate");
        respiratory_rate = JsonFormUtils.getFieldValue(jsonPayload, "respiratory_rate");
        temperature = JsonFormUtils.getFieldValue(jsonPayload, "temperature");
        systolic = JsonFormUtils.getFieldValue(jsonPayload, "systolic");
        diastolic = JsonFormUtils.getFieldValue(jsonPayload, "diastolic");
        urineProtein = JsonFormUtils.getFieldValue(jsonPayload, "urine_protein");
        urineAcetone = JsonFormUtils.getFieldValue(jsonPayload, "urine_acetone");
        fundal_height = JsonFormUtils.getFieldValue(jsonPayload, "fundal_height");
        contraction_frequency = JsonFormUtils.getFieldValue(jsonPayload, "contraction_frequency");
        fetal_heart_rate = JsonFormUtils.getFieldValue(jsonPayload, "fetal_heart_rate");
    }

    @Override
    public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String jsonPayload) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        if (isFullyCompleted()) {
            return context.getString(R.string.lb_fully_completed_action);
        } else if (isPartiallyCompleted()) {
            return context.getString(R.string.lb_partially_completed_action);
        } else {
            return "";
        }
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (isFullyCompleted()) {
            return BaseLDVisitAction.Status.COMPLETED;
        } else if (isPartiallyCompleted()) {
            return BaseLDVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseLDVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {

    }

    private boolean isFullyCompleted() {
        return (StringUtils.isNotBlank(general_condition) &&
                StringUtils.isNotBlank(pulse_rate) &&
                StringUtils.isNotBlank(respiratory_rate) &&
                StringUtils.isNotBlank(temperature) &&
                StringUtils.isNotBlank(systolic) &&
                StringUtils.isNotBlank(diastolic) &&
                StringUtils.isNotBlank(urineAcetone) &&
                StringUtils.isNotBlank(urineProtein) &&
                fundalHeightCaptured(memberObject.getBaseEntityId()) || StringUtils.isNotBlank(fundal_height) &&
                StringUtils.isNotBlank(contraction_frequency) &&
                StringUtils.isNotBlank(fetal_heart_rate)
        );
    }

    private boolean isPartiallyCompleted() {
        return (StringUtils.isNotBlank(general_condition) ||
                StringUtils.isNotBlank(pulse_rate) ||
                StringUtils.isNotBlank(respiratory_rate) ||
                StringUtils.isNotBlank(temperature) ||
                StringUtils.isNotBlank(systolic) ||
                StringUtils.isNotBlank(diastolic) ||
                StringUtils.isNotBlank(urineAcetone) ||
                StringUtils.isNotBlank(urineProtein) ||
                StringUtils.isNotBlank(fundal_height) ||
                StringUtils.isNotBlank(contraction_frequency) ||
                StringUtils.isNotBlank(fetal_heart_rate)
        );
    }

    public static boolean fundalHeightCaptured(String baseEntityId) {
        return LDDao.getFundalHeight(baseEntityId) != null;
    }

    public static boolean featalLieCaptured(String baseEntityId) {
        return LDDao.getFetalLie(baseEntityId) != null;
    }

}
