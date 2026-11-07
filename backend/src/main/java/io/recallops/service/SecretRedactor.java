package io.recallops.service;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class SecretRedactor {
    private static final Pattern[] RULES = {
        Pattern.compile("(?i)(password|passwd|token|api[_-]?key|secret|authorization)(\\s*[=:]\\s*)([^\\s,;]+)"),
        Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._~+/-]+=*"),
        Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"),
        Pattern.compile("(?i)(mysql://[^:/\\s]+:)[^@\\s]+(@)")
    };
    public String redact(String input) {
        if (input == null) return "";
        String result = input;
        result = RULES[1].matcher(result).replaceAll("Bearer [REDACTED]");
        result = RULES[0].matcher(result).replaceAll("$1$2[REDACTED]");
        result = RULES[2].matcher(result).replaceAll("[REDACTED_AWS_KEY]");
        return RULES[3].matcher(result).replaceAll("$1[REDACTED]$2");
    }
}
