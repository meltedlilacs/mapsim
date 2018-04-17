float elevationNoise(float x, float y) {
	return pow(fancyNoise(x + width, y + height), 0.35);
}

float fancyNoise(float x, float y) {
	return   1 * noise( 1 * x,  1 * y)
       + 0.5 * noise( 2 * x,  2 * y)
      + 0.25 * noise( 4 * x,  4 * y)
     + 0.125 * noise( 8 * x,  8 * y)
    + 0.0625 * noise(16 * x, 16 * y);
}