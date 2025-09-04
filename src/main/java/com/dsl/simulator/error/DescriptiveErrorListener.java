// File: com/dsl/simulator/error/DescriptiveErrorListener.java
package com.dsl.simulator.error;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;
import java.util.ArrayList;

public class DescriptiveErrorListener extends BaseErrorListener {
    private final List<String> errorMessages = new ArrayList<>();
    private static final List<String> VALID_KEYWORDS = List.of(
            "deploy", "move", "print", "simulateOrbit", "deployGroundStation",
            "link", "unlink", "send", "receive", "predictPass", "maneuver"
    );

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String errorToken = offendingSymbol.toString();

        String misspelledWord = errorToken.substring(errorToken.indexOf("'") + 1, errorToken.lastIndexOf("'"));

        String bestMatch = findBestMatch(misspelledWord);

        if (bestMatch != null) {
            errorMessages.add(String.format("Error at line %d: Unknown command '%s'. Did you mean '%s'?",
                    line, misspelledWord, bestMatch));
        } else {
            errorMessages.add(String.format("Syntax error at line %d:%d - %s", line, charPositionInLine, msg));
        }
    }

    private String findBestMatch(String misspelled) {
        LevenshteinDistance distance = new LevenshteinDistance();
        String bestMatch = null;
        int minDistance = 2; // Only suggest if the typo is 1 or 2 characters off.

        for (String keyword : VALID_KEYWORDS) {
            int d = distance.apply(misspelled, keyword);
            if (d < minDistance) {
                minDistance = d;
                bestMatch = keyword;
            }
        }
        return bestMatch;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}