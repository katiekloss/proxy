import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class HttpRequest
{
	String method;
	String uri;
	int httpMajorVersion;
	int httpMinorVersion;
	HashMap<String,String> headers;
	String body;

	public HttpRequest(BufferedReader reader)
	{
		this.body = "";

		String line;
		try
		{
			line = reader.readLine();
		}
		catch(IOException e)
		{
			// Throw this
			System.out.println("Error reading HTTP request from socket");
			return;
		}

		Pattern requestPattern = Pattern.compile("(.+) (.+) HTTP/([0-9]+)\\.([0-9]+)");
		Matcher matcher = requestPattern.matcher(line);
		if(!matcher.matches())
		{
			// Throw
			return;
		}

		this.method = matcher.group(1);
		this.uri = matcher.group(2);
		this.httpMajorVersion = Integer.parseInt(matcher.group(3));
		this.httpMinorVersion = Integer.parseInt(matcher.group(4));

		headers = new HashMap<String,String>();
		while(true)
		{
			try
			{
				line = reader.readLine();
			}
			catch(IOException e)
			{
				System.out.println("Error reading from HTTP socket");
				return;
			}

			if(line.equals(""))
				break;
			if(line == null)
				return;

			String[] parts = line.split(": ", 2);
			if(parts.length != 2)
			{
				System.out.println("Got malformed HTTP header");
				return;
			}
			this.headers.put(parts[0], parts[1]);
		}
		if(!this.headers.containsKey("Content-Length"))
			return;

		int received_bytes = 0;
		while(received_bytes < new Integer(this.headers.get("Content-Length")))
		{
			try
			{
				char data = (char)reader.read();
				if(data == -1)
					return;
				this.body += data;
				received_bytes++;
			}
			catch(IOException e)
			{
				System.out.println("Error reading from HTTP socket");
				return;
			}
		}
	}

	public String serialize()
	{
		String out = "";
		out += this.method + " " + this.uri + " HTTP/" + this.httpMajorVersion +
			"." + this.httpMinorVersion + "\n";
		for(String header : this.headers.keySet())
		{
			String value = this.headers.get(header);
			out += header + ": " + value + "\n";
		}
		out += "\n";
		if(this.body.length() > 0)
		{
			out += body;
		}
		return out;
	}
}
