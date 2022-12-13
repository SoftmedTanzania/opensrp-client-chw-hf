package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.listener.CoreBottomNavigationListener;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.fragment.FamilyRegisterFragment;
import org.smartregister.chw.hf.listener.HfFamilyBottomNavListener;
import org.smartregister.chw.hf.model.FamilyRegisterModel;
import org.smartregister.family.presenter.BaseFamilyRegisterPresenter;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class FamilyRegisterActivity extends CoreFamilyRegisterActivity {

    public static void registerBottomNavigation(BottomNavigationHelper bottomNavigationHelper,
                                                BottomNavigationView bottomNavigationView, Activity activity) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new CoreBottomNavigationListener(activity));
        }

        if (!BuildConfig.SUPPORT_QR)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);

        if(!BuildConfig.SUPPORT_REPORT)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);

    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);

        if(!BuildConfig.SUPPORT_REPORT)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);

        bottomNavigationView.setOnNavigationItemSelectedListener(new HfFamilyBottomNavListener(this, bottomNavigationView));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacilityMenu.getInstance(this, null, null);
        HealthFacilityApplication.getInstance().notifyAppContextChange(); // initialize the language (bug in translation)

        action = getIntent().getStringExtra(CoreConstants.ACTIVITY_PAYLOAD.ACTION);
        if (action != null && action.equals(CoreConstants.ACTION.START_REGISTRATION)) {
            startRegistration();
        }
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FamilyRegisterFragment();
    }

    @Override
    protected void initializePresenter() {
        presenter = new BaseFamilyRegisterPresenter(this, new FamilyRegisterModel());
    }
}
