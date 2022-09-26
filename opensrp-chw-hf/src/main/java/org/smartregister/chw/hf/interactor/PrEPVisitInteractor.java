package org.smartregister.chw.hf.interactor;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.PrEP.PrEPVisitTypeActionHelper;
import org.smartregister.chw.kvp.contract.BaseKvpVisitContract;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.interactor.BaseKvpVisitInteractor;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;
import org.smartregister.chw.kvp.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PrEPVisitInteractor extends BaseKvpVisitInteractor {

    String visitType;

    public PrEPVisitInteractor(String visitType) {
        this.visitType = visitType;
    }

    @Override
    protected String getCurrentVisitType() {
        if (StringUtils.isNotBlank(visitType)) {
            return visitType;
        }
        return super.getCurrentVisitType();
    }

    @Override
    protected void populateActionList(BaseKvpVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            try {
                evaluateVisitType(details);
            } catch (BaseKvpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateVisitType(Map<String, List<VisitDetail>> details) throws BaseKvpVisitAction.ValidationException {

        PrEPVisitTypeActionHelper actionHelper = new PrEPVisitTypeActionHelper();
        BaseKvpVisitAction action = getBuilder(context.getString(R.string.prep_visit_type))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.PrEP_FOLLOWUP_FORMS.VISIT_TYPE)
                .build();

        actionList.put(context.getString(R.string.prep_visit_type), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.PrEP_FOLLOWUP_VISIT;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.PrEP_FOLLOWUP;
    }
}
