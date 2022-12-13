package org.smartregister.chw.hf.activity;

import static com.vijay.jsonwizard.utils.FormUtils.fields;
import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;
import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;
import static org.smartregister.chw.hf.utils.Constants.Events.PARTNER_REGISTRATION_EVENT;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.EXISTING_PARTNER_REQUEST_CODE;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.INTENT_BASE_ENTITY_ID;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.NEW_PARTNER_REQUEST_CODE;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.PARTNER_BASE_ENTITY_ID;
import static org.smartregister.chw.hf.utils.JsonFormUtils.METADATA;
import static org.smartregister.chw.hf.utils.JsonFormUtils.SYNC_LOCATION_ID;
import static org.smartregister.chw.pmtct.util.NCUtils.getClientProcessorForJava;
import static org.smartregister.chw.pmtct.util.NCUtils.getSyncHelper;
import static org.smartregister.family.util.JsonFormUtils.STEP2;
import static org.smartregister.util.JsonFormUtils.ENCOUNTER_LOCATION;
import static org.smartregister.util.JsonFormUtils.STEP1;
import static org.smartregister.util.Utils.getAllSharedPreferences;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.dao.LocationsDao;
import org.smartregister.domain.UniqueId;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class PartnerRegistrationActivity extends SecuredActivity implements View.OnClickListener {

    private String clientBaseEntityId;


    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_partner_registration);
        this.clientBaseEntityId = getIntent().getStringExtra(INTENT_BASE_ENTITY_ID);
        setupView();
    }


    @Override
    protected void onResumption() {
        //overridden
    }

    public void setupView() {
        ImageView closeImageView = findViewById(R.id.close);
        ConstraintLayout newClientRegistrationView = findViewById(R.id.new_client_registration);
        ConstraintLayout existingClientRegistrationView = findViewById(R.id.existing_client_registration);

        newClientRegistrationView.setOnClickListener(this);
        existingClientRegistrationView.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close) {
            finish();
        } else if (id == R.id.new_client_registration) {
            startPartnerRegistration();
        } else if (id == R.id.existing_client_registration) {
            searchForPartner();
        }
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return OpdLibrary.getInstance().getUniqueIdRepository();
    }

    private void startPartnerRegistration() {

        UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
        final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
        if (StringUtils.isBlank(entityId)) {
            Toast.makeText(this, R.string.no_unique_id, Toast.LENGTH_SHORT).show();
        } else {
            JSONObject jsonObject = getFormAsJson(org.smartregister.chw.hf.utils.Constants.JsonForm.getPartnerRegistrationForm(), entityId, null);
            startFormActivity(jsonObject);
        }
    }

    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) {
        try {
            JSONObject form;

            form = (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, formName);


            if (form == null) {
                return null;
            }
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(form, STEP1, SYNC_LOCATION_ID);
            CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                    Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), syncLocationField);
            form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

            String newEntityId = entityId;
            if (StringUtils.isNotBlank(entityId)) {
                newEntityId = entityId.replace("-", "");
            }

            JSONObject stepOneUniqueId = getFieldJSONObject(fields(form, STEP1), Constants.JSON_FORM_KEY.UNIQUE_ID);

            if (stepOneUniqueId != null) {
                stepOneUniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                stepOneUniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, newEntityId + "_Family");
            }

            JSONObject stepTwoUniqueId = getFieldJSONObject(fields(form, STEP2), Constants.JSON_FORM_KEY.UNIQUE_ID);
            if (stepTwoUniqueId != null) {
                stepTwoUniqueId.remove(org.smartregister.family.util.JsonFormUtils.VALUE);
                stepTwoUniqueId.put(org.smartregister.family.util.JsonFormUtils.VALUE, newEntityId);
            }

            org.smartregister.family.util.JsonFormUtils.addLocHierarchyQuestions(form);
            return form;

        } catch (Exception e) {
            Timber.e(e, "Error loading All Client registration form");
        }
        return null;
    }

    public void startFormActivity(JSONObject jsonObject) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(OpdConstants.JSON_FORM_EXTRA.JSON, jsonObject.toString());

        Form form = new Form();
        form.setName(getString(org.smartregister.chw.core.R.string.client_registration));
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setPreviousLabel(getResources().getString(org.smartregister.chw.core.R.string.back));
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, NEW_PARTNER_REQUEST_CODE);
    }

    private void searchForPartner() {
        startActivityForResult(new Intent(this, AllMaleClientsActivity.class), EXISTING_PARTNER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXISTING_PARTNER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String partner_id = data.getStringExtra(INTENT_BASE_ENTITY_ID);
            savePartnerDetails(partner_id, clientBaseEntityId);
            finish();

        }
        if (requestCode == NEW_PARTNER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
            Timber.d("JSONResult : %s", jsonString);

            JSONObject form;
            try {
                form = new JSONObject(jsonString);

                String encounterType;
                encounterType = form.getString(OpdJsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(CoreConstants.EventType.FAMILY_REGISTRATION)) {
                    RegisterParams registerParam = new RegisterParams();
                    registerParam.setEditMode(false);
                    registerParam.setFormTag(OpdJsonFormUtils.formTag(OpdUtils.context().allSharedPreferences()));
                    saveForm(jsonString, registerParam);
                    finish();

                }
            } catch (JSONException e) {
                Timber.e(e);
            }

        }

    }

    private void saveForm(String jsonString, RegisterParams registerParam) {
        try {
            List<OpdEventClient> opdEventClientList = processRegistration(jsonString);
            if (opdEventClientList == null || opdEventClientList.isEmpty()) {
                return;
            }
            saveRegistration(opdEventClientList, jsonString, registerParam);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public List<OpdEventClient> processRegistration(String jsonString) {

        List<OpdEventClient> opdEventClients = AllClientsUtils.getOpdEventClients(jsonString);
        try {
            JSONObject syncLocationField = CoreJsonFormUtils.getJsonField(new JSONObject(jsonString), STEP1, SYNC_LOCATION_ID);
            for (OpdEventClient opdEventClient : opdEventClients) {
                opdEventClient.getEvent().setLocationId(CoreJsonFormUtils.getSyncLocationUUIDFromDropdown(syncLocationField));
            }
        } catch (JSONException e) {
            Timber.e(e, "Error retrieving Sync location Field");
        }
        return opdEventClients;
    }

    public void saveRegistration(@NonNull List<OpdEventClient> allClientEventList, @NonNull String jsonString,
                                 @NonNull RegisterParams params) {
        try {
            List<String> currentFormSubmissionIds = new ArrayList<>();

            for (int i = 0; i < allClientEventList.size(); i++) {
                try {

                    OpdEventClient allClientEvent = allClientEventList.get(i);
                    Client baseClient = allClientEvent.getClient();
                    Event baseEvent = allClientEvent.getEvent();
                    addClient(params, baseClient);
                    addEvent(params, currentFormSubmissionIds, baseEvent);
                    updateOpenSRPId(jsonString, params, baseClient);
                    addImageLocation(jsonString, baseClient, baseEvent);
                    if (baseEvent.getEventType().equalsIgnoreCase("Family Member Registration")) {
                        savePartnerDetails(baseEvent.getBaseEntityId(), clientBaseEntityId);
                    }
                } catch (Exception e) {
                    Timber.e(e, "ChwAllClientRegisterInteractor --> saveRegistration");
                }
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            getClientProcessorForJava().processClient(getSyncHelper().getEvents(currentFormSubmissionIds));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e, "OpdRegisterInteractor --> saveRegistration");
        }
    }

    private void addClient(@NonNull RegisterParams params, Client baseClient) throws JSONException {
        JSONObject clientJson = new JSONObject(OpdJsonFormUtils.gson.toJson(baseClient));
        if (params.isEditMode()) {
            try {
                org.smartregister.family.util.JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
            } catch (Exception e) {
                Timber.e(e, "ChwAllClientRegisterInteractor --> mergeAndSaveClient");
            }
        } else {
            getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
        }
    }

    private void addImageLocation(String jsonString, Client baseClient, Event baseEvent) {
        if (baseClient != null || baseEvent != null) {
            String imageLocation = OpdJsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
            if (StringUtils.isNotBlank(imageLocation)) {
                org.smartregister.family.util.JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }
        }
    }

    private void updateOpenSRPId(String jsonString, RegisterParams params, Client baseClient) {
        if (params.isEditMode()) {
            // UnAssign current OpenSRP ID
            if (baseClient != null) {
                String newOpenSrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey).replace("-", "");
                String currentOpenSrpId = org.smartregister.family.util.JsonFormUtils.getString(jsonString, org.smartregister.family.util.JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
                if (!newOpenSrpId.equals(currentOpenSrpId)) {
                    //OpenSRP ID was changed
                    getUniqueIdRepository().open(currentOpenSrpId);
                }
            }

        } else {
            if (baseClient != null) {
                String openSrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                if (StringUtils.isNotBlank(openSrpId) && !openSrpId.contains(Constants.IDENTIFIER.FAMILY_SUFFIX)) {
                    //Mark OpenSRP ID as used
                    getUniqueIdRepository().close(openSrpId);
                }
            }
        }
    }

    private void addEvent(RegisterParams params, List<String> currentFormSubmissionIds, Event baseEvent) throws JSONException {
        if (baseEvent != null) {
            JSONObject eventJson = new JSONObject(OpdJsonFormUtils.gson.toJson(baseEvent));
            getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson, params.getStatus());
            currentFormSubmissionIds.add(eventJson.getString(EventClientRepository.event_column.formSubmissionId.toString()));
        }
    }

    protected void savePartnerDetails(String partnerBaseEntityId, String clientBaseEntityId) {
        AllSharedPreferences sharedPreferences = getAllSharedPreferences();
        Event baseEvent = (Event) new Event()
                .withBaseEntityId(clientBaseEntityId)
                .withEventDate(new Date())
                .withEventType(PARTNER_REGISTRATION_EVENT)
                .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                .withEntityType(CoreConstants.TABLE_NAME.ANC_MEMBER)
                .withProviderId(sharedPreferences.fetchRegisteredANM())
                .withLocationId(sharedPreferences.fetchDefaultLocalityId(sharedPreferences.fetchRegisteredANM()))
                .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                .withClientDatabaseVersion(BuildConfig.DATABASE_VERSION)
                .withClientApplicationVersion(BuildConfig.VERSION_CODE)
                .withDateCreated(new Date());

        baseEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(PARTNER_BASE_ENTITY_ID)
                        .withValue(partnerBaseEntityId)
                        .withFieldCode(PARTNER_BASE_ENTITY_ID)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        // tag docs
        org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), baseEvent);
        try {
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}