package abcvtagger.ui.tagcloud.impl;

import abcvtagger.ui.tagcloud.FontSizeComputationStrategy;

public class LinearFontSizeComputationStrategy extends FontSizeComputationStrategyImpl
    implements FontSizeComputationStrategy {
 
    public LinearFontSizeComputationStrategy(int numSizes, String prefix) {
       super(numSizes,prefix);
    }
 
    protected double scaleCount(double count) {
        return  count;
    }
}
