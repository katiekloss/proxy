import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable
{
	private Socket client;

	ClientHandler(Socket client)
	{
		this.client = client;
	}

	public void run()
	{
		BufferedReader inputReader;
		PrintStream outputStream;
		try
		{
			inputReader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			outputStream = new PrintStream(this.client.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("Failed to initialize input/output streams!");
			return;
		}

		while(true)
		{
			if(this.client.isClosed()) break;

			HttpRequest request;
			try
			{
				request = new HttpRequest(inputReader);
			}
			catch(HttpParseException e)
			{
				System.out.println("Error parsing HTTP message: " + e.getMessage() + " (" +
					this.client.getInetAddress() + ":" + this.client.getPort() + ")");
				break;
			}
			if(!request.method.equals("CONNECT"))
			{
				URL url;
				try
				{
					url = new URL(request.uri);
				}
				catch(MalformedURLException e)
				{
					System.out.println("Error parsing URL: " + request.uri);
					break;
				}

				// Rewrite the request with the upstream URL and host
				request.uri = url.getPath();
				if(url.getQuery() != null) request.uri += "?" + url.getQuery();

				request.removeHeader("Host");
				request.addHeader("Host", url.getHost());

				System.out.println("Requesting " + request.uri + " from upstream " + url.getHost());
				Socket upstreamSocket;
				try
				{
					upstreamSocket = new Socket(url.getHost(), url.getPort() < 0 ? 80 : url.getPort());
				}
				catch(UnknownHostException e)
				{
					// Return 404?
					return;
				}
				catch(IOException e)
				{
					// ?
					return;
				}
				
				try
				{
					OutputStream output = upstreamSocket.getOutputStream();
					output.write(request.serialize().getBytes());
				}
				catch(IOException e)
				{
					// Return 500?
					return;
				}

				HttpResponse response;
				try
				{
					response = new HttpResponse(upstreamSocket.getInputStream());
				}
				catch(IOException e)
				{
					// Return 500?
					return;
				}
				catch(HttpParseException e)
				{
					// uhh..
					return;
				}

				try
				{
					upstreamSocket.close();
				}
				catch(IOException e) { }
				
				System.out.println("\t Received " + response.statusCode + " " + response.reason);

				try
				{
					outputStream.write(response.serialize().toByteArray());
				}
				catch(IOException e) { }
			}
			try
			{
				this.client.close();
			}
			catch (IOException e)
			{
				System.out.println("Failed to cleanly close client socket");
			}
		}
	}
}
