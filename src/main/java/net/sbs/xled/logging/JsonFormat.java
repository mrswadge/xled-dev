package net.sbs.xled.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFormat extends Formatter {
	@Override
	public String format(LogRecord record) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(record);
		} catch (JsonProcessingException ex) {
			Logger.getLogger(JsonFormat.class.getName()).log(Level.SEVERE, null, ex);
		}
		return StringUtils.appendIfMissing(jsonInString, System.getProperty("line.separator"));
	}
}
