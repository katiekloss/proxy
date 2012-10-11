import java.net.*;
import java.io.*;

public class Proxy
{
	public static void main(String[] args)
	{
		ServerSocket serverSocket;
		Socket client;

		try
		{
			serverSocket = new ServerSocket(5509);
		}
		catch(IOException e)
		{
			System.out.println("Failed to listen on port 5509");
			return;
		}

		while(true)
		{
			try
			{
				client = serverSocket.accept();
			}
			catch(IOException e)
			{
				System.out.println("Error occurred while listening for a connection");
				return;
			}
			ClientHandler handler = new ClientHandler(client);
			Thread handlerThread = new Thread(handler);
			handlerThread.start();
		}
	}
}
