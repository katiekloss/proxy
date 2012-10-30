class HttpParseException extends Exception
{
	String message;
	public HttpParseException(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return this.message;
	}
}