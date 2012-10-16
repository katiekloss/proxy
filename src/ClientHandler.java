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

		HttpRequest request = new HttpRequest(inputReader);
		System.out.println("Received request for \"" + request.uri + "\"");
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
