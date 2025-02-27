package dev.quantumfusion.dashloader.def.datatest;

import dev.quantumfusion.dashloader.def.TestUtils;
import dev.quantumfusion.dashloader.def.data.DashIdentifier;
import dev.quantumfusion.dashloader.def.data.DashModelIdentifier;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.data.font.DashBitmapFont;
import dev.quantumfusion.dashloader.def.data.font.DashBlankFont;
import dev.quantumfusion.dashloader.def.data.font.DashTrueTypeFont;
import dev.quantumfusion.dashloader.def.data.font.DashUnicodeFont;
import dev.quantumfusion.dashloader.def.data.image.DashSpriteImpl;
import dev.quantumfusion.dashloader.def.data.model.components.DashBakedQuad;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashAndPredicate;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashOrPredicate;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashSimplePredicate;
import dev.quantumfusion.dashloader.def.data.model.predicates.DashStaticPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("RegistryData Serialization Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegistryDataTest {


	@Test
	public void testBlockStateRegistryData() {
		TestUtils.testCreation(DashBlockState.class);
	}

	@Test
	public void testFontRegistryData() {
		TestUtils.testCreation(DashBitmapFont.class, DashBlankFont.class, DashTrueTypeFont.class, DashUnicodeFont.class);
	}

	@Test
	public void testIdentifierRegistryData() {
		TestUtils.testCreation(DashIdentifier.class, DashModelIdentifier.class);
	}

	@Test
	public void testPropertyRegistryData() {
	}

	@Test
	public void testPropertyValueRegistryData() {
	}

	@Test
	public void testSpriteRegistryData() {
		TestUtils.testCreation(DashSpriteImpl.class);
	}

	@Test
	public void testPredicateRegistryData() {
		TestUtils.testCreation(DashAndPredicate.class, DashOrPredicate.class, DashSimplePredicate.class, DashStaticPredicate.class);
	}

	@Test
	public void testRegistryBakedQuadData() {
		TestUtils.testCreation(DashBakedQuad.class);
	}


}
