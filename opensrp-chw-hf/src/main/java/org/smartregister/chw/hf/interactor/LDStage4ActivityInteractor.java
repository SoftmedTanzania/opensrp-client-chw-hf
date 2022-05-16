package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.util.JsonFormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class LDStage4ActivityInteractor extends BaseLDVisitInteractor {

    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    protected Context context;
    private MemberObject memberObject;

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        this.memberObject = memberObject;

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {
                evaluateMotherStatus();
                evaluateNewBorn();
            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }


    @Override
    protected String getEncounterType() {
        return "LD Stage 4";
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);
        try {
            LDVisitUtils.processVisits(memberID);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void evaluateMotherStatus() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.mother_status);
        MotherStatusActionHelper actionHelper = new MotherStatusActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDStage4.getLdMotherStatus())
                .build();
        actionList.put(title, action);
    }

    private void evaluateNewBorn() throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.new_born);
        NewBornActionHelper actionHelper = new NewBornActionHelper();
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(false)
                .withHelper(actionHelper)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withFormName(Constants.JsonForm.LDStage4.getLdNewBorn())
                .build();
        actionList.put(title, action);
    }


    public BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }


    private static class NewBornActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String newbornStatus;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            newbornStatus = JsonFormUtils.getFieldValue(jsonPayload, "newborn_status");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(newbornStatus)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //implement
        }
    }

    private static class MotherStatusActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

        private String status;
        private Context context;

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            status = JsonFormUtils.getFieldValue(jsonPayload, "status");
        }

        @Override
        public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String jsonPayload) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseLDVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(status)) {
                return BaseLDVisitAction.Status.COMPLETED;
            } else {
                return BaseLDVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseLDVisitAction ldVisitAction) {
            //implement
        }
    }
}
