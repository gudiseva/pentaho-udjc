import org.json.XML;
import org.json.JSONException;
import org.json.JSONObject;

private String xmlField;
private String jsonField;
private String responseField;

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
		xmlField = getParameter("XML_FIELD");
		jsonField = getParameter("JSON_FIELD");
		responseField = getParameter("RESPONSE_FIELD");
		first = false;
	}

    // It is always safest to call createOutputRow() to ensure that your output row's Object[] is large
    // enough to handle any new fields you are creating in this step.
    //
    Object[] outputRow = createOutputRow(objRow, data.outputRowMeta.size());
	

    try
    {
		String xml = get(Fields.In, xmlField).getString(objRow);

		if (xml == null || xml.isEmpty()) {
			//logBasic("--- <<< XML :: Null or Empty >>> ---");
			return true;
		}
        
		//logBasic("XML: " + xml);
        JSONObject json = XML.toJSONObject( xml );
        String jsonString = json.toString();
		//logBasic("JSON: " + jsonString);
        get(Fields.Out, jsonField).setValue(outputRow, jsonString);
		get(Fields.Out, responseField).setValue(outputRow, true);
    }
    catch ( JSONException e )
    {
        get(Fields.Out, responseField).setValue(outputRow, false);
    }    

    // putRow will send the row on to the default output hop.
    //
    putRow(data.outputRowMeta, outputRow);

    return true;
}
