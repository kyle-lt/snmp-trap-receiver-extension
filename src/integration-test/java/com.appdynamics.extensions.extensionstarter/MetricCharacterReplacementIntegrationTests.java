package com.appdynamics.extensions.extensionstarter;

import org.junit.Test;

public class MetricCharacterReplacementIntegrationTests {

    // todo: 1 - 2 cases conventional replacement without any replacer configured for | , : and replacement of , : | with configured replacers
    //todo 2 - replacement of characters configured 2 times - make sure the most recent gets applied
    // todo 3 - non-ascii: 2 cases - if replacer configured, replace. Else, the metric gets dropped
    // todo 4 - wrong replacement = valid ascii char being replaced by an invalid char | , :

    @Test
    public void testDefaultCharacterReplacement() {

    }

    @Test
    public void testDefaultCharacterOverrides() {}

    @Test
    public void testWhenMultipleReplacementsAreConfiguredForSameCharacterThenLastOneIsUsed () {}


    @Test
    public void testWhenReplacementForNonAsciiCharacterIsPresent() {}

    @Test
    public void testWhenReplacementForNonAsciiCharacterIsAbsent() {}

    @Test
    public void testWhenInvalidReplacementConfiguredForAsciiCharacter() {

    }


}

