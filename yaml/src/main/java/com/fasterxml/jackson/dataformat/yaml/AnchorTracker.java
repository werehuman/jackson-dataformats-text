package com.fasterxml.jackson.dataformat.yaml;

import com.fasterxml.jackson.core.JsonToken;
import org.snakeyaml.engine.v1.common.Anchor;
import org.snakeyaml.engine.v1.events.ScalarEvent;

import java.util.*;

/**
 * Remembers parsed {@link JsonToken JsonTokens} for every YAML anchor.
 */
class AnchorTracker
{
    /**
     * Parsed earlier json token and source scalar event linked to token.
     * <p>
     * State of {@link YAMLParser} depends from many fields and this state
     * can be altered by subclasses. Instead of keeping real state of a parser
     * this class holds initial event from snakeyaml library. This approach
     * allows to significantly simplify logic of {@link AnchorTracker}
     * and reduce its memory consumption but forces parser to parse holden
     * scalar several times.
     */
    static class JsonTokenWithPayload
    {
        final JsonToken jsonToken;
        final ScalarEvent scalarEvent;

        JsonTokenWithPayload(JsonToken jsonToken, ScalarEvent scalarEvent)
        {
            this.jsonToken = Objects.requireNonNull(jsonToken);
            this.scalarEvent = scalarEvent;
        }
    }

    private static class AnchorInfo
    {
        int startDepth;
        final List<JsonTokenWithPayload> tokens;

        AnchorInfo(int startDepth)
        {
            this.startDepth = startDepth;
            tokens = new ArrayList<>();
        }
    }

    private final Map<Anchor, AnchorInfo> anchorsInfo = new HashMap<>();
    private Set<Anchor> trackingAnchors = new HashSet<>();

    /**
     * Depth of array/object nesting.
     * Every YAML node can be anchored TODO
     */
    private int globalDepth = 0;

    void feed(Anchor unseenEarlierAnchor, ScalarEvent scalarEvent, JsonToken jsonToken)
    {
        if (unseenEarlierAnchor != null) {
            // According to specification, anchors can be overridden.
            // See https://yaml.org/spec/1.2/spec.html#id2786196
            trackingAnchors.add(unseenEarlierAnchor);
            anchorsInfo.put(unseenEarlierAnchor, new AnchorInfo(globalDepth));
        }

        switch (jsonToken) {
            case START_ARRAY:
            case START_OBJECT:
                ++globalDepth;
                break;
            case END_ARRAY:
            case END_OBJECT:
                --globalDepth;
                break;
        }

        for (Anchor candidate : trackingAnchors) {
            anchorsInfo.get(candidate).tokens.add(new JsonTokenWithPayload(jsonToken, scalarEvent));
        }

        trackingAnchors.removeIf(candidate -> anchorsInfo.get(candidate).startDepth == globalDepth);
    }

    List<JsonTokenWithPayload> getTokensForAnchor(Anchor anchor)
    {
        AnchorInfo result = anchorsInfo.get(anchor);
        if (result == null) {
            throw new RuntimeException("TODO"); // TODO
        }
        return result.tokens;
    }
}
