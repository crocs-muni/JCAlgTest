package algtestprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.System.out;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;


public class TpmSupportTable {
    enum ParsePhase { PROPERTIES, ALGORITHMS, COMMANDS, ECC_CURVES }

    static String HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"
                + "<html>\n<head>"
                + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\n"
                + "<link type=\"text/css\" href=\"style.css\" rel=\"stylesheet\">\n"
                + "<script class=\"jsbin\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>\n"
                + "<title>TPM 2.0 support test</title>\n"
                + "<script>$(function(){ $(\"#tab td\").hover(function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 2px solid #74828F\"});$(this).closest(\"tr\").css({\"border\":\" 2px solid #74828F\"});},function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 0px\"}); $(this).closest(\"tr\").css({\"border\":\" 0px\"});});});</script>\n"
                + "</head>\n\n"
                + "<body style=\"margin-top:50px; padding:20px\">\n\n";

    static final String BASIC_INFO[] = {"Basic info", "Image version"};

    static final String QUICKTEST_PROPERTIES_FIXED_STR[] = {
        "Quicktest_properties-fixed",
        "TPM_PT_FAMILY_INDICATOR",
        "TPM_PT_LEVEL",
        "TPM_PT_REVISION",
        "TPM_PT_DAY_OF_YEAR",
        "TPM_PT_YEAR",
        "TPM_PT_MANUFACTURER",
        "TPM_PT_VENDOR_STRING_1",
        "TPM_PT_VENDOR_STRING_2",
        "TPM_PT_VENDOR_STRING_3",
        "TPM_PT_VENDOR_STRING_4",
        "TPM_PT_VENDOR_TPM_TYPE",
        "TPM_PT_FIRMWARE_VERSION_1",
        "TPM_PT_FIRMWARE_VERSION_2",
        "TPM_PT_INPUT_BUFFER",
        "TPM_PT_TPM2_HR_TRANSIENT_MIN",
        "TPM_PT_TPM2_HR_PERSISTENT_MIN",
        "TPM_PT_HR_LOADED_MIN",
        "TPM_PT_ACTIVE_SESSIONS_MAX",
        "TPM_PT_PCR_COUNT",
        "TPM_PT_PCR_SELECT_MIN",
        "TPM_PT_CONTEXT_GAP_MAX",
        "TPM_PT_NV_COUNTERS_MAX",
        "TPM_PT_NV_INDEX_MAX",
        "TPM_PT_MEMORY",
        "TPM_PT_CLOCK_UPDATE",
        "TPM_PT_CONTEXT_HASH",
        "TPM_PT_CONTEXT_SYM",
        "TPM_PT_CONTEXT_SYM_SIZE",
        "TPM_PT_ORDERLY_COUNT",
        "TPM_PT_MAX_COMMAND_SIZE",
        "TPM_PT_MAX_RESPONSE_SIZE",
        "TPM_PT_MAX_DIGEST",
        "TPM_PT_MAX_OBJECT_CONTEXT",
        "TPM_PT_MAX_SESSION_CONTEXT",
        "TPM_PT_PS_FAMILY_INDICATOR",
        "TPM_PT_PS_LEVEL",
        "TPM_PT_PS_REVISION",
        "TPM_PT_PS_DAY_OF_YEAR",
        "TPM_PT_PS_YEAR",
        "TPM_PT_SPLIT_MAX",
        "TPM_PT_TOTAL_COMMANDS",
        "TPM_PT_LIBRARY_COMMANDS",
        "TPM_PT_VENDOR_COMMANDS",
        "TPM_PT_NV_BUFFER_MAX",
    };

    static final HashMap<Integer, String> TPM2_ALG_ID_STR;
    static {
        TPM2_ALG_ID_STR = new HashMap<>();
        TPM2_ALG_ID_STR.put(0x0001, "TPM2_ALG_RSA");
        TPM2_ALG_ID_STR.put(0x0004, "TPM2_ALG_SHA");
        TPM2_ALG_ID_STR.put(0x0004, "TPM2_ALG_SHA1");
        TPM2_ALG_ID_STR.put(0x0005, "TPM2_ALG_HMAC");
        TPM2_ALG_ID_STR.put(0x0006, "TPM2_ALG_AES");
        TPM2_ALG_ID_STR.put(0x0007, "TPM2_ALG_MGF1");
        TPM2_ALG_ID_STR.put(0x0008, "TPM2_ALG_KEYEDHASH");
        TPM2_ALG_ID_STR.put(0x000A, "TPM2_ALG_XOR");
        TPM2_ALG_ID_STR.put(0x000B, "TPM2_ALG_SHA256");
        TPM2_ALG_ID_STR.put(0x000C, "TPM2_ALG_SHA384");
        TPM2_ALG_ID_STR.put(0x000D, "TPM2_ALG_SHA512");
        TPM2_ALG_ID_STR.put(0x0010, "TPM2_ALG_NULL");
        TPM2_ALG_ID_STR.put(0x0012, "TPM2_ALG_SM3_256");
        TPM2_ALG_ID_STR.put(0x0013, "TPM2_ALG_SM4");
        TPM2_ALG_ID_STR.put(0x0014, "TPM2_ALG_RSASSA");
        TPM2_ALG_ID_STR.put(0x0015, "TPM2_ALG_RSAES");
        TPM2_ALG_ID_STR.put(0x0016, "TPM2_ALG_RSAPSS");
        TPM2_ALG_ID_STR.put(0x0017, "TPM2_ALG_OAEP");
        TPM2_ALG_ID_STR.put(0x0018, "TPM2_ALG_ECDSA");
        TPM2_ALG_ID_STR.put(0x0019, "TPM2_ALG_ECDH");
        TPM2_ALG_ID_STR.put(0x001A, "TPM2_ALG_ECDAA");
        TPM2_ALG_ID_STR.put(0x001B, "TPM2_ALG_SM2");
        TPM2_ALG_ID_STR.put(0x001C, "TPM2_ALG_ECSCHNORR");
        TPM2_ALG_ID_STR.put(0x001D, "TPM2_ALG_ECMQV");
        TPM2_ALG_ID_STR.put(0x0020, "TPM2_ALG_KDF1_SP800_56A");
        TPM2_ALG_ID_STR.put(0x0021, "TPM2_ALG_KDF2");
        TPM2_ALG_ID_STR.put(0x0022, "TPM2_ALG_KDF1_SP800_108");
        TPM2_ALG_ID_STR.put(0x0023, "TPM2_ALG_ECC");
        TPM2_ALG_ID_STR.put(0x0025, "TPM2_ALG_SYMCIPHER");
        TPM2_ALG_ID_STR.put(0x0026, "TPM2_ALG_CAMELLIA");
        TPM2_ALG_ID_STR.put(0x0040, "TPM2_ALG_CTR");
        TPM2_ALG_ID_STR.put(0x0027, "TPM2_ALG_SHA3_256");
        TPM2_ALG_ID_STR.put(0x0028, "TPM2_ALG_SHA3_384");
        TPM2_ALG_ID_STR.put(0x0029, "TPM2_ALG_SHA3_512");
        TPM2_ALG_ID_STR.put(0x0041, "TPM2_ALG_OFB");
        TPM2_ALG_ID_STR.put(0x0042, "TPM2_ALG_CBC");
        TPM2_ALG_ID_STR.put(0x0043, "TPM2_ALG_CFB");
        TPM2_ALG_ID_STR.put(0x0044, "TPM2_ALG_ECB");
    }

    static final HashMap<Integer, String> TPM2_CC_STR;
    static {
        TPM2_CC_STR = new HashMap<>();
        TPM2_CC_STR.put(0x0000011f, "TPM2_CC_NV_UndefineSpaceSpecial");
        TPM2_CC_STR.put(0x00000120, "TPM2_CC_EvictControl");
        TPM2_CC_STR.put(0x00000121, "TPM2_CC_HierarchyControl");
        TPM2_CC_STR.put(0x00000122, "TPM2_CC_NV_UndefineSpace");
        TPM2_CC_STR.put(0x00000124, "TPM2_CC_ChangeEPS");
        TPM2_CC_STR.put(0x00000125, "TPM2_CC_ChangePPS");
        TPM2_CC_STR.put(0x00000126, "TPM2_CC_Clear");
        TPM2_CC_STR.put(0x00000127, "TPM2_CC_ClearControl");
        TPM2_CC_STR.put(0x00000128, "TPM2_CC_ClockSet");
        TPM2_CC_STR.put(0x00000129, "TPM2_CC_HierarchyChangeAuth");
        TPM2_CC_STR.put(0x0000012a, "TPM2_CC_NV_DefineSpace");
        TPM2_CC_STR.put(0x0000012b, "TPM2_CC_PCR_Allocate");
        TPM2_CC_STR.put(0x0000012c, "TPM2_CC_PCR_SetAuthPolicy");
        TPM2_CC_STR.put(0x0000012d, "TPM2_CC_PP_Commands");
        TPM2_CC_STR.put(0x0000012e, "TPM2_CC_SetPrimaryPolicy");
        TPM2_CC_STR.put(0x0000012f, "TPM2_CC_FieldUpgradeStart");
        TPM2_CC_STR.put(0x00000130, "TPM2_CC_ClockRateAdjust");
        TPM2_CC_STR.put(0x00000131, "TPM2_CC_CreatePrimary");
        TPM2_CC_STR.put(0x00000132, "TPM2_CC_NV_GlobalWriteLock");
        TPM2_CC_STR.put(0x00000133, "TPM2_CC_GetCommandAuditDigest");
        TPM2_CC_STR.put(0x00000134, "TPM2_CC_NV_Increment");
        TPM2_CC_STR.put(0x00000135, "TPM2_CC_NV_SetBits");
        TPM2_CC_STR.put(0x00000136, "TPM2_CC_NV_Extend");
        TPM2_CC_STR.put(0x00000137, "TPM2_CC_NV_Write");
        TPM2_CC_STR.put(0x00000138, "TPM2_CC_NV_WriteLock");
        TPM2_CC_STR.put(0x00000139, "TPM2_CC_DictionaryAttackLockReset");
        TPM2_CC_STR.put(0x0000013a, "TPM2_CC_DictionaryAttackParameters");
        TPM2_CC_STR.put(0x0000013b, "TPM2_CC_NV_ChangeAuth");
        TPM2_CC_STR.put(0x0000013c, "TPM2_CC_PCR_Event");
        TPM2_CC_STR.put(0x0000013d, "TPM2_CC_PCR_Reset");
        TPM2_CC_STR.put(0x0000013e, "TPM2_CC_SequenceComplete");
        TPM2_CC_STR.put(0x0000013f, "TPM2_CC_SetAlgorithmSet");
        TPM2_CC_STR.put(0x00000140, "TPM2_CC_SetCommandCodeAuditStatus");
        TPM2_CC_STR.put(0x00000141, "TPM2_CC_FieldUpgradeData");
        TPM2_CC_STR.put(0x00000142, "TPM2_CC_IncrementalSelfTest");
        TPM2_CC_STR.put(0x00000143, "TPM2_CC_SelfTest");
        TPM2_CC_STR.put(0x00000144, "TPM2_CC_Startup");
        TPM2_CC_STR.put(0x00000145, "TPM2_CC_Shutdown");
        TPM2_CC_STR.put(0x00000146, "TPM2_CC_StirRandom");
        TPM2_CC_STR.put(0x00000147, "TPM2_CC_ActivateCredential");
        TPM2_CC_STR.put(0x00000148, "TPM2_CC_Certify");
        TPM2_CC_STR.put(0x00000149, "TPM2_CC_PolicyNV");
        TPM2_CC_STR.put(0x0000014a, "TPM2_CC_CertifyCreation");
        TPM2_CC_STR.put(0x0000014b, "TPM2_CC_Duplicate");
        TPM2_CC_STR.put(0x0000014c, "TPM2_CC_GetTime");
        TPM2_CC_STR.put(0x0000014d, "TPM2_CC_GetSessionAuditDigest");
        TPM2_CC_STR.put(0x0000014e, "TPM2_CC_NV_Read");
        TPM2_CC_STR.put(0x0000014f, "TPM2_CC_NV_ReadLock");
        TPM2_CC_STR.put(0x00000150, "TPM2_CC_ObjectChangeAuth");
        TPM2_CC_STR.put(0x00000151, "TPM2_CC_PolicySecret");
        TPM2_CC_STR.put(0x00000152, "TPM2_CC_Rewrap");
        TPM2_CC_STR.put(0x00000153, "TPM2_CC_Create");
        TPM2_CC_STR.put(0x00000154, "TPM2_CC_ECDH_ZGen");
        TPM2_CC_STR.put(0x00000155, "TPM2_CC_HMAC");
        TPM2_CC_STR.put(0x00000156, "TPM2_CC_Import");
        TPM2_CC_STR.put(0x00000157, "TPM2_CC_Load");
        TPM2_CC_STR.put(0x00000158, "TPM2_CC_Quote");
        TPM2_CC_STR.put(0x00000159, "TPM2_CC_RSA_Decrypt");
        TPM2_CC_STR.put(0x0000015b, "TPM2_CC_HMAC_Start");
        TPM2_CC_STR.put(0x0000015c, "TPM2_CC_SequenceUpdate");
        TPM2_CC_STR.put(0x0000015d, "TPM2_CC_Sign");
        TPM2_CC_STR.put(0x0000015e, "TPM2_CC_Unseal");
        TPM2_CC_STR.put(0x00000160, "TPM2_CC_PolicySigned");
        TPM2_CC_STR.put(0x00000161, "TPM2_CC_ContextLoad");
        TPM2_CC_STR.put(0x00000162, "TPM2_CC_ContextSave");
        TPM2_CC_STR.put(0x00000163, "TPM2_CC_ECDH_KeyGen");
        TPM2_CC_STR.put(0x00000164, "TPM2_CC_EncryptDecrypt");
        TPM2_CC_STR.put(0x00000165, "TPM2_CC_FlushContext");
        TPM2_CC_STR.put(0x00000167, "TPM2_CC_LoadExternal");
        TPM2_CC_STR.put(0x00000168, "TPM2_CC_MakeCredential");
        TPM2_CC_STR.put(0x00000169, "TPM2_CC_NV_ReadPublic");
        TPM2_CC_STR.put(0x0000016a, "TPM2_CC_PolicyAuthorize");
        TPM2_CC_STR.put(0x0000016b, "TPM2_CC_PolicyAuthValue");
        TPM2_CC_STR.put(0x0000016c, "TPM2_CC_PolicyCommandCode");
        TPM2_CC_STR.put(0x0000016d, "TPM2_CC_PolicyCounterTimer");
        TPM2_CC_STR.put(0x0000016e, "TPM2_CC_PolicyCpHash");
        TPM2_CC_STR.put(0x0000016f, "TPM2_CC_PolicyLocality");
        TPM2_CC_STR.put(0x00000170, "TPM2_CC_PolicyNameHash");
        TPM2_CC_STR.put(0x00000171, "TPM2_CC_PolicyOR");
        TPM2_CC_STR.put(0x00000172, "TPM2_CC_PolicyTicket");
        TPM2_CC_STR.put(0x00000173, "TPM2_CC_ReadPublic");
        TPM2_CC_STR.put(0x00000174, "TPM2_CC_RSA_Encrypt");
        TPM2_CC_STR.put(0x00000176, "TPM2_CC_StartAuthSession");
        TPM2_CC_STR.put(0x00000177, "TPM2_CC_VerifySignature");
        TPM2_CC_STR.put(0x00000178, "TPM2_CC_ECC_Parameters");
        TPM2_CC_STR.put(0x00000179, "TPM2_CC_FirmwareRead");
        TPM2_CC_STR.put(0x0000017a, "TPM2_CC_GetCapability");
        TPM2_CC_STR.put(0x0000017b, "TPM2_CC_GetRandom");
        TPM2_CC_STR.put(0x0000017c, "TPM2_CC_GetTestResult");
        TPM2_CC_STR.put(0x0000017d, "TPM2_CC_Hash");
        TPM2_CC_STR.put(0x0000017e, "TPM2_CC_PCR_Read");
        TPM2_CC_STR.put(0x0000017f, "TPM2_CC_PolicyPCR");
        TPM2_CC_STR.put(0x00000180, "TPM2_CC_PolicyRestart");
        TPM2_CC_STR.put(0x00000181, "TPM2_CC_ReadClock");
        TPM2_CC_STR.put(0x00000182, "TPM2_CC_PCR_Extend");
        TPM2_CC_STR.put(0x00000183, "TPM2_CC_PCR_SetAuthValue");
        TPM2_CC_STR.put(0x00000184, "TPM2_CC_NV_Certify");
        TPM2_CC_STR.put(0x00000185, "TPM2_CC_EventSequenceComplete");
        TPM2_CC_STR.put(0x00000186, "TPM2_CC_HashSequenceStart");
        TPM2_CC_STR.put(0x00000187, "TPM2_CC_PolicyPhysicalPresence");
        TPM2_CC_STR.put(0x00000188, "TPM2_CC_PolicyDuplicationSelect");
        TPM2_CC_STR.put(0x00000189, "TPM2_CC_PolicyGetDigest");
        TPM2_CC_STR.put(0x0000018a, "TPM2_CC_TestParms");
        TPM2_CC_STR.put(0x0000018b, "TPM2_CC_Commit");
        TPM2_CC_STR.put(0x0000018c, "TPM2_CC_PolicyPassword");
        TPM2_CC_STR.put(0x0000018d, "TPM2_CC_ZGen_2Phase");
        TPM2_CC_STR.put(0x0000018e, "TPM2_CC_EC_Ephemeral");
        TPM2_CC_STR.put(0x0000018f, "TPM2_CC_PolicyNvWritten");
        TPM2_CC_STR.put(0x00000190, "TPM2_CC_PolicyTemplate");
        TPM2_CC_STR.put(0x00000191, "TPM2_CC_CreateLoaded");
        TPM2_CC_STR.put(0x00000192, "TPM2_CC_PolicyAuthorizeNV");
        TPM2_CC_STR.put(0x00000193, "TPM2_CC_EncryptDecrypt2");
        TPM2_CC_STR.put(0x00000194, "TPM2_CC_AC_GetCapability");
        TPM2_CC_STR.put(0x00000195, "TPM2_CC_AC_Send");
        TPM2_CC_STR.put(0x00000196, "TPM2_CC_Policy_AC_SendSelect");
    }

    static final HashMap<Integer, String> TPM2_ECC_CURVE_STR;
    static {
        TPM2_ECC_CURVE_STR = new HashMap<>();
        TPM2_ECC_CURVE_STR.put(0x0001, "TPM2_ECC_NIST_P192");
        TPM2_ECC_CURVE_STR.put(0x0002, "TPM2_ECC_NIST_P224");
        TPM2_ECC_CURVE_STR.put(0x0003, "TPM2_ECC_NIST_P256");
        TPM2_ECC_CURVE_STR.put(0x0004, "TPM2_ECC_NIST_P384");
        TPM2_ECC_CURVE_STR.put(0x0005, "TPM2_ECC_NIST_P521");
        TPM2_ECC_CURVE_STR.put(0x0010, "TPM2_ECC_BN_P256");
        TPM2_ECC_CURVE_STR.put(0x0011, "TPM2_ECC_BN_P638");
        TPM2_ECC_CURVE_STR.put(0x0020, "TPM2_ECC_SM2_P256");
    }

    static class TpmSupportInfo {
        public HashMap<String, String> propertiesMap = new HashMap();
        public HashSet<Integer> commands = new HashSet();
        public HashSet<Integer> algorithms = new HashSet();
        public HashSet<Integer> ecc_curves = new HashSet();
    }

    public static void generateHTMLTable(String basePath) throws IOException {
        String filesPath = basePath + "results" + File.separator;
        File dir = new File(filesPath);
        String[] filesArray = Arrays.stream(dir.list()).filter(s -> s.endsWith(".csv")).toArray(String[]::new);

        if (filesArray == null || !dir.isDirectory()) {
            System.out.println("directory '" + filesPath + "' is empty");
            return;
        }

        TpmSupportInfo[] supportInfo = new TpmSupportInfo[filesArray.length];
        for (int i = 0; i < filesArray.length; ++i) {
            supportInfo[i] = new TpmSupportInfo();
            parseSupportFile(filesPath + filesArray[i], supportInfo[i]);
        }
        String filename = basePath + "AlgTest_tpm_html_table.html";
        FileOutputStream file = new FileOutputStream(filename);

        file.write(HEADER.getBytes());
        writeTpmList(file, filesArray);
        writeCheckboxes(file, filesArray);
        writeSupportTable(file, filesArray, supportInfo);
        file.close();
    }

    static void parseSupportFile(String filePath, TpmSupportInfo supportInfo) throws IOException {
        try {
            String strLine;
            ParsePhase phase = ParsePhase.PROPERTIES;
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            while ((strLine = br.readLine()) != null) {
                if (strLine.isEmpty()) { continue; }
                if (strLine.contains("Quicktest_algorithms")) {
                    phase = ParsePhase.ALGORITHMS; continue;
                }
                if (strLine.contains("Quicktest_commands")) {
                    phase = ParsePhase.COMMANDS; continue;
                }
                if (strLine.contains("Quicktest_ecc-curves")) {
                    phase = ParsePhase.ECC_CURVES; continue;
                }
                switch (phase) {
                case PROPERTIES:
                    String[] fields = strLine.split(";\\s+");
                    if (!fields[0].isEmpty() && fields.length == 2) {
                        supportInfo.propertiesMap.put(fields[0], fields[1]);
                    }
                    break;
                case ALGORITHMS:
                    supportInfo.algorithms.add(Integer.decode(strLine));
                    break;
                case COMMANDS:
                    supportInfo.commands.add(Integer.decode(strLine));
                    break;
                case ECC_CURVES:
                    supportInfo.ecc_curves.add(Integer.decode(strLine));
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
        }
    }

    static String[] parseTpmName(String fileName) {
        int firmwareIdx = fileName.indexOf("%");
        String[] names = new String[2];
        names[0] = fileName.replace("%", " ").substring(0, fileName.indexOf(".csv"));
        names[1] = fileName.substring(0, firmwareIdx);
        return names;
    }
    static String getShortTpmName(String fileName) {
        return parseTpmName(fileName)[1];
    }
    static String getLongTpmName(String fileName) {
        return parseTpmName(fileName)[0];
    }

    static void writeTpmList(FileOutputStream file, String[] filesArray) throws IOException {
        String tpmList = "<div class=\"container-fluid\">\n<h3 id=\"LIST\">Tested TPM abbreviations</h3>\n";

        for (int i = 0; i < filesArray.length; ++i) {
            String filename = filesArray[i];
            int firmwareIdx = filename.indexOf("%");
            String tpmName = filename.substring(0, firmwareIdx);
            // TODO authors

            String firmwareVersion = filename.substring(firmwareIdx + 1, filename.indexOf(".csv"));
            System.out.println(tpmName);
            System.out.println(firmwareVersion);

            tpmList += "<b>t" + i + "</b>  " + tpmName + ", FW=" + firmwareVersion + ",";
            // TODO filesSupport

            tpmList += "<br>\n";
        }
        tpmList += "<br>\n";
        file.write(tpmList.getBytes());
        file.flush();
    }

    static void writeCheckboxes(FileOutputStream file, String[] filesArray) throws IOException {
        String checkboxes = "<h4>Click on each checkbox to show/hide corresponding column (TPM chip)</h4>\n\t<div class=\"row\" id=\"grpChkBox\">\n";
        for (int i = 0; i < filesArray.length; ++i) {
            int rowIdx = i % (filesArray.length / 3 + 1);
            if (rowIdx == 0)
                checkboxes += "<div class=\"col-lg-4 .col-sm-4\">\n";

            checkboxes += "\t\t<p style=\"margin:0;\"><input type=\"checkbox\" name=\""+i+"\" /> <b>t"+i+"</b> - "+getLongTpmName(filesArray[i])+"</p>\n";

            if (rowIdx == filesArray.length / 3)
                checkboxes += "\t</div>\n";
        }
        checkboxes += "\t<br>\n\t</div>\n</div>\n";
        checkboxes += "<input type=\"button\" class=\"btn btn-default\" id=\"checkAll\" onclick=\"checkAll('grpChkBox')\" value=\"Select all\">\n";
        checkboxes += "<input type=\"button\" class=\"btn btn-default\" id=\"uncheckAll\" onclick=\"uncheckAll('grpChkBox')\" value=\"Deselect all\">\n";
        checkboxes += "\n</br></br>\n\n";
        file.write(checkboxes.getBytes());
    }

    static void writeSupportTable(FileOutputStream file, String[] filesArray, TpmSupportInfo[] supportInfo) throws IOException {
        String table = "<table id=\"tab\" width=\"600px\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\">\r\n";

        // Insert helper column identification for mouseover row & column jquery highlight
        table += "<colgroup>";
        for (int i = 0; i < filesArray.length + 1; i++) { table += "<col />"; } // + 1 because of column with algorithm name
        table += "</colgroup>\r\n";
        file.write(table.getBytes());

        // table head
        file.write("<thead>".getBytes());
        formatTableProperty(filesArray, BASIC_INFO, supportInfo, file);
        file.write("</thead>".getBytes());

        // table body
        file.write("<tbody>".getBytes());
        formatTableProperty(filesArray, QUICKTEST_PROPERTIES_FIXED_STR, supportInfo, file);
        formatTableAlgorithm(filesArray, "Quicktest_algorithms", TPM2_ALG_ID_STR, supportInfo, file);
        formatTableAlgorithm(filesArray, "Quicktest_commands", TPM2_CC_STR, supportInfo, file);
        formatTableAlgorithm(filesArray, "Quicktest_ecc-curves", TPM2_ECC_CURVE_STR, supportInfo, file);
        file.write("</tbody>".getBytes());

        // footer
        String footer = "</table>\n</div>\n\n";
        footer += "<script type=\"text/javascript\" src=\"footer.js\"></script>\n";
        footer += "<a href=\"#\" class=\"back-to-top\"></a>\n";
        footer += "\n</body>\n</html>";
        file.write(footer.getBytes());
        file.flush();
    }

    static void formatTableProperty(String[] filesArray, String[] classInfo, TpmSupportInfo[] supportInfo, FileOutputStream file) throws IOException {
        String html = "<tr>\n" + "<td class='dark'>" + classInfo[0] + "</td>\n";
        if (classInfo[0].equals("Basic info")) {
            for (int i = 0; i < supportInfo.length; ++i) {
                html += "  <th class='dark_index "+i+"' title = '" + getLongTpmName(filesArray[i]) + "'>t"+i+"</th>\n";
            }
        } else {
            for (int i = 0; i < supportInfo.length; ++i) {
                html += "  <td class='dark_index' title = '" + getLongTpmName(filesArray[i]) + "'>t"+i+"</td>\n";
            }
        }
        html += "</tr>\n";

        for (int i = 1; i < classInfo.length; ++i) { // skip class name
            html += "<tr>\n";
            String property = classInfo[i];
            html += "  <td class='light'>" + property + "</td>\n";

            for (int fileIndex = 0; fileIndex < supportInfo.length; ++fileIndex) {
                html += "  ";
                HashMap<String, String> propertiesMap = supportInfo[fileIndex].propertiesMap;
                if (!propertiesMap.containsKey(property)) {
                    html += "<td class='light_maybe'>-</td>\n";
                    continue;
                }
                String value = propertiesMap.get(property);
                String title = "title='" + getShortTpmName(filesArray[fileIndex]) + " : " + property + " : " + value + "'";
                html += "<td class='light_info' " + title + ">" + value + "</td>\n";
            }
            html += "</tr>\n";
        }
        file.write(html.getBytes());
    }

    static void formatTableAlgorithm(String[] filesArray, String className, HashMap<Integer, String> featureMap, TpmSupportInfo[] supportInfo, FileOutputStream file) throws IOException {
        String html = "<tr>\n" + "<td class='dark'>" + className + "</td>\n";
        for (int i = 0; i < supportInfo.length; ++i) {
            html += "  <td class='dark_index' title = '" + getLongTpmName(filesArray[i]) + "'>t"+i+"</td>\n";
        }
        html += "</tr>\n";

        for (Map.Entry<Integer, String> feature : featureMap.entrySet()) {
            html += "<tr>\n";
            String featureName = feature.getValue();
            html += "  <td class='light'>" + featureName + "</td>\n";
            for (int fileIndex = 0; fileIndex < supportInfo.length; ++fileIndex) {
                html += "  ";
                HashSet<Integer> featureSet = new HashSet();
                if (className.equals("Quicktest_algorithms")) {
                    featureSet = supportInfo[fileIndex].algorithms;
                }
                if (className.equals("Quicktest_commands")) {
                    featureSet = supportInfo[fileIndex].commands;
                }
                if (className.equals("Quicktest_ecc-curves")) {
                    featureSet = supportInfo[fileIndex].ecc_curves;
                }

                String td_class;
                String value;
                if (featureSet.contains(feature.getKey())) {
                    td_class = "light_yes";
                    value = "yes";
                } else {
                    td_class = "light_no";
                    value = "no";
                }
                String title = "title='" + getShortTpmName(filesArray[fileIndex]) + " : " + featureName + " : " + value + "'";
                html += "<td class='"+td_class+"' " + title + ">"+value+"</td>\n";
            }
            html += "</tr>\n";
        }
        file.write(html.getBytes());
    }
}
