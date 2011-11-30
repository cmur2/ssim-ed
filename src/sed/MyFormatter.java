package sed;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {
	
	private static final String lineSep = System.getProperty("line.separator");
	
	public String format(LogRecord record) {
		StringBuffer buf = new StringBuffer(180);

		buf.append('[');
		buf.append(record.getThreadID());
		buf.append("] ");
		buf.append(record.getLevel());
		buf.append(' ');
		buf.append(record.getSourceClassName());
		buf.append(": ");
		buf.append(formatMessage(record));
		buf.append(lineSep);

		Throwable throwable = record.getThrown();
		if(throwable != null) {
			StringWriter sink = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sink, true));
			buf.append(sink.toString());
		}
		return buf.toString();
	}
}
