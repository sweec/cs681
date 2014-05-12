package project;

public class BasicAuthenticator implements Authenticator {

	@Override
	public boolean authenticate(String userinfo) {
		if (userinfo == null || !userinfo.startsWith("Basic "))
			return false;
		String info = userinfo.split(" ")[1];
		if (!UserInfo.getInstance().hasBasicUser(info))
			return false;
		return true;
	}

}
