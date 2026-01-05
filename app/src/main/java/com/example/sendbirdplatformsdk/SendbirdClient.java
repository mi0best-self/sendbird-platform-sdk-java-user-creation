package com.example.sendbirdplatformsdk;

import org.sendbird.client.ApiClient;
import org.sendbird.client.Configuration;

public final class SendbirdClient {

    // 보안상 실제 앱에 하드코딩은 비권장 (테스트 목적)
    private static final String API_TOKEN = "1d74347d6bf61fcd271a465a0ad3824185c93982";
    private static final String APP_ID = "D947D299-34DA-4DFC-A69C-3235335819E6";

    private SendbirdClient() {}

    public static ApiClient create() {
        ApiClient client = Configuration.getDefaultApiClient();
        client.addDefaultHeader("Api-Token", API_TOKEN);
        client.setBasePath("https://api-" + APP_ID + ".sendbird.com");
        return client;
    }
}
