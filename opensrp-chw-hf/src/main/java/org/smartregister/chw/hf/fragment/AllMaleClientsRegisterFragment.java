package org.smartregister.chw.hf.fragment;

import static org.smartregister.chw.hf.utils.HfReferralUtils.REGISTER_TYPE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.smartregister.chw.core.fragment.CoreAllClientsRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.configs.AllClientsRegisterRowOptions;
import org.smartregister.chw.hf.provider.HfAllMaleClientsQueryProvider;
import org.smartregister.chw.hf.provider.HfMaleClientRegisterProvider;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;

public class AllMaleClientsRegisterFragment extends CoreAllClientsRegisterFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        OpdConfiguration opdConfiguration = new OpdConfiguration.Builder(HfAllMaleClientsQueryProvider.class)
                .setBottomNavigationEnabled(true)
                .setOpdRegisterRowOptions(AllClientsRegisterRowOptions.class)
                .build();

        setOpdRegisterQueryProvider(ConfigurationInstancesHelper.newInstance(opdConfiguration.getOpdRegisterQueryProvider()));
        return rootView;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        View dueOnlyLayout = view.findViewById(R.id.due_only_layout);
        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        layoutParams.height = 20;
        toolbar.setLayoutParams(layoutParams);

        dueOnlyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void goToClientDetailActivity(@NonNull CommonPersonObjectClient commonPersonObjectClient) {
        String registerType = commonPersonObjectClient.getDetails().get(REGISTER_TYPE);

        if (registerType != null) {
            switch (registerType) {
                case CoreConstants.REGISTER_TYPE.HIV:
                case CoreConstants.REGISTER_TYPE.HTS:
                    AllClientsUtils.goToHivProfile(this.getActivity(), commonPersonObjectClient);
                    break;
                case CoreConstants.REGISTER_TYPE.TB:
                    AllClientsUtils.goToTbProfile(this.getActivity(), commonPersonObjectClient);
                    break;
                default:
                    AllClientsUtils.goToClientProfile(this.getActivity(), commonPersonObjectClient);
                    break;
            }
        } else {
            AllClientsUtils.goToClientProfile(this.getActivity(), commonPersonObjectClient);
        }
    }

    @Override
    public void initializeAdapter() {
        HfMaleClientRegisterProvider maleClientRegisterProvider = new HfMaleClientRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        CommonRepository commonRepository = context().commonrepository(this.tablename);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, maleClientRegisterProvider, commonRepository);
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
}

