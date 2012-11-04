import java.net.*;
import java.util.*;

public class DnsCache
{
	// I'm a singleton! Since multiple threads can access me safely,
	// I can cache more than if each thread had its own cache!

	HashMap<String, Pair<InetAddress, Long>> _cache;
	int timeout;

	private static class DnsCacheSingleton {
		public static final DnsCache instance = new DnsCache();
	}

	public static DnsCache getInstance()
	{
		return DnsCacheSingleton.instance;
	}

	private DnsCache()
	{
		this._cache = new HashMap<String, Pair<InetAddress, Long>>();
		this.timeout = 30;
	}

	public static InetAddress get(String hostname)
	{
		if(getInstance()._cache.containsKey(hostname))
		{
			if(getInstance()._cache.get(hostname).two + getInstance().timeout > (System.currentTimeMillis() / 1000))
				return getInstance()._cache.get(hostname).one;
		}

		InetAddress address;
		try
		{
			address = InetAddress.getByName(hostname);
		}
		catch(UnknownHostException e)
		{
			return null;
		}

		getInstance()._cache.put(hostname, new Pair<InetAddress, Long>(address, System.currentTimeMillis() / 1000));
		return address;	
	}
}