package de.mycrobase.ssim.ed.weather.ext;

import java.util.Random;

import de.mycrobase.ssim.ed.weather.WeatherPropertyGenerator;

public class Generators {
    
    public static class RandomFloatGenerator implements WeatherPropertyGenerator {
        
        private Random random = new Random();
        
        private float min;
        private float max;
        
        /**
         * @param min inclusive
         * @param max exclusive
         */
        public RandomFloatGenerator(float min, float max) {
            this.min = min;
            this.max = max;
        }
        
        @Override
        public Object generate() {
            return random.nextFloat() * (max-min) + min;
        }
    }
}
