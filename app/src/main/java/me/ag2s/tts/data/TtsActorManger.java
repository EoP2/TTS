package me.ag2s.tts.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * 发音人列表：改为内置的静态列表，不再联网获取。
 * <p>
 * 原来是启动时请求 https://speech.platform.bing.com/.../voices/list 拿全量音色，
 * 但这个域名的免费接口已经不稳定/失效，而且拿到的三百多个音色里很多也已经跟着失效，
 * 所以这里换成一份手工核对过、当前可正常使用的音色子集（参考自配套网页版工具里用的那份列表）。
 * <p>
 * 如果以后想加音色，直接在下面 buildActors() 里加一行 new TtsActor(...) 即可，不需要联网。
 */
public class TtsActorManger {
    private static final String TAG = "TtsActorManger";

    private static volatile TtsActorManger instance;

    public static TtsActorManger getInstance() {
        if (instance == null) {
            synchronized (TtsActorManger.class) {
                if (instance == null) {
                    instance = new TtsActorManger();
                }
            }

        }
        return instance;
    }


    private final List<TtsActor> actors;

    private TtsActorManger() {
        actors = buildActors();
        Collections.sort(actors, TtsActorComparator.INSTANCE);
    }

    /**
     * 内置的音色列表。
     * TtsActor 字段含义：技术名(用于实际合成)、简称(同技术名，界面标题)、性别(true=女)、地区、备注(界面副标题里显示的友好名称)。
     */
    private static List<TtsActor> buildActors() {
        List<TtsActor> list = new ArrayList<>();

        // 中文（大陆）
        list.add(newActor("zh-CN-XiaoxiaoNeural", true, "zh-CN", "晓晓"));
        list.add(newActor("zh-CN-XiaoyiNeural", true, "zh-CN", "晓伊"));
        list.add(newActor("zh-CN-YunxiNeural", false, "zh-CN", "云希"));
        list.add(newActor("zh-CN-YunjianNeural", false, "zh-CN", "云健"));
        list.add(newActor("zh-CN-YunxiaNeural", false, "zh-CN", "云夏"));
        list.add(newActor("zh-CN-YunyangNeural", false, "zh-CN", "云扬"));
        list.add(newActor("zh-CN-liaoning-XiaobeiNeural", true, "zh-CN-liaoning", "晓北（辽宁）"));
        list.add(newActor("zh-CN-shaanxi-XiaoniNeural", true, "zh-CN-shaanxi", "晓妮（陕西）"));

        // 中文（粤语/香港）
        list.add(newActor("zh-HK-HiuGaaiNeural", true, "zh-HK", "曉佳（粤）"));
        list.add(newActor("zh-HK-HiuMaanNeural", true, "zh-HK", "曉曼（粤）"));
        list.add(newActor("zh-HK-WanLungNeural", false, "zh-HK", "雲龍（粤）"));

        // 中文（台湾）
        list.add(newActor("zh-TW-HsiaoChenNeural", true, "zh-TW", "曉臻（台）"));
        list.add(newActor("zh-TW-HsiaoYuNeural", true, "zh-TW", "曉雨（台）"));
        list.add(newActor("zh-TW-YunJheNeural", false, "zh-TW", "雲哲（台）"));

        // 日语
        list.add(newActor("ja-JP-NanamiNeural", true, "ja-JP", "七海"));
        list.add(newActor("ja-JP-KeitaNeural", false, "ja-JP", "圭太"));

        // 英语（美式）
        list.add(newActor("en-US-AriaNeural", true, "en-US", "Aria"));
        list.add(newActor("en-US-JennyNeural", true, "en-US", "Jenny"));
        list.add(newActor("en-US-GuyNeural", false, "en-US", "Guy"));
        list.add(newActor("en-US-ChristopherNeural", false, "en-US", "Christopher"));
        list.add(newActor("en-US-EricNeural", false, "en-US", "Eric"));
        list.add(newActor("en-US-RogerNeural", false, "en-US", "Roger"));

        // 英语（英式）
        list.add(newActor("en-GB-SoniaNeural", true, "en-GB", "Sonia"));
        list.add(newActor("en-GB-LibbyNeural", true, "en-GB", "Libby"));
        list.add(newActor("en-GB-RyanNeural", false, "en-GB", "Ryan"));
        list.add(newActor("en-GB-ThomasNeural", false, "en-GB", "Thomas"));

        // 韩语
        list.add(newActor("ko-KR-SunHiNeural", true, "ko-KR", "선히"));
        list.add(newActor("ko-KR-InJoonNeural", false, "ko-KR", "인준"));

        // 法语
        list.add(newActor("fr-FR-DeniseNeural", true, "fr-FR", "Denise"));
        list.add(newActor("fr-FR-HenriNeural", false, "fr-FR", "Henri"));

        // 德语
        list.add(newActor("de-DE-KatjaNeural", true, "de-DE", "Katja"));
        list.add(newActor("de-DE-KillianNeural", false, "de-DE", "Killian"));

        // 其它
        list.add(newActor("es-ES-ElviraNeural", true, "es-ES", "Elvira（西班牙）"));
        list.add(newActor("es-MX-DaliaNeural", true, "es-MX", "Dalia（墨西哥）"));

        return list;
    }

    private static TtsActor newActor(@NonNull String shortName, boolean female, @NonNull String locale, @NonNull String displayName) {
        return new TtsActor(shortName, shortName, female, locale, displayName);
    }

    public List<TtsActor> sortByLocale(List<TtsActor> list, Locale locale) {
        return list;
    }

    @Nullable
    public TtsActor getByName(@NonNull String name) {
        for (TtsActor actor : actors) {
            if (actor.getShortName().equalsIgnoreCase(name) || actor.getName().equalsIgnoreCase(name)) {
                return actor;
            }
        }

        return null;
    }

    /**
     * 获取所有Actor
     *
     * @return List<TtsActor>
     */
    @SuppressWarnings("unused")
    public synchronized List<TtsActor> getActors() {
        return this.actors;
    }

    /**
     * 获取当前Locale支持的Actor
     *
     * @param locale locale
     * @return List<TtsActor>
     */
    @SuppressWarnings("unused")
    public List<TtsActor> getActorsByLocale(Locale locale) {
        List<TtsActor> newActors = new ArrayList<>();
        for (TtsActor actor : actors) {
            //语言相同或者地区相同
            if (actor.getLocale().getISO3Language().equals(locale.getISO3Language()) || actor.getLocale().getISO3Country().equals(locale.getISO3Country())) {
                newActors.add(actor);
            }
        }
        Collections.sort(newActors, new TtsActorComparator(locale));
        return newActors;
    }

}
