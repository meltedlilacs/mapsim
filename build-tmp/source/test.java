import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Collections; 
import java.util.Arrays; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class test extends PApplet {




ArrayList<ArrayList<Square>> grid = new ArrayList<ArrayList<Square>>();
HashMap<Float, Float> elevations = new HashMap<Float, Float>();
ArrayList<Tree> trees = new ArrayList<Tree>();

float tscale = 0.00075f; // time scale for main noise functions
float gscale = 4; // grid scale

public void setup() {
	// settings
	
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
			elevations.put(e0, 0.0f);
			elevations.put(e1, 0.0f);
			elevations.put(e2, 0.0f);
			elevations.put(e3, 0.0f);
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
		float v = i*50.0f/keys.size();
		v = pow(v, 1.35f);
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
				s.c = lerpColor(colorBiome(s.biome), colorBiome(commonBiome), maxCount*1.0f/(maxCount + currentCount));
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
			for(int[] indices : getCircAdjIndices(x, y, floor((3.0f*noise(0.5f*j+1000))))) {
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
			&& noise(0.01f*s.avgX, 0.01f*s.avgY) > 0.65f && randomGaussian() > 2.375f) {
				float[] xs = {s.coords[0].x, s.coords[1].x, s.coords[2].x, s.coords[3].x};
				float[] ys = {s.coords[0].y, s.coords[1].y, s.coords[2].y, s.coords[3].y};
				float h = random(25, 60);
				float snowLength = ((s.biome == Biome.SNOW ? random(h-20, h-5) : 0));
				trees.add(new Tree(random(min(xs), max(xs)), random(min(ys), max(ys)), s.avgZ,
					        random(10, 15), h, snowLength, random(PI), lerpColor(0xff346051, 0xff5B866B, random(1))));
			}
		}
	}
}

float i = 0;
public void draw() {
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
// r is in squares
public ArrayList<int[]> getAdjIndices(int x, int y, int r) {
	ArrayList<int[]> adjIndices = new ArrayList<int[]>();
	for(int tx = x-r; tx <= x+r; tx++) {
		for(int ty = y-r; ty <= y+r; ty++) {
			if(tx >= 0 && tx < grid.size() && ty >= 0 && ty < grid.get(0).size()) {
				int[] indices = {tx, ty};
				adjIndices.add(indices);
			}
		}
	}
	return adjIndices;
}

// r is in pixels
public ArrayList<int[]> getCircAdjIndices(int x, int y, int r) {
	ArrayList<int[]> adjIndices = getAdjIndices(x, y, r);
	for(int i = adjIndices.size()-1; i >= 0; i--) {
		if((pow(adjIndices.get(i)[0]-x, 2) + pow(adjIndices.get(i)[1]-y, 2)) > pow(r, 2)) {
			adjIndices.remove(i);
		}
	}
	return adjIndices;
}
enum Biome {RIVER, DEEPWATER, WATER, BEACH, FOREST, JUNGLE, SAVANNAH, DESERT, SNOW};

public Biome calcBiome(float x, float y, float elevation) {
	float e = elevation/400.0f;
	if(e < 0.0005f) return Biome.DEEPWATER;
	if(e < 0.01f) return Biome.WATER;
	e += fancyNoise(x*tscale, y*tscale)*0.5f - 0.25f;
	if(e < 0.225f) return Biome.BEACH;
	else if(e < 0.4f) return Biome.FOREST;
	else if(e < 0.6f) return Biome.JUNGLE;
	else if(e < 0.7f) return Biome.SAVANNAH;
	else if(e < 0.8f) return Biome.DESERT;
	else return Biome.SNOW;
}

public boolean isWater(Biome biome) {
	return biome == Biome.RIVER || biome == Biome.DEEPWATER || biome == Biome.WATER;
}

public int colorBiome(Biome biome) {
	if(biome == Biome.DEEPWATER) return 0xff001B3E;
	if(biome == Biome.WATER || biome == Biome.RIVER) return 0xff006899;
	if(biome == Biome.BEACH) return 0xff99823D;
	if(biome == Biome.FOREST) return 0xff47591F;
	if(biome == Biome.JUNGLE) return 0xff404026;
	if(biome == Biome.SAVANNAH) return 0xff4C2E00;
	if(biome == Biome.DESERT) return 0xff7F5300;
	return 0xffF2F2F2; // snow
}

public int blendDistance(Biome biome) {
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
public float elevationNoise(float x, float y) {
	return pow(fancyNoise(x + width, y + height), 0.35f);
}

public float fancyNoise(float x, float y) {
	return   1 * noise( 1 * x,  1 * y)
       + 0.5f * noise( 2 * x,  2 * y)
      + 0.25f * noise( 4 * x,  4 * y)
     + 0.125f * noise( 8 * x,  8 * y)
    + 0.0625f * noise(16 * x, 16 * y);
}
class Square {
	Square(PVector p0, PVector p1, PVector p2, PVector p3) {
		coords = new PVector[4];
		coords[0] = p0;
		coords[1] = p1;
		coords[2] = p2;
		coords[3] = p3;
		avgX = (coords[0].x+coords[1].x+coords[2].x+coords[3].x)/4;
		avgY = (coords[0].y+coords[1].y+coords[2].y+coords[3].y)/4;
		avgZ = (coords[0].z+coords[1].z+coords[2].z+coords[3].z)/4;
	}

	public void draw() {
		fill(c);

		beginShape();
		vertex(coords[0].x, coords[0].y, coords[0].z);
		vertex(coords[1].x, coords[1].y, coords[1].z);
		vertex(coords[2].x, coords[2].y, coords[2].z);
		vertex(coords[3].x, coords[3].y, coords[3].z);
		endShape();
	}

	PVector[] coords;
	Biome biome;
	float avgX, avgY, avgZ;
	int c;
}
class Tree {
	Tree(float x_, float y_, float z_, float r_, float h_, float snowLength_, float angle_, int c_) {
		x = x_;
		y = y_;
		z = z_;
		r = r_;
		h = h_;
		snowLength = snowLength_;
		s = r*snowLength/h + 1; // +1 to get right outside of the size of the tree
		angle = angle_;
		c = c_;
	}

	public void pyramid(float x_, float y_) {
		beginShape();
		vertex(-x_, -x_, 0);
		vertex(0,  0,   y_);
		vertex(x_, -x_, 0);
		endShape();
		beginShape();
		vertex(x_, -x_, 0);
		vertex(0,   0,   y_);
		vertex(x_, x_, 0);
		endShape();
		beginShape();
		vertex(x_, x_, 0);
		vertex(0,   0,   y_);
		vertex(-x_, x_, 0);
		endShape();
		beginShape();
		vertex(-x_, x_, 0);
		vertex(0,   0,   y_);
		vertex(-x_, -x_, 0);
		endShape();
	}

	public void draw() {
		fill(c);
		pushMatrix();
		translate(x, y, z);
		rotate(angle);
		pyramid(r, h);
		if(snowLength > 0) {
			fill(colorBiome(Biome.SNOW));
			translate(0, 0, h-snowLength);
			pyramid(s, snowLength);
		}
		popMatrix();
	}

	float x, y, z, r, h, snowLength, s, angle;
	int c;
}
  public void settings() { 	size(1000, 1000, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "test" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
