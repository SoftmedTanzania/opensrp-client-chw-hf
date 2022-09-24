package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.kvp.KvpClientStatusActionHelper;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class KvpBioMedicalVisitInteractor extends BaseKvpVisitInteractor {

    @Override
    protected void populateActionList(BaseKvpVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            try {
                evaluateClientStatus(details);
            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateClientStatus(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        KvpClientStatusActionHelper actionHelper = new KvpClientStatusActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.client_status))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.KVP_BIO_MEDICAL_SERVICE_FORMS.KVP_CLIENT_STATUS)
                .build();

        actionList.put(context.getString(R.string.client_status), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.KVP_BIO_MEDICAL_SERVICE_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.KVP_FOLLOW_UP;
    }
}
