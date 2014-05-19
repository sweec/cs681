package project;

import java.net.HttpURLConnection;

public class BasicAuthenticator implements Authenticator {
	//private String realm = "basic realm";

	@Override
	public boolean authenticate(HttpExchange ex) {
		String url = ex.getRequestURI();
		if (url == null)
			return true;
		AppInfo app = AppInfo.getInstance();
		String realm = app.getProperty(url);
		if (realm == null)
			return true;
		String basicUser = ex.getRequestHeader("Authorization");
		String info = null;
		if (basicUser != null && basicUser.startsWith("Basic ")) {
			info = basicUser.split(" ")[1];
			if (app.hasRealmUser(realm, app.getBasicUser(info)))
				return true;
		}
		ex.setErrorResponse(HttpURLConnection.HTTP_UNAUTHORIZED);
		ex.setResponseHeader("WWW-Authenticate", "Basic realm=\""+realm+"\"");
		ex.sendResponse();
		return false;
	}

	@Override
	public String toString() {
		return "Basic";
	}
}
