import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class HttpRequest
{
	public enum Method
	{
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	}

	Method method;
	String uri;
	int httpMajorVersion;
	int httpMinorVersion;
	HashMap<String,String> headers;
	String body;

	public HttpRequest(BufferedReader reader)
	{
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

		if(matcher.group(1).equals("GET"))
			this.method = Method.GET;
		else if(matcher.group(1).equals("POST"))
			this.method = Method.POST;
		else if(matcher.group(1).equals("HEAD"))
			this.method = Method.HEAD;
		else if(matcher.group(1).equals("OPTIONS"))
			this.method = Method.OPTIONS;
		else if(matcher.group(1).equals("PUT"))
			this.method = Method.PUT;
		else if(matcher.group(1).equals("DELETE"))
			this.method = Method.DELETE;
		else if(matcher.group(1).equals("TRACE"))
			this.method = Method.TRACE;
		else if(matcher.group(1).equals("CONNECT"))
			this.method = Method.CONNECT;
		else
		{
			// AAAAAAAAAAAAAAAA
		}

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
		this.body = "";
		while(received_bytes < new Integer(this.headers.get("Content-Length")))
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
			if(line == null)
				return;

			this.body += line + "\n";
			received_bytes += line.length();
		}
	}
}
