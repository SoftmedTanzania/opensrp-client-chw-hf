package org.smartregister.chw.hf.domain.cdp_reports;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CdpIssuingReportObject extends ReportObject {
    private final Context context;
    private Date reportDate;

    public CdpIssuingReportObject(Date reportDate, Context context) {
        super(reportDate);
        this.reportDate = reportDate;
        this.context = context;
    }


    @Override
    public JSONObject getIndicatorData() throws JSONException {
        JSONArray dataArray = new JSONArray();
        List<Map<String, String>> getHfCdpStockLogList = ReportDao.getHfIssuingCdpStockLog(reportDate);

        int i = 0;
        for (Map<String, String> getHfCdpStockLog : getHfCdpStockLogList) {
            JSONObject reportJsonObject = new JSONObject();
            reportJsonObject.put("id", ++i);

            reportJsonObject.put("male-condoms", getCdpClientDetails(getHfCdpStockLog, "male_condoms"));
            reportJsonObject.put("female-condoms", getCdpClientDetails(getHfCdpStockLog, "female_condoms"));
            reportJsonObject.put("point-of-service", getCdpClientDetails(getHfCdpStockLog, "point_of_service"));
            reportJsonObject.put("other-pos", getCdpClientDetails(getHfCdpStockLog, "other_pos"));
           dataArray.put(reportJsonObject);
        }

        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("reportData", dataArray);

        return resultJsonObject;
    }

    private String getCdpClientDetails(Map<String, String> chwRegistrationFollowupClient, String key) {
        String details = chwRegistrationFollowupClient.get(key);
        if (StringUtils.isNotBlank(details)) {
            return details;
        }
        return "-";
    }


}
