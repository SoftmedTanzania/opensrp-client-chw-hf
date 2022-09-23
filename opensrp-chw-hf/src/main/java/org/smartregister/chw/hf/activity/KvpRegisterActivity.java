package org.smartregister.chw.hf.activity;

import org.smartregister.chw.core.activity.CoreKvpRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.fragment.KvpRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class KvpRegisterActivity extends CoreKvpRegisterActivity {

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new KvpRegisterFragment();
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.KVP);
        }
    }
}
