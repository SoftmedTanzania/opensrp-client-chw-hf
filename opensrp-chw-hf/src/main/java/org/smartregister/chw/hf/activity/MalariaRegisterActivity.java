package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getMalariaConfirmation;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreMalariaRegisterActivity;
import org.smartregister.chw.hf.fragment.MalariaRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class MalariaRegisterActivity extends CoreMalariaRegisterActivity {

    public static void startMalariaRegistrationActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, MalariaRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.malaria.util.Constants.ACTIVITY_PAYLOAD.MALARIA_FORM_NAME, getMalariaConfirmation());
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        activity.startActivity(intent);
    }

    @Override
    protected void registerBottomNavigation() {

        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new MalariaRegisterFragment();
    }

}
