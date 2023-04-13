package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.hf.utils.Constants.ReportConstants.CDPReportKeys.ISSUING_AT_THE_FACILITY_REPORTS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.CDPReportKeys.ISSUING_FROM_THE_FACILITY_REPORTS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.CDPReportKeys.RECEIVING_REPORTS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.EID_MONTHLY;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.THREE_MONTHS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.TWELVE_MONTHS;
import static org.smartregister.chw.hf.utils.Constants.ReportConstants.PMTCTReportKeys.TWENTY_FOUR_MONTHS;
import static org.smartregister.util.Utils.getAllSharedPreferences;

import android.content.Context;
import android.webkit.JavascriptInterface;


public class HfWebAppInterface {
    private static final String DEFAULT_LOCALITY_NAME = "dfltLocName";
    Context mContext;

    String reportType;


    public HfWebAppInterface(Context c, String reportType) {
        mContext = c;
        this.reportType = reportType;
    }

    @JavascriptInterface
    public String getData(String key) {
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.PMTCT_REPORT)) {
            switch (key) {
                case THREE_MONTHS:
                    ReportUtils.setPrintJobName("report_ya_miezi_mitatu-" + ReportUtils.getReportPeriod() + ".pdf");
                    return ReportUtils.PMTCTReports.computeThreeMonths(ReportUtils.getReportDate());
                case TWELVE_MONTHS:
                    ReportUtils.setPrintJobName("report_ya_miezi_kumi_na_mbili-" + ReportUtils.getReportPeriod() + ".pdf");
                    return ReportUtils.PMTCTReports.computeTwelveMonths(ReportUtils.getReportDate());
                case TWENTY_FOUR_MONTHS:
                    ReportUtils.setPrintJobName("report_ya_miaka_miwili-" + ReportUtils.getReportPeriod() + ".pdf");
                    return ReportUtils.PMTCTReports.computeTwentyFourMonths(ReportUtils.getReportDate());
                case EID_MONTHLY:
                    ReportUtils.setPrintJobName("report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
                    return ReportUtils.PMTCTReports.computeEIDMonthly(ReportUtils.getReportDate());
                default:
                    return "";
            }
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.PNC_REPORT)) {
            ReportUtils.setPrintJobName("pnc_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.PNCReports.computePncReport(ReportUtils.getReportDate());
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.ANC_REPORT)) {
            ReportUtils.setPrintJobName("anc_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.ANCReports.computeAncReport(ReportUtils.getReportDate());
        }
        if( reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.CBHS_REPORT)){
            ReportUtils.setPrintJobName("cbhs_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.CBHSReports.computeCbhsReport(ReportUtils.getReportDate());
        }
        if(reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.LTFU_SUMMARY)){
            ReportUtils.setPrintJobName("ltfu_summary_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.LTFUReports.computeLTFUReport(ReportUtils.getReportDate());
        }
        if(reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.LD_REPORT)){
            ReportUtils.setPrintJobName("wodi_ya_wazazi_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.LDReports.computeLdReport(ReportUtils.getReportDate());
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.MOTHER_CHAMPION_REPORT)){
            ReportUtils.setPrintJobName("mother_champion_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.MotherChampionReports.computeMotherChampionReport(ReportUtils.getReportDate());
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.SELF_TESTING_REPORT)){
            ReportUtils.setPrintJobName("self_testing_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.SelfTestingReport.computeSelfTestingReportReport(ReportUtils.getReportDate());
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.KVP_REPORT)){
            ReportUtils.setPrintJobName("kvp_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
            return ReportUtils.KvpReport.computeReport(ReportUtils.getReportDate());
        }
        if (reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.CONDOM_DISTRIBUTION_REPORT)){
              switch (key) {
                case ISSUING_AT_THE_FACILITY_REPORTS:
                    ReportUtils.setPrintJobName("CDP_issuing_at_the_facility_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
                    return ReportUtils.CDPReports.computeIssuingAtFacilityReports(ReportUtils.getReportDate());
                  case ISSUING_FROM_THE_FACILITY_REPORTS:
                      ReportUtils.setPrintJobName("CDP_issuing_from_the_facility_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
                      return ReportUtils.CDPReports.computeIssuingFromFacilityReports(ReportUtils.getReportDate());
                  case RECEIVING_REPORTS:
                      ReportUtils.setPrintJobName("CDP_receiving_report_ya_mwezi-" + ReportUtils.getReportPeriod() + ".pdf");
                      return ReportUtils.CBHSReport.computeReport(ReportUtils.getReportDate());

                default:
                    return "";
            }
        }
        return "";
    }

    @JavascriptInterface
    public String getDataPeriod() {
        return ReportUtils.getReportPeriod();
    }

    @JavascriptInterface
    public String getDataPeriod(String reportKey) {
        if(reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.PMTCT_REPORT)){
            return ReportUtils.getReportPeriodForCohortReport(reportKey);
        }
        if(reportType.equalsIgnoreCase(Constants.ReportConstants.ReportTypes.CONDOM_DISTRIBUTION_REPORT)){
            return ReportUtils.getReportPeriodForCohortReport(reportKey);
        }

        return ReportUtils.getReportPeriod();
    }

    @JavascriptInterface
    public String getReportingFacility() {
        return getAllSharedPreferences().getPreference(DEFAULT_LOCALITY_NAME);
    }
}