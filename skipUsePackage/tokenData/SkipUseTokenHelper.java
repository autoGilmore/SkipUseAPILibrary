package com.autogilmore.throwback.skipUsePackage.tokenData;

import java.util.Random;

import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;

/* 
 * Helper class for building, validation and creation of SkipUse tokens. 
 * If a problem is found with this code, check the SkipUse API documentation / repo for updates.
*/
public class SkipUseTokenHelper {
	private SkipUseToken skipUseToken = new SkipUseToken();

	private final String INITIATING_CODE = "1111";
	private final int IDENTIFY_CODE_LENGTH = INITIATING_CODE.length();
	private final int STANDARD_SEED_LENGTH = IDENTIFY_CODE_LENGTH * 2;
	private final int STANDARD_CONNECTION_LENGTH = STANDARD_SEED_LENGTH * 2;
	private final int BUILD_SEED_LENGTH = STANDARD_SEED_LENGTH + 1;
	private final int BUILD_CONNECTION_LENGTH = STANDARD_CONNECTION_LENGTH + 1;

	private final String TKN_TYP_CD_STANDARD = "STANDARD";
	private final String TKN_TYP_CD_BUILD = "BUILD";
	private final String TKN_TYP_CD_INVALID = "INVALID";

	public SkipUseTokenHelper() {
	}

	public SkipUseToken getSkipUseToken() {
		return skipUseToken;
	}

	public void processToken(String fromServerToken) throws SkipUseException {
		if (fromServerToken != null && !fromServerToken.isEmpty()) {
			SkipUseToken _theirToken = _createSkipUseTokenFromTokenString(fromServerToken);

			if (isValidToken(_theirToken)) {
				String revertPendUSeed = skipUseToken.getPendUSeed();
				skipUseToken.setPendUSeed(skipUseToken.getPendUSeed() + _theirToken.getSeed());
				skipUseToken.setToId(_theirToken.getToId());
				skipUseToken.setFromId(_theirToken.getFromId());
				skipUseToken.setSeed(_theirToken.getSeed());
				if (skipUseToken != null && isApproved(skipUseToken)) {
					approve(skipUseToken);
					skipUseToken = buildReply(skipUseToken, _theirToken);
				} else {
					skipUseToken.setPendUSeed(revertPendUSeed);
				}
			} else {
				throw new SkipUseException("Incomming server token was invalid");
			}
		} else {
			throw new SkipUseException("Incomming server token was empty");
		}
	}

	public SkipUseToken getInitiateToken() {
		skipUseToken = new SkipUseToken();
		skipUseToken.setToId(INITIATING_CODE);
		skipUseToken.setSeed(getNewSeedKey(false));
		skipUseToken.setMSeed(skipUseToken.getSeed());
		skipUseToken.setFromId(getSeedCode(skipUseToken.getSeed(), skipUseToken.getCycleCnt()));
		skipUseToken.setPendMSeed(removeSeedCode(skipUseToken.getSeed(), skipUseToken.getFromId()));
		skipUseToken.setCycleCnt(0);
		skipUseToken.setTokenType(TKN_TYP_CD_STANDARD);
		return skipUseToken;
	}

	private SkipUseToken buildReply(SkipUseToken clientCommToken, SkipUseToken theirToken) {
		if (clientCommToken.getPendMSeed().length() < 10) {
			clientCommToken.setTokenType(TKN_TYP_CD_BUILD);
			clientCommToken.setSeed(getNewSeedKey(true));
		} else {
			clientCommToken.setTokenType(TKN_TYP_CD_STANDARD);
			clientCommToken.setSeed(getNewSeedKey(false));
		}

		// set toId:
		if (clientCommToken.getUSeed().length() == 0)
			clientCommToken.setUSeed(theirToken.getSeed().toString());
		clientCommToken
				.setToId(getSeedCode(clientCommToken.getUSeed(), clientCommToken.getCycleCnt()));
		clientCommToken.setPendUSeed(
				removeSeedCode(clientCommToken.getUSeed(), clientCommToken.getToId()));

		clientCommToken.setMSeed(clientCommToken.getMSeed() + clientCommToken.getSeed());
		clientCommToken
				.setFromId(getSeedCode(clientCommToken.getMSeed(), clientCommToken.getCycleCnt()));
		clientCommToken.setPendMSeed(
				removeSeedCode(clientCommToken.getMSeed(), clientCommToken.getFromId()));
		clientCommToken.setCycleCnt(
				clientCommToken.getCycleCnt() == 0 ? 3 : clientCommToken.getCycleCnt() - 1);
		clientCommToken.setLastSentToken(clientCommToken.toString());

		return clientCommToken;
	}

	private void approve(SkipUseToken token) {
		token.setMSeed(removeSeedCode(token.getPendMSeed(), token.getToId()));
		token.setUSeed(removeSeedCode(token.getPendUSeed(), token.getFromId()));
		token.setToId("");
		token.setFromId("");
		token.setSeed("");
	}

	private boolean isApproved(SkipUseToken clientCommToken) {
		boolean isToMe = false;
		String toMeExpectedCode = getSeedCode(clientCommToken.getPendMSeed(),
				clientCommToken.getCycleCnt());
		if (toMeExpectedCode.equals(clientCommToken.getToId())) {
			isToMe = true;
		}

		if (isToMe) {
			String fromThemExpectedCode = getSeedCode(clientCommToken.getPendUSeed(),
					clientCommToken.getCycleCnt());
			if (fromThemExpectedCode.equals(clientCommToken.getFromId())) {
				return true;
			}
		}
		return false;
	}

	private String getNewSeedKey(boolean isBuild) {
		int strLength = isBuild ? BUILD_SEED_LENGTH : STANDARD_SEED_LENGTH;
		StringBuilder newServerKey = new StringBuilder();
		Random rand = new Random();
		while (newServerKey.length() < strLength) {
			int n = rand.nextInt(10);
			if (n != STANDARD_SEED_LENGTH - 2)
				newServerKey.append(n);
		}
		return newServerKey.toString();
	}

	private String removeSeedCode(String seed, String code) {
		if (seed != null && code != null) {
			int expectedSeedLength = seed.length() - code.length();
			String returnString = seed.replaceFirst(code, "");
			if (returnString.length() != expectedSeedLength) {
				return "";
			} else {
				return returnString;
			}
		}
		return seed;
	}

	private SkipUseToken _createSkipUseTokenFromTokenString(String tokenStr) {
		SkipUseToken token = new SkipUseToken();
		int tokenLength = tokenStr.length();
		if (tokenLength == STANDARD_SEED_LENGTH + IDENTIFY_CODE_LENGTH) {
			token.setTokenType(TKN_TYP_CD_STANDARD);
		} else if (tokenLength == STANDARD_CONNECTION_LENGTH) {
			token.setTokenType(TKN_TYP_CD_STANDARD);
		} else if (tokenLength == BUILD_CONNECTION_LENGTH) {
			token.setTokenType(TKN_TYP_CD_BUILD);
		} else {
			token.setTokenType(TKN_TYP_CD_INVALID);
		}
		if (!token.getTokenType().equals(TKN_TYP_CD_INVALID)) {
			int buildIndexIncr = token.getTokenType().equals(TKN_TYP_CD_BUILD) ? 1 : 0;
			int fromIndexStart = tokenLength - IDENTIFY_CODE_LENGTH;
			int seedIndexStandardLast = STANDARD_SEED_LENGTH + buildIndexIncr;
			int toIndexStart = seedIndexStandardLast;
			int toIndexEnd = fromIndexStart;
			token.setFromId(tokenStr.substring(fromIndexStart));
			token.setToId(tokenStr.substring(toIndexStart, toIndexEnd));
			token.setSeed(tokenStr.substring(0, seedIndexStandardLast));
		} else {
			return null;
		}
		return token;
	}

	private String getSeedCode(String seed, int startIndex) {
		int codeSize = 4;
		if (seed != null && seed.length() >= codeSize) {
			if (seed.length() >= startIndex + codeSize) {
				return seed.substring(startIndex, startIndex + codeSize);
			} else {
				return seed.substring(seed.length() - codeSize, seed.length());
			}
		}
		return "";
	}

	private boolean isValidTokenString(String token, String tokenType) {
		boolean isValid = true;
		if (tokenType.equals(TKN_TYP_CD_STANDARD)) {
			if (token.length() != STANDARD_CONNECTION_LENGTH) {
				isValid = false;
			}
		} else if (tokenType.equals(TKN_TYP_CD_BUILD)) {
			if (token.length() != BUILD_CONNECTION_LENGTH) {
				isValid = false;
			}
		}
		if (isValid)
			return token.matches("[0-5|7-9]+");
		return false;
	}

	private boolean isValidToken(SkipUseToken token) {
		if (token != null && token.getTokenType() != null) {
			return isValidToken(token, token.getTokenType());
		}
		return false;
	}

	private boolean isValidToken(SkipUseToken token, String tokenType) {
		return isValidTokenString(token.toString(), tokenType);
	}

}
