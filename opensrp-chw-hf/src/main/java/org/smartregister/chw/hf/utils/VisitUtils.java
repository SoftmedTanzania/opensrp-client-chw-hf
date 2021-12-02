package org.smartregister.chw.hf.utils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class VisitUtils extends org.smartregister.chw.anc.util.VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(AncLibrary.getInstance().visitRepository(), AncLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        List<Visit> visits = visitRepository.getAllUnSynced(calendar.getTime().getTime());

        List<Visit> firstVisitsCompleted = new ArrayList<>();
        List<Visit> followupVisitsCompleted = new ArrayList<>();

        for(Visit v : visits){
            if(v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_FIRST_FACILITY_VISIT)){
               try {
                   JSONObject jsonObject = new JSONObject(v.getJson());
                   JSONArray obs = jsonObject.getJSONArray("obs");

                   boolean isMedicalAndSurgicalHistoryDone = computeCompletionStatus(obs, "medical_surgical_history");
                   boolean isObstetricExaminationDone = computeCompletionStatus(obs, "abdominal_scars");
                   boolean isBaselineInvestigationDone = computeCompletionStatus(obs, "glucose_in_urine");
                   boolean isTTVaccinationDone = computeCompletionStatus(obs, "tt_card");
                   if(isMedicalAndSurgicalHistoryDone &&
                      isObstetricExaminationDone &&
                      isBaselineInvestigationDone &&
                      isTTVaccinationDone  ){
                       firstVisitsCompleted.add(v);
                   }
               } catch (Exception e){
                   Timber.e(e);
               }
            }
            else if(v.getVisitType().equalsIgnoreCase(Constants.Events.ANC_RECURRING_FACILITY_VISIT)){
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    JSONArray obs = jsonObject.getJSONArray("obs");

                    boolean isTriageDone = computeCompletionStatus(obs, "rapid_examination");
                    boolean isConsultationDone = computeCompletionStatus(obs, "examination_findings");
                    boolean isLabTestsDone = computeCompletionStatus(obs, "lab_tests");
                    boolean isPharmacyDone = computeCompletionStatus(obs, "iron_folate_supplements");
                    boolean isPregnancyStatusDone = computeCompletionStatus(obs,"pregnancy_status");

                    if(isTriageDone &&
                            isConsultationDone &&
                            isLabTestsDone &&
                            isPharmacyDone &&
                            isPregnancyStatusDone){
                        followupVisitsCompleted.add(v);
                    }
                } catch (Exception e){
                    Timber.e(e);
                }
            }
        }
        if(firstVisitsCompleted.size() > 0){
            processVisits(firstVisitsCompleted,visitRepository,visitDetailsRepository);
        }

        if(followupVisitsCompleted.size() > 0){
            processVisits(followupVisitsCompleted,visitRepository,visitDetailsRepository);
            for(Visit v: followupVisitsCompleted) {
                if (isNextVisitsCancelled(v)) {
                   createCancelledEvent(v.getJson());
                }
            }
        }
    }

    private static void createCancelledEvent(String json) throws Exception {
        Event baseEvent = new Gson().fromJson(json, Event.class);
        baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
        baseEvent.setEventType("ANC Close Followup Visits");
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for(int i = 0; i < size; i++){
            JSONObject checkObj = obs.getJSONObject(i);
            if(checkObj.getString("fieldCode").equalsIgnoreCase(checkString)){
                return true;
            }
        }
        return false;
    }

    public static boolean isNextVisitsCancelled(Visit visit){
        boolean isCancelled = false;
        try{
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for(int i = 0; i< size; i++){
                JSONObject checkObj = obs.getJSONObject(i);
                if(checkObj.getString("fieldCode").equalsIgnoreCase("pregnancy_status")){
                    JSONArray values = checkObj.getJSONArray("values");
                    if(!(values.getString(0).equalsIgnoreCase("viable"))){
                        isCancelled = true;
                        break;
                    }
                }
            }
        }catch (Exception e){
            Timber.e(e);
        }
        return isCancelled;
    }

}
