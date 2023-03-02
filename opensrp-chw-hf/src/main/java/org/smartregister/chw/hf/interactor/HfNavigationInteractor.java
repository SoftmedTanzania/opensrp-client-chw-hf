package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.dao.NavigationDao;
import org.smartregister.chw.core.interactor.NavigationInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.repository.AllSharedPreferences;

public class HfNavigationInteractor extends NavigationInteractor {
    protected HfNavigationInteractor() {
        super();
    }

    public static NavigationInteractor getInstance() {
        if (!(instance instanceof HfNavigationInteractor)) {
            instance = new HfNavigationInteractor();
        }

        return instance;
    }

    @Override
    protected int getCount(String tableName) {
        if (CoreConstants.TABLE_NAME.REFERRAL.equals(tableName.toLowerCase().trim())) {
            AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
            String anm = allSharedPreferences.fetchRegisteredANM();
            String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);
            String sqlReferral = "select count(*) " +
                    "from " + Constants.Tables.REFERRAL + " p " +
                    "inner join ec_family_member m on p.entity_id = m.base_entity_id COLLATE NOCASE " +
                    "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE " +
                    "inner join task t on p.id = t.reason_reference COLLATE NOCASE " +
                    "where m.date_removed is null and t.business_status = '" + CoreConstants.BUSINESS_STATUS.REFERRED + "' " +
                    "AND t.location <> '" + currentLoaction + "' COLLATE NOCASE " +
                    "AND p.chw_referral_service <> 'LTFU' COLLATE NOCASE ";
            return NavigationDao.getQueryCount(sqlReferral);
        } else if (CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION.equals(tableName.toLowerCase().trim())) {
            String sqlMalaria = "select count (p.base_entity_id) from ec_malaria_confirmation p " +
                    "inner join ec_family_member m on p.base_entity_id = m.base_entity_id COLLATE NOCASE " +
                    "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE " +
                    "where m.date_removed is null and p.is_closed = 0 " +
                    "AND p.malaria  IS NULL " +
                    "AND p.is_closed = 0 " +
                    "AND datetime('NOW') <= datetime(p.last_interacted_with/1000, 'unixepoch', 'localtime','+15 days')";
            return NavigationDao.getQueryCount(sqlMalaria);
        }
        return super.getCount(tableName);

    }
}
