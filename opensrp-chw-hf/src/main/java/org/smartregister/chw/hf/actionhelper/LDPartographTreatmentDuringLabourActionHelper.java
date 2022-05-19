package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author issyzac 5/19/22
 */
public class LDPartographTreatmentDuringLabourActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    MemberObject memberObject;
    Context context;

    private String drugsAdministered;
    private String drugs;
    private String ivFluid;

    public LDPartographTreatmentDuringLabourActionHelper(MemberObject memberObject){
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            drugsAdministered = CoreJsonFormUtils.getValue(jsonObject, "drugs_administered");
            drugs = CoreJsonFormUtils.getValue(jsonObject, "drugs");
            ivFluid = CoreJsonFormUtils.getValue(jsonObject, "iv_fluid");
        }catch (Exception e){
            Timber.e(e);
        }
    }

    @Override
    public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return "";
    }

    @Override
    public String postProcess(String jsonPayload) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        String subtitle = ""+ context.getString(R.string.partograph_treatment_during_labour_drugs_administered);

        if (drugsAdministered.contains("oxytocin"))
            subtitle += "Oxytocin (drops/min)";

        if (drugsAdministered.contains("drugs"))
            subtitle += ", "+ context.getString(R.string.partograph_treatment_during_labour_drugs)+" : "+drugs;

        if (drugsAdministered.contains("iv_fluid"))
            subtitle += ", "+ context.getString(R.string.partograph_treatment_during_labour_iv_fluid)+" : "+ivFluid;

        return StringUtils.isNotBlank(drugsAdministered) ?
                subtitle : "";
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(drugsAdministered)){
            return BaseLDVisitAction.Status.COMPLETED;
        }else{
            return BaseLDVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
        Timber.v("Payload received");
    }
}
