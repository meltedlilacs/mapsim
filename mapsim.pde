import java.util.Collections;
import java.util.Arrays;

ArrayList<ArrayList<Square>> grid = new ArrayList<ArrayList<Square>>();
HashMap<Float, Float> elevations = new HashMap<Float, Float>();
ArrayList<Tree> trees = new ArrayList<Tree>();

float tscale = 0.00075; // time scale for main noise functions
float gscale = 4; // grid scale

void setup() {
	// settings
	size(1000, 1000, P3D);
	noStroke();
	ellipseMode(CENTER);

	// generate inital elevations
	for(int x = -width; x < width - gscale; x += gscale) {
		grid.add(new ArrayList<Square>());
		for(int y = -height; y < height - gscale; y += gscale) {
			float e0 = elevationNoise(x*tscale, y*tscale);
			float e1 = elevationNoise((x+gscale)*tscale, y*tscale);
			float e2 = elevationNoise((x+gscale)*tscale, (y+gscale)*tscale);
			float e3 = elevationNoise(x*tscale, (y+gscale)*tscale);
			elevations.put(e0, 0.0);
			elevations.put(e1, 0.0);
			elevations.put(e2, 0.0);
			elevations.put(e3, 0.0);
			grid.get(grid.size()-1).add(new Square(
				new PVector(x,        y,        e0),
				new PVector(x+gscale, y,        e1),
				new PVector(x+gscale, y+gscale, e2),
				new PVector(x,        y+gscale, e3)
			));
		}
	}

	// redistribute elevations to be on a nice curve
	ArrayList<Float> keys = new ArrayList<Float>();
	keys.addAll(elevations.keySet());
	Collections.sort(keys);
	for(int i = 0; i < keys.size(); i++) {
		float v = i*50.0/keys.size();
		v = pow(v, 1.35);
		elevations.put(keys.get(i), v);
	}
	for(ArrayList<Square> row : grid) {
		for(Square s : row) {
			for(int i = 0; i < 4; i++) {
				s.coords[i].z = elevations.get(s.coords[i].z);
			}
			s.avgZ = (s.coords[0].z+s.coords[1].z+s.coords[2].z+s.coords[3].z)/4;
			s.biome = calcBiome(s.avgX, s.avgY, s.avgZ);
		}
	}

	// blend colors
	for(int x = 0; x < grid.size(); x++) {
		for(int y = 0; y < grid.get(0).size(); y++) {
			Square s = grid.get(x).get(y);
			HashMap<Biome, Integer> biomeCount = new HashMap<Biome, Integer>();
			for(Biome b : Biome.values()) {
				biomeCount.put(b, 0);
			}
			ArrayList<int[]> adjIndices = getCircAdjIndices(x, y, blendDistance(s.biome));
			for(int[] indices : adjIndices) {
				Biome b = grid.get(indices[0]).get(indices[1]).biome;
				biomeCount.put(b, biomeCount.get(b) + 1);
			}
			int currentCount = biomeCount.get(s.biome);
			biomeCount.remove(s.biome);
			int maxCount = Collections.max(biomeCount.values());
			Biome commonBiome = s.biome;
			for(Biome b : biomeCount.keySet()) {
				if(biomeCount.get(b).equals(maxCount)) {
					commonBiome = b;
					break;
				}
			}
			if(((isWater(s.biome) && isWater(commonBiome)) || (!isWater(s.biome) && !isWater(commonBiome)))
			&& !(commonBiome == Biome.SNOW && s.biome != Biome.SNOW)) {
				s.c = lerpColor(colorBiome(s.biome), colorBiome(commonBiome), maxCount*1.0/(maxCount + currentCount));
			}
			else {
				s.c = colorBiome(s.biome);
			}
		}
	}

	// generate rivers
	ArrayList<Square> visited = new ArrayList<Square>();
	for(int i = 0; i < 20; i++) {
		int x = floor(random(grid.size()));
		int y = floor(random(grid.get(0).size()));
		int j = 0;
		while(!visited.contains(grid.get(x).get(y)) && grid.get(x).get(y).biome != Biome.DEEPWATER && grid.get(x).get(y).biome != Biome.WATER) {
			for(int[] indices : getCircAdjIndices(x, y, floor((3.0*noise(0.5*j+1000))))) {
				grid.get(indices[0]).get(indices[1]).biome = Biome.RIVER;
				grid.get(indices[0]).get(indices[1]).c = colorBiome(Biome.RIVER);
			}
			// grid.get(x).get(y).biome = Biome.RIVER;
			visited.add(grid.get(x).get(y));
			ArrayList<int[]> adjIndices = getAdjIndices(x, y, 1);
			HashMap<Float, int[]> adjElevations = new HashMap<Float, int[]>();
			for(int[] indices : adjIndices) {
				adjElevations.put(grid.get(indices[0]).get(indices[1]).avgZ, indices);
			}
			int[] answer = adjElevations.get(Collections.min(adjElevations.keySet()));
			x = answer[0];
			y = answer[1];
		}
		j++;
	}

	// generate trees
	for(ArrayList<Square> row : grid) {
		for(Square s : row) {
			if(s.biome != Biome.BEACH && s.biome != Biome.RIVER && s.biome != Biome.WATER && s.biome != Biome.DEEPWATER
			&& noise(0.01*s.avgX, 0.01*s.avgY) > 0.65 && randomGaussian() > 2.375) {
				float[] xs = {s.coords[0].x, s.coords[1].x, s.coords[2].x, s.coords[3].x};
				float[] ys = {s.coords[0].y, s.coords[1].y, s.coords[2].y, s.coords[3].y};
				float h = random(25, 60);
				float snowLength = ((s.biome == Biome.SNOW ? random(h-20, h-5) : 0));
				trees.add(new Tree(random(min(xs), max(xs)), random(min(ys), max(ys)), s.avgZ,
					        random(10, 15), h, snowLength, random(PI), lerpColor(#346051, #5B866B, random(1))));
			}
		}
	}
}

float i = 0;
void draw() {
	background(0);
	camera(0, -750, (height/2) / tan(PI/6), width/2, height/2, 0, 0, 1, 0);
	lights();
	translate(width/2, height/2, 0);
	rotateX(PI/2);
	rotateZ(i);
	for(ArrayList<Square> row : grid) {
		for(Square s : row) {
			s.draw();
		}
	}
	for(Tree t : trees) {
		t.draw();
	}
	i += PI/64;
}