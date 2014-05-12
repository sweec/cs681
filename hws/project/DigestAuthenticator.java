package project;

public class DigestAuthenticator implements Authenticator {

	@Override
	public boolean authenticate(String userinfo) {
		if (userinfo == null || !userinfo.startsWith("Digest "))
			return false;
		String info = userinfo.split(" ", 2)[1];
		if (!UserInfo.getInstance().hasDigestUser(info))
			return false;
		return true;
	}

}
