package de.mycrobase.ssim.ed.helper;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.mycrobase.ssim.ed.helper.categories.Slow;

@RunWith(Categories.class)
@Categories.IncludeCategory(Slow.class)
@Suite.SuiteClasses(AllTests.class)
public class SlowTests {
    
}
