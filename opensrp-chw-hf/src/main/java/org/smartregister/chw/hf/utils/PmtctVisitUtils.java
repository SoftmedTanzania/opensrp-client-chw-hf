package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;
import static org.smartregister.util.Utils.getAllSharedPreferences;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.dao.HomeVisitDao;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.model.ChildModel;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.dao.HfPmtctDao;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.repository.VisitDetailsRepository;
import org.smartregister.chw.pmtct.repository.VisitRepository;
import org.smartregister.chw.pmtct.util.VisitUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public class PmtctVisitUtils extends VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(PmtctLibrary.getInstance().visitRepository(), PmtctLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {

        List<Visit> visits = visitRepository.getAllUnSynced();
        List<Visit> pmtctFollowupVisits = new ArrayList<>();


        for (Visit v : visits) {
            Date visitDate = new Date(v.getDate().getTime());
            int daysDiff = TimeUtils.getElapsedDays(visitDate);
            if (daysDiff > 1 && v.getVisitType().equalsIgnoreCase(org.smartregister.chw.pmtct.util.Constants.EVENT_TYPE.PMTCT_FOLLOWUP)) {
                try {
                    JSONObject jsonObject = new JSONObject(v.getJson());
                    String baseEntityId = jsonObject.getString("baseEntityId");
                    JSONArray obs = jsonObject.getJSONArray("obs");
                    List<Boolean> checks = new ArrayList<Boolean>();

                    boolean isFollowupStatusDone = computeCompletionStatus(obs, "followup_status");

                    boolean isCounsellingDone = computeCompletionStatus(obs, "is_client_counselled");
                    boolean isClinicalStagingDone = computeCompletionStatus(obs, "clinical_staging_disease");
                    boolean isTbScreeningDone = computeCompletionStatus(obs, "on_tb_treatment");
                    boolean isArvPrescriptionDone = computeCompletionStatus(obs, "prescribed_regimes");
                    boolean isNextVisitSet = computeCompletionStatus(obs, "next_facility_visit_date");

                    boolean isBaselineInvestigationComplete = computeCompletionStatus(obs, "liver_function_test_conducted") && computeCompletionStatus(obs, "renal_function_test_conducted");


                    checks.add(isFollowupStatusDone);

                    if (isContinuingWithServices(v)) {
                        checks.add(isCounsellingDone);
                        checks.add(isClinicalStagingDone);
                        checks.add(isTbScreeningDone);
                        checks.add(isArvPrescriptionDone);
                        checks.add(isNextVisitSet);

                        if (HfPmtctDao.isEligibleForBaselineInvestigation(baseEntityId) || HfPmtctDao.isEligibleForBaselineInvestigationOnFollowupVisit(baseEntityId)) {
                            checks.add(isBaselineInvestigationComplete);
                        }
                    }


                    if (!checks.contains(false)) {
                        pmtctFollowupVisits.add(v);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

            }
        }

        if (pmtctFollowupVisits.size() > 0) {
            processVisits(pmtctFollowupVisits, visitRepository, visitDetailsRepository);
            for (Visit v : pmtctFollowupVisits) {
                if (isWomanTransferOut(v)) {
                    createHeiTransferOutUpdate(v.getJson());
                }
            }
        }
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isContinuingWithServices(Visit visit) {
        boolean isContinuingWithServices = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("followup_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if ((values.getString(0).equalsIgnoreCase("continuing_with_services"))) {
                        isContinuingWithServices = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isContinuingWithServices;
    }

    public static boolean isWomanTransferOut(Visit visit) {
        boolean isWomanTransferOut = false;
        try {
            JSONObject jsonObject = new JSONObject(visit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");
            int size = obs.length();
            for (int i = 0; i < size; i++) {
                JSONObject checkObj = obs.getJSONObject(i);
                if (checkObj.getString("fieldCode").equalsIgnoreCase("followup_status")) {
                    JSONArray values = checkObj.getJSONArray("values");
                    if (values.getString(0).equalsIgnoreCase("transfer_out")) {
                        isWomanTransferOut = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return isWomanTransferOut;
    }

    private static void createHeiTransferOutUpdate(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        String motherBaseEntityId = jsonObject.getString("baseEntityId");
        List<ChildModel> childModels = HfPncDao.childrenForPncWoman(motherBaseEntityId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        //process if the mother has children
        if (childModels.size() > 0) {
            for (ChildModel childModel : childModels) {
                String childBaseEntityId = childModel.getBaseEntityId();
                AllSharedPreferences sharedPreferences = getAllSharedPreferences();
                Event baseEvent = (Event) new Event()
                        .withBaseEntityId(childBaseEntityId)
                        .withEventDate(new Date())
                        .withEventType(Constants.Events.HEI_FOLLOWUP)
                        .withEntityType(Constants.TableName.HEI_FOLLOWUP)
                        .withFormSubmissionId(UUID.randomUUID().toString())
                        .withDateCreated(new Date());

                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("followup_status")
                                .withValue("transfer_out")
                                .withFieldCode("followup_status")
                                .withFieldType("transfer_out")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("visit_number")
                                .withFieldCode("visit_number")
                                .withValue(String.valueOf(HeiDao.getVisitNumber(childBaseEntityId)))
                                .withFieldType("transfer_out")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                baseEvent.addObs(
                        (new Obs())
                                .withFormSubmissionField("followup_visit_date")
                                .withFieldCode("followup_visit_date")
                                .withValue(sdf.format(new Date()))
                                .withFieldType("followup_visit_date")
                                .withFieldDataType("text")
                                .withParentCode("")
                                .withHumanReadableValues(new ArrayList<>()));
                // tag docs
                org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(sharedPreferences, baseEvent);
                NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
            }
        }

    }

    public static void deleteSavedEvent(AllSharedPreferences allSharedPreferences, String baseEntityId, String eventId, String formSubmissionId, String type) {
        Event event = (Event) new Event()
                .withBaseEntityId(baseEntityId)
                .withEventDate(new Date())
                .withEventType(org.smartregister.chw.anc.util.Constants.EVENT_TYPE.DELETE_EVENT)
                .withLocationId(org.smartregister.chw.anc.util.JsonFormUtils.locationId(allSharedPreferences))
                .withProviderId(allSharedPreferences.fetchRegisteredANM())
                .withEntityType(type)
                .withFormSubmissionId(UUID.randomUUID().toString())
                .withDateCreated(new Date());

        event.addDetails(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_EVENT_ID, eventId);
        event.addDetails(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.DELETE_FORM_SUBMISSION_ID, formSubmissionId);

        try {
            NCUtils.processEvent(event.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(event)));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static void deleteProcessedVisit(String visitID, String baseEntityId) {
        // check if the event
        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();
        org.smartregister.chw.anc.domain.Visit visit = AncLibrary.getInstance().visitRepository().getVisitByVisitId(visitID);
        if (visit == null || !visit.getProcessed()) return;

        Event processedEvent = HomeVisitDao.getEventByFormSubmissionId(visit.getFormSubmissionId());
        if (processedEvent == null) return;

        PmtctVisitUtils.deleteSavedEvent(allSharedPreferences, baseEntityId, processedEvent.getEventId(), processedEvent.getFormSubmissionId(), "event");
        AncLibrary.getInstance().visitRepository().deleteVisit(visitID);
    }


    // Implementation used to save children
    public static void generateAndSaveFormsForEachChild(Map<String, List<JSONObject>> jsonObjectMap, String motherBaseId, String familyBaseEntityId, String dob, String familyName) {

        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();

        JSONArray childFields;
        for (Map.Entry<String, List<JSONObject>> entry : jsonObjectMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                childFields = new JSONArray();
                for (JSONObject jsonObject : entry.getValue()) {
                    try {
                        String replaceString = jsonObject.getString(org.smartregister.chw.anc.util.JsonFormUtils.KEY);

                        JSONObject childField = new JSONObject(jsonObject.toString().replaceAll(replaceString, replaceString.substring(0, replaceString.lastIndexOf("_"))));

                        childFields.put(childField);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                saveChild(childFields, motherBaseId, allSharedPreferences, familyBaseEntityId, dob, familyName);
            }
        }
    }

    public static Map<String, List<JSONObject>> getChildFieldMaps(JSONArray fields) {
        Map<String, List<JSONObject>> jsonObjectMap = new HashMap();

        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject jsonObject = fields.getJSONObject(i);
                String key = jsonObject.getString(org.smartregister.chw.anc.util.JsonFormUtils.KEY);
                String keySplit = key.substring(key.lastIndexOf("_"));
                if (keySplit.matches(".*\\d.*")) {

                    String formattedKey = keySplit.replaceAll("[^\\d.]", "");
                    if (formattedKey.length() < 10)
                        continue;
                    List<JSONObject> jsonObjectList = jsonObjectMap.get(formattedKey);

                    if (jsonObjectList == null)
                        jsonObjectList = new ArrayList<>();

                    jsonObjectList.add(jsonObject);
                    jsonObjectMap.put(formattedKey, jsonObjectList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObjectMap;
    }

    private static void saveChild(JSONArray childFields, String motherBaseId, AllSharedPreferences
            allSharedPreferences, String familyBaseEntityId, String dob, String familyName) {
        String uniqueChildID = AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId();

        if (StringUtils.isNotBlank(uniqueChildID)) {
            String childBaseEntityId = org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString();
            try {

                JSONObject surNameObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(childFields, DBConstants.KEY.SUR_NAME);
                String surName = surNameObject != null ? surNameObject.optString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE) : null;

                String lastName = sameASFamilyNameCheck(childFields) ? familyName : surName;
                JSONObject pncForm = new BaseAncRegisterModel().getFormAsJson(
                        AncLibrary.getInstance().context().applicationContext(),
                        org.smartregister.chw.anc.util.Constants.FORMS.PNC_CHILD_REGISTRATION,
                        childBaseEntityId,
                        allSharedPreferences.getPreference(AllConstants.CURRENT_LOCATION_ID)
                );
                pncForm = org.smartregister.chw.anc.util.JsonFormUtils.populatePNCForm(pncForm, childFields, familyBaseEntityId, motherBaseId, uniqueChildID, dob, lastName);
                processPncChild(childFields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId, uniqueChildID, lastName, dob);
                if (pncForm != null) {
                    saveRegistration(pncForm.toString(), EC_CHILD);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);

        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    private static boolean sameASFamilyNameCheck(JSONArray childFields) {
        if (childFields.length() > 0) {
            JSONObject sameAsFamNameCheck = org.smartregister.util.JsonFormUtils.getFieldJSONObject(childFields, DBConstants.KEY.SAME_AS_FAM_NAME_CHK);
            sameAsFamNameCheck = sameAsFamNameCheck != null ? sameAsFamNameCheck : org.smartregister.util.JsonFormUtils.getFieldJSONObject(childFields, DBConstants.KEY.SAME_AS_FAM_NAME);
            JSONObject sameAsFamNameObject = sameAsFamNameCheck.optJSONArray(DBConstants.KEY.OPTIONS).optJSONObject(0);
            if (sameAsFamNameCheck != null) {
                return sameAsFamNameObject.optBoolean(org.smartregister.chw.anc.util.JsonFormUtils.VALUE);
            }
        }
        return false;
    }

    public static void processPncChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId, String uniqueChildID, String lastName, String dob) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            Map<String, String> identifiers = new HashMap<>();
            identifiers.put(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.OPENSPR_ID, uniqueChildID.replace("-", ""));
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date = formatter.parse(dob);
            pncChild.setLastName(lastName);
            pncChild.setBirthdate(date);
            pncChild.setIdentifiers(identifiers);
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.MOTHER, motherBaseId);

            JSONObject eventJson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            AncLibrary.getInstance().getUniqueIdRepository().close(pncChild.getIdentifier(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.OPENSPR_ID));

            NCUtils.getSyncHelper().addClient(pncChild.getBaseEntityId(), eventJson);

        } catch (Exception e) {
            Timber.e(e);
        }
    }


}
