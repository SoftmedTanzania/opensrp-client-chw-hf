package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.JsonForm.HIV_REGISTRATION;

import android.content.Context;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.activity.CoreFamilyOtherMemberProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.custom_views.CoreFamilyMemberFloatingMenu;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.utils.BAJsonFormUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.chw.hf.dao.HfHivDao;
import org.smartregister.chw.hf.fragment.FamilyOtherMemberProfileFragment;
import org.smartregister.chw.hf.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LFTUFormUtils;
import org.smartregister.chw.hiv.dao.HivIndexDao;
import org.smartregister.chw.hivst.dao.HivstDao;
import org.smartregister.chw.kvp.dao.KvpDao;
import org.smartregister.chw.malaria.dao.MalariaDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.fragment.BaseFamilyOtherMemberProfileFragment;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.DBConstants;
import org.smartregister.view.contract.BaseProfileContract;

import timber.log.Timber;

public class FamilyOtherMemberProfileActivity extends CoreFamilyOtherMemberProfileActivity {
    private FamilyMemberFloatingMenu familyFloatingMenu;
    private BAJsonFormUtils baJsonFormUtils;
    private String dob;
    private String gender;
    private RelativeLayout layoutFamilyHasRow;

    @Override
    protected void onCreation() {
        super.onCreation();
        baJsonFormUtils = new BAJsonFormUtils(HealthFacilityApplication.getInstance());
        dob = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false);
        gender = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.GENDER, false);
        setIndependentClient(false);
        String passedTitle = getIntent().getStringExtra(CoreConstants.INTENT_KEY.TOOLBAR_TITLE);
        if (!StringUtils.isBlank(passedTitle)) {
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            toolbarTitle.setText(passedTitle);
        } else {
            updateToolbarTitle(this, R.id.toolbar_title, familyName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setupMenuOptions(menu);
        return true;
    }

    @Override
    protected void startAncRegister() {
        //TODO implement start anc register for HF
    }

    @Override
    protected void startPncRegister() {
        PncRegisterActivity.startPncRegistrationActivity(FamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                CoreConstants.JSON_FORM.getPregnancyOutcome(), null, familyBaseEntityId, familyName, null, false);
    }

    @Override
    protected void startFpRegister() {
        FpRegisterActivity.startFpRegistrationActivity(this, baseEntityId, dob, CoreConstants.JSON_FORM.getFpRegistrationForm(gender), FamilyPlanningConstants.ActivityPayload.REGISTRATION_PAYLOAD_TYPE);
    }

    @Override
    protected void startFpChangeMethod() {
        FpRegisterActivity.startFpRegistrationActivity(this, baseEntityId, dob, CoreConstants.JSON_FORM.getFpChangeMethodForm(gender), FamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    protected void startMalariaRegister() {
        //TODO implement start malaria register for HF
    }

    @Override
    protected void startHivRegister() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, baseEntityId, HIV_REGISTRATION, (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, HIV_REGISTRATION).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startTbRegister() {
        try {
            TbRegisterActivity.startTbFormActivity(this, baseEntityId, CoreConstants.JSON_FORM.getTbRegistration(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getTbRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startMalariaFollowUpVisit() {
        // TODO -> Implement for HF
    }

    @Override
    protected void startHfMalariaFollowupForm() {
        MalariaFollowUpVisitActivityHelper.startMalariaFollowUpActivity(this, baseEntityId);
    }

    @Override
    protected void startPmtctRegisration() {
        //Do nothing - not required here
    }

    @Override
    protected void startLDRegistration() {
        try {
            LDRegisterActivity.startLDRegistrationActivity(FamilyOtherMemberProfileActivity.this, baseEntityId);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void startHivstRegistration() {
        String gender = Utils.getValue(commonPersonObject.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.GENDER, false);
        HivstRegisterActivity.startHivstRegistrationActivity(FamilyOtherMemberProfileActivity.this, baseEntityId, gender);
    }

    @Override
    protected void startKvpPrEPRegistration() {
        //do nothing--> this is for chw
    }

    @Override
    protected void startKvpRegistration() {
        String gender = AllClientsUtils.getClientGender(baseEntityId);
        String dob = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false);
        int age = Utils.getAgeFromDate(dob);
        if (gender.equalsIgnoreCase(Constants.GENDER.MALE)) {
            KvpRegisterActivity.startKvpScreeningMale(FamilyOtherMemberProfileActivity.this, baseEntityId, gender, age);
        }
        if (gender.equalsIgnoreCase(Constants.GENDER.FEMALE)) {
            KvpRegisterActivity.startKvpScreeningFemale(FamilyOtherMemberProfileActivity.this, baseEntityId, gender, age);
        }
    }

    @Override
    protected void startPrEPRegistration() {
        String gender = AllClientsUtils.getClientGender(baseEntityId);
        String dob = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false);
        int age = Utils.getAgeFromDate(dob);
        PrEPRegisterActivity.startMe(this, baseEntityId, gender, age);
    }

    @Override
    protected void startAgywScreening() {
        //do nothing
    }

    @Override
    protected void setIndependentClient(boolean b) {
        this.isIndependent = false;
    }

    @Override
    protected void removeIndividualProfile() {
        Timber.d("Remove member action is not required in HF");
    }

    @Override
    public void setFamilyServiceStatus(String status) {
        super.setFamilyServiceStatus(status);
        layoutFamilyHasRow.setVisibility(View.GONE);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        layoutFamilyHasRow = this.findViewById(org.smartregister.chw.core.R.id.family_has_row);
    }

    @Override
    protected void startEditMemberJsonForm(Integer title_resource, CommonPersonObjectClient client) {
        JSONObject form;
        if (title_resource != null) {
            form = baJsonFormUtils.getAutoJsonEditMemberFormString(getResources().getString(title_resource), CoreConstants.JSON_FORM.getFamilyMemberRegister(),
                    this, client, Utils.metadata().familyMemberRegister.updateEventType, familyName, commonPersonObject.getCaseId().equalsIgnoreCase(primaryCaregiver));
        } else {
            form = baJsonFormUtils.getAutoJsonEditMemberFormString(null, CoreConstants.JSON_FORM.getFamilyMemberRegister(),
                    this, client, Utils.metadata().familyMemberRegister.updateEventType, familyName, commonPersonObject.getCaseId().equalsIgnoreCase(primaryCaregiver));
        }
        try {
            startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected BaseProfileContract.Presenter getFamilyOtherMemberActivityPresenter(
            String familyBaseEntityId, String baseEntityId, String familyHead, String primaryCaregiver, String villageTown, String familyName) {
        return new FamilyOtherMemberActivityPresenter(this, new BaseFamilyOtherMemberProfileActivityModel(),
                null, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }

    @Override
    protected CoreFamilyMemberFloatingMenu getFamilyMemberFloatingMenu() {
        if (familyFloatingMenu == null) {
            prepareFab();
        }
        return familyFloatingMenu;
    }

    @Override
    protected Context getFamilyOtherMemberProfileActivity() {
        return FamilyOtherMemberProfileActivity.this;
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivity() {
        return FamilyProfileActivity.class;
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        super.updateHasPhone(hasPhone);
    }

    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        onClickFloatingMenu = viewId -> {
            if (viewId == R.id.call_layout) {
                FamilyCallDialogFragment.launchDialog(this, familyBaseEntityId);
            }
            if (viewId == R.id.refer_to_facility_layout) {
                String gender = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.GENDER, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    LFTUFormUtils.startLTFUReferral(this, baseEntityId, gender, Utils.getAgeFromDate(dob));
                }
            }
        };
    }

    @Override
    protected BaseFamilyOtherMemberProfileFragment getFamilyOtherMemberProfileFragment() {
        return FamilyOtherMemberProfileFragment.newInstance(getIntent().getExtras());
    }

    private void prepareFab() {
        familyFloatingMenu = new FamilyMemberFloatingMenu(this);
    }

    private void setupMenuOptions(Menu menu) {
        String gender = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.GENDER, false);
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_anc_registration).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(false);
        menu.findItem(R.id.action_tb_registration).setVisible(false);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);

        if (HealthFacilityApplication.getApplicationFlavor().hasMalaria())
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(!MalariaDao.isRegisteredForMalaria(baseEntityId));

        if (isOfReproductiveAge(commonPersonObject, gender)) {
            if (gender.equalsIgnoreCase("female") && !AncDao.isANCMember(baseEntityId)) {
                menu.findItem(R.id.action_pregnancy_confirmation).setVisible(true);
                menu.findItem(R.id.action_pregnancy_out_come).setVisible(true);
                menu.findItem(R.id.action_pmtct_register).setVisible(true);
            }
            menu.findItem(R.id.action_fp_change).setVisible(false);
            menu.findItem(R.id.action_fp_initiation).setVisible(false);
        }

        if (HealthFacilityApplication.getApplicationFlavor().hasHivst()) {
            String dob = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false);
            int age = Utils.getAgeFromDate(dob);
            menu.findItem(R.id.action_hivst_registration).setVisible(!HivstDao.isRegisteredForHivst(baseEntityId) && age >= 15);
        }

        if (HealthFacilityApplication.getApplicationFlavor().hasKvpPrEP()) {
            String dob = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false);
            int age = Utils.getAgeFromDate(dob);
            menu.findItem(R.id.action_kvp_registration).setVisible(!KvpDao.isRegisteredForKvp(baseEntityId) && age >= 15);
        }

        if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH) {
            menu.findItem(R.id.action_hiv_registration).setVisible(!(HfHivDao.isHivMember(baseEntityId) || HivIndexDao.isRegisteredIndex(baseEntityId)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == org.smartregister.chw.core.R.id.action_pregnancy_confirmation) {
            startPregnancyConfirmation();
            return true;
        } else if (i == org.smartregister.chw.core.R.id.action_malaria_diagnosis) {
            startHfMalariaFollowupForm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void startPregnancyConfirmation() {
        AncRegisterActivity.startAncRegistrationActivity(FamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                CoreConstants.JSON_FORM.ANC_PREGNANCY_CONFIRMATION, null, familyBaseEntityId, familyName);
    }

    private boolean isOfReproductiveAge(CommonPersonObjectClient commonPersonObject, String gender) {
        if (gender.equalsIgnoreCase("Female")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 10, 49);
        } else if (gender.equalsIgnoreCase("Male")) {
            return Utils.isMemberOfReproductiveAge(commonPersonObject, 15, 49);
        } else {
            return false;
        }
    }
}
