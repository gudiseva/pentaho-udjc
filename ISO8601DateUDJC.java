import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

private String dataDateField;
private String dbTimeStampField;
private String evtDateTimeField;
private String evtOldEventDateTimeField;
private String evtReceivedTimeField;

private String isoDataDateField;
private String isoDBTimeStampField;
private String isoEvtDateTimeField;
private String isoEvtOldEventDateTimeField;
private String isoEvtReceivedTimeField;

private String responseField;
private	 boolean rowInError = false;
private	 String errMsg;
private	 int errCnt = 0;


private final TimeZone tz = TimeZone.getTimeZone("UTC");
private final DateFormat inputDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
private final DateFormat outputDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
private final DateFormat isoDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");


public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
{
    // First, get a row from the default input hop
    //
    Object[] objRow = getRow();

    // If the row object is null, we are done processing.
    //
    if (objRow == null) {
      setOutputDone();
      return false;
    }


	if (first) {
		dataDateField = getParameter("DATA_DATE");
		dbTimeStampField = getParameter("DB_TIMESTAMP");
		evtDateTimeField = getParameter("EVT_DATETIME");
		evtOldEventDateTimeField = getParameter("EVT_OLD_DATETIME");
		evtReceivedTimeField = getParameter("EVT_RECEIVED_TIME");

		isoDataDateField = getParameter("ISO_DATA_DATE");
		isoDBTimeStampField = getParameter("ISO_DB_TIMESTAMP");
		isoEvtDateTimeField = getParameter("ISO_EVT_DATETIME");
		isoEvtOldEventDateTimeField = getParameter("ISO_EVT_OLD_DATETIME");
		isoEvtReceivedTimeField = getParameter("ISO_EVT_RECEIVED_TIME");

		responseField = getParameter("RESPONSE_FIELD");
		first = false;
	}

    // It is always safest to call createOutputRow() to ensure that your output row's Object[] is large
    // enough to handle any new fields you are creating in this step.
    //
    Object[] outputRow = createOutputRow(objRow, data.outputRowMeta.size());
	

    try
    {
		inputDF.setTimeZone(tz);
		outputDF.setTimeZone(tz);

		String dataDate = get(Fields.In, dataDateField).getString(objRow);
		if (dataDate == null || dataDate.isEmpty()) {
			//logBasic("--- <<< DataDate :: Null or Empty >>> ---");
			//return true;
		} else {
			//logBasic("DataDate: " + dataDate);
			Date dataDateTemp = inputDF.parse(dataDate);
			String isoDataDateString = outputDF.format(dataDateTemp);
			//logBasic("isoDataDate: " + isoDataDateString);
	        get(Fields.Out, isoDataDateField).setValue(outputRow, isoDataDateString);
		}       


		String dbTimeStamp = get(Fields.In, dbTimeStampField).getString(objRow);
		if (dbTimeStamp == null || dbTimeStamp.isEmpty()) {
			//logBasic("--- <<< DBTimeStamp :: Null or Empty >>> ---");
			//return true;
		} else {       
			//logBasic("DBTimeStamp: " + dbTimeStamp);
			Date dbTimeStampTemp = inputDF.parse(dbTimeStamp);
			String isoDBTimeStampString = outputDF.format(dbTimeStampTemp);
			//logBasic("isoDBTimeStamp: " + isoDBTimeStampString);
        	get(Fields.Out, isoDBTimeStampField).setValue(outputRow, isoDBTimeStampString);
		}

		String evtDateTime = get(Fields.In, evtDateTimeField).getString(objRow);
		if (evtDateTime == null || evtDateTime.isEmpty()) {
			//logBasic("--- <<< EvtDateTime :: Null or Empty >>> ---");
			//return true;
		} else {
			//logBasic("EvtDateTime: " + evtDateTime);
			Date evtDateTimeTemp = inputDF.parse(evtDateTime);
			String isoEvtDateTimeString = outputDF.format(evtDateTimeTemp);
			//logBasic("isoEvtDateTime: " + isoEvtDateTimeString);
        	get(Fields.Out, isoEvtDateTimeField).setValue(outputRow, isoEvtDateTimeString);
		}       


		String evtOldEventDateTime = get(Fields.In, evtOldEventDateTimeField).getString(objRow);
		if (evtOldEventDateTime == null || evtOldEventDateTime.isEmpty()) {
			//logBasic("--- <<< EvtOldEventDateTime :: Null or Empty >>> ---");
			//return true;
		} else {       
			//logBasic("EvtOldEventDateTime: " + evtOldEventDateTime);
			Date evtOldEventDateTimeTemp = inputDF.parse(evtOldEventDateTime);
			String isoEvtOldEventDateTimeString = outputDF.format(evtOldEventDateTimeTemp);
			//logBasic("isoEvtOldEventDateTime: " + isoEvtOldEventDateTimeString);
    	    get(Fields.Out, isoEvtOldEventDateTimeField).setValue(outputRow, isoEvtOldEventDateTimeString);
		}


		String evtReceivedTime = get(Fields.In, evtReceivedTimeField).getString(objRow);
		if (evtReceivedTime == null || evtReceivedTime.isEmpty()) {
			//logBasic("--- <<< EvtReceivedTime :: Null or Empty >>> ---");
			//return true;
		} else {
			//logBasic("EvtReceivedTime: " + evtReceivedTime);
			Date evtReceivedTimeTemp = inputDF.parse(evtReceivedTime);
			String isoEvtReceivedTimeString = outputDF.format(evtReceivedTimeTemp);
			//logBasic("isoEvtReceivedTime: " + isoEvtReceivedTimeString);
	        get(Fields.Out, isoEvtReceivedTimeField).setValue(outputRow, isoEvtReceivedTimeString);
		}       

		get(Fields.Out, responseField).setValue(outputRow, true);
    }
    catch ( ParseException ex )
    {
        get(Fields.Out, responseField).setValue(outputRow, false);

		rowInError = true;
		errMsg = ex.getMessage();
		errCnt = errCnt + 1;
    }    
    catch ( Exception e )
    {
        get(Fields.Out, responseField).setValue(outputRow, false);

		rowInError = true;
		errMsg = e.getMessage();
		errCnt = errCnt + 1;
    }    


	if ( !rowInError ) {
	    // putRow will send the row on to the default output hop.
      //
	    putRow(data.outputRowMeta, outputRow);
	} else {
		// Output errors to the error hop.  Right click on step and choose "Error Handling..."
    //
		putError(data.outputRowMeta, outputRow, errCnt, errMsg, "Unparseable date", "RangeError");
	}

    return true;
}
