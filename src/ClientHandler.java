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
		DataInputStream inputStream;
		PrintStream outputStream;
		try
		{
			inputStream = new DataInputStream(this.client.getInputStream());
			outputStream = new PrintStream(this.client.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("Failed to initialize input/output streams!");
			return;
		}

		outputStream.println("Hello, world!");

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
