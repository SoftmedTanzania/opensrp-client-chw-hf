package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.BaseReferralRegisterFragmentContract;
import org.smartregister.chw.core.presenter.BaseReferralFragmentPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.HashSet;

public class ReferralFragmentPresenter extends BaseReferralFragmentPresenter {
    protected BaseReferralRegisterFragmentContract.View view;

    public ReferralFragmentPresenter(BaseReferralRegisterFragmentContract.View view, BaseReferralRegisterFragmentContract.Model model) {
        super(view);
        this.model = model;
        this.view = view;
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String countSelect = model.countSelect(CoreConstants.TABLE_NAME.TASK, mainCondition);
        String mainSelect = model.mainSelect(CoreConstants.TABLE_NAME.TASK, CoreConstants.TABLE_NAME.FAMILY_MEMBER, mainCondition);

        view.initializeQueryParams(CoreConstants.TABLE_NAME.FAMILY_MEMBER, countSelect, mainSelect);
        view.initializeAdapter(new HashSet<>(), CoreConstants.TABLE_NAME.TASK);

        view.countExecute();
        view.filterandSortInInitializeQueries();
    }

    @Override
    public void fetchClient() {
        interactor.getClientDetails(getBaseEntityId(), this, getTaskFocus());
    }

    @Override
    public void clientDetails(CommonPersonObjectClient client) {
        view.setClient(client);
    }
}
