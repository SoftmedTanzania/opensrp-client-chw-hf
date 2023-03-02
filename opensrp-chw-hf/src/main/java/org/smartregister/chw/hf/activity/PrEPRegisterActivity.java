package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.core.activity.CoreKvpRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.fragment.PrEPRegisterFragment;
import org.smartregister.chw.kvp.util.Constants;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class PrEPRegisterActivity extends CoreKvpRegisterActivity {

    public static void startMe(Activity activity, String baseEntityId, String gender, int age) {
        Intent intent = new Intent(activity, PrEPRegisterActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.KVP_FORM_NAME, Constants.FORMS.PrEP_REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.GENDER, gender);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.AGE, age);

        activity.startActivity(intent);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PrEPRegisterFragment();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = FacilityMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.PrEP);
        }
    }
}
