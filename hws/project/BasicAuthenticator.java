package project;

import java.net.HttpURLConnection;

public class BasicAuthenticator implements Authenticator {
	private String realm = "basic realm";

	@Override
	public boolean authenticate(HttpExchange ex) {
		String basicUser = ex.getRequestHeader("Authorization");
		String user = null;
		if (basicUser != null && basicUser.startsWith("Basic ")) {
			user = basicUser.split(" ")[1];
			if (UserInfo.getInstance().hasBasicUser(user))
				return true;
		}
		ex.makeErrorResponse(HttpURLConnection.HTTP_UNAUTHORIZED);
		ex.setResponseHeader("WWW-Authenticate", "Basic realm=\""+realm+"\"");
		ex.sendResponse();
		return false;
	}

}
