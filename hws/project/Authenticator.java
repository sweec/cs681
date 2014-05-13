package project;

public interface Authenticator {
	public abstract boolean authenticate(HttpExchange ex);
}
