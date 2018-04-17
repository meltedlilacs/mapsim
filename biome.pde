enum Biome {RIVER, DEEPWATER, WATER, BEACH, FOREST, JUNGLE, SAVANNAH, DESERT, SNOW};

Biome calcBiome(float x, float y, float elevation) {
	float e = elevation/400.0;
	if(e < 0.0005) return Biome.DEEPWATER;
	if(e < 0.01) return Biome.WATER;
	e += fancyNoise(x*tscale, y*tscale)*0.5 - 0.25;
	if(e < 0.225) return Biome.BEACH;
	else if(e < 0.4) return Biome.FOREST;
	else if(e < 0.6) return Biome.JUNGLE;
	else if(e < 0.7) return Biome.SAVANNAH;
	else if(e < 0.8) return Biome.DESERT;
	else return Biome.SNOW;
}

boolean isWater(Biome biome) {
	return biome == Biome.RIVER || biome == Biome.DEEPWATER || biome == Biome.WATER;
}

color colorBiome(Biome biome) {
	if(biome == Biome.DEEPWATER) return #001B3E;
	if(biome == Biome.WATER || biome == Biome.RIVER) return #006899;
	if(biome == Biome.BEACH) return #99823D;
	if(biome == Biome.FOREST) return #47591F;
	if(biome == Biome.JUNGLE) return #404026;
	if(biome == Biome.SAVANNAH) return #4C2E00;
	if(biome == Biome.DESERT) return #7F5300;
	return #F2F2F2; // snow
}

int blendDistance(Biome biome) {
	if(biome == Biome.RIVER) return 0; // should never be called
	if(biome == Biome.DEEPWATER) return 10;
	if(biome == Biome.WATER)return 10;
	if(biome == Biome.BEACH) return 10;
	if(biome == Biome.FOREST) return 5;
	if(biome == Biome.JUNGLE) return 10;
	if(biome == Biome.SAVANNAH) return 10;
	if(biome == Biome.DESERT) return 10;
	return 0; // snow
}