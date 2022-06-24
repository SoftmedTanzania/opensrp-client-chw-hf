package org.smartregister.chw.hf.activity;

import org.smartregister.chw.core.activity.BaseReferralRegister;
import org.smartregister.chw.core.presenter.BaseReferralPresenter;
import org.smartregister.chw.hf.fragment.ReferralRegisterFragment;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class ReferralRegisterActivity extends BaseReferralRegister {

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initializePresenter() {
        presenter = new BaseReferralPresenter();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new ReferralRegisterFragment();
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);

    }

}
