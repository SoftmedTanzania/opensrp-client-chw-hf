package org.smartregister.chw.hf.fragment;

import android.os.Bundle;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.activity.HivstResultViewActivity;
import org.smartregister.chw.hivst.fragment.BaseHivstResultViewFragment;
import org.smartregister.chw.hivst.util.Constants;
import org.smartregister.chw.hivst.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.Utils;

public class HivstResultsViewFragment extends BaseHivstResultViewFragment {


    public static HivstResultsViewFragment newInstance(String baseEntityId) {
        HivstResultsViewFragment hivstResultsViewFragment = new HivstResultsViewFragment();
        Bundle b = new Bundle();
        b.putString(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        hivstResultsViewFragment.setArguments(b);
        return hivstResultsViewFragment;
    }

    @Override
    public void openResultsForm(CommonPersonObjectClient client) {
        String baseEntityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String entityId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_ID, false);
        String kitFor = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.KIT_FOR, false);
        String clientTestingApproach = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.CLIENT_TESTING_APPROACH, false);
        try {
            JSONObject jsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets(requireContext(), Constants.FORMS.HIVST_RECORD_RESULTS);
            JSONObject global = jsonObject.getJSONObject("global");
            global.putOpt("kit_for", kitFor);
            global.putOpt("client_testing_approach", clientTestingApproach);
            HivstResultViewActivity.startResultsForm(getContext(), jsonObject.toString(), baseEntityId, entityId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}