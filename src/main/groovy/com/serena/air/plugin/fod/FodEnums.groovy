package com.serena.air.plugin.fod

class FodEnums {

    final static String fodScanTool = "Deployment Automation";
    final static String fodScanToolVersion ="6.3.2";
    final static String fodScanMethodType = "CICD";

    public enum ScanType {
        Static,
        Dynamic

        ScanType() {}
    }

    public enum APILookupItemTypes {
        All,
        MobileScanPlatformTypes,
        MobileScanFrameworkTypes,
        MobileScanEnvironmentTypes,
        MobileScanRoleTypes,
        MobileScanExternalDeviceTypes,
        DynamicScanEnvironmentFacingTypes,
        DynamicScanAuthenticationTypes,
        TimeZones,
        RepeatScheduleTypes,
        GeoLocations,
        SDLCStatusTypes,
        DayOfWeekTypes,
        BusinessCriticalityTypes,
        ReportTemplateTypes,
        AnalysisStatusTypes,
        ScanStatusTypes,
        ReportFormats,
        Roles,
        ScanPreferenceTypes,
        AuditPreferenceTypes,
        EntitlementFrequencyTypes,
        ApplicationTypes,
        ScanTypes,
        AttributeTypes,
        AttributeDataTypes,
        MultiFactorAuthorizationTypes,
        ReportTypes,
        ReportStatusTypes,
        PassFailReasonTypes,
        DynamicScanWebServiceTypes
    }

    public enum RemediationScanPreferenceType {
        RemediationScanIfAvailable(0),
        RemediationScanOnly(1),
        NonRemediationScanOnly(2);

        private final int _val;

        RemediationScanPreferenceType(int val) {
            this._val = val;
        }

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 0:
                    return "RemediationScanIfAvailable";
                case 1:
                    return "RemediationScanOnly";
                case 2:
                default:
                    return "NonRemediationScanOnly";
            }
        }

        public static RemediationScanPreferenceType fromInt(int val) {
            switch (val) {

                case 1:
                    return RemediationScanOnly;
                case 0:
                    return RemediationScanIfAvailable;
                case 2:
                default:
                    return NonRemediationScanOnly;
            }
        }
    }

    public enum DynamicAssessmentType {
        DynamicWebsiteAssessment(119),
        DynamicPlusWebsiteAssessment(120),
        DynamicPlusWebServicesAssessment(121);

        private final int _val;

        DynamicAssessmentType(int val) {
            this._val = val;
        }

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 1:
                    return "DynamicWebsiteAssessment";
                case 2:
                    return "DynamicPlusWebsiteAssessment";
                case 3:
                default:
                    return "DynamicPlusWebServicesAssessment";
            }
        }

        public static DynamicAssessmentType fromInt(int val) {
            switch (val) {

                case 1:
                    return DynamicWebsiteAssessment;
                case 0:
                    return DynamicPlusWebsiteAssessment;
                case 2:
                default:
                    return DynamicPlusWebServicesAssessment;
            }
        }
    }

    public enum EntitlementPreferenceType {
        SingleScanOnly(1),
        SubscriptionOnly(2),
        SingleScanFirstThenSubscription(3),
        SubscriptionFirstThenSingleScan(4) ;

        private final int _val;

        EntitlementPreferenceType(int val) {
            this._val = val;
        }
        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 1:
                    return "SingleScanOnly";
                case 2:
                    return "SubscriptionOnly";
                case 3:
                    return "SingleScanFirstThenSubscription";
                case 4:
                default:
                    return "SubscriptionFirstThenSingleScan";
            }
        }

        public static EntitlementPreferenceType fromInt(int val) {
            switch (val) {
                case 1:
                    return SingleScanOnly;
                case 2:
                    return SubscriptionOnly;
                case 3:
                    return SingleScanFirstThenSubscription ;
                case 4:
                    return SubscriptionFirstThenSingleScan;
                default:
                    return null;
            }
        }
    }

    public enum EntitlementFrequencyType {
        SingleScan(1),
        Subscription(2) ;

        private final int _val;

        EntitlementFrequencyType(int val) {
            this._val = val;
        }
        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 1:
                    return "SingleScan";
                case 2:
                    return "Subscription";
                default:
                    return "Subscription";
            }
        }

        public static EntitlementFrequencyType fromInt(int val) {
            switch (val) {
                case 1:
                    return SingleScan;
                case 2:
                    return Subscription;
                default:
                    return null;
            }
        }
    }

    public enum InProgressScanActionType {
        DoNotStartScan(0),
        CancelScanInProgress(1);

        private final int _val;

        InProgressScanActionType(int val) {
            this._val = val;
        }

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 1:
                    return "CancelInProgressScan";
                case 0:
                default:
                    return "DoNotStartScan";
            }
        }

        public static InProgressScanActionType fromInt(int val) {
            switch (val) {
                case 1:
                    return CancelScanInProgress;
                case 0:
                default:
                    return DoNotStartScan;
            }
        }
    }
}
