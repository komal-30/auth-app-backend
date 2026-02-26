package com.project.auth.app.backend.helpers;

import java.util.UUID;

public class UserHelper {

	public static UUID parseUUID(String userId) {
		return UUID.fromString(userId);
	}

}
