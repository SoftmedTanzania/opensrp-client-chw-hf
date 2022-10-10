package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDao extends AbstractDao {
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    public static int getReportPerIndicatorCode(String indicatorCode, Date reportDate) {
        String reportDateString = simpleDateFormat.format(reportDate);
        String sql = "SELECT indicator_value\n" +
                "FROM indicator_daily_tally\n" +
                "WHERE indicator_code = '" + indicatorCode + "'\n" +
                "  AND date((substr('" + reportDateString + "', 7, 4) || '-' || substr('" + reportDateString + "', 4, 2) || '-' || '01')) = date((substr(day, 1, 4) || '-' || substr(day, 6, 2) || '-' || '01'))\n" +
                "ORDER BY day DESC LIMIT 1";

        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "indicator_value");

        List<Integer> res = readData(sql, map);


        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        } else
            return 0;
    }

    public static List<Map<String, String>> getMotherChampions(Date reportDate) {
        String sql = "SELECT chw_name, provider_id \n" +
                "from ec_mother_champion_followup \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01')\n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_anc_partner_community_feedback \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01') \n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_sbcc \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(sbcc_date, 7, 4) || '-' || substr(sbcc_date, 4, 2) || '-' || '01')\n" +
                "UNION\n" +
                "SELECT chw_name, provider_id\n" +
                "from ec_pmtct_community_feedback\n" +
                "WHERE chw_name IS NOT NULL\n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  6, 2) ||\n" +
                "           '-' || '01')\n";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("chw_name", cursor.getString(cursor.getColumnIndex("chw_name")));
            data.put("provider_id", cursor.getString(cursor.getColumnIndex("provider_id")));
            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static List<Map<String, String>> getCHWRegistrationFollowUpClients(Date reportDate)
    {
        String sql = "SELECT\n" +
                "                                                       ecr.cbhs_number as cbhs_number,\n" +
                "                                                       ecr.reasons_for_registration as registration_reason,\n" +
                "                                                       ecf.client_hiv_status_during_registration as hiv_status_during_registration,\n" +
                "                                                       '-' as tb_status_during_registration,\n" +
                "                                                       ecr.ctc_number as clinic_registration_number,\n" +
                "                                                       'CTC' as type_of_clinic,\n" +
                "                                                       (date() - fm.dob) as age,\n" +
                "                                                       fm.gender,\n" +
                "                                                       ecf.client_hiv_status_after_testing as status_after_testing,\n" +
                "                                                       ecf.hiv_services_provided,\n" +
                "                                                       ecf.supplies_provided,\n" +
                "                                                       tasks.issued_referrals,\n" +
                "                                                       tasks.successful_referrals,\n" +
                "                                                       ecf.state_of_therapy,\n" +
                "                                                       ecf.registration_or_followup_status\n" +
                "                                                       FROM ec_cbhs_register ecr\n" +
                "                                                       INNER JOIN ec_family_member fm on fm.base_entity_id = ecr.base_entity_id\n" +
                "                                                       INNER JOIN (SELECT entity_id,last_interacted_with,hiv_services_provided,state_of_therapy,registration_or_followup_status,\n" +
                "                                                       supplies_provided,client_behaviour_and_environmental_risk,client_hiv_status_during_registration,\n" +
                "                                                       client_tb_status_during_registration,client_hiv_status_after_testing, count(id)\n" +
                "                                                       as number_of_followups from ec_cbhs_followup group by entity_id)ecf on fm.base_entity_id = ecf.entity_id\n" +
                "                                                       LEFT JOIN (select for, sum(case when code = 'Referral' and business_status != 'Cancelled' then 1 else 0 end) as 'issued_referrals',\n" +
                "                                                       sum(case when code = 'Referral' and business_status != 'Cancelled' and business_status = 'Complete' then 1\n" +
                "                                                       else 0 end) as 'successful_referrals' from Task where focus in ('Suspected HIV', 'HIV Treatment and Care') group by for)tasks on fm.base_entity_id = tasks.for\n" +
                "                                                       WHERE ctc_number is not NULL and date(substr(strftime('%Y-%m-%d', datetime(ecf.last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) || '-' ||\n" +
                "                                                      substr(strftime('%Y-%m-%d', datetime(ecf.last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) || '-' || '01') =\n" +
                "                                                      date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01'))\n" +
                "                                                      group by fm.base_entity_id\n" +
                "                                                UNION\n" +
                "                                                SELECT\n" +
                "                                                       ecr.cbhs_number as cbhs_number,\n" +
                "                                                       ecr.reasons_for_registration as registration_reason,\n" +
                "                                                       '-' as hiv_status_during_registration,\n" +
                "                                                       ecf.client_tb_status_during_registration as tb_status_during_registration,\n" +
                "                                                       ecr.tb_number as clinic_registration_number,\n" +
                "                                                       'TB' as type_of_clinic,\n" +
                "                                                       (date() - fm.dob) as age,\n" +
                "                                                       fm.gender,\n" +
                "                                                       ecf.client_tb_status_after_testing as status_after_testing,\n" +
                "                                                       ecf.hiv_services_provided,\n" +
                "                                                       ecf.supplies_provided,\n" +
                "                                                       tasks.issued_referrals,\n" +
                "                                                       tasks.successful_referrals,\n" +
                "                                                       ecf.state_of_therapy,\n" +
                "                                                       ecf.registration_or_followup_status\n" +
                "                                                FROM ec_cbhs_register ecr\n" +
                "                                                       INNER JOIN ec_family_member fm on fm.base_entity_id = ecr.base_entity_id\n" +
                "                                                       INNER JOIN (SELECT entity_id,last_interacted_with,hiv_services_provided,state_of_therapy,registration_or_followup_status,\n" +
                "                                                       supplies_provided,client_behaviour_and_environmental_risk,client_hiv_status_during_registration,\n" +
                "                                                       client_tb_status_during_registration,client_tb_status_after_testing, count(id)\n" +
                "                                                       as number_of_followups from ec_cbhs_followup group by entity_id)ecf on fm.base_entity_id = ecf.entity_id\n" +
                "                                                       LEFT JOIN (select for, sum(case when code = 'Referral' and business_status != 'Cancelled' then 1 else 0 end) as 'issued_referrals',\n" +
                "                                                       sum(case when code = 'Referral' and business_status != 'Cancelled' and business_status = 'Complete' then 1\n" +
                "                                                       else 0 end) as 'successful_referrals' from Task where focus in ('Suspected TB') group by for)tasks on fm.base_entity_id = tasks.for\n" +
                "                                                WHERE tb_number is not NULL and date(substr(strftime('%Y-%m-%d', datetime(ecf.last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) || '-' ||\n" +
                "                                                 substr(strftime('%Y-%m-%d', datetime(ecf.last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) || '-' || '01') =\n" +
                "                                                  date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01'))";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("cbhs_number", cursor.getString(cursor.getColumnIndex("cbhs_number")));
            data.put("registration_reason", cursor.getString(cursor.getColumnIndex("registration_reason")));
            data.put("hiv_status_during_registration", cursor.getString(cursor.getColumnIndex("hiv_status_during_registration")));
            data.put("tb_status_during_registration", cursor.getString(cursor.getColumnIndex("tb_status_during_registration")));
            data.put("clinic_registration_number", cursor.getString(cursor.getColumnIndex("clinic_registration_number")));
            data.put("type_of_clinic", cursor.getString(cursor.getColumnIndex("type_of_clinic")));
            data.put("age", cursor.getString(cursor.getColumnIndex("age")));
            data.put("gender", cursor.getString(cursor.getColumnIndex("gender")));
            data.put("status_after_testing", cursor.getString(cursor.getColumnIndex("status_after_testing")));
            data.put("hiv_services_provided", cursor.getString(cursor.getColumnIndex("hiv_services_provided")));
            data.put("supplies_provided", cursor.getString(cursor.getColumnIndex("supplies_provided")));
            data.put("issued_referrals", cursor.getString(cursor.getColumnIndex("issued_referrals")));
            data.put("successful_referrals", cursor.getString(cursor.getColumnIndex("successful_referrals")));
            data.put("state_of_therapy", cursor.getString(cursor.getColumnIndex("state_of_therapy")));
            data.put("registration_or_followup_status", cursor.getString(cursor.getColumnIndex("registration_or_followup_status")));
            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }


}
