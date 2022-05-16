package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDStage4ActivityInteractor;
import org.smartregister.chw.ld.activity.BaseLDVisitActivity;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.presenter.BaseLDVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.text.MessageFormat;

public class LDStage4Activity extends BaseLDVisitActivity {

    public static void startStage4Activity(Activity activity, String baseEntityID, Boolean editMode) {

        Intent intent = new Intent(activity, LDStage4Activity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);

        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);

    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseLDVisitPresenter(memberObject, this, new LDStage4ActivityInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(R.string.ld_stage_4)));
    }
}
