package org.smartregister.chw.hf.interactor;

import static org.smartregister.chw.anc.util.VisitUtils.getVisitDetailsOnly;
import static org.smartregister.chw.anc.util.VisitUtils.getVisitGroups;
import static org.smartregister.chw.anc.util.VisitUtils.getVisitsOnly;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.CoreBaseAncMedicalHistoryInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AncRegistrationDetailsInteractor extends CoreBaseAncMedicalHistoryInteractor {
    public static List<Visit> getVisits(String memberID, String... eventTypes) {

        List<Visit> visits = new ArrayList<>();
        if (eventTypes != null && eventTypes.length > 0) {
            for (String eventType : eventTypes) {
                List<Visit> visit = getVisitsOnly(memberID, eventType);
                visits.addAll(visit);
            }
        } else {
            getVisitsOnly(memberID, CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION);
        }

        int x = 0;
        while (visits.size() > x) {
            Visit visit = visits.get(x);
            List<VisitDetail> detailList = getVisitDetailsOnly(visit.getVisitId());
            visits.get(x).setVisitDetails(getVisitGroups(detailList));
            x++;
        }

        return visits;
    }

    @Override
    public void getMemberHistory(final String memberID, final Context context, final BaseAncMedicalHistoryContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {

            String[] eventTypes = new String[2];
            eventTypes[0] = CoreConstants.EventType.ANC_PREGNANCY_CONFIRMATION;
            eventTypes[1] = CoreConstants.EventType.ANC_FOLLOWUP_CLIENT_REGISTRATION;
            List<Visit> visits = getVisits(memberID, eventTypes);
            final List<Visit> all_visits = new ArrayList<>(visits);

            for (Visit visit : visits) {
                List<Visit> child_visits = VisitUtils.getChildVisits(visit.getVisitId());
                all_visits.addAll(child_visits);
            }
            appExecutors.mainThread().execute(() -> callBack.onDataFetched(all_visits));
        };

        appExecutors.diskIO().execute(runnable);
    }
}
