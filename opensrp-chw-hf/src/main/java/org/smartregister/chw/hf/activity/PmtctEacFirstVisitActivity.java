package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.EacFirstVisitInteractor;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.presenter.BasePmtctHomeVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;

import java.text.MessageFormat;


public class PmtctEacFirstVisitActivity extends PmtctFollowupVisitActivity{
    public static void startEacActivity(Activity activity, String baseEntityID,Boolean isEditMode) {
        Intent intent = new Intent(activity, PmtctEacFirstVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID,baseEntityID );
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivity(intent);
    }
    @Override
    protected void registerPresenter() {
        presenter = new BasePmtctHomeVisitPresenter(memberObject,this,new EacFirstVisitInteractor());
    }
    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(R.string.record_eac_first_visit)));
    }
}
