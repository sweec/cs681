package project;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DigestAuthenticator implements Authenticator {
	private String realm = "digest realm";
	private HashMap<String, String> nonces = new HashMap<String, String>();
	private HashMap<String, Long> time = new HashMap<String, Long>();
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	@Override
	public boolean authenticate(HttpExchange ex) {
		String digestInfo = ex.getRequestHeader("Authorization");
		String client = ex.getRemoteUniqueId();
		lock.readLock().lock();
		String nonce = nonces.get(client);
		Long before = time.get(client);
		lock.readLock().unlock();
		if (before != null && (System.currentTimeMillis()-before) > HttpUtility.MaxSessionIdleTime)
			nonce = null;
		if (nonce != null && digestInfo != null && digestInfo.startsWith("Digest ")) {
			do {
				DigestRequest request = new DigestRequest(digestInfo.split(" ", 2)[1].trim());
				String username = request.getRequestInfo("username");
				if (username == null) break;
				String requestRealm = request.getRequestInfo("realm");
				if (!realm.equals(requestRealm)) break;
				String requestNonce = request.getRequestInfo("nonce");
				if (!nonce.equals(requestNonce)) break;
				String requestNc = request.getRequestInfo("nc");
				if (requestNc == null) break;
				String requestCnonce = request.getRequestInfo("cnonce");
				if (requestCnonce == null) break;
				String requestResponse = request.getRequestInfo("response");
				if (requestResponse == null) break;
				String requestQop = request.getRequestInfo("qop");
				String HA1 = UserInfo.getInstance().getDigestUser(username);
				if (HA1 == null) break;
				String HA2 = HttpUtility.toMD5(ex.getRequestCommand()+":"+ex.getRrequestURI());
				String HAResponse = null;
				if (requestQop == null || !requestQop.equals("auth")) {
					HAResponse = HttpUtility.toMD5(HA1+":"+nonce+":"+HA2);
				} else
					HAResponse = HttpUtility.toMD5(HA1+":"+nonce+":"+requestNc+":"+requestCnonce+":"+requestQop+":"+HA2);
				if (!requestResponse.equals(HAResponse)) break;
				return true;
			} while (true);
		}
		String seed = String.valueOf(System.currentTimeMillis()) +
				ex.getRemoteAddress();
		nonce = HttpUtility.toMD5(seed);
		lock.writeLock().lock();
		nonces.put(client, nonce);
		time.put(client, System.currentTimeMillis());
		//System.out.println(nonce);
		lock.writeLock().unlock();
		DigestResponse response = new DigestResponse();
		response.setResponseInfo("nonce", nonce);
		response.setResponseInfo("realm", realm);
		response.setResponseInfo("qop", "auth");
		ex.makeErrorResponse(HttpURLConnection.HTTP_UNAUTHORIZED);
		ex.setResponseHeader("Server", "Java server");
		ex.setResponseHeader("Date", HttpUtility.getGMT(System.currentTimeMillis()));
		ex.setResponseHeader("WWW-Authenticate", response.getResponseInfo());
		ex.sendResponse();
		return false;
	}

	private class DigestRequest {
		private HashMap<String, String> request = new HashMap<String, String>();
		
		public DigestRequest(String info) {
			if (info != null) {
				String[] kvs = info.split(",\n* *");
				for (String kv:kvs) {
					String[] KV = kv.split("=", 2);
					if (KV.length > 1) {
						if (KV[1].startsWith("\""))	// remove "
							KV[1] = KV[1].substring(1, KV[1].length()-1);
						request.put(KV[0], KV[1]);
						//System.out.println(KV[0]+": "+KV[1]);
					}
				}
			}
		}
		
		public String getRequestInfo(String key) {
			return request.get(key);
		}
	}
	
	private class DigestResponse {
		private HashMap<String, String> response = new HashMap<String, String>();
		
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
