package censetagger.ui.tagcloud.impl;

import censetagger.ui.tagcloud.FontSizeComputationStrategy;

public class LogFontSizeComputationStrategy  extends FontSizeComputationStrategyImpl
    implements FontSizeComputationStrategy {

    public LogFontSizeComputationStrategy(int numSizes, String prefix) {
       super(numSizes,prefix);
    }

    protected double scaleCount(double count) {
        return  Math.log10(count);
    }
}
