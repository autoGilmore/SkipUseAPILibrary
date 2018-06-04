package com.autogilmore.throwback.skipUsePackage.tokenData;

/* 
 * A token code used by the API SkipUse for server calls. 
 * If a problem is found with this code, check the SkipUse API documentation / repo for updates.
*/
public class SkipUseToken {
	private String id = "";
	private String lastSentToken = "";
	private String toId = "";
	private String fromId = "";
	private String pendMSeed = "";
	private String mSeed = "";
	private String pendUSeed = "";
	private String seed = "";
	private String uSeed = "";
	private String tokenType = "";
	private int cycleCnt = 0;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLastSentToken() {
		return lastSentToken;
	}

	public void setLastSentToken(String lastSentToken) {
		this.lastSentToken = lastSentToken;
	}

	public String getFromId() {
		if (fromId == null)
			return "";
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getMSeed() {
		if (mSeed == null)
			return "";
		return mSeed;
	}

	public void setMSeed(String mSeed) {
		this.mSeed = mSeed;
	}

	public String getUSeed() {
		if (uSeed == null)
			return "";
		return uSeed;
	}

	public void setUSeed(String uSeed) {
		this.uSeed = uSeed;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public int getCycleCnt() {
		return cycleCnt;
	}

	public void setCycleCnt(int cycleCnt) {
		this.cycleCnt = cycleCnt;
	}

	public String getPendMSeed() {
		if (pendMSeed == null)
			return "";
		return pendMSeed;
	}

	public void setPendMSeed(String pendMSeed) {
		this.pendMSeed = pendMSeed;
	}

	public String getPendUSeed() {
		if (pendUSeed == null)
			return "";
		return pendUSeed;
	}

	public void setPendUSeed(String pendUSeed) {
		this.pendUSeed = pendUSeed;
	}

	public String getSeed() {
		if (seed == null)
			return "";
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public void debugToken() {
		System.out.println("------");
		System.out.println("id: " + id);
		System.out.println("toId: " + toId);
		System.out.println("uSeed: " + uSeed);
		System.out.println("pendUSeed: " + pendUSeed);
		System.out.println("fromId: " + fromId);
		System.out.println("seed: " + seed);
		System.out.println("mSeed: " + mSeed);
		System.out.println("pendMSeed: " + pendMSeed);
		System.out.println("tokenType: " + tokenType);
		System.out.println("lastSentToken: " + lastSentToken);
		System.out.println("cycleCnt: " + cycleCnt);
		System.out.println("------");
	}

	@Override
	public String toString() {
		return "" + seed + toId + fromId;
	}
}