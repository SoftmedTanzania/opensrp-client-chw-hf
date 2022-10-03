package org.smartregister.chw.hf.domain.self_testing_reports;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.domain.ReportObject;

import java.util.Date;

public class SelfTestingMonthlyReportObject extends ReportObject {


    private final String[] questionsGroup = new String[]{
            "2","2-i","2-ii","2-iii","2-iv","2-v","2-vi",
            "3","3-i","3-ii","3-iii","3-iv","3-v","3-vi",
            "7","7-i","7-ii","7-iii","7-iv","7-v","7-vi",
            "8","8-i","8-ii","8-iii","8-iv","8-v","8-vi",
            "9","9-i","9-ii","9-iii","9-iv","9-v","9-vi"
    };
    private final String[] ageGroup = new String[]{
            "15-19","20-24","25-29","30-34","35-39","40-44","45-49",">50"
    };
    private final String[] genderGroup = new String[]{
            "ME","KE"
    };

    private final Date reportDate;
    private JSONObject jsonObject ;

    public SelfTestingMonthlyReportObject(Date reportDate) {
        super(reportDate);
        this.reportDate = reportDate;
    }

    @Override
    public JSONObject getIndicatorData() throws JSONException {
        jsonObject = new JSONObject();
        for (String questiongroup : questionsGroup) {   //rows
            for (String agegroup : ageGroup) {  //columns
                for (String gendergroup : genderGroup) {  //concstenate rows columns and gendergroup
                    Log.d("anga_all_1","hivst"+"-"+questiongroup+"-"+agegroup+"-"+gendergroup);
                        jsonObject.put("hivst"+"-"+questiongroup+"-"+agegroup+"-"+gendergroup,
                                ReportDao.getReportPerIndicatorCode("hivst"+"-"+questiongroup+"-"+agegroup+"-"+gendergroup, reportDate));
                }
            }
        }
        // get total of all Male & Female in Qn 2 & 7
        //and the whole total for both of them
        func_getTotal();

        return jsonObject;
    }

    private int getToatlPerEachIndictor(String total_indicatorCode,String question,String gender) throws JSONException {
        int total_of_gender_given = 0;
        for (String agegroup: ageGroup){
                total_of_gender_given += ReportDao.getReportPerIndicatorCode(total_indicatorCode+"-"+agegroup+"-"+gender, reportDate);
            jsonObject.put("hivst"+"-"+question+"-jumla-"+gender,total_of_gender_given);  //display the total for specified gender
        }
        return total_of_gender_given;
    }



    private void func_getTotal() throws JSONException {
        for (String question:questionsGroup) {   //rows
            int total_of_both_male_and_female = getToatlPerEachIndictor("hivst"+"-"+question,question,"ME")
                            +getToatlPerEachIndictor("hivst"+"-"+question,question,"KE");
            Log.d("total_of_both",""+total_of_both_male_and_female);
            jsonObject.put("hivst"+"-"+question+"-jumla-both-ME",total_of_both_male_and_female);
            jsonObject.put("hivst"+"-"+question+"-jumla-both-KE",total_of_both_male_and_female);
        }
    }


}
