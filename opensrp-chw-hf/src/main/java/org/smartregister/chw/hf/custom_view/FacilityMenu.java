package org.smartregister.chw.hf.custom_view;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.hf.presenter.HfNavigationPresenter;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import java.lang.ref.WeakReference;

import timber.log.Timber;

public class FacilityMenu extends NavigationMenu {

    public static NavigationMenu getInstance(Activity activity, View parentView, Toolbar myToolbar) {
        try {
            SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(instance);
            activityWeakReference = new WeakReference<>(activity);
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (instance == null) {
                    instance = new FacilityMenu();
                }
                if (!(instance instanceof FacilityMenu)) {
                    instance = new FacilityMenu();
                }

                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(instance);
                ((FacilityMenu) instance).init(activity, parentView, myToolbar);
                return instance;
            } else {
                return null;
            }
        } catch (OutOfMemoryError e) {
            Timber.e(e);
        }

        return null;
    }

    @Override
    protected void init(Activity activity, View myParentView, Toolbar myToolbar) {
        try {
            setParentView(activity, parentView);
            toolbar = myToolbar;
            parentView = myParentView;
            mPresenter = new HfNavigationPresenter(application, this, modelFlavor);
            prepareViews(activity);
            mPresenter.updateTableMap(menuFlavor.getTableMapValues());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}
