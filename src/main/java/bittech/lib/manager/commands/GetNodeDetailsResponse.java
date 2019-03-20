package bittech.lib.manager.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bittech.lib.protocol.Response;
import bittech.lib.utils.exceptions.ExceptionInfo;
import bittech.lib.utils.json.RawJson;

public class GetNodeDetailsResponse implements Response {
	
	public String name;
	public String statusCode;
	public String status;
	
	public Map<String, String> summary = new LinkedHashMap<String, String>();
	
	public Map<String, Object> details = new LinkedHashMap<String, Object>();
	
	public RawJson logs;
	
	public List<String> supportedCommands = new ArrayList<String>();
	
	public List<ExceptionInfo> exceptions = new ArrayList<ExceptionInfo>();

	public GetNodeDetailsResponse() {

	}
	
	
//	
//	
//	private static class Summary {
//		String kasa = "123";
//	}
//	
//	private static class Info {
//		String kasa = "123";
//		String powod = "zbedna";
//	}	
//	
//	public static void main(String[] args) throws Exception {
//		Gson gson = (new GsonBuilder()).registerTypeAdapter(RawJson.class, new RawJsonAdapter()).create();
//		GetNodeDetailsResponse resp = new GetNodeDetailsResponse();
//		resp.name = "duke-1";
//		resp.statusCode = "ok";
//		resp.status = "Jest git";
//		resp.summary = new RawJson(new Summary());
//		resp.info = new RawJson(new Info());
//		LOGGER.debug(gson.toJson(resp));
//	}

	
}



