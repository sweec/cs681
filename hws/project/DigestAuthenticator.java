package project;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map.Entry;

public class DigestAuthenticator implements Authenticator {
	private String realm = "digest realm";
	private String nonce = null;

	@Override
	public boolean authenticate(HttpExchange ex) {
		String digestUser = ex.getRequestHeader("Authorization");
		DigestInfo user = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (nonce != null && digestUser != null && digestUser.startsWith("Digest ")) {
			do {
				user = new DigestInfo(digestUser.split(" ", 2)[1].trim());
				String username = user.getRequestInfo("username");
				if (username == null) break;
				String requestRealm = user.getRequestInfo("realm");
				if (!realm.equals(requestRealm)) break;
				String requestNonce = user.getRequestInfo("nonce");
				if (!nonce.equals(requestNonce)) break;
				String requestNc = user.getRequestInfo("nc");
				if (requestNc == null) break;
				String requestCnonce = user.getRequestInfo("cnonce");
				if (requestCnonce == null) break;
				String requestResponse = user.getRequestInfo("response");
				if (requestResponse == null) break;
				String requestQop = user.getRequestInfo("qop");
				String HA1 = UserInfo.getInstance().getDigestUser(username);
				if (HA1 == null) break;
				String HA2 = new String(md.digest((ex.getRequestCommand()+":"+ex.getRrequestURI()).getBytes()));
				String response = null;
				if (requestQop == null || !requestQop.equals("auth")) {
					response = new String(md.digest((HA1+":"+nonce+":"+HA2).getBytes()));
				} else
					response = new String(md.digest((HA1+":"+nonce+":"+requestNc+":"+requestCnonce+":"+requestQop+":"+HA2).getBytes()));
				if (!requestResponse.equals(response)) break;
				return true;
			} while (true);
		}
		if (nonce == null) {
			String seed = String.valueOf(System.currentTimeMillis()) +
					ex.getRemoteAddress();
			nonce = new String(md.digest(seed.getBytes()));
		}
		user.setResponseInfo("nonce", nonce);
		user.setResponseInfo("realm", realm);
		user.setResponseInfo("qop", "auth");
		ex.makeErrorResponse(HttpURLConnection.HTTP_UNAUTHORIZED);
		ex.setResponseHeader("Server", "Java server");
		ex.setResponseHeader("Date", HttpUtility.getGMT(System.currentTimeMillis()));
		ex.setResponseHeader("WWW-Authenticate", user.getResponseInfo());
		ex.sendResponse();
		return false;
	}

	private class DigestInfo {
		private HashMap<String, String> request = new HashMap<String, String>();
		private HashMap<String, String> response = new HashMap<String, String>();
		
		public DigestInfo(String info) {
			if (info != null) {
				String[] kvs = info.split(",\n* *");
				for (String kv:kvs) {
					String[] KV = kv.split("=", 2);
					if (KV.length > 1) {
						if (KV[1].startsWith("\""))	// remove "
							KV[1] = KV[1].substring(1, KV[1].length()-1);
						request.put(KV[0], KV[1]);
					}
				}
			}
		}
		
		public String getRequestInfo(String key) {
			return request.get(key);
		}
		
		public void setResponseInfo(String key, String value) {
			response.put(key, value);
		}
		
		public String getResponseInfo() {
			StringBuilder info = new StringBuilder("Digest ");
			int len0 = info.length();
			for (Entry<String, String> kv:response.entrySet()) {
				info.append(kv.getKey());
				info.append("=");
				info.append("\""+kv.getValue()+"\"");
				info.append(",");
			}
			int len = info.length();
			if (len > len0) {
				info.setLength(len-1);
				return info.toString();
			} else
				return null;
		}
	}
}
