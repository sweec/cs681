package project;

public class NullAuthenticator implements Authenticator {

	@Override
	public boolean authenticate(HttpExchange ex) {
		return true;
	}

}
