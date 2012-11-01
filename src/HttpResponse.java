import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;
import java.math.*;
import java.util.zip.*;

public class HttpResponse
{
	int statusCode;
	String reason;
	int httpMajorVersion;
	int httpMinorVersion;
	HashMap<String,String> headers;
	ByteArrayOutputStream body;

	public byte[] readLine(InputStream stream)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while(true)
		{
			int aByte;
			try
			{
				aByte = stream.read();
			}
			catch(IOException e) { return null; }

			if(aByte == '\r')
			{
				stream.mark(1);
				try
				{
					aByte = stream.read();
				}
				catch(IOException e) { return null; }

				if(aByte == '\n')
					return outputStream.toByteArray();
				else
				{
					try
					{
						stream.reset();
					}
					catch(IOException e) { return null; }
				}
			} else {
				outputStream.write(aByte);
			}
		}
	}

	public HttpResponse(InputStream reader) throws HttpParseException
	{
		byte[] line;
		this.body = new ByteArrayOutputStream();

		line = readLine(reader);
		
		if(line == null) throw new HttpParseException("Empty HTTP message");

		Pattern requestPattern = Pattern.compile("HTTP/([0-9]+)\\.([0-9]+) ([0-9]+) (.+)");
		Matcher matcher = requestPattern.matcher(new String(line));
		if(!matcher.matches())
		{
			throw new HttpParseException("Invalid status line: " + line);
		}

		this.httpMajorVersion = Integer.parseInt(matcher.group(1));
		this.httpMinorVersion = Integer.parseInt(matcher.group(2));
		this.statusCode = Integer.parseInt(matcher.group(3));
		this.reason = matcher.group(4);

		headers = new HashMap<String,String>();
		while(true)
		{
			String headerLine = new String(readLine(reader));

			if(headerLine.equals(""))
				break;
			if(headerLine == null)
				return;

			String[] parts = headerLine.split(": ", 2);
			if(parts.length != 2)
			{
				throw new HttpParseException("Got malformed HTTP header:" + headerLine);
			}
			this.headers.put(parts[0], parts[1]);
		}

		if(!this.headers.containsKey("Content-Length"))
			return;

		int bodySize = Integer.parseInt(this.headers.get("Content-Length"));
		for(int i = 0; i < bodySize; i++)
		{
			try
			{
				this.body.write(reader.read());
			}
			catch(IOException e) { }
		}
	}

	public ByteArrayOutputStream serialize()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for(char c : ("HTTP/" + this.httpMajorVersion + "." + this.httpMinorVersion + " " +
			this.statusCode + " " + this.reason + "\n").toCharArray())	out.write(c);

		for(String header : this.headers.keySet())
			for(char c : (header + ": " + this.headers.get(header) + "\n").toCharArray())
				out.write(c);

		out.write('\n');
		if(this.body.size() > 0)
		{
			try
			{
				this.body.writeTo(out);
			}
			catch(IOException e)
			{
				// meh.
			}
		}
		return out;
	}
}