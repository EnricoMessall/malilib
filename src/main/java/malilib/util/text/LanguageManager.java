package malilib.util.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import malilib.MaLiLib;
import malilib.MaLiLibReference;
import malilib.util.FileUtils;

public class LanguageManager
{
    public static final LanguageManager INSTANCE = new LanguageManager();
    protected final Properties translations = new Properties();

    protected LanguageManager()
    {
        this.reload();
    }

    public void reload()
    {
        try
        {
            this.translations.clear();
            this.translations.load(new InputStreamReader(FileUtils.openModResource(MaLiLibReference.MOD_ID, "/lang/en_us.lang"), StandardCharsets.UTF_8));
        }
        catch (IOException e)
        {
            MaLiLib.LOGGER.error("Exception trying to load translations: ", e);
        }
    }

    public boolean hasTranslation(String key)
    {
        return this.translations.containsKey(key);
    }

    public String translate(String key)
    {
        return this.translations.getProperty(key, key);
    }

    public String translate(String key, Object... args)
    {
        String translated = this.translations.getProperty(key, key);
        return String.format(translated, args);
    }
}
