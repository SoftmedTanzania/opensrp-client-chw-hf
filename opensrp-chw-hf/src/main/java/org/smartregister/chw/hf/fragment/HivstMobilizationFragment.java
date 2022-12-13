package org.smartregister.chw.hf.fragment;

import android.view.View;

import org.json.JSONObject;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.chw.hivst.fragment.BaseHivstMobilizationRegisterFragment;

import timber.log.Timber;

public class HivstMobilizationFragment extends BaseHivstMobilizationRegisterFragment {

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        try {
            NavigationMenu.getInstance(getActivity(), null, toolbar);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu.getInstance(getActivity(), null, toolbar);
    }


    @Override
    protected void startForm(JSONObject form) {
        requireActivity().startActivityForResult(org.smartregister.chw.core.utils.FormUtils.getStartFormActivity(form, requireActivity().getString(R.string.hivst_mobilization), requireActivity()), JsonFormUtils.REQUEST_CODE_GET_JSON);
    }
}
