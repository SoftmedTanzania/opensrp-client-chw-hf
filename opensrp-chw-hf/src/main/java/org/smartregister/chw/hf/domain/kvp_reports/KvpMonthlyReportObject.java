package org.smartregister.chw.hf.domain.kvp_reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;

public class KvpMonthlyReportObject extends ReportObject {


    private final String[] questionsGroups = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","18","19","20","21","22",
            "17-a","17-b","17-c","17-d"
    };
    private final String[] ageGroups = new String[]{
            "<10","10-14","15-19","20-24","25-29","30-34","35-39","40-44","45-49",">50"
    };
    private final String[] kvpGroups = new String[]{
            "pwid","pwud","fsw","msm","agyw","mobile_population","serodiscordant_couple","other_vulnerable_population"
    };
    private final String[] genderGroups = new String[]{
            "ME","KE"
    };

    private final Date reportDate;
    private JSONObject jsonObject ;

    public KvpMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        jsonObject = new JSONObject();
        for (String questionGroup : questionsGroups) {   //rows
            for (String ageGroup : ageGroups) {  //columns
                for (String kvpGroup : kvpGroups) {
                    for (String genderGroup : genderGroups) {  //concstenate rows columns and gendergroup
                        jsonObject.put("kvp" + "-" + questionGroup + "-" + ageGroup + "-" + kvpGroup + "-" + genderGroup,
                                ReportDao.getReportPerIndicatorCode("kvp" + "-" + questionGroup + "-" + ageGroup +"-" + kvpGroup + "-" + genderGroup, reportDate));
                    }
                }
            }
        }
        // get total of all Male & Female in Qn 2 & 7
        //and the whole total for both of them
        funcGetTotal();

        return jsonObject;
    }

    private int getTotalPerEachIndicator(String total_indicatorCode, String question, String gender) throws JSONException {
        int totalOfGenderGiven = 0;
        for (String agegroup: ageGroups){
                totalOfGenderGiven += ReportDao.getReportPerIndicatorCode(total_indicatorCode+"-"+agegroup+"-"+gender, reportDate);
            jsonObject.put("kvp"+"-"+question+"-jumla-"+gender,totalOfGenderGiven);  //display the total for specified gender
        }
        return totalOfGenderGiven;
    }


    private void funcGetTotal() throws JSONException {
        for (String question: questionsGroups) {   //rows
            int totalOfBothMaleAndFemale = getTotalPerEachIndicator("kvp"+"-"+question,question,"ME")
                            + getTotalPerEachIndicator("kvp"+"-"+question,question,"KE");
            jsonObject.put("kvp"+"-"+question+"-jumla-both-ME",totalOfBothMaleAndFemale);
            jsonObject.put("kvp"+"-"+question+"-jumla-both-KE",totalOfBothMaleAndFemale);
        }
    }

}
