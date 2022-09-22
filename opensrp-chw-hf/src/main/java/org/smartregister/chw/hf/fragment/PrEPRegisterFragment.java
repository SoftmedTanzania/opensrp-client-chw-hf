package org.smartregister.chw.hf.fragment;

import org.smartregister.chw.core.fragment.CoreKvpRegisterFragment;
import org.smartregister.chw.core.model.CoreKvpRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.PrEPRegisterFragmentPresenter;

public class PrEPRegisterFragment extends CoreKvpRegisterFragment {

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new PrEPRegisterFragmentPresenter(this, new CoreKvpRegisterFragmentModel(), null);
    }

}
