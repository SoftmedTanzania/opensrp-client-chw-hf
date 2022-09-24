package org.smartregister.chw.hf.listener;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.smartregister.chw.hf.activity.KvpBioMedicalServiceActivity;
import org.smartregister.chw.kvp.domain.ServiceCard;
import org.smartregister.chw.kvp.handlers.BaseServiceActionHandler;
import org.smartregister.chw.kvp.util.Constants;

public class KvpServiceActionHandler extends BaseServiceActionHandler {
    @Override
    protected void startVisitActivity(Context context, ServiceCard serviceCard, String baseEntityId) {
        boolean isEditMode = isEditMode(serviceCard.getServiceEventName(), baseEntityId);
        if (serviceCard.getServiceId().equals(Constants.SERVICES.KVP_BIO_MEDICAL)) {
            KvpBioMedicalServiceActivity.startKvpBioMedicalServiceActivity((Activity) context, baseEntityId, isEditMode);
            return;
        }
        Toast.makeText(context, serviceCard.getServiceName() + "Loading activity... ", Toast.LENGTH_SHORT).show();
    }
}
