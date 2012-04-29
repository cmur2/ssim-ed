package de.mycrobase.ssim.ed.util.lang;

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;

import de.altimos.util.translator.MissingTranslationException;
import de.altimos.util.translator.TranslatorListener;

public class TListener implements TranslatorListener {
    
    private static final Logger logger = Logger.getLogger(TListener.class);
    
    @Override
    public void localeChanged(Locale oldLocale, Locale newLocale) {
        logger.info(String.format("Locale changed from %s to %s", oldLocale, newLocale));
    }
    
    @Override
    public String translationNotFound(String key, Locale locale, String domain,
        Object[] args, MissingResourceException ex)
    {
        logger.warn(String.format("Translation resource %s for locale %s not found!", domain, locale));
        return String.format("[%s:%s]", locale, key);
    }
    
    @Override
    public String stringNotFound(String key, Locale locale, String domain,
        Object[] args, MissingTranslationException ex)
    {
        logger.warn(String.format("String %s in %s for locale %s not found!", key, domain, locale));
        return String.format("[%s:%s]", locale, key);
    }
}
