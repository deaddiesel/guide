package com.deaddiesel.mods.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MarkdownParser {

    public static List<ParsedLine> parseMarkdownFile(ResourceLocation location) {
        List<ParsedLine> parsedLines = new ArrayList<>();

        Minecraft.getInstance().getResourceManager().getResource(location).ifPresent(resource -> {
            try (var stream = resource.open();
                 var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    parsedLines.add(new ParsedLine(line, lineNumber++));
                }
            } catch (Exception e) {
                // Logger removed
            }
        });

        return parsedLines;
    }

    public record ParsedLine(String text, int lineNumber) {
        public ParsedLine {
            Objects.requireNonNull(text, "Line text cannot be null");
        }
    }

}