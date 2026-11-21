package cl.ipss.saborgourmet.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

/** Formato de fechas y mensajes de interfaz en español de Chile. */
@Configuration
public class WebConfig {
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("es-CL"));
    }
}
