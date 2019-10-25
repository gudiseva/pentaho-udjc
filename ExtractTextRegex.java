import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

private String rowField;
private String lineField;

	// --- <<< Static Variables >>> ---
    static final String MONEY_NEGATIVE_PATTERN = "\\(\\$(?<%s>.+)\\)";
    static final String MONEY_POSITIVE_PATTERN = "\\$(?<%s>.+)";

    static final String PERCENTAGE_NEGATIVE_PATTERN = "\\((?<%s>(\\d+|\\.|,)+)\\)%%";
    static final String PERCENTAGE_POSITIVE_PATTERN = "(?<%s>(\\d+|\\.|,)+)%%";

	static final String fmt = "(%s|%s)";
	static final String mfmt = ".*\\s+%s\\s+%s";

    static final String INVESTOR_REF_PATTERN = "(.*)Investor\\s+Ref\\s*:\\s*(?<investorref>.+)";
	static final Pattern INVESTOR_REF = Pattern.compile(INVESTOR_REF_PATTERN);
	// --- <<< [END] >>> ---

public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
{
	Object[] objRow = getRow();

	if (objRow == null){
		setOutputDone();
		return false;
	}

	if (first) {
		rowField = getParameter("ROW_FIELD");
		lineField = getParameter("LINE_FIELD");
		first = false;
	}

	Object[] outputRow = createOutputRow(objRow, data.outputRowMeta.size());

	String line = get(Fields.In, rowField).getString(objRow);

	if (line == null || line.isEmpty()) {
		//logBasic("--- <<< Line :: Null or Empty >>> ---");
		return true;
	}
		// --- <<< Money Patterns >>> ---
		final String mmt[] = { get(MONEY_POSITIVE_PATTERN, "mtd"), get(MONEY_NEGATIVE_PATTERN, "mtdn") };    
	    final String MONEY_PATTERN_MTD = String.format(fmt, (Object[])mmt);

    	final String myt[] = { get(MONEY_POSITIVE_PATTERN, "ytd"), get(MONEY_NEGATIVE_PATTERN, "ytdn") };
	    final String MONEY_PATTERN_YTD = String.format(fmt, (Object[])myt);

	    final String mmtyt[] = { MONEY_PATTERN_MTD, MONEY_PATTERN_YTD };
		final Pattern MONEY_ROW = Pattern.compile(String.format(mfmt, (Object[])mmtyt));

		Matcher moneyMatch = MONEY_ROW.matcher(line);
		// --- <<< [END] >>> ---

		// --- <<< Percentage Patterns >>> ---
		final String pmt[] = { get(PERCENTAGE_POSITIVE_PATTERN, "mtd"), get(PERCENTAGE_NEGATIVE_PATTERN, "mtdn") };
	    final String PERCENTAGE_PATTERN_MTD = String.format(fmt, (Object[])pmt);

		final String pyt[] = { get(PERCENTAGE_POSITIVE_PATTERN, "ytd"), get(PERCENTAGE_NEGATIVE_PATTERN, "ytdn") };
		final String PERCENTAGE_PATTERN_YTD = String.format(fmt, (Object[])pyt);

		final String pmtyt[] = { PERCENTAGE_PATTERN_MTD, PERCENTAGE_PATTERN_YTD };
		final Pattern PERCENTAGE_ROW = Pattern.compile(String.format(mfmt, (Object[])pmtyt));

		Matcher percentMatch = PERCENTAGE_ROW.matcher(line);
		// --- <<< [END] >>> ---


	// --- New_Page [Start] ---
	boolean newPage = checkLine(line, "Confidential");
	if(newPage) {
		//logBasic("New_Page : " +  newPage);
		get(Fields.Out, lineField).setValue(outputRow, "New_Page : " +  newPage);
	} 
	// --- New_Page [END] ---

	// --- Investor_Ref [Start] ---
	final String invRef = getInvestorRef(line);
	if (invRef != null){
		//logBasic("Investor_Ref : " +  invRef);
		get(Fields.Out, lineField).setValue(outputRow, "Investor_Ref : " + invRef);
	}
	// --- Investor_Ref [END] ---

	// --- Class_IPO_Series [Start] ---

    final Pattern SHARE_CLASS_AND_SERIES = Pattern.compile(".+Class\\s+(?<class>\\w+)-(?<ipo>\\w+)(.+Series\\s+(?<serie>.+))?");
	Matcher matcher = SHARE_CLASS_AND_SERIES.matcher(line);
            if (matcher.matches()) {
                String shareClass = matcher.group("class");
                String ipoStatus = matcher.group("ipo");
                String series = matcher.group("serie");
                //logBasic(">>> Class :: IPO :: Series >>> : " +  shareClass + " :: " + ipoStatus + " :: " + series);
				get(Fields.Out, lineField).setValue(outputRow, "Class_IPO_Series : " + shareClass + "|" + ipoStatus + "|" + series);
            }

	// --- Class_IPO_Series [END] ---

	// --- For_the_Month_Ending [Start] ---
		final String textToCheck = "For the Month Ending";
		if (line.startsWith(textToCheck)) {
			final String monthEnd = line.replace(textToCheck, "").trim();
			//logBasic("For_the_Month_Ending : " +  monthEnd);
			get(Fields.Out, lineField).setValue(outputRow, "For_the_Month_Ending : " + "\"" + monthEnd + "\"");
			get(Fields.Out, lineField).setValue(outputRow, "For_the_Month_Ending : " + monthEnd);
		}
	// --- For_the_Month_Ending [END] ---

	// --- Opening_Balance_Investor_Capital [Start] ---
		boolean openBal = checkLine(line, "Opening Balance, Investor's Capital");
		if (openBal) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Opening_Balance_Investor_Capital : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Opening_Balance_Investor_Capital : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Opening_Balance_Investor_Capital [END] ---

	// --- Subscriptions [Start] ---
		boolean subs = checkLine(line, "Subscriptions");
		if (subs) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Subscriptions : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Subscriptions : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Subscriptions [END] ---

	// --- Withdrawals_Distributions [Start] ---
		boolean drawDist = checkLine(line, "Withdrawals/Distributions");
		if (drawDist) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Withdrawals_Distributions : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Withdrawals_Distributions : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Withdrawals_Distributions [END] ---

	// --- Transfers [Start] ---
		boolean xfer = checkLine(line, "Transfers");
		if (xfer) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Transfers : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Transfers : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Transfers [END] ---

	// --- Non_IPO_Income_Loss [Start] ---
		boolean nonIpo = checkLine(line, "Non IPO Income");
		if (nonIpo) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Non_IPO_Income_Loss : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Non_IPO_Income_Loss : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Non_IPO_Income_Loss [END] ---

	// --- Asset-Based_Fees [Start] ---
		boolean assetFee = checkLine(line, "Asset-Based Fees");
		if (assetFee) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Asset-Based_Fees : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Asset_Based_Fees : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Asset-Based_Fees [END] ---

	// --- Front_Office_Compensation_Expenses [Start] ---
		boolean frontExp = checkLine(line, "Front Office Compensation Expenses");
		if (frontExp) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Front_Office_Compensation_Expenses : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Front_Office_Compensation_Expenses : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Front_Office_Compensation_Expenses [END] ---

	// --- IPO_Income_Loss [Start] ---
		boolean ipo = checkLine(line, "IPO Income");
		if (ipo) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("IPO_Income_Loss : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "IPO_Income_Loss : " + data[0] + "|" + data[1]);
	        }

		}
	// --- IPO_Income_Loss [END] ---

	// --- Advisory_Fees [Start] ---
		boolean advFee = checkLine(line, "Advisory Fees");
		if (advFee) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Advisory_Fees : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Advisory_Fees : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Advisory_Fees [END] ---

	// --- Pass_Through_Expense [Start] ---
		boolean passExp = checkLine(line, "Pass-Through Expense");
		if (passExp) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Pass_Through_Expense : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Pass_Through_Expense : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Pass_Through_Expense [END] ---

	// --- Incentive_Fees [Start] ---
		boolean incFee = checkLine(line, "Incentive Fees");
		if (incFee) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Incentive_Fees : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Incentive_Fees : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Incentive_Fees [END] ---

	// --- Total_Profit_Loss [Start] ---
		boolean profitLoss = checkLine(line, "Total Profit");
		if (profitLoss) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Total_Profit_Loss : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Total_Profit_Loss : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Total_Profit_Loss [END] ---

	// --- Ending_Balance_Investor_Capital [Start] ---
		boolean invCap = checkLine(line, "Ending Balance, Investor's Capital");
		if (invCap) {

        	if (moneyMatch.matches()) {
	            BigDecimal[] data = capture(moneyMatch, line);
				//logBasic("Ending_Balance_Investor_Capital : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Ending_Balance_Investor_Capital : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Ending_Balance_Investor_Capital [END] ---

	// --- Net_Rate_of_Return [Start] ---
		boolean netRate = checkLine(line, "Net Rate of Return");
		if (netRate) {

        	if (percentMatch.matches()) {
	            BigDecimal[] data = capture(percentMatch, line);
				//logBasic("Net_Rate_of_Return : " +  data[0] + "|" + data[1]);
				get(Fields.Out, lineField).setValue(outputRow, "Net_Rate_of_Return : " + data[0] + "|" + data[1]);
	        }

		}
	// --- Net_Rate_of_Return [END] ---

	// --- Shares_NAV [Start] ---
		final String textToCheck2 = "Number of shares";
		if (line.contains(textToCheck2)) {
			final Pattern SHARES_FIELD_PATTERN = Pattern.compile("Number of shares\\s+(?<numbshares>(\\d+|\\.|,)+)\\s+NAV Per Share\\s+(?<navbshares>(\\d+|\\.|,)+)");

            Matcher matcher2 = SHARES_FIELD_PATTERN.matcher(line);
            if (matcher2.matches()) {
                BigDecimal numberOfShares = getNumber(matcher2, "numbshares");
                BigDecimal navPerShare = getNumber(matcher2, "navbshares");
				//logBasic(">>> Shares :: NAV >>> : " +  numberOfShares + " :: " + navPerShare);
				get(Fields.Out, lineField).setValue(outputRow, "Shares_NAV : " + numberOfShares + "|" + navPerShare);
            }
			
		}
	// --- Shares_NAV [END] ---
 
	putRow(data.outputRowMeta, outputRow);
	return true;
}


    static BigDecimal[] capture(Matcher matcher, String line) {
		if (line != null) {
			if (line.trim().isEmpty()) {
				return null;
			}

		}

		return new BigDecimal[] { getNumber(matcher, "mtd"), getNumber(matcher, "ytd") };
    }

	static String getInvestorRef(String line) {

		if (line != null) {
			Matcher matcher = INVESTOR_REF.matcher(line);

			if (matcher.find( )){
				String invRef = matcher.group("investorref").trim();		
				// logBasic("Found Investor Ref: " + invRef);
				return invRef;
			}

		}
		return null;
	}

    static BigDecimal getNumber(Matcher matcher, String groupName) {
        String data = matcher.group(groupName);

        if (data == null) {
            data = "-" + matcher.group(groupName + "n");
        }

        try {
            return new BigDecimal(cleanString(data));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid extracted number " + data + ", group " + groupName);
        }
    }

    static String cleanString(String s) {
        return s.trim().replace(",", "");
    }

    static String get(String moneyPositivePattern, String name) {
		String objArr[] = { name };
        return String.format(moneyPositivePattern, (Object[])objArr);
    }


    private static boolean checkLine(String line, String textToCheck) {
        return line.startsWith(textToCheck) || line.replaceAll("\\s+", "").toLowerCase()
                .startsWith(textToCheck.replaceAll("\\s+", "").toLowerCase());
    }
