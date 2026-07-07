package me.ag2s.tts.services;

public final class Constants {
    /**
     * 是否使用自定义语音
     */
    public static final String USE_CUSTOM_VOICE = "use_custom_voice";
    /**
     * 自定义语音的名称
     */
    public static final String CUSTOM_VOICE = "custom_voice";
    /**
     * 自定义语音的index
     */
    public static final String CUSTOM_VOICE_INDEX = "custom_voice_index";

    /**
     * 音量
     */
    public static final String VOICE_VOLUME = "voice_volume";

    /**
     * 自定义服务器地址（域名或完整URL，例如 https://your-worker.workers.dev）
     * 微软官方免费直连接口已失效，现在语音合成统一走这个地址，为必填项。
     */
    public static final String CUSTOM_SERVER_URL = "custom_server_url";

    /**
     * App内语速调整量（百分点，叠加在系统"文字转语音"设置的语速基础上）。
     * 默认0表示不额外调整，即完全跟随系统设置，行为与之前版本一致。
     */
    public static final String VOICE_RATE_DELTA = "voice_rate_delta";
    /**
     * App内音调调整量（百分点，叠加在系统"文字转语音"设置的音调基础上）。
     * 默认0表示不额外调整，即完全跟随系统设置，行为与之前版本一致。
     */
    public static final String VOICE_PITCH_DELTA = "voice_pitch_delta";


    public final static String[] supportedLanguages = {"zho-CHN", "zho-HKG", "zho-TWN", "jpn-JPN", "kor-KOR", "ara-EGY", "ara-SAU", "bul-BGR", "cat-ESP", "ces-CZE", "cym-GBR", "dan-DNK", "deu-AUT", "deu-CHE", "deu-DEU", "ell-GRC", "eng-AUS", "eng-CAN", "eng-GBR", "eng-HKG", "eng-IRL", "eng-IND", "eng-NZL", "eng-PHL", "eng-SGP", "eng-USA", "eng-ZAF", "spa-ARG", "spa-COL", "spa-ESP", "spa-MEX", "spa-USA", "est-EST", "fin-FIN", "fra-BEL", "fra-CAN", "fra-CHE", "fra-FRA", "gle-IRL", "guj-IND", "heb-ISR", "hin-IND", "hrv-HRV", "hun-HUN", "ind-IDN", "ita-ITA", "lit-LTU", "lav-LVA", "mar-IND", "msa-MYS", "mlt-MLT", "nob-NOR", "nld-BEL", "nld-NLD", "pol-POL", "por-BRA", "por-PRT", "ron-ROU", "rus-RUS", "slk-SVK", "slv-SVN", "swe-SWE", "swa-KEN", "tam-IND", "tel-IND", "tha-THA", "tur-TUR", "ukr-UKR", "urd-PAK", "vie-VNM"};

}
